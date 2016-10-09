package vfcorp.loyalty;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import com.squareup.connect.Payment;
import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.Transaction;

public class DailySummaryCallable implements Callable {

    private final int LOYALTY_CUSTOMER_ID_LENGTH = 17;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        @SuppressWarnings("unchecked")
        List<LocationTransactionDetails> transactionDetailsByLocation = (List<LocationTransactionDetails>) message
                .getProperty("transactionDetailsByLocation", PropertyScope.INVOCATION);

        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html>");
        builder.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        builder.append("<body><table><tr>");
        builder.append("<td><b>Store ID</b><td>");
        builder.append("<td><b>Total Transactions</b><td>");
        builder.append("<td><b>Gross Sales</b><td>");
        builder.append("<td><b>Unique Employees</b><td>");
        builder.append("<td><b>Loyalty Customers</b></b><td>");
        builder.append("</tr>");

        for (LocationTransactionDetails locationTransactionDetails : transactionDetailsByLocation) {
            String storeId = locationTransactionDetails.getStoreId();

            // Find totals and number of unique employees who have taken a
            // transaction
            int numTransactions = 0;
            int gpvTotal = 0;

            HashMap<String, Boolean> employeeCache = new HashMap<String, Boolean>();
            for (Payment payment : locationTransactionDetails.getPayments()) {
                numTransactions += 1;
                gpvTotal += payment.getGrossSalesMoney().getAmount();

                for (com.squareup.connect.Tender tender : payment.getTender()) {
                    if (tender.getEmployeeId() != null) {
                        employeeCache.put(tender.getEmployeeId(), true);
                    }
                }
            }
            int numEmployees = employeeCache.size();

            // Find number of unique customers collected
            HashMap<String, Boolean> customerCache = new HashMap<String, Boolean>();
            for (Transaction transaction : locationTransactionDetails.getTransactions()) {
                for (com.squareup.connect.v2.Tender tender : transaction.getTenders()) {
                    if (tender.getCustomerId() != null) {
                        Customer customer = locationTransactionDetails.getCustomers().get(tender.getCustomerId());
                        if (customer != null && customer.getReferenceId() != null
                                && customer.getReferenceId().length() == LOYALTY_CUSTOMER_ID_LENGTH) {
                            customerCache.put(customer.getReferenceId(), true);
                        }
                    }
                }
            }
            int numCustomers = customerCache.size();

            builder.append(appendRow(storeId, numTransactions, gpvTotal, numEmployees, numCustomers));
        }

        builder.append("</table></body></html>");
        return builder.toString();
    }

    private String appendRow(String storeId, int numTransactions, int gpvTotal, int numEmployees, int numCustomers) {
        return String.format("<tr><td>%s<td><td>%d<td><td>%s<td><td>%d<td><td>%d<td></tr>", storeId, numTransactions,
                formatTotal(gpvTotal), numEmployees, numCustomers);
    }

    private String formatTotal(int gpv) {
        NumberFormat n = NumberFormat.getCurrencyInstance(Locale.US);
        return n.format(gpv / 100.0);
    }
}
