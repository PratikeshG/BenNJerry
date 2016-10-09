package vfcorp.loyalty;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import com.squareup.connect.Employee;
import com.squareup.connect.Payment;
import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.Transaction;

public class LoyaltyAggregationCallable implements Callable {

    private final int LOYALTY_CUSTOMER_ID_LENGTH = 17;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        @SuppressWarnings("unchecked")
        List<LocationTransactionDetails> transactionDetailsByLocation = (List<LocationTransactionDetails>) message
                .getPayload();

        // Temporary use this loyalty process to generate daily email summaries
        // TODO(bhartard): Remove once emails are no longer required by VFC
        // corporate
        message.setProperty("transactionDetailsByLocation", transactionDetailsByLocation, PropertyScope.INVOCATION);

        HashMap<String, LoyaltyEntryPayload> loyaltyPayloadSet = new HashMap<String, LoyaltyEntryPayload>();

        for (LocationTransactionDetails locationTransactionDetails : transactionDetailsByLocation) {
            String storeId = locationTransactionDetails.getStoreId();

            // Create a tender_id:employee_id mapping
            HashMap<String, String> tenderEmployeeMapping = new HashMap<String, String>();
            for (Payment payment : locationTransactionDetails.getPayments()) {
                for (com.squareup.connect.Tender tender : payment.getTender()) {
                    if (tender.getEmployeeId() != null) {
                        tenderEmployeeMapping.put(tender.getId(), tender.getEmployeeId());
                    }
                }
            }

            for (Transaction transaction : locationTransactionDetails.getTransactions()) {
                for (com.squareup.connect.v2.Tender tender : transaction.getTenders()) {
                    if (tender.getCustomerId() != null) {
                        Customer customer = locationTransactionDetails.getCustomers().get(tender.getCustomerId());

                        if (customer != null && customer.getReferenceId() != null
                                && customer.getReferenceId().length() == LOYALTY_CUSTOMER_ID_LENGTH) {

                            String employeeId = tenderEmployeeMapping.get(tender.getId());
                            Employee employee = locationTransactionDetails.getEmployees().get(employeeId);
                            String associateId = (employee != null && employee.getExternalId() != null)
                                    ? employee.getExternalId() : "";

                            LoyaltyEntryPayload loyaltyPayload = new LoyaltyEntryPayload();
                            loyaltyPayload.setStoreId(storeId);
                            loyaltyPayload.setAssociateId(associateId);
                            loyaltyPayload.setCustomer(customer);

                            loyaltyPayloadSet.put(customer.getReferenceId(), loyaltyPayload);
                        } else {
                            if (customer != null && customer.getReferenceId() == null) {
                                throw new Exception("Loyalty customer missing generated reference ID. Customer: "
                                        + customer.getId());
                            } else if (customer != null
                                    && customer.getReferenceId().length() != LOYALTY_CUSTOMER_ID_LENGTH) {
                                // Square might start returning customer objects
                                // on transactions without merchant adding them
                                throw new Exception("Loyalty customer has invalid reference ID length. Customer: "
                                        + customer.getId());
                            } else {
                                throw new Exception(
                                        "Loyalty customer not found in cache for tender: " + tender.getId());
                            }
                        }
                    }
                }
            }
        }

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
        cal.add(Calendar.DAY_OF_YEAR, -1);

        StringBuilder builder = new StringBuilder();
        for (String key : loyaltyPayloadSet.keySet()) {
            LoyaltyEntryPayload loyaltyPayload = loyaltyPayloadSet.get(key);
            LoyaltyEntry entry = new LoyaltyEntry(loyaltyPayload.getStoreId(), cal, loyaltyPayload.getAssociateId(),
                    loyaltyPayload.getCustomer());
            builder.append(entry.toString() + "\r\n");
        }

        return builder.toString();
    }
}
