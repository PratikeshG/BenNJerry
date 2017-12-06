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

public class TntLocationDetails {
    protected String locationNumber;
    protected String locationName;
    protected String city;
    protected String state;
    protected String zip;
    protected String rbu;
    protected String saName;

    public TntLocationDetails(List<Map<String, String>> dbLocationRows, String locationName) {
        this.locationName = locationName.replaceAll(",", "");
        this.locationNumber = findLocationNumber(locationName);
        this.rbu = "";
        this.city = "";
        this.state = "";
        this.zip = "";
        this.saName = "";

        for (Map<String, String> row : dbLocationRows) {
            if (locationNumber.equals(row.get("locationNumber"))) {
                this.city = row.get("city");
                this.state = row.get("state");
                this.rbu = row.get("rbu");
                this.zip = row.get("zip");
                this.saName = row.get("saName");
            }
        }
    }

    public static Payment[] getPayments(SquareClient squareClientV1, Map<String, String> params) throws Exception {
        // V1 Payments - ignore no-sale and cash-only payments
        Payment[] allPayments = squareClientV1.payments().list(params);
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

    public static Transaction[] getTransactions(SquareClientV2 squareClientV2, Map<String, String> params)
            throws Exception {
        // V2 Transactions - ignore no-sales
        params.put("sort_order", "ASC"); // v2 default is DESC
        Transaction[] allTransactions = squareClientV2.transactions().list(params);
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
