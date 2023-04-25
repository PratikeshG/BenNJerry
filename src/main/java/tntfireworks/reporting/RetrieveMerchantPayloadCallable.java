package tntfireworks.reporting;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.v2.Payment;
import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.PaymentRefund;
import com.squareup.connect.v2.Payout;
import com.squareup.connect.v2.PayoutEntry;
import com.squareup.connect.v2.SquareClientV2;
import com.squareup.connect.v2.Tender;


import tntfireworks.TntDatabaseApi;
import util.ConnectV2MigrationHelper;
import util.DbConnection;
import util.SquarePayload;
import util.TimeManager;

public class RetrieveMerchantPayloadCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(RetrieveMerchantPayloadCallable.class);
    private static final int DAY_RANGE = 1;
    private static final int SETTLEMENTS_REPORT_TYPE = 1;
    private static final int TRANSACTIONS_REPORT_TYPE = 2;
    private static final int ABNORMAL_TRANSACTIONS_REPORT_TYPE = 3;
    private static final int CHARGEBACK_REPORT_TYPE = 4;
    private static final int LOCATION_SALES_REPORT_TYPE = 5;
    private static final int ITEM_SALES_REPORT_TYPE = 7;
    private static final int CREDIT_DEBIT_REPORT_TYPE = 8;
    private static final int GROSS_SALES_REPORT_TYPE = 9;
    private static final int YOY_GROSS_SALES_REPORT_TYPE = 10;
    private static final String SEASON_REPORTING_ENABLED = "TRUE";
    private static final String TNT_DEFAULT_LOCATION_NAME_PREFIX = "DEFAULT";
    private String startOfSeason;

    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}?autoReconnect=true")
    private String databaseUrl;
    @Value("${mysql.user}")
    private String databaseUser;
    @Value("${mysql.password}")
    private String databasePassword;
    @Value("${api.url}")
    private String apiUrl;
    @Value("${api.version}")
    private String apiVersion;
    @Value("${encryption.key.tokens}")
    private String encryptionKey;
    @Value("${tntfireworks.reporting.cron.timezone}")
    private String cronTimeZone;
    private String timeZone;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        // get mule session vars
        int offset = Integer.parseInt(message.getProperty("offset", PropertyScope.SESSION));
        int range = Integer.parseInt(message.getProperty("range", PropertyScope.SESSION));
        int reportType = Integer.parseInt(message.getProperty("reportType", PropertyScope.SESSION));
        String ytd = message.getProperty("ytd", PropertyScope.SESSION);

        // get deployment payload from queue-splitter
        SquarePayload deployment = (SquarePayload) message.getPayload();

        // get startOfSeason date
        startOfSeason = deployment.getStartOfSeason();

        // get time zone for file
        timeZone = util.Constants.PST_TIME_ZONE_ID;
        if (cronTimeZone != null && !cronTimeZone.isEmpty()) {
            timeZone = cronTimeZone;
        }

        // set range to interval from season start date if YTD set to true
        if (ytd.equals(SEASON_REPORTING_ENABLED)) {
            range = computeSeasonInterval(startOfSeason, offset, TimeZone.getTimeZone(timeZone));
        }

        // initialize connect v2 api clients
        SquareClientV2 squareClientV2 = new SquareClientV2(apiUrl, deployment.getAccessToken(encryptionKey), "2023-03-15");

        // retrieve location details according to reportType and store into
        // abstracted object (reportPayload)
        logger.info("Retrieving location details for merchant: " + deployment.getMerchantId());
        return getMerchantPayload(reportType, squareClientV2, offset, range);
    }

    public static int computeSeasonInterval(String startOfSeason, int offset, TimeZone tz) {
        int range = 0;

        // parse season month, day, year and offset day/month by 1
        // startOfSeason format yyyy-mm-dd
        String[] dateParams = startOfSeason.split("-");
        int month, day, year;
        if (dateParams.length == 3) {
            year = Integer.parseInt(dateParams[0]);
            month = Integer.parseInt(dateParams[1]) - 1;
            day = Integer.parseInt(dateParams[2]) - 1;
        } else {
            throw new RuntimeException("Invalid format for startOfSeason (yyyy-mm-dd)");
        }

        // calendar instances
        Calendar startOfSeasonCal = Calendar.getInstance(tz);
        Calendar today = Calendar.getInstance(tz);

        // set month day year according to params
        startOfSeasonCal.set(Calendar.YEAR, year);
        startOfSeasonCal.set(Calendar.MONTH, month);
        startOfSeasonCal.set(Calendar.DAY_OF_MONTH, day);

        // set all calendars to start at midnight
        startOfSeasonCal.set(Calendar.HOUR_OF_DAY, 0);
        startOfSeasonCal.set(Calendar.MINUTE, 0);
        startOfSeasonCal.set(Calendar.SECOND, 0);
        startOfSeasonCal.set(Calendar.MILLISECOND, 0);

        // add negative offset to calendar so that 'range' accounts for the
        // defined offset
        today.add(Calendar.DATE, -offset);
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        range = (int) TimeUnit.MILLISECONDS
                .toDays(Math.abs(today.getTimeInMillis() - startOfSeasonCal.getTimeInMillis()));

        return range;
    }

    private List<TntReportLocationPayload> getMerchantPayload(int reportType, SquareClientV2 squareClientV2, int offset, int range)
    		throws Exception {
        // initialize dbConnection
        DbConnection dbConnection = new DbConnection(databaseUrl, databaseUser, databasePassword);

        // initialize payload to store ItemSalesFile objects
        List<TntReportLocationPayload> merchantPayload = new ArrayList<TntReportLocationPayload>();

        try {
            // get db information for later lookup
            // initialized TntDatabaseApi for information lookup
            TntDatabaseApi tntDatabaseApi = new TntDatabaseApi(dbConnection);

            // dbLocationRows used for all report Types
            List<Map<String, String>> dbLocationRows = tntDatabaseApi
                    .submitQuery(tntDatabaseApi.generateLocationSQLSelect());

            // dbItemRows only used for ITEM_SALES_REPORT_TYPE
            List<Map<String, String>> dbItemRows = null;

            // dbLoadNumber only used for CREDIT_DEBIT_REPORT_TYPE
            List<Map<String, String>> dbLoadNumbers = null;

            if (reportType == ITEM_SALES_REPORT_TYPE) {
                dbItemRows = tntDatabaseApi.submitQuery(tntDatabaseApi.generateItemSQLSelect());
            } else if (reportType == CREDIT_DEBIT_REPORT_TYPE) {
                dbLoadNumbers = tntDatabaseApi.submitQuery(tntDatabaseApi.generateLoadNumberSQLSelect());
            }
            tntDatabaseApi.close();

            // iterate through each location in merchant and aggregate account
            // data
            for (Location location : squareClientV2.locations().list()) {
	            if (isValidLocation(location, dbLocationRows)) {
	                // initialize TntLocationDetails with DB data
	                TntLocationDetails locationDetails = new TntLocationDetails(dbLocationRows, location);

	                // define time intervals to pull payment data
	                // - dayTimeInterval is currently only used for 5/6, report 7, report 9
	                // - aggregateInterval is used by all flows
	                Map<String, String> dayTimeInterval = TimeManager.getPastDayInterval(DAY_RANGE, offset,
	                        locationDetails.sqLocationTimeZone);
	                Map<String, String> aggregateIntervalParams = TimeManager.getPastDayInterval(range, offset,
	                        locationDetails.sqLocationTimeZone);

	                switch (reportType) {
	                    case SETTLEMENTS_REPORT_TYPE:
	                        merchantPayload.add(generatePayoutsPayload(squareClientV2, locationDetails,
	                    			aggregateIntervalParams, offset));
	                        break;
	                    case TRANSACTIONS_REPORT_TYPE:
	                        merchantPayload.add(generateOrdersPayload(squareClientV2,locationDetails, aggregateIntervalParams, offset));
	                        break;
	                    case ABNORMAL_TRANSACTIONS_REPORT_TYPE:

	                        merchantPayload.add(generateAbnormalOrdersPayload(squareClientV2, locationDetails,
	                                aggregateIntervalParams, offset));
	                        break;
	                    case CHARGEBACK_REPORT_TYPE:
	                        // report 4 is currently generated from a different source
	                        break;
	                    case LOCATION_SALES_REPORT_TYPE:
	                        merchantPayload.add(generateLocationSalesPayload(squareClientV2, locationDetails,
	                                aggregateIntervalParams, dayTimeInterval, offset));
	                        break;
	                    case ITEM_SALES_REPORT_TYPE:
	                    	merchantPayload.add(generateItemSalesPayload(squareClientV2, locationDetails,
	                                aggregateIntervalParams, dayTimeInterval, dbItemRows, offset));
	                        break;
	                    case CREDIT_DEBIT_REPORT_TYPE:
	                        merchantPayload.add(generateCreditDebitPayload(squareClientV2, locationDetails,
	                                aggregateIntervalParams, dbLoadNumbers, offset));
	                        break;
	                    case GROSS_SALES_REPORT_TYPE:
	   	                        merchantPayload.add(generateGrossSalesPayload(squareClientV2, locationDetails,
		                                aggregateIntervalParams, dayTimeInterval, offset));
	                        break;
	                    case YOY_GROSS_SALES_REPORT_TYPE:
	                        merchantPayload.add(generateYoyGrossSalesPayload(squareClientV2, locationDetails,
	                                aggregateIntervalParams, dayTimeInterval, offset));
	                        break;
	                }
	            }
            }
        } catch (Exception e) {
            logger.error("ERROR: caught exception while aggregating '" + reportType + "' details: " + e.getCause());
        }

        return merchantPayload;

    }

    private boolean isValidLocation(Location location, List<Map<String, String>> dbLocationRows) {
        return TntLocationDetails.isTntLocation(dbLocationRows, location.getName())
                && location.getStatus().equals(Location.LOCATION_STATUS_ACTIVE)
                && !location.getName().contains(TNT_DEFAULT_LOCATION_NAME_PREFIX);
    }

    private PayoutsPayload generatePayoutsPayload(SquareClientV2 squareClientV2,
            TntLocationDetails locationDetails, Map<String, String> aggregateIntervalParams, int offset)
            throws Exception {
        PayoutsPayload payoutsPayload = new PayoutsPayload(timeZone, offset, locationDetails);
        aggregateIntervalParams.put(util.Constants.SORT_ORDER_V2, util.Constants.SORT_ORDER_ASC_V2);
        Payout[] payouts = TntLocationDetails.getPayouts(squareClientV2, locationDetails.sqLocationId, aggregateIntervalParams);
        Map<String, String> payoutEntryParams = new HashMap<String, String>();
        payoutEntryParams.put(util.Constants.SORT_ORDER_V2, util.Constants.SORT_ORDER_ASC_V2);
        for (Payout payout : payouts) {
        	PayoutEntry[] payoutEntries = TntLocationDetails.getPayoutEntries(squareClientV2, payout.getId(), payoutEntryParams);
            payoutsPayload.addEntry(payout, payoutEntries);
        }

        return payoutsPayload;
    }

    private OrdersPayload generateOrdersPayload(SquareClientV2 squareClientV2, TntLocationDetails locationDetails,
    		Map<String, String> aggregateIntervalParams, int offset)
            throws Exception {
        // - each OrdersPayload includes order data from a single location
        // - while the payload object name may imply V2Orders data, the payload
        // mainly consists of V1 Payments data and is called a OrdersPayload because
        // the report name is defined by TNT as 'Orders Report'
        OrdersPayload ordersPayload = new OrdersPayload(timeZone, offset, locationDetails);

        // need to obtain Connect V2 Tender Fees and V2
        // Tender Entry Methods to map to V1 Tenders
        Map<String, Integer> tenderToFee = new HashMap<String, Integer>();
        Map<String, String> tenderToEntryMethod = new HashMap<String, String>();
        aggregateIntervalParams.put(util.Constants.SORT_ORDER_V2, util.Constants.SORT_ORDER_ASC_V2);
        Order[] orders = TntLocationDetails.getOrders(squareClientV2, locationDetails.sqLocationId, aggregateIntervalParams);
        for (Order order : orders) {
        	if(order != null && order.getTenders() != null) {
        		for (Tender tender : order.getTenders()) {
                    tenderToFee.put(tender.getId(), tender.getProcessingFeeMoney().getAmount());
                    if (tender.getCardDetails() != null && tender.getCardDetails().getEntryMethod() != null) {
                        tenderToEntryMethod.put(tender.getId(), tender.getCardDetails().getEntryMethod());
                    }
                }
        	}
        }
        Payment[] payments = TntLocationDetails.getPaymentsV2(squareClientV2, locationDetails.sqLocationId, aggregateIntervalParams);
        Map<String, Payment> tenderToPayment = TntLocationDetails.getTenderToPayment(orders, payments, squareClientV2, aggregateIntervalParams);
        for (Order order : orders) {
            ordersPayload.addEntry(order, tenderToFee, tenderToEntryMethod, tenderToPayment);
        }

        return ordersPayload;
    }

    private AbnormalOrdersPayload generateAbnormalOrdersPayload(SquareClientV2 squareClientV2,
            TntLocationDetails locationDetails, Map<String, String> aggregateIntervalParams, int offset)
            throws Exception {
        aggregateIntervalParams.put(util.Constants.SORT_ORDER_V2, util.Constants.SORT_ORDER_ASC_V2);
        Order[] orders = TntLocationDetails.getOrders(squareClientV2, locationDetails.sqLocationId, aggregateIntervalParams);
        Payment[] payments = TntLocationDetails.getPaymentsV2(squareClientV2, locationDetails.sqLocationId, aggregateIntervalParams);
        Map<String, Payment> tenderToPayment = TntLocationDetails.getTenderToPayment(orders, payments, squareClientV2, aggregateIntervalParams);
        AbnormalOrdersPayload abnormalOrdersPayload = new AbnormalOrdersPayload(timeZone, offset,
                locationDetails, tenderToPayment);
        for (Order order : orders) {
            abnormalOrdersPayload.addEntry(order);
        }

        return abnormalOrdersPayload;
    }

    private LocationSalesPayload generateLocationSalesPayload(SquareClientV2 squareClientV2,
            TntLocationDetails locationDetails, Map<String, String> aggregateIntervalParams,
            Map<String, String> dayTimeInterval, int offset) throws Exception {
        // get order data for location payload
        LocationSalesPayload locationSalesPayload = new LocationSalesPayload(timeZone, offset, dayTimeInterval,
                locationDetails);
        aggregateIntervalParams.put(util.Constants.SORT_ORDER_V2, util.Constants.SORT_ORDER_ASC_V2);
        Order[] orders = TntLocationDetails.getOrders(squareClientV2, locationDetails.sqLocationId, aggregateIntervalParams);
        Payment[] payments = TntLocationDetails.getPaymentsV2(squareClientV2, locationDetails.sqLocationId, aggregateIntervalParams);
        Map<String, Payment> tenderToPayment = TntLocationDetails.getTenderToPayment(orders, payments, squareClientV2, aggregateIntervalParams);
        for (Order order : orders) {
            locationSalesPayload.addEntry(order, tenderToPayment);
        }

        return locationSalesPayload;
    }

    private ItemSalesPayload generateItemSalesPayload(SquareClientV2 squareClientV2, TntLocationDetails locationDetails,
            Map<String, String> aggregateIntervalParams, Map<String, String> dayTimeInterval,
            List<Map<String, String>> dbItemRows, int offset) throws Exception {
        // get item sales payload for single location
        ItemSalesPayload itemSalesPayload = new ItemSalesPayload(timeZone, offset, dayTimeInterval, locationDetails);
        aggregateIntervalParams.put(util.Constants.SORT_ORDER_V2, util.Constants.SORT_ORDER_ASC_V2);

        // Get orders and payments for just one location, over the entire range
        Order[] orders = TntLocationDetails.getOrders(squareClientV2, locationDetails.sqLocationId, aggregateIntervalParams);
        Payment[] payments = TntLocationDetails.getPaymentsV2(squareClientV2, locationDetails.sqLocationId, aggregateIntervalParams);

        // Get refunds for orders
        Map<String, List<PaymentRefund>> ordersToRefundsMap = ConnectV2MigrationHelper.getRefundsForOrders(squareClientV2, orders, payments);

        // Get catalog objects referenced by orders
        Map<String, CatalogObject> catalogObjects = ConnectV2MigrationHelper.getCatalogObjectsForOrder(squareClientV2, orders);

        // Process each order for report
        for (Order order : orders) {
        	itemSalesPayload.addOrder(order, catalogObjects, ordersToRefundsMap, dbItemRows);
        }

        return itemSalesPayload;
    }

    private CreditDebitPayload generateCreditDebitPayload(SquareClientV2 squareClientV2,
            TntLocationDetails locationDetails, Map<String, String> aggregateIntervalParams,
            List<Map<String, String>> dbLoadNumbers, int offset) throws Exception {
        // "credit debit" report, payload is per location
        // loadNumber represents the total number of credit
        // debit reports sent and is tracked in DB
        int loadNumber = 0;
        for (Map<String, String> row : dbLoadNumbers) {
            if (row.get(TntDatabaseApi.DB_LOAD_NUMBER_REPORT_NAME_COLUMN)
                    .equals(TntDatabaseApi.DB_LOAD_NUMBER_REPORT8_NAME)) {
                loadNumber = Integer.parseInt(row.get(TntDatabaseApi.DB_LOAD_NUMBER_COUNT_COLUMN));
            }
        }

        CreditDebitPayload creditDebitPayload = new CreditDebitPayload(timeZone, offset, loadNumber, locationDetails);
        aggregateIntervalParams.put(util.Constants.SORT_ORDER_V2, util.Constants.SORT_ORDER_ASC_V2);
        Order[] orders = TntLocationDetails.getOrders(squareClientV2, locationDetails.sqLocationId, aggregateIntervalParams);
        Payment[] payments = TntLocationDetails.getPaymentsV2(squareClientV2, locationDetails.sqLocationId, aggregateIntervalParams);
        Map<String, Payment> tenderToPayment = TntLocationDetails.getTenderToPayment(orders, payments, squareClientV2, aggregateIntervalParams);

        for (Order order : orders) {
            creditDebitPayload.addEntry(order, tenderToPayment);
        }

        return creditDebitPayload;
    }

    private GrossSalesPayload generateGrossSalesPayload(SquareClientV2 squareClientV2, TntLocationDetails locationDetails,
            Map<String, String> aggregateIntervalParams, Map<String, String> dayTimeInterval, int offset)
            throws Exception {
        // get order data for gross sales payload
        GrossSalesPayload grossSalesPayload = new GrossSalesPayload(timeZone, offset, dayTimeInterval, locationDetails);
        aggregateIntervalParams.put(util.Constants.SORT_ORDER_V2, util.Constants.SORT_ORDER_ASC_V2);
        Order[] orders = TntLocationDetails.getOrders(squareClientV2, locationDetails.sqLocationId, aggregateIntervalParams);
        Payment[] payments = TntLocationDetails.getPaymentsV2(squareClientV2, locationDetails.sqLocationId, aggregateIntervalParams);
        Map<String, Payment> tenderToPayment = TntLocationDetails.getTenderToPayment(orders, payments, squareClientV2, aggregateIntervalParams);
        Map<String, List<PaymentRefund>> orderToRefundsMap = ConnectV2MigrationHelper.getRefundsForOrders(squareClientV2, orders, payments);
        for (Order order : orders) {
            grossSalesPayload.addOrder(order, tenderToPayment, orderToRefundsMap);
        }

        return grossSalesPayload;
    }

    private YoyGrossSalesPayload generateYoyGrossSalesPayload(SquareClientV2 squareClientV2,
            TntLocationDetails locationDetails, Map<String, String> aggregateIntervalParams,
            Map<String, String> dayTimeInterval, int offset) throws Exception {
        // get order data for gross sales payload
        GrossSalesPayload currentGrossSalesPayload = new GrossSalesPayload(timeZone, offset, dayTimeInterval,
                locationDetails);
        aggregateIntervalParams.put(util.Constants.SORT_ORDER_V2, util.Constants.SORT_ORDER_ASC_V2);
        Order[] orders = TntLocationDetails.getOrders(squareClientV2, locationDetails.sqLocationId, aggregateIntervalParams);
        Payment[] payments = TntLocationDetails.getPaymentsV2(squareClientV2, locationDetails.sqLocationId, aggregateIntervalParams);
        Map<String, Payment> tenderToPayment = TntLocationDetails.getTenderToPayment(orders, payments, squareClientV2, aggregateIntervalParams);
        Map<String, List<PaymentRefund>> orderToRefundsMap = ConnectV2MigrationHelper.getRefundsForOrders(squareClientV2, orders, payments);
        for (Order order : orders) {
            currentGrossSalesPayload.addOrder(order, tenderToPayment, orderToRefundsMap);
        }

        // get prior year time intervals
        Map<String, String> prevDayTimeInterval = getPreviousTimeInterval(dayTimeInterval,
                locationDetails.sqLocationTimeZone);
        Map<String, String> prevAggregateIntervalParams = getPreviousTimeInterval(aggregateIntervalParams,
                locationDetails.sqLocationTimeZone);
        // generate gross sales payload from prior year
        GrossSalesPayload prevGrossSalesPayload = new GrossSalesPayload(timeZone, offset, prevDayTimeInterval,
                locationDetails);
        prevAggregateIntervalParams.put(util.Constants.SORT_ORDER_V2, util.Constants.SORT_ORDER_ASC_V2);
        Order[] prevOrders = TntLocationDetails.getOrders(squareClientV2, locationDetails.sqLocationId, prevAggregateIntervalParams);
        Payment[] prevPayments = TntLocationDetails.getPaymentsV2(squareClientV2, locationDetails.sqLocationId, prevAggregateIntervalParams);
        Map<String, Payment> prevTenderToPayment = TntLocationDetails.getTenderToPayment(prevOrders, prevPayments, squareClientV2, prevAggregateIntervalParams);
        Map<String, List<PaymentRefund>> prevOrderToRefundsMap = ConnectV2MigrationHelper.getRefundsForOrders(squareClientV2, prevOrders, prevPayments);
        for (Order order : prevOrders) {
            prevGrossSalesPayload.addOrder(order, prevTenderToPayment, prevOrderToRefundsMap);
        }

        return new YoyGrossSalesPayload(timeZone, offset, dayTimeInterval, locationDetails, currentGrossSalesPayload,
                prevGrossSalesPayload);
    }

    private Map<String, String> getPreviousTimeInterval(Map<String, String> timeInterval, String timeZone)
            throws ParseException {
        Map<String, String> prevTimeInterval = new HashMap<>();

        // use Calendar to calculate previous year
        Calendar beginTime = TimeManager.toCalendar(timeInterval.get(util.Constants.BEGIN_TIME));
        Calendar endTime = TimeManager.toCalendar(timeInterval.get(util.Constants.END_TIME));

        // set time to 1 year prior
        beginTime.add(Calendar.YEAR, -1);
        endTime.add(Calendar.YEAR, -1);

        // convert to iso8601 format
        prevTimeInterval.put("begin_time", TimeManager.toIso8601(beginTime, timeZone));
        prevTimeInterval.put("end_time", TimeManager.toIso8601(endTime, timeZone));

        return prevTimeInterval;
    }
}
