package tntfireworks.reporting;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.v2.Tender;
import com.squareup.connect.v2.TenderCardDetails;
import com.squareup.connect.v2.Transaction;

/*
 * Report 3 - "Abnormal Transactions Report" - Emailed daily
 *
 * Report 3 contains a predefined list of transaction behavior that TNT Fireworks wants to track to monitor potentially
 * fraudulent transactions. The report logic only checks for potential fraudulent activity for a single day. The
 * following is the list of transaction behaviors defined as alerts:
 *
 * "Alert 1" Card Present Transaction exceeds $1000
 * "Alert 2" Card Not Present Transaction exceeds $500
 * "Alert 3" Card Not Present transaction >3 times in one day at same location
 * "Alert 4" Same card used 4 or more times across entire master account in one day
 * "Alert 5" Same dollar amount run on card-tender consecutively at same location 3 or more times in one day
 * "Alert 6" Card Not Present Transaction exceeds $150
 *
 * Each row in the file represents a single transaction and is associated with a transaction id and alert type.
 * If a transaction is detected in multiple alerts, the transaction will be listed in a separate row for each triggered
 * alert. Hence, the same transaction ID can occur multiple times in a report.
 *
 */

public class AbnormalTransactionsPayload extends TntReportLocationPayload {
    private static Logger logger = LoggerFactory.getLogger(AbnormalTransactionsPayload.class);
    private static final String ABNORMAL_TRANSACTIONS_FILE_HEADER = String.format(
            "%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s \n", "Alert Level", "Alert Type",
            "Location Number", "SQ Customer Id", "City", "State", "Sales Associate", "Transaction Amount",
            "Transaction Date (UTC)", "Transaction Status", "Card Brand", "Last 4 Digits", "Transaction Id",
            "Entry Method", "RBU", "Card Fingerprint");

    // alert 1, 2, 6 are amount_money thresholds
    private static final int CP_AMOUNT_LIMIT = 100000;
    private static final int CNP_AMOUNT_LIMIT_1 = 50000;
    private static final int CNP_AMOUNT_LIMIT_2 = 15000;
    // alert 3 is the number of CNP transactions
    private static final int CNP_TRANSACTIONS_PER_LOCATION_LIMIT = 3;
    // alert 4 is the number of times a unique card is used at the merchant
    protected static final int SAME_CARD_PER_MERCHANT_LIMIT = 4;
    // alert 5 is the number of consecutive same-dollar amount transactions
    private static final int SAME_AMOUNT_PER_LOCATION_LIMIT = 3;
    // define CP and CNP entry methods as defined in Connect V2
    private static final ArrayList<String> CP_ENTRY_METHODS = new ArrayList<String>(
            Arrays.asList(TenderCardDetails.ENTRY_METHOD_SWIPED, TenderCardDetails.ENTRY_METHOD_EMV,
                    TenderCardDetails.ENTRY_METHOD_CONTACTLESS));
    private static final ArrayList<String> CNP_ENTRY_METHODS = new ArrayList<String>(
            Arrays.asList(TenderCardDetails.ENTRY_METHOD_ON_FILE, TenderCardDetails.ENTRY_METHOD_KEYED));

    // alerts list is final list of alerts to use to generate report
    private List<AbnormalTransactionsEntry> alerts;

    // - alert 3 and 5 need to track transactions at the class level
    // - used as a running window of transactions across a single location
    private List<Transaction> alert3Transactions;
    private List<Transaction> alert5Transactions;

    // entire list of transactions for a single location
    private List<Transaction> locationTransactions;
    private String locationId;

    // previous amount needs to be stored for alert 5
    int prevAmt;

    public AbnormalTransactionsPayload(String timeZone, TntLocationDetails locationDetails) {
        super(timeZone, locationDetails, ABNORMAL_TRANSACTIONS_FILE_HEADER);
        this.prevAmt = 0;
        alerts = new ArrayList<AbnormalTransactionsEntry>();
        alert3Transactions = new ArrayList<Transaction>();
        alert5Transactions = new ArrayList<Transaction>();
        locationTransactions = new ArrayList<Transaction>();
        locationId = "";
    }

    public List<Transaction> getLocationTransactions() {
        return locationTransactions;
    }

    public String getLocationId() {
        return locationId;
    }

    public void addAlert4Entry(Transaction transaction) throws Exception {
        int alertLevel = 4;

        for (Tender tender : transaction.getTenders()) {
            if (tender.getType().equals(Tender.TENDER_TYPE_CARD)
                    && transaction.getProduct().equals(Transaction.TRANSACTION_PRODUCT_REGISTER)) {
                alerts.add(new AbnormalTransactionsEntry(transaction, tender, alertLevel));
            }
        }
    }

    public void addEntry(Transaction transaction) throws Exception {
        /*
         * alert types
         *
         * -----------------------------------------------------------
         * 1 | Card Present Transaction exceeds $1,000
         * 2 | Card Not Present Transaction exceeds $500
         * 3 | Card Not Present transaction >3 times in one day at same location
         * 4 | Same card used 4 or more times across entire master account in one day
         * 5 | Same dollar amount run on card-tender consecutively at same location 3 or more times in one day
         * 6 | Card Not Present Transaction exceeds $150
         *
         */
        try {
            // check transaction against different alert types
            checkAlert1(transaction);
            checkAlert26(transaction);
            checkAlert3(transaction);
            checkAlert5(transaction);

            // - for abnormal transactions report, alert 4 requires
            // post-processing of alert 4 at the merchant level
            // - store transaction here for alert 4 processing at AbnormalTransactionsReportAggregatorCallable
            locationTransactions.add(transaction);
            locationId = transaction.getLocationId();
        } catch (ParseException e) {
            e.printStackTrace();
            logger.error("Calendar Exception from adding transaction to Abnormal Transactions Report: " + e);
        }
    }

    private void checkAlert1(Transaction transaction) throws Exception {
        /*
         * alert level 1 criteria
         * 1. Card Present transaction
         * 2. Entry Method: SWIPED, EMV, CONTACTLESS
         * 3. Product: REGISTER
         * 4. Tender Type: not CASH or NO_SALE
         * 5. threshold > $1000.00
         *
         */
        int alertLevel = 1;
        for (Tender tender : transaction.getTenders()) {
            if (tender.getType().equals(Tender.TENDER_TYPE_CARD)
                    && transaction.getProduct().equals(Transaction.TRANSACTION_PRODUCT_REGISTER)) {
                String entryMethod = "";
                if (tender.getCardDetails() != null) {
                    entryMethod = tender.getCardDetails().getEntryMethod();
                }
                if (CP_ENTRY_METHODS.contains(entryMethod) && tender.getAmountMoney().getAmount() > CP_AMOUNT_LIMIT) {
                    alerts.add(new AbnormalTransactionsEntry(transaction, tender, alertLevel));
                }
            }
        }
    }

    // alert 2 and 6 have same criteria, different thresholds
    // alert 2 > $500, alert 6 > $150
    private void checkAlert26(Transaction transaction) throws Exception {
        /*
         * alert level 2 criteria
         * 1. Card NOT Present transaction
         * 2. Entry Method: KEYED, ON_FILE
         * 3. Product: REGISTER
         * 4. Tender Type: not CASHor NO_SALE
         * 5. threshold > $500.00
         *
         * alert level 6 criteria
         * 1. All of above except threshold > $150.00
         *
         */
        int alertLevel = 6;
        for (Tender tender : transaction.getTenders()) {
            if (tender.getType().equals(Tender.TENDER_TYPE_CARD)
                    && transaction.getProduct().equals(Transaction.TRANSACTION_PRODUCT_REGISTER)) {
                String entryMethod = "";
                if (tender.getCardDetails() != null) {
                    entryMethod = tender.getCardDetails().getEntryMethod();
                }
                if (CNP_ENTRY_METHODS.contains(entryMethod)
                        && tender.getAmountMoney().getAmount() > CNP_AMOUNT_LIMIT_2) {
                    if (tender.getAmountMoney().getAmount() > CNP_AMOUNT_LIMIT_1) {
                        alertLevel = 2;
                    }
                    alerts.add(new AbnormalTransactionsEntry(transaction, tender, alertLevel));
                }
            }
        }
    }

    // alert 3 is a CNP check with $0 threshold
    private void checkAlert3(Transaction transaction) throws Exception {
        int alertLevel = 3;
        for (Tender tender : transaction.getTenders()) {
            if (tender.getType().equals(Tender.TENDER_TYPE_CARD)
                    && transaction.getProduct().equals(Transaction.TRANSACTION_PRODUCT_REGISTER)) {
                String entryMethod = "";
                if (tender.getCardDetails() != null) {
                    entryMethod = tender.getCardDetails().getEntryMethod();
                }
                if (CNP_ENTRY_METHODS.contains(entryMethod)) {
                    // add cnp transaction to running/buffered list
                    alert3Transactions.add(transaction);

                    // if THRESHOLD+1 CNP transactions recorded, add first
                    // THRESHOLD+1 to alerts list
                    // else, if >THRESHOLD+1 transactions recorded, add newest
                    // transaction
                    if (alert3Transactions.size() == (CNP_TRANSACTIONS_PER_LOCATION_LIMIT + 1)) {
                        for (Transaction bufferedTransaction : alert3Transactions) {
                            alerts.add(new AbnormalTransactionsEntry(bufferedTransaction, tender, alertLevel));
                        }
                    } else if (alert3Transactions.size() > (CNP_TRANSACTIONS_PER_LOCATION_LIMIT + 1)) {
                        alerts.add(new AbnormalTransactionsEntry(transaction, tender, alertLevel));
                    }
                }
            }
        }
    }

    // alert 5 checks for same dollar amount run on card-tender consecutively at
    // same location 3 or more times in one day
    private void checkAlert5(Transaction transaction) throws Exception {
        int alertLevel = 5;
        // for alert 5, keep consecutive list of transactions with same dollar
        // amounts
        for (Tender tender : transaction.getTenders()) {
            if (tender.getType().equals(Tender.TENDER_TYPE_CARD)) {
                int currentAmt = tender.getAmountMoney().getAmount();
                if (currentAmt == prevAmt) {
                    alert5Transactions.add(transaction);
                } else {
                    if (alert5Transactions.size() >= SAME_AMOUNT_PER_LOCATION_LIMIT) {
                        // store buffer of consecutive transactions with same
                        // amount use transactionId as unique identifier
                        for (Transaction bufferedTransaction : alert5Transactions) {
                            alerts.add(new AbnormalTransactionsEntry(bufferedTransaction, tender, alertLevel));
                        }
                    }
                    // clear buffer to start tracking new transaction amount
                    alert5Transactions.clear();
                    alert5Transactions.add(transaction);

                    // store new amount for tracking
                    prevAmt = currentAmt;
                }
            }
        }
    }

    public List<String> getRows() {
        ArrayList<String> rows = new ArrayList<String>();

        for (AbnormalTransactionsEntry entry : alerts) {
            String row = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s \n",
                    Integer.toString(entry.level), entry.description, locationDetails.locationNumber, entry.customerId,
                    locationDetails.city, locationDetails.state, locationDetails.saName, entry.tenderAmt,
                    entry.transactionDate, entry.status, entry.cardBrand, entry.last4, entry.transactionId,
                    entry.entryMethod, locationDetails.rbu, entry.fingerprint);
            rows.add(row);
        }
        return rows;
    }

    private class AbnormalTransactionsEntry {
        private final String alert1 = String.format("CP transaction > $%s", String.valueOf(CP_AMOUNT_LIMIT / 100));
        private final String alert2 = String.format("CNP transaction > $%s", String.valueOf(CNP_AMOUNT_LIMIT_1 / 100));
        private final String alert3 = String.format("CNP occurred more than %s times at a single location",
                String.valueOf(CNP_TRANSACTIONS_PER_LOCATION_LIMIT));
        private final String alert4 = String.format(
                "Same card used %s or more times across all locations under a master account during season",
                String.valueOf(SAME_CARD_PER_MERCHANT_LIMIT));
        private final String alert5 = String.format(
                "Same dollar amount run consecutively at same location %s or more times",
                String.valueOf(SAME_AMOUNT_PER_LOCATION_LIMIT));
        private final String alert6 = String.format("CNP transaction > $%s", String.valueOf(CNP_AMOUNT_LIMIT_2 / 100));

        private final String[] alertDescriptions = { alert1, alert2, alert3, alert4, alert5, alert6 };

        private int level;
        private String description;
        private String transactionDate;
        private String transactionId;
        private String cardBrand;
        private String last4;
        private String status;
        private String entryMethod;
        private String customerId;
        private String fingerprint;
        private String tenderAmt;

        private AbnormalTransactionsEntry(Transaction transaction, Tender tender, int level) throws Exception {
            // alert-specific data
            this.description = "";
            this.level = level;
            // transaction-level data
            this.transactionDate = transaction.getCreatedAt();
            this.transactionId = transaction.getId();
            // tender-level data
            this.cardBrand = "";
            this.last4 = "";
            this.status = "";
            this.entryMethod = "";
            this.customerId = "";
            this.fingerprint = "";
            this.tenderAmt = "";

            if (level > 0 && level <= alertDescriptions.length) {
                this.description = alertDescriptions[level - 1];
            } else {
                throw new Exception("Invalid alert level for Abnormal Transactions Report");
            }

            // check for occasional null values for card detail fields below from the API
            if (tender.getType().equals(Tender.TENDER_TYPE_CARD)) {
                if (tender.getCardDetails() != null && tender.getCardDetails().getCard() != null) {
                    this.cardBrand = tender.getCardDetails().getCard().getCardBrand();
                }
                if (tender.getCardDetails() != null && tender.getCardDetails().getCard() != null) {
                    this.last4 = tender.getCardDetails().getCard().getLast4();
                }
                if (tender.getCardDetails() != null) {
                    this.status = tender.getCardDetails().getStatus();
                }
                if (tender.getCardDetails() != null) {
                    this.entryMethod = tender.getCardDetails().getEntryMethod();
                }
                if (tender.getCardDetails() != null && tender.getCardDetails().getCard() != null) {
                    this.fingerprint = tender.getCardDetails().getCard().getFingerprint();
                }
                if (tender.getCustomerId() != null) {
                    this.customerId = tender.getCustomerId();
                }
                if (tender.getAmountMoney() != null) {
                    this.tenderAmt = formatCurrencyTotal(tender.getAmountMoney().getAmount());
                }
            }
        }
    }
}
