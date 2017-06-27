package tntfireworks.reporting;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.Employee;
import com.squareup.connect.Payment;
import com.squareup.connect.Settlement;
import com.squareup.connect.SquareClient;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.SquareClientV2;
import com.squareup.connect.v2.Transaction;

import tntfireworks.TntDatabaseApi;
import util.DbConnection;
import util.SquarePayload;
import util.TimeManager;

public class DeploymentDetailsOptimizedCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(DeploymentDetailsCallable.class);

    private String databaseUrl;
    private String databaseUser;
    private String databasePassword;

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public void setDatabaseUser(String databaseUser) {
        this.databaseUser = databaseUser;
    }

    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        // initialize db connection
        DbConnection dbConnection = new DbConnection(databaseUrl, databaseUser, databasePassword);

        // get session vars
        int offset = Integer.parseInt(message.getProperty("offset", PropertyScope.SESSION));
        int range = Integer.parseInt(message.getProperty("range", PropertyScope.SESSION));
        int reportType = Integer.parseInt(message.getProperty("reportType", PropertyScope.SESSION));
        String apiUrl = message.getProperty("apiUrl", PropertyScope.SESSION);
        String apiVersion = message.getProperty("apiVersion", PropertyScope.SESSION);

        // compute YTD range if range = 365
        if (range == 365) {
            // - initialize startOfSeason as 03/01/2017
            // - use default tz as Los Angeles
            TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
            range = computeSeasonInterval(02, 0, 2017, tz);
        }

        // get deployment from queue-splitter
        SquarePayload deployment = (SquarePayload) message.getPayload();

        // initialize connect v1/v2 api clients
        SquareClient squareClientV1 = new SquareClient(deployment.getAccessToken(), apiUrl, apiVersion,
                deployment.getMerchantId());
        SquareClientV2 squareClientV2 = new SquareClientV2(apiUrl, deployment.getAccessToken());

        // retrieve location details according to reportType and store into abstracted object (reportPayload) 
        List<TntReportFile> masterPayload = null;
        logger.info("Retrieving location details for merchant: " + deployment.getMerchantId());
        switch (reportType) {
            case 5:
            case 6:
                masterPayload = getLocationSalesPayload(squareClientV2, dbConnection, offset, range);
                break;
            case 7:
                masterPayload = getItemSalesPayload(squareClientV1, squareClientV2, dbConnection, offset, range);
                break;
        }

        return masterPayload;
    }

    public static int computeSeasonInterval(int month, int day, int year, TimeZone tz) {
        int range = 0;

        // calendar instances
        Calendar startOfSeason = Calendar.getInstance(tz);
        Calendar today = Calendar.getInstance(tz);

        // set month day year according to params
        startOfSeason.set(Calendar.YEAR, year);
        startOfSeason.set(Calendar.MONTH, month);
        startOfSeason.set(Calendar.DAY_OF_MONTH, day);

        // set all calendars to start at midnight
        startOfSeason.set(Calendar.HOUR_OF_DAY, 0);
        startOfSeason.set(Calendar.MINUTE, 0);
        startOfSeason.set(Calendar.SECOND, 0);
        startOfSeason.set(Calendar.MILLISECOND, 0);

        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        range = (int) TimeUnit.MILLISECONDS
                .toDays(Math.abs(today.getTimeInMillis() - startOfSeason.getTimeInMillis()));

        return range;
    }

    // sales total by location
    private List<TntReportFile> getLocationSalesPayload(SquareClientV2 squareClientV2, DbConnection dbConnection,
            int offset, int range) {

        // initialize payload to store LocationSalesFile objects
        List<TntReportFile> masterPayload = new ArrayList<TntReportFile>();
        try {
            // set file date       
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
            // 0 = current date
            cal.add(Calendar.DAY_OF_YEAR, 0);
            String fileDate = TimeManager.toSimpleDateTimeInTimeZone(cal, "America/Los_Angeles", "MM-dd-yy");

            // get db information for later lookup
            TntDatabaseApi tntDatabaseApi = new TntDatabaseApi(dbConnection);
            List<Map<String, String>> dbLocationRows = tntDatabaseApi
                    .submitQuery(tntDatabaseApi.generateLocationSQLSelect());
            tntDatabaseApi.close();

            for (Location location : squareClientV2.locations().list()) {
                if (!location.getName().contains("DEACTIVATED") && !location.getName().contains("DEFAULT")) {
                    // define time intervals to pull payment data
                    Map<String, String> dayTimeInterval = TimeManager.getPastDayInterval(1, offset,
                            location.getTimezone());
                    squareClientV2.setLocation(location.getId());

                    // lookup TNT location specific data 
                    String locationNumber = findLocationNumber(location.getName());
                    String rbu = "";
                    for (Map<String, String> row : dbLocationRows) {
                        if (locationNumber.equals(row.get("locationNumber"))) {
                            rbu = row.get("rbu");
                        }
                    }

                    LocationSalesFile locationSalesFile = new LocationSalesFile(fileDate, dayTimeInterval,
                            locationNumber,
                            rbu);
                    Map<String, String> aggregateInterval = TimeManager.getPastDayInterval(range, offset,
                            location.getTimezone());
                    aggregateInterval.put("sort_order", "ASC"); // v2 default is DESC

                    for (Transaction transaction : getTransactions(squareClientV2, aggregateInterval)) {
                        locationSalesFile.addTransaction(transaction);
                    }
                    masterPayload.add(locationSalesFile);
                }
            }
        } catch (Exception e) {
            logger.error("ERROR: caught exception while aggregating 'Report 5/6' details: " + e);
        }

        return masterPayload;
    }

    // item sale totals by location
    private List<TntReportFile> getItemSalesPayload(SquareClient squareClientV1, SquareClientV2 squareClientV2,
            DbConnection dbConnection, int offset, int range) {

        // initialize payload to store ItemSalesFile objects
        List<TntReportFile> masterPayload = new ArrayList<TntReportFile>();

        try {
            // set file date       
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
            // 0 = current date
            cal.add(Calendar.DAY_OF_YEAR, 0);
            String fileDate = TimeManager.toSimpleDateTimeInTimeZone(cal, "America/Los_Angeles", "MM-dd-yy");

            // get db information for later lookup
            TntDatabaseApi tntDatabaseApi = new TntDatabaseApi(dbConnection);
            List<Map<String, String>> dbLocationRows = tntDatabaseApi
                    .submitQuery(tntDatabaseApi.generateLocationSQLSelect());
            List<Map<String, String>> dbItemRows = tntDatabaseApi
                    .submitQuery(tntDatabaseApi.generateItemSQLSelect());
            tntDatabaseApi.close();

            for (Location location : squareClientV2.locations().list()) {
                if (!location.getName().contains("DEACTIVATED") && !location.getName().contains("DEFAULT")) {
                    // define time intervals to pull payment data
                    Map<String, String> dayTimeInterval = TimeManager.getPastDayInterval(1, offset,
                            location.getTimezone());
                    squareClientV1.setLocation(location.getId());
                    squareClientV2.setLocation(location.getId());

                    // lookup TNT location specific data 
                    String locationNumber = findLocationNumber(location.getName());
                    String rbu = "";
                    for (Map<String, String> row : dbLocationRows) {
                        if (locationNumber.equals(row.get("locationNumber"))) {
                            rbu = row.get("rbu");
                        }
                    }

                    ItemSalesFile itemSalesFile = new ItemSalesFile(fileDate, dayTimeInterval, locationNumber, rbu);
                    Map<String, String> aggregateIntervalParams = TimeManager.getPastDayInterval(range, offset,
                            location.getTimezone());
                    aggregateIntervalParams.put("sort_order", "ASC"); // v2 default is DESC

                    for (Payment payment : getPayments(squareClientV1, aggregateIntervalParams)) {
                        itemSalesFile.addFileEntry(payment, dbItemRows);
                    }
                    masterPayload.add(itemSalesFile);
                }
            }
        } catch (Exception e) {
            logger.error("ERROR: caught exception while aggregating 'Report 7' details: " + e);
        }

        return masterPayload;
    }

    /*
    * Helper function to parse location number
    * 
    * - per TNT spec, all upcoming seasons will follow new naming convention
    *   location name = TNT location number
    * - old seasons followed convention of 'NAME (#LocationNumber)'
    * 
    */
    protected String findLocationNumber(String locationName) {
        String locationNumber = "";

        // old location name =  'NAME (#Location Number)'
        String oldPattern = "\\w+\\s*\\(#([a-zA-Z0-9\\s]+)\\)";
        Pattern p = Pattern.compile(oldPattern);
        Matcher m = p.matcher(locationName);

        if (m.find()) {
            locationNumber = m.group(1);
        } else {
            if (!locationName.equals("")) {
                locationNumber = locationName;
            }
        }
        return locationNumber;
    }

    private Payment[] getPayments(SquareClient squareClient, Map<String, String> params)
            throws Exception {
        // V1 Payments - ignore no-sale and cash-only payments
        Payment[] allPayments = squareClient.payments().list(params);
        List<Payment> payments = new ArrayList<Payment>();

        for (Payment payment : allPayments) {
            boolean hasValidPaymentTender = false;
            for (com.squareup.connect.Tender tender : payment.getTender()) {
                if (!tender.getType().equals("CASH") && !tender.getType().equals("NO_SALE")) {
                    hasValidPaymentTender = true;
                }
            }
            if (hasValidPaymentTender) {
                payments.add(payment);
            }
        }
        return payments.toArray(new Payment[0]);
    }

    private Transaction[] getTransactions(SquareClientV2 squareClient, Map<String, String> params)
            throws Exception {
        // V2 Transactions - ignore no-sales and cash-only transactions
        params.put("sort_order", "ASC"); // v2 default is DESC
        Transaction[] allTransactions = squareClient.transactions().list(params);
        List<Transaction> transactions = new ArrayList<Transaction>();

        for (Transaction transaction : allTransactions) {
            boolean hasValidTransactionTender = false;
            for (com.squareup.connect.v2.Tender tender : transaction.getTenders()) {
                if (!tender.getType().equals("NO_SALE")) {
                    hasValidTransactionTender = true;
                }
            }
            if (hasValidTransactionTender) {
                transactions.add(transaction);
            }
        }

        return transactions.toArray(new Transaction[0]);
    }

    private Map<String, Employee> getEmployees(SquareClient squareClient) throws Exception {
        Map<String, Employee> employeeMap = new HashMap<String, Employee>();

        for (Employee employee : squareClient.employees().list()) {
            employeeMap.put(employee.getId(), employee);
        }

        return employeeMap;
    }

    private Settlement[] getSettlements(SquareClient squareClient, Map<String, String> params) throws Exception {
        // V1 Settlements
        return squareClient.settlements().list(params);
    }
}