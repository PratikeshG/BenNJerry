package tntfireworks.reporting;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.Tender;
import com.squareup.connect.v2.Transaction;

import tntfireworks.TntDatabaseApi;
import util.DbConnection;
import util.TimeManager;

public class AbnormalTransactionsFile {
    private static Logger logger = LoggerFactory.getLogger(AbnormalTransactionsFile.class);
    private static final int ALERT_THRESHOLD_1 = 100000;
    private static final int ALERT_THRESHOLD_2 = 50000;
    private static final int ALERT_THRESHOLD_6 = 15000;

    /* alert types
     * 
     * Alert Level  | Description
     * -----------------------------------------------------------
     *      1       | Card Present Transaction exceeds $1,000
     *      2       | Card Not Present Transaction exceeds $500
     *      3       | Card Not Present transaction more than 3 times in one day at same location
     *      4       | Same card used 4 or more times across entire population in one day (tentative for entire season)
     *      5       | Same dollar amount run on card-tender consecutively at same location 3 or more times in one day
     *      6       | Card Not Present Transaction exceeds $150
     *  
     */
    private List<AlertEntry> alerts;
    private String fileDate;
    private int offset;

    public AbnormalTransactionsFile(List<List<TntLocationDetails>> deploymentsAggregate, DbConnection dbConnection,
            int offset)
            throws Exception {

        // set offset for dayTimeInterval
        this.offset = offset;

        // set file date
        this.fileDate = getDate("America/Los_Angeles", "MM-dd-yy", 0);

        // initialize instances for alert entries and tender id to employee mapping
        this.alerts = new ArrayList<AlertEntry>();

        // cache location data from tnt database to limit to 1 query submission
        TntDatabaseApi tntDatabaseApi = new TntDatabaseApi(dbConnection);
        List<Map<String, String>> dbLocationRows = tntDatabaseApi
                .submitQuery(tntDatabaseApi.generateLocationSQLSelect());
        tntDatabaseApi.close();

        // map of key (4 digits + card brand) and AlertEntry[] 
        Map<String, List<AlertEntry>> alert4Entries = new HashMap<String, List<AlertEntry>>();

        for (List<TntLocationDetails> deployment : deploymentsAggregate) {
            for (TntLocationDetails locationDetails : deployment) {
                // dayTimeInterval for all alerts except for alert4
                Location location = locationDetails.getLocation();
                Map<String, String> dayTimeInterval = TimeManager.getPastDayInterval(1, offset,
                        location.getTimezone());
                // use calendar objects to daily interval
                Calendar beginTime = TimeManager.toCalendar(dayTimeInterval.get("begin_time"));
                Calendar endTime = TimeManager.toCalendar(dayTimeInterval.get("end_time"));

                List<Transaction> alert3Transactions = new ArrayList<Transaction>();
                Map<String, List<Transaction>> alert5Transactions = new HashMap<String, List<Transaction>>();
                List<Transaction> alert5Buffer = new ArrayList<Transaction>();

                // used for alert type 5, initialize to 0 value for start
                int prevAmt = 0;

                for (Transaction transaction : locationDetails.getTransactions()) {
                    // used to filter which transactions to use for daily alerts
                    Calendar transactionTime = TimeManager.toCalendar(transaction.getCreatedAt());

                    // determine if this transaction should be included in "daily" totals
                    if (beginTime.compareTo(transactionTime) <= 0 && endTime.compareTo(transactionTime) > 0) {
                        // alert 1, threshold $1000
                        int alertLevel = 0;
                        if (checkCpAlert(transaction, ALERT_THRESHOLD_1)) {
                            alertLevel = 1;
                            alerts.add(new AlertEntry(locationDetails, transaction, alertLevel, dbLocationRows));
                        }

                        // alert 2 and 6 have same criteria, different thresholds
                        // alert 2 > $500, alert 7 > $150
                        if (checkCnpAlert(transaction, ALERT_THRESHOLD_6)) {
                            alertLevel = 6;
                            if (checkCnpAlert(transaction, ALERT_THRESHOLD_2)) {
                                alertLevel = 2;
                            }
                            alerts.add(new AlertEntry(locationDetails, transaction, alertLevel, dbLocationRows));
                        }

                        for (Tender tender : transaction.getTenders()) {
                            // for alert 3, keep running list of transactions with CNP entry methods
                            String entryMethod = "";
                            try {
                                entryMethod = tender.getCardDetails().getEntryMethod();
                            } catch (Exception e) {
                                logger.warn("No entryMethod for this tender: " + tender.getId());
                            }

                            if (entryMethod.equals("MANUAL") || entryMethod.equals("WEB_FORM")) {
                                alert3Transactions.add(transaction);
                                break;
                            }

                            // alert 5
                            if (tender.getType().equals("CARD")) {
                                // for alert 5, keep consecutive list of transactions with same dollar amounts
                                int currentAmt = tender.getAmountMoney().getAmount();

                                if (currentAmt == prevAmt) {
                                    alert5Buffer.add(transaction);
                                } else {
                                    if (alert5Buffer.size() >= 3) {
                                        // store buffer of consecutive transactions with same amount
                                        // use transactionId as unique identifier
                                        alert5Transactions.put(transaction.getId(), new ArrayList(alert5Buffer));
                                    }
                                    // clear buffer to start tracking new transaction amount
                                    alert5Buffer.clear();
                                    alert5Buffer.add(transaction);

                                    // store new amount for tracking
                                    prevAmt = currentAmt;
                                }
                            }
                        }
                    }

                    // for alert 4, keep running list of cards (using 4 digits + brand)
                    for (Tender tender : transaction.getTenders()) {
                        if (tender.getType().equals("CARD")) {
                            try {
                                String last4 = tender.getCardDetails().getCard().getLast4();
                                String cardBrand = tender.getCardDetails().getCard().getCardBrand();

                                // use last 4 digits and card brand as key to each card
                                String key = String.format("%s%s", last4, cardBrand);
                                if (alert4Entries.containsKey(key)) {
                                    List<AlertEntry> potentialAlerts = alert4Entries.get(key);
                                    potentialAlerts
                                            .add(new AlertEntry(locationDetails, transaction, 4, dbLocationRows));
                                    alert4Entries.put(key, potentialAlerts);
                                } else {
                                    List<AlertEntry> potentialAlerts = new ArrayList<AlertEntry>();
                                    potentialAlerts
                                            .add(new AlertEntry(locationDetails, transaction, 4, dbLocationRows));
                                    alert4Entries.put(key, potentialAlerts);
                                }
                            } catch (Exception e) {
                                logger.warn("Card details missing to create unique key for alert 4 transactions.");
                            }
                        }
                    }
                } // for (transaction : transactions)

                // for alert 3, check size of alert3Transactions list and add to alerts
                if (alert3Transactions.size() > 3) {
                    for (Transaction transaction : alert3Transactions) {
                        alerts.add(new AlertEntry(locationDetails, transaction, 3, dbLocationRows));
                    }
                }

                // for alert 5, find same dollar amount consecutively >=3 times per day
                for (String key : alert5Transactions.keySet()) {
                    if (alert5Transactions.get(key).size() >= 3) {
                        for (Transaction transaction : alert5Transactions.get(key)) {
                            alerts.add(new AlertEntry(locationDetails, transaction, 5, dbLocationRows));
                        }
                    }
                }
            } // for (locationDetails : deployment) 
        } // for (deployment : deployments)

        // for alert 4, check size of alert4Transactions list and add to alerts
        for (String key : alert4Entries.keySet()) {
            if (alert4Entries.get(key).size() >= 4) {
                for (AlertEntry alert : alert4Entries.get(key)) {
                    alerts.add(alert);
                }
            }
        }

    } // AbnormalTransactionsFile constructor

    private boolean checkCpAlert(Transaction transaction, int threshold) {
        /* 
         * alert level 1 criteria
         *      1. Card Present transaction
         *      2. Entry Method: SWIPED
         *      3. Product: REGISTER only
         *      4. Tender Type: not CASH or NO_SALE (only valid tender types are passed to this function)
         *      5. threshold > $1000.00
         *      
         */

        for (Tender tender : transaction.getTenders()) {
            String entryMethod = "";
            try {
                entryMethod = tender.getCardDetails().getEntryMethod();
            } catch (Exception e) {
                logger.warn("No entryMethod for this tender: " + tender.getId());
            }

            if (transaction.getProduct().equals("REGISTER") && entryMethod.equals("SWIPED")) {
                if (tender.getAmountMoney().getAmount() > threshold) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkCnpAlert(Transaction transaction, int threshold) {
        /* 
         * alert level 2 criteria
         *      1. Card NOT Present transaction
         *      2. Entry Method: MANUAL, WEB_FORM
         *      3. Product: EXTERNAL_API or REGISTER (does not matter)
         *      4. Tender Type: not CASH or NO_SALE (only valid tender types are passed to this function)
         *      5. threshold > $500.00
         *  
         *  alert level 6 criteria
         *      1. All of above except threshold > $150.00
         */

        for (Tender tender : transaction.getTenders()) {
            String entryMethod = "";

            try {
                entryMethod = tender.getCardDetails().getEntryMethod();
            } catch (Exception e) {
                logger.warn("No entryMethod for this tender: " + tender.getId());
            }

            if ((entryMethod.equals("MANUAL") || entryMethod.equals("WEB_FORM"))
                    && tender.getAmountMoney().getAmount() > threshold) {
                return true;
            }
        }
        return false;
    }

    private String getDate(String timezone, String dateFormat, int offset) throws Exception {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timezone));

        // 0 = current date
        cal.add(Calendar.DAY_OF_YEAR, offset);

        return TimeManager.toSimpleDateTimeInTimeZone(cal, timezone, dateFormat);
    }

    public String getFileDate() {
        return fileDate;
    }

    private String formatTotal(int gpv) {
        NumberFormat n = NumberFormat.getCurrencyInstance(Locale.US);
        return n.format(gpv / 100.0).replaceAll(",", "");
    }

    public String generateBatchReport() {
        StringBuilder reportBuilder = new StringBuilder();

        // write file header
        String fileHeader = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s \n",
                "Alert Level", "Alert Type", "Location Number", "SQ Customer Id", "City", "State",
                "Sales Associate", "Transaction Amount", "Transaction Date (UTC)", "Transaction Status", "Card Brand",
                "Last 4 Digits", "Transaction Id", "Entry Method", "RBU");
        reportBuilder.append(fileHeader);

        // NOTE: WHEN COMPILING REPORT, LOOP THROUGH TENDERS FOR
        //     1. card brand
        //     2. last 4 digits
        //     3. entry method
        //     4. transaction status
        //     5. status
        //     6. customerId
        for (AlertEntry alert : alerts) {
            for (Tender tender : alert.tenders) {
                String cardBrand = "";
                String last4 = "";
                String status = "";
                String entryMethod = "";
                String customerId = "";

                // occasional null values for card detail fields below from the API
                if (tender.getType().equals("CARD")) {
                    try {
                        cardBrand = tender.getCardDetails().getCard().getCardBrand();
                        if (cardBrand == null) {
                            cardBrand = "";
                        }
                    } catch (Exception e) {
                        logger.warn("Missing card type for tenderId: " + tender.getId());
                    }
                    try {
                        last4 = tender.getCardDetails().getCard().getLast4();
                        if (last4 == null) {
                            last4 = "";
                        }
                    } catch (Exception e) {
                        logger.warn("Missing 4 digits for tenderId: " + tender.getId());
                    }
                    try {
                        status = tender.getCardDetails().getStatus();
                        if (status == null) {
                            status = "";
                        }
                    } catch (Exception e) {
                        logger.warn("Missing status for tenderId: " + tender.getId());
                    }
                    try {
                        entryMethod = tender.getCardDetails().getEntryMethod();
                        if (entryMethod == null) {
                            entryMethod = "";
                        }
                    } catch (Exception e) {
                        logger.warn("Missing entry method for tenderId: " + tender.getId());
                    }
                    try {
                        customerId = tender.getCustomerId();
                        if (customerId == null) {
                            customerId = "";
                        }
                    } catch (Exception e) {
                        logger.warn("No customerId associated with tender");
                    }
                }

                String fileRow = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s \n",
                        Integer.toString(alert.level), alert.type, alert.locationNumber, customerId, alert.city,
                        alert.state, alert.saName, formatTotal(tender.getAmountMoney().getAmount()),
                        alert.transactionDate, status, cardBrand, last4, alert.transactionId, entryMethod, alert.rbu);
                reportBuilder.append(fileRow);
            }
        }
        return reportBuilder.toString();
    }

    private class AlertEntry {
        private final String[] types = {
                "CP transaction > $1000",
                "CNP transaction > $500",
                "CNP occurred more than 3 times at a single location",
                "Same card used 4 or more times across all locations during season",
                "Same dollar amount run consecutively at same location 3 or more times",
                "CNP transaction > $150"
        };
        private int level;
        private String type;
        private String locationNumber;
        private String city;
        private String state;
        private List<Tender> tenders;
        private String transactionDate;
        private String transactionId;
        private String rbu;
        private String saName;

        private AlertEntry(TntLocationDetails locationDetails, Transaction transaction, int level,
                List<Map<String, String>> dbLocationRows) throws Exception {
            // initialize to context values
            this.level = level;
            this.locationNumber = findLocationNumber(locationDetails.getLocation().getName());
            this.transactionDate = transaction.getCreatedAt();
            this.transactionId = transaction.getId();

            // set type description
            this.type = "";
            if (level > 0 && level <= types.length) {
                this.type = types[level - 1];
            } else {
                throw new Exception("Invalid alert level for Abnormal Transactions Report");
            }

            // initialize to blank values
            this.rbu = "";
            this.city = "";
            this.state = "";
            this.saName = "";

            // copy tenders
            this.tenders = new ArrayList<Tender>();
            for (Tender tender : transaction.getTenders()) {
                tenders.add(tender);
            }

            // get remaining data from location rows in db
            for (Map<String, String> row : dbLocationRows) {
                if (this.locationNumber.equals(row.get("locationNumber"))) {
                    this.city = row.get("city");
                    this.state = row.get("state");
                    this.rbu = row.get("rbu");
                    this.saName = row.get("saName");
                }
            }
        }

        /* 
         * Helper function to parse location number
         * 
         * - per TNT spec, all upcoming seasons will follow new naming convention
         *   location name = TNT location number
         * - old seasons followed convention of 'NAME (#LocationNumber)'
         * 
         */
        private String findLocationNumber(String locationName) {
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
    }
}
