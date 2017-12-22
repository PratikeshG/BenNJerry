package tntfireworks.reporting;

import java.sql.SQLException;
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

import com.squareup.connect.Payment;
import com.squareup.connect.Settlement;
import com.squareup.connect.SquareClient;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.SquareClientV2;
import com.squareup.connect.v2.Tender;
import com.squareup.connect.v2.Transaction;

import tntfireworks.TntDatabaseApi;
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
    private static final String SEASON_REPORTING_ENABLED = "TRUE";
    private static final String TNT_DEFAULT_LOCATION_NAME_PREFIX = "DEFAULT";

    @Value("${tntfireworks.startOfSeason}")
    private String startOfSeason;
    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}")
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

        // get session vars
        int offset = Integer.parseInt(message.getProperty("offset", PropertyScope.SESSION));
        int range = Integer.parseInt(message.getProperty("range", PropertyScope.SESSION));
        int reportType = Integer.parseInt(message.getProperty("reportType", PropertyScope.SESSION));
        String ytd = message.getProperty("ytd", PropertyScope.SESSION);

        // get time zone for file
        timeZone = util.Constants.PST_TIME_ZONE_ID;
        if (cronTimeZone != null && !cronTimeZone.isEmpty()) {
            timeZone = cronTimeZone;
        }

        // set range to interval from season start date if YTD set to true
        if (ytd.equals(SEASON_REPORTING_ENABLED)) {
            range = computeSeasonInterval(startOfSeason, offset, TimeZone.getTimeZone(timeZone));
        }

        // get deployment from queue-splitter
        SquarePayload deployment = (SquarePayload) message.getPayload();

        // initialize connect v1/v2 api clients
        SquareClient squareClientV1 = new SquareClient(deployment.getAccessToken(encryptionKey), apiUrl, apiVersion,
                deployment.getMerchantId());
        SquareClientV2 squareClientV2 = new SquareClientV2(apiUrl, deployment.getAccessToken(encryptionKey));

        // retrieve location details according to reportType and store into
        // abstracted object (reportPayload)
        logger.info("Retrieving location details for merchant: " + deployment.getMerchantId());
        return getMerchantPayload(reportType, squareClientV1, squareClientV2, offset, range);
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

    private List<TntReportLocationPayload> getMerchantPayload(int reportType, SquareClient squareClientV1,
            SquareClientV2 squareClientV2, int offset, int range) throws ClassNotFoundException, SQLException {

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
                if (isValidLocation(location)) {
                    // initialize TntLocationDetails with DB data
                    TntLocationDetails locationDetails = new TntLocationDetails(dbLocationRows, location.getName());

                    // define time intervals to pull payment data
                    // - dayTimeInterval is currently only used for 5/6 and report 7
                    // - aggregateInterval is used by remaining flows
                    Map<String, String> dayTimeInterval = TimeManager.getPastDayInterval(DAY_RANGE, offset,
                            location.getTimezone());
                    Map<String, String> aggregateIntervalParams = TimeManager.getPastDayInterval(range, offset,
                            location.getTimezone());

                    // set squareClient to specific location
                    squareClientV1.setLocation(location.getId());
                    squareClientV2.setLocation(location.getId());

                    switch (reportType) {
                        case SETTLEMENTS_REPORT_TYPE:
                            merchantPayload.add(generateSettlementsPayload(squareClientV1, locationDetails,
                                    aggregateIntervalParams));
                            break;
                        case TRANSACTIONS_REPORT_TYPE:
                            merchantPayload.add(generateTransactionsPayload(squareClientV2, squareClientV1,
                                    locationDetails, aggregateIntervalParams));
                            break;
                        case ABNORMAL_TRANSACTIONS_REPORT_TYPE:
                            merchantPayload.add(generateAbnormalTransactionsPayload(squareClientV2, locationDetails,
                                    aggregateIntervalParams));
                            break;
                        case CHARGEBACK_REPORT_TYPE:
                            // report 4 is currently generated from a different source
                            break;
                        case LOCATION_SALES_REPORT_TYPE:
                            merchantPayload.add(generateLocationSalesPayload(squareClientV2, locationDetails,
                                    aggregateIntervalParams, dayTimeInterval));
                            break;
                        case ITEM_SALES_REPORT_TYPE:
                            merchantPayload.add(generateItemSalesPayload(squareClientV2, squareClientV1,
                                    locationDetails, aggregateIntervalParams, dayTimeInterval, dbItemRows));
                            break;
                        case CREDIT_DEBIT_REPORT_TYPE:
                            merchantPayload.add(generateCreditDebitPayload(squareClientV1, locationDetails,
                                    aggregateIntervalParams, dbLoadNumbers));
                            break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("ERROR: caught exception while aggregating '" + reportType + "' details: " + e);
        }

        return merchantPayload;
    }

    private boolean isValidLocation(Location location) {
        return location.getStatus().equals(Location.LOCATION_STATUS_ACTIVE)
                && !location.getName().contains(TNT_DEFAULT_LOCATION_NAME_PREFIX);
    }

    private SettlementsPayload generateSettlementsPayload(SquareClient squareClientV1,
            TntLocationDetails locationDetails, Map<String, String> aggregateIntervalParams) throws Exception {
        SettlementsPayload settlementsPayload = new SettlementsPayload(timeZone, locationDetails);
        aggregateIntervalParams.put(util.Constants.SORT_ORDER_V1, util.Constants.SORT_ORDER_ASC_V1);

        for (Settlement settlement : TntLocationDetails.getSettlements(squareClientV1, aggregateIntervalParams)) {
            settlementsPayload.addEntry(settlement);
        }

        return settlementsPayload;
    }

    private TransactionsPayload generateTransactionsPayload(SquareClientV2 squareClientV2, SquareClient squareClientV1,
            TntLocationDetails locationDetails, Map<String, String> aggregateIntervalParams) throws Exception {
        // - each TransactionsPayload includes transaction data from a single location
        // - while the payload object name may imply V2Transactions data, the payload
        // mainly consists of V1 Payments data and is called a TransactionsPayload because
        // the report name is defined by TNT as 'Transactions Report'
        TransactionsPayload transactionsPayload = new TransactionsPayload(timeZone, locationDetails);

        // need to obtain Connect V2 Tender Fees and V2
        // Tender Entry Methods to map to V1 Tenders
        Map<String, Integer> tenderToFee = new HashMap<String, Integer>();
        Map<String, String> tenderToEntryMethod = new HashMap<String, String>();
        aggregateIntervalParams.put(util.Constants.SORT_ORDER_V2, util.Constants.SORT_ORDER_ASC_V2);
        for (Transaction transaction : TntLocationDetails.getTransactions(squareClientV2, aggregateIntervalParams)) {
            for (Tender tender : transaction.getTenders()) {
                tenderToFee.put(tender.getId(), tender.getProcessingFeeMoney().getAmount());
                if (tender.getCardDetails() != null && tender.getCardDetails().getEntryMethod() != null) {
                    tenderToEntryMethod.put(tender.getId(), tender.getCardDetails().getEntryMethod());
                }
            }
        }

        aggregateIntervalParams.put(util.Constants.SORT_ORDER_V1, util.Constants.SORT_ORDER_ASC_V1);
        for (Payment payment : TntLocationDetails.getPayments(squareClientV1, aggregateIntervalParams)) {
            transactionsPayload.addEntry(payment, tenderToFee, tenderToEntryMethod);
        }

        return transactionsPayload;
    }

    private AbnormalTransactionsPayload generateAbnormalTransactionsPayload(SquareClientV2 squareClientV2,
            TntLocationDetails locationDetails, Map<String, String> aggregateIntervalParams) throws Exception {
        aggregateIntervalParams.put(util.Constants.SORT_ORDER_V2, util.Constants.SORT_ORDER_ASC_V2);
        AbnormalTransactionsPayload abnormalTransactionsPayload = new AbnormalTransactionsPayload(timeZone,
                locationDetails);
        for (Transaction transaction : TntLocationDetails.getTransactions(squareClientV2, aggregateIntervalParams)) {
            abnormalTransactionsPayload.addEntry(transaction);
        }

        return abnormalTransactionsPayload;
    }

    private LocationSalesPayload generateLocationSalesPayload(SquareClientV2 squareClientV2,
            TntLocationDetails locationDetails, Map<String, String> aggregateIntervalParams,
            Map<String, String> dayTimeInterval) throws Exception {
        // get transaction data for location payload
        LocationSalesPayload locationSalesPayload = new LocationSalesPayload(timeZone, dayTimeInterval,
                locationDetails);
        aggregateIntervalParams.put(util.Constants.SORT_ORDER_V2, util.Constants.SORT_ORDER_ASC_V2);
        for (Transaction transaction : TntLocationDetails.getTransactions(squareClientV2, aggregateIntervalParams)) {
            locationSalesPayload.addEntry(transaction);
        }

        return locationSalesPayload;
    }

    private ItemSalesPayload generateItemSalesPayload(SquareClientV2 squareClientV2, SquareClient squareClientV1,
            TntLocationDetails locationDetails, Map<String, String> aggregateIntervalParams,
            Map<String, String> dayTimeInterval, List<Map<String, String>> dbItemRows) throws Exception {
        // get item sales payload for single location
        ItemSalesPayload itemSalesPayload = new ItemSalesPayload(timeZone, dayTimeInterval, locationDetails);
        aggregateIntervalParams.put(util.Constants.SORT_ORDER_V1, util.Constants.SORT_ORDER_ASC_V1);
        for (Payment payment : TntLocationDetails.getPayments(squareClientV1, aggregateIntervalParams)) {
            itemSalesPayload.addEntry(payment, dbItemRows);
        }

        return itemSalesPayload;
    }

    private CreditDebitPayload generateCreditDebitPayload(SquareClient squareClientV1,
            TntLocationDetails locationDetails, Map<String, String> aggregateIntervalParams,
            List<Map<String, String>> dbLoadNumbers) throws Exception {
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

        CreditDebitPayload creditDebitPayload = new CreditDebitPayload(timeZone, loadNumber, locationDetails);
        aggregateIntervalParams.put(util.Constants.SORT_ORDER_V1, util.Constants.SORT_ORDER_ASC_V1);
        for (Payment payment : TntLocationDetails.getPayments(squareClientV1, aggregateIntervalParams)) {
            creditDebitPayload.addEntry(payment);
        }

        return creditDebitPayload;
    }
}
