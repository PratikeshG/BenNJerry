package tntfireworks.reporting;

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
    private static final String DEFAULT_TIME_ZONE = "America/Los_Angeles";
    private static final int SETTLEMENTS_REPORT_TYPE = 1;
    private static final int TRANSACTIONS_REPORT_TYPE = 2;
    private static final int ABNORMAL_TRANSACTIONS_REPORT_TYPE = 3;
    private static final int CHARGEBACK_REPORT_TYPE = 4;
    private static final int LOCATION_SALES_REPORT_TYPE = 5;
    private static final int ITEM_SALES_REPORT_TYPE = 7;
    private static final int CREDIT_DEBIT_REPORT_TYPE = 8;

    private String timeZone;

    @Value("${tntfireworks.startOfSeason}")
    private String startOfSeason;
    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}")
    private String databaseUrl;
    @Value("${mysql.user}")
    private String databaseUser;
    @Value("${mysql.password}")
    private String databasePassword;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        // initialize dbConnection
		DbConnection dbConnection = new DbConnection(databaseUrl, databaseUser, databasePassword);

        // get session vars
        int offset = Integer.parseInt(message.getProperty("offset", PropertyScope.SESSION));
        int range = Integer.parseInt(message.getProperty("range", PropertyScope.SESSION));
        int reportType = Integer.parseInt(message.getProperty("reportType", PropertyScope.SESSION));
        String apiUrl = message.getProperty("apiUrl", PropertyScope.SESSION);
        String apiVersion = message.getProperty("apiVersion", PropertyScope.SESSION);

        // get time zone for file
        timeZone = DEFAULT_TIME_ZONE;
        String cronTimeZone = message.getProperty("cronTimeZone", PropertyScope.INVOCATION);
        if (cronTimeZone != null && !cronTimeZone.isEmpty()) {
        	timeZone = cronTimeZone;
        }

        // compute season range if range = 365
        if (range == 365) {
            range = computeSeasonInterval(startOfSeason, TimeZone.getTimeZone(timeZone));
        }

        // get deployment from queue-splitter
        SquarePayload deployment = (SquarePayload) message.getPayload();

        // initialize connect v1/v2 api clients
        SquareClient squareClientV1 = new SquareClient(deployment.getAccessToken(), apiUrl, apiVersion,
                deployment.getMerchantId());
        SquareClientV2 squareClientV2 = new SquareClientV2(apiUrl, deployment.getAccessToken());

        // retrieve location details according to reportType and store into abstracted object (reportPayload)
        logger.info("Retrieving location details for merchant: " + deployment.getMerchantId());
        return getMerchantPayload(reportType, squareClientV1, squareClientV2, dbConnection, offset, range);
    }

    public static int computeSeasonInterval(String startOfSeason, TimeZone tz) {
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

        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        range = (int) TimeUnit.MILLISECONDS
                .toDays(Math.abs(today.getTimeInMillis() - startOfSeasonCal.getTimeInMillis()));

        return range;
    }

    private List<TntReportLocationPayload> getMerchantPayload(int reportType, SquareClient squareClientV1, SquareClientV2 squareClientV2,
            DbConnection dbConnection, int offset, int range) {

        // initialize payload to store ItemSalesFile objects
        List<TntReportLocationPayload> merchantPayload = new ArrayList<TntReportLocationPayload>();

        try {
            // get db information for later lookup
        	// initialized TntDatabaseApi for information lookup
            TntDatabaseApi tntDatabaseApi = new TntDatabaseApi(dbConnection);
            List<Map<String, String>> dbLocationRows = tntDatabaseApi
                    .submitQuery(tntDatabaseApi.generateLocationSQLSelect());
            List<Map<String, String>> dbItemRows = tntDatabaseApi
                    .submitQuery(tntDatabaseApi.generateItemSQLSelect());
    	    List<Map<String, String>> dbLoadNumbers = tntDatabaseApi
    	            .submitQuery(tntDatabaseApi.generateLoadNumberSQLSelect());
            tntDatabaseApi.close();

            // iterate through each location in merchant and aggregate account data
            for (Location location : squareClientV2.locations().list()) {
                if (!location.getName().contains("DEACTIVATED") && !location.getName().contains("DEFAULT")) {
                    // define time intervals to pull payment data
                	//     - dayTimeInterval is currently only used for report 5/6 and report 7
                	//     - aggregateInterval is used by remaining flows
                    Map<String, String> dayTimeInterval = TimeManager.getPastDayInterval(1, offset,
                            location.getTimezone());
                    Map<String, String> aggregateIntervalParams = TimeManager.getPastDayInterval(range, offset,
                            location.getTimezone());
                    aggregateIntervalParams.put("sort_order", "ASC"); // v2 default is DESC

                    // set squareClient to specific location
                    squareClientV1.setLocation(location.getId());
                    squareClientV2.setLocation(location.getId());

                    switch (reportType) {
                    	case SETTLEMENTS_REPORT_TYPE:
                    		// settlements report
                            SettlementsPayload settlementsPayload = new SettlementsPayload(timeZone, location.getName(), dbLocationRows);
                    		for (Settlement settlement : TntLocationDetails.getSettlements(squareClientV1, aggregateIntervalParams)) {
                    			settlementsPayload.addEntry(settlement);
                    		}

                            merchantPayload.add(settlementsPayload);
                    		break;
                    	case TRANSACTIONS_REPORT_TYPE:

                    	    // - each TransactionsPayload includes transaction data from a single location
                    	    // - while the payload object name may imply V2 Transactions data, the payload mainly consists of V1 Payments data
                    	    //   and is called a TransactionsPayload because the report name is defined by TNT as 'Transactions Report'
                    	    TransactionsPayload transactionsPayload = new TransactionsPayload(timeZone, location.getName(), dbLocationRows);

                    	    // need to obtain Connect V2 Tender Fees and V2 Tender Entry Methods to map to V1 Tenders
                    	    Map<String, Integer> tenderToFee = new HashMap<String, Integer>();
                    	    Map<String, String> tenderToEntryMethod = new HashMap<String, String>();

                    	    for (Transaction transaction : TntLocationDetails.getTransactions(squareClientV2, aggregateIntervalParams)) {
                    	        for (Tender tender : transaction.getTenders()) {
                                    tenderToFee.put(tender.getId(), tender.getProcessingFeeMoney().getAmount());
                                    if (tender.getCardDetails() != null && tender.getCardDetails().getEntryMethod() != null) {
                                        tenderToEntryMethod.put(tender.getId(), tender.getCardDetails().getEntryMethod());
                                    }
                    	        }
                    	    }

                    	    for (Payment payment : TntLocationDetails.getPayments(squareClientV1, aggregateIntervalParams)) {
                    	        transactionsPayload.addEntry(payment, tenderToFee, tenderToEntryMethod);
                    	    }

                    	    merchantPayload.add(transactionsPayload);
                    	    break;
                    	case ABNORMAL_TRANSACTIONS_REPORT_TYPE:
                    	    // detect anomaly transactions across locations / per merchant account
                            AbnormalTransactionsPayload abnormalTransactionsPayload =
                                new AbnormalTransactionsPayload(timeZone, aggregateIntervalParams, location.getName(), dbLocationRows);
                            for (Transaction transaction : TntLocationDetails.getTransactions(squareClientV2, aggregateIntervalParams)) {
                                abnormalTransactionsPayload.addEntry(transaction);
                            }

                            merchantPayload.add(abnormalTransactionsPayload);
                    	    break;
                    	case CHARGEBACK_REPORT_TYPE:
                    		// report 4 is currently generated from a different source
                    	    break;
                        case LOCATION_SALES_REPORT_TYPE:
                        	// added separate case statement for clarity
                        	// get transaction data for location payload
                            LocationSalesPayload locationSalesPayload = new LocationSalesPayload(timeZone, dayTimeInterval,
                                    location.getName(), dbLocationRows);
                            for (Transaction transaction : TntLocationDetails.getTransactions(squareClientV2, aggregateIntervalParams)) {
                                locationSalesPayload.addEntry(transaction);
                            }

                            merchantPayload.add(locationSalesPayload);
                            break;
                        case ITEM_SALES_REPORT_TYPE:
                        	// get item sales payload for single location
                            ItemSalesPayload itemSalesPayload = new ItemSalesPayload(timeZone, dayTimeInterval, location.getName(), dbLocationRows);
                            for (Payment payment : TntLocationDetails.getPayments(squareClientV1, aggregateIntervalParams)) {
                                itemSalesPayload.addEntry(payment, dbItemRows);
                            }

                        	merchantPayload.add(itemSalesPayload);
                            break;
                        case CREDIT_DEBIT_REPORT_TYPE:
                        	// "credit debit" report, payload is per location
                    	    // loadNumber represents the total number of credit debit reports sent and is tracked in DB
                    	    int loadNumber = 0;
                    	    for (Map<String, String> row : dbLoadNumbers) {
                    	        if (row.get("reportName").equals(CreditDebitPayload.DB_REPORT_NAME)) {
                    	            loadNumber = Integer.parseInt(row.get("count"));
                    	        }
                    	    }

                            CreditDebitPayload creditDebitPayload = new CreditDebitPayload(timeZone, loadNumber, location.getName(), dbLocationRows);
                        	for (Payment payment : TntLocationDetails.getPayments(squareClientV1, aggregateIntervalParams)) {
                        		creditDebitPayload.addEntry(payment);
                        	}

                        	merchantPayload.add(creditDebitPayload);
                        	break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("ERROR: caught exception while aggregating '" + reportType + "' details: " + e);
        }

        return merchantPayload;
    }
}
