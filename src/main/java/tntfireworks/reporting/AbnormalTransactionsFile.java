package tntfireworks.reporting;

import java.sql.SQLException;
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

import com.squareup.connect.Payment;
import com.squareup.connect.v2.Tender;
import com.squareup.connect.v2.Transaction;

import tntfireworks.TntDatabaseApi;
import util.DbConnection;
import util.TimeManager;

public class AbnormalTransactionsFile {
    private static Logger logger = LoggerFactory.getLogger(AbnormalTransactionsFile.class);

    /* alert types
     * 
     * Alert Level  | Description
     * -----------------------------------------------------------
     *      1       | Card Present Transaction exceeds $3,000
     *      2       | Card Not Present Transaction exceeds $1,500
     *      3       | Card Not Present transaction more than 5 times in one day at same location
     *      4       | Same card used 4 or more times across entire population during season (in one day?)
     *      5       | Same dollar amount run consecutively at same location 3 or more times in one day
     *      6       | Card Present Transaction exceeds $1,000
     *      7       | Card Not Present Transaction exceeds $500
     *  
     */
    private List<AlertEntry> alerts;
    private Map<String, String> tenderToEmployee;
    private String fileDate;

    public AbnormalTransactionsFile(List<List<TntLocationDetails>> deploymentsAggregate, DbConnection dbConnection)
            throws Exception {
        // set file date
        fileDate = getDate("America/Los_Angeles", "MM-dd-yy", 0);

        // initialize instances for alert entries and tender id to employee mapping
        alerts = new ArrayList<AlertEntry>();
        tenderToEmployee = new HashMap<String, String>();

        // cache location data from tnt database to limit to 1 query submission
        TntDatabaseApi tntDatabaseApi = new TntDatabaseApi(dbConnection);
        List<Map<String, String>> dbLocationRows = tntDatabaseApi
                .submitQuery(tntDatabaseApi.generateLocationSQLSelect());
        tntDatabaseApi.close();

        // map of key (4 digits + card brand) and AlertEntry[] 
        Map<String, List<AlertEntry>> alert4Entries = new HashMap<String, List<AlertEntry>>();

        for (List<TntLocationDetails> deployment : deploymentsAggregate) {
            for (TntLocationDetails locationDetails : deployment) {
                List<Transaction> alert3Transactions = new ArrayList<Transaction>();
                Map<String, List<Transaction>> alert5Transactions = new HashMap<String, List<Transaction>>();

                for (Transaction transaction : locationDetails.getTransactions()) {
                    // alert 1 and 6 have same criteria, different thresholds
                    // alert 1 > $3000, alert 6 > $1000
                    int alertLevel = 0;
                    int alertThreshold1 = 10000;
                    int alertThreshold6 = 1000;
                    if (checkCpAlert(transaction, alertThreshold6)) {
                        alertLevel = 6;
                        if (checkCpAlert(transaction, alertThreshold1)) {
                            alertLevel = 1;
                        }
                        alerts.add(new AlertEntry(locationDetails, transaction, alertLevel, dbLocationRows));
                    }

                    // alert 2 and 7 have same criteria, different thresholds
                    // alert 2 > $1500, alert 7 > $500
                    int alertThreshold2 = 10000;
                    int alertThreshold7 = 1000;
                    if (checkCnpAlert(transaction, alertThreshold7)) {
                        alertLevel = 7;
                        if (checkCnpAlert(transaction, alertThreshold2)) {
                            alertLevel = 2;
                        }
                        alerts.add(new AlertEntry(locationDetails, transaction, alertLevel, dbLocationRows));
                    }

                    // for alert 3, keep running list of transactions with CNP entry methods
                    for (Tender tender : transaction.getTenders()) {
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
                    }

                    // for alert 4, keep running list of cards (using 4 digits + brand)
                    for (Tender tender : transaction.getTenders()) {
                        try {
                            String last4 = tender.getCardDetails().getCard().getLast4();
                            String cardBrand = tender.getCardDetails().getCard().getCardBrand();

                            // use last 4 digits and card brand as key to each card
                            String key = String.format("%s%s", last4, cardBrand);
                            if (alert4Entries.containsKey(key)) {
                                List<AlertEntry> potentialAlerts = alert4Entries.get(key);
                                potentialAlerts.add(new AlertEntry(locationDetails, transaction, 4, dbLocationRows));
                                alert4Entries.put(key, potentialAlerts);
                            } else {
                                List<AlertEntry> potentialAlerts = new ArrayList<AlertEntry>();
                                potentialAlerts.add(new AlertEntry(locationDetails, transaction, 4, dbLocationRows));
                                alert4Entries.put(key, potentialAlerts);
                            }
                        } catch (Exception e) {
                            logger.warn("Card details missing to create unique key for alert 4 transactions.");
                        }

                        // for alert 5, keep running list of transactions with same dollar amounts
                        String key = Integer.toString(tender.getAmountMoney().getAmount());

                        if (alert5Transactions.containsKey(key)) {
                            List<Transaction> transactions = alert5Transactions.get(key);
                            transactions.add(transaction);
                            alert5Transactions.put(key, transactions);
                        } else {
                            List<Transaction> transactions = new ArrayList<Transaction>();
                            transactions.add(transaction);
                            alert5Transactions.put(key, transactions);
                        }
                    }

                } // for (transaction : transactions)

                // for alert 3, check size of alert3Transactions list and add to alerts
                if (alert3Transactions.size() > 5) {
                    for (Transaction transaction : alert3Transactions) {
                        alerts.add(new AlertEntry(locationDetails, transaction, 3, dbLocationRows));
                    }
                }

                // for alert 5, find same dollar amount >=3 times per day
                for (String key : alert5Transactions.keySet()) {
                    if (alert5Transactions.get(key).size() >= 3) {
                        for (Transaction transaction : alert5Transactions.get(key)) {
                            alerts.add(new AlertEntry(locationDetails, transaction, 5, dbLocationRows));
                        }
                    }
                }

                // keep local cache of tender id to employee id mapping for later use
                for (Payment payment : locationDetails.getPayments()) {
                    for (com.squareup.connect.Tender tender : payment.getTender()) {
                        if (tender.getEmployeeId() != null) {
                            if (locationDetails.getEmployees().containsKey(tender.getEmployeeId())) {
                                String employeeFName = locationDetails.getEmployees().get(tender.getEmployeeId())
                                        .getFirstName();
                                String employeeLName = locationDetails.getEmployees().get(tender.getEmployeeId())
                                        .getLastName();
                                String employeeName = String.format("%s %s", employeeLName, employeeFName);
                                tenderToEmployee.put(tender.getId(), employeeName);
                            }
                        }
                    }
                }
            } // for (locationDetails : deployment) 
        } // for (deployment : deployments)

        // for alert 4, check size of alert4Transactions list and add to alerts
        for (

        String key : alert4Entries.keySet()) {
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
         *      5. threshold > $3000.00
         *      
         *  alert level 6 criteria
         *      1. All of above except threshold > $1000
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
         *      5. threshold > $1500.00
         *  
         *  alert level 7 criteria
         *      1. All of above except threshold > $500
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
        //     1. sales associate id
        //     2. card type
        //     3. last 4 digits
        //     4. entry method
        //     5. transaction status
        for (AlertEntry alert : alerts) {
            for (Tender tender : alert.tenders) {
                String salesAssociate = "";
                String cardBrand = "";
                String last4 = "";
                String status = "";
                String entryMethod = "";

                /*
                 *  - occasional null values for card detail fields below from the API
                 *  - this is normal behavior since valid tender types could include gift cards      
                 */
                if (tender.getType().equals("CARD")) {
                    try {
                        cardBrand = tender.getCardDetails().getCard().getCardBrand();
                    } catch (Exception e) {
                        logger.warn("Missing card type for tenderId: " + tender.getId());
                        cardBrand = "";
                    }
                    try {
                        last4 = tender.getCardDetails().getCard().getLast4();
                    } catch (Exception e) {
                        logger.warn("Missing 4 digits for tenderId: " + tender.getId());
                        last4 = "";
                    }
                    try {
                        status = tender.getCardDetails().getStatus();
                    } catch (Exception e) {
                        logger.warn("Missing status for tenderId: " + tender.getId());
                        status = "";
                    }
                    try {
                        entryMethod = tender.getCardDetails().getEntryMethod();
                    } catch (Exception e) {
                        logger.warn("Missing entry method for tenderId: " + tender.getId());
                        entryMethod = "";
                    }
                }

                // get employee id using map
                if (tenderToEmployee.containsKey(tender.getId())) {
                    salesAssociate = tenderToEmployee.get(tender.getId());
                }

                String fileRow = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s \n",
                        Integer.toString(alert.level), alert.type, alert.locationNumber, alert.customerId, alert.city,
                        alert.state, salesAssociate, formatTotal(tender.getAmountMoney().getAmount()),
                        alert.transactionDate, status, cardBrand, last4, alert.transactionId, entryMethod, alert.rbu);
                reportBuilder.append(fileRow);
            }
        }
        return reportBuilder.toString();
    }

    private class AlertEntry {
        private int level;
        private String type;
        private String locationNumber;
        private String customerId;
        private String city;
        private String state;
        private List<Tender> tenders;
        private String transactionDate;
        private String transactionId;
        private String rbu;

        private AlertEntry(TntLocationDetails locationDetails, Transaction transaction, int level,
                List<Map<String, String>> dbLocationRows) throws SQLException {
            // initialize to context values
            this.level = level;
            this.locationNumber = findLocationNumber(locationDetails.getLocation().getName());
            this.transactionDate = transaction.getCreatedAt();
            this.transactionId = transaction.getId();

            this.customerId = "";
            try {
                if (transaction.getCustomerId() != null || !transaction.getCustomerId().equals("null")) {
                    this.customerId = transaction.getCustomerId();
                }
            } catch (Exception e) {
                logger.warn("No customer_id associated with transction id: " + transaction.getId());
            }

            // initialize to blank values
            this.type = "";
            this.rbu = "";
            this.city = "";
            this.state = "";

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
