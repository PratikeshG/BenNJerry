package tntfireworks.reporting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.squareup.connect.Payment;
import com.squareup.connect.Settlement;
import com.squareup.connect.SquareClient;
import com.squareup.connect.v2.SquareClientV2;
import com.squareup.connect.v2.Transaction;

public class TntLocationDetailsHelper {

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

    public static Settlement[] getSettlements(SquareClient squareClientV1, Map<String, String> params) throws Exception {
        // V1 Settlements
        return squareClientV1.settlements().list(params);
    }
}
