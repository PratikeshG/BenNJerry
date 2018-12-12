package tntfireworks.reporting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.squareup.connect.Payment;
import com.squareup.connect.Settlement;
import com.squareup.connect.SquareClient;
import com.squareup.connect.v2.SquareClientV2;
import com.squareup.connect.v2.Transaction;

import tntfireworks.TntDatabaseApi;

public class TntLocationDetails {
    protected String locationNumber;
    protected String locationName;
    protected String city;
    protected String state;
    protected String zip;
    protected String rbu;
    protected String saName;
    protected String saNumber;

    public TntLocationDetails(List<Map<String, String>> dbLocationRows, String locationName) {
        this.locationName = locationName.replaceAll(",", "");
        this.locationNumber = findLocationNumber(locationName);
        this.rbu = "";
        this.city = "";
        this.state = "";
        this.zip = "";
        this.saName = "";
        this.saNumber = "";

        for (Map<String, String> row : dbLocationRows) {
            if (locationNumber.equals(row.get(TntDatabaseApi.DB_LOCATION_LOCATION_NUMBER_COLUMN))) {
                this.city = row.get(TntDatabaseApi.DB_LOCATION_CITY_COLUMN);
                this.state = row.get(TntDatabaseApi.DB_LOCATION_STATE_COLUMN);
                this.rbu = row.get(TntDatabaseApi.DB_LOCATION_RBU_COLUMN);
                this.zip = row.get(TntDatabaseApi.DB_LOCATION_ZIP_COLUMN);
                this.saName = row.get(TntDatabaseApi.DB_LOCATION_SA_NAME_COLUMN);
                this.saNumber = row.get(TntDatabaseApi.DB_LOCATION_SA_NUMBER_COLUMN);
            }
        }
    }

    public static Payment[] getPayments(SquareClient squareClientV1, Map<String, String> params) throws Exception {
        // V1 Payments - ignore no-sale and cash-only payments
        Payment[] allPayments = squareClientV1.payments().list(params);
        List<Payment> payments = new ArrayList<Payment>();

        // - only add payments with valid tenders
        for (Payment payment : allPayments) {
            if (hasValidPaymentTender(payment)) {
                payments.add(payment);
            }
        }

        return payments.toArray(new Payment[0]);
    }

    private static boolean hasValidPaymentTender(Payment payment) {
        for (com.squareup.connect.Tender tender : payment.getTender()) {
            if (!tender.getType().equals(tender.TENDER_TYPE_NO_SALE)) {
                return true;
            }
        }

        return false;
    }

    public static Transaction[] getTransactions(SquareClientV2 squareClientV2, Map<String, String> params)
            throws Exception {
        // V2 Transactions - ignore no-sales
        params.put(util.Constants.SORT_ORDER_V2, util.Constants.SORT_ORDER_ASC_V2); // v2 default is DESC
        Transaction[] allTransactions = squareClientV2.transactions().list(params);
        List<Transaction> transactions = new ArrayList<Transaction>();

        for (Transaction transaction : allTransactions) {
            if (hasValidTransactionTender(transaction)) {
                transactions.add(transaction);
            }
        }

        return transactions.toArray(new Transaction[0]);
    }

    private static boolean hasValidTransactionTender(Transaction transaction) {
        for (com.squareup.connect.v2.Tender tender : transaction.getTenders()) {
            if (!tender.getType().equals(tender.TENDER_TYPE_NO_SALE)) {
                return true;
            }
        }

        return false;
    }

    public static Settlement[] getSettlements(SquareClient squareClientV1, Map<String, String> params)
            throws Exception {
        // V1 Settlements
        return squareClientV1.settlements().list(params);
    }

    /*
     * Helper function to parse location number
     *
     * - per TNT spec, all upcoming seasons will follow new naming convention
     * location name = TNT location number - old seasons followed convention of
     * 'NAME (#LocationNumber)'
     *
     */
    private String findLocationNumber(String locationName) {
        String locationNumber = "";

        // old location name = 'NAME (#Location Number)'
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
