package vfcorp.reporting;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.Employee;
import com.squareup.connect.Payment;
import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.CustomerGroup;
import com.squareup.connect.v2.Transaction;

import util.CloudStorageApi;
import vfcorp.Util;

public class LoyaltyAggregationCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(LoyaltyAggregationCallable.class);

    private final int LOYALTY_CUSTOMER_ID_LENGTH = 17;

    @Value("${google.storage.bucket.archive}")
    private String archiveBucket;
    @Value("${google.storage.account.credentials}")
    private String storageCredentials;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        @SuppressWarnings("unchecked")
        List<LocationTransactionDetails> transactionDetailsByLocation = (List<LocationTransactionDetails>) message
                .getPayload();

        String CUSTOMER_GROUP_EMAIL = (String) message.getProperty("customerGroupEmail", PropertyScope.INVOCATION);

        // Temporary use this loyalty process to generate daily email summaries
        // TODO(bhartard): Remove once emails are no longer required by VFC
        message.setProperty("transactionDetailsByLocation", transactionDetailsByLocation, PropertyScope.INVOCATION);

        HashMap<String, LoyaltyEntryPayload> loyaltyPayloadSet = new HashMap<String, LoyaltyEntryPayload>();

        for (LocationTransactionDetails locationTransactionDetails : transactionDetailsByLocation) {
            String storeId = Util.getStoreNumber(locationTransactionDetails.getLocation().getName());

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

                        boolean customerHasContactInfo = (customer.getEmailAddress() != null
                                && customer.getEmailAddress().length() > 0)
                                || (customer.getPhoneNumber() != null && customer.getPhoneNumber().length() > 0);

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

                            if (customer.getGroups() != null) {
                                for (CustomerGroup group : customer.getGroups()) {
                                    if (group.getId().equals(CUSTOMER_GROUP_EMAIL)) {
                                        loyaltyPayload.setEmailOptIn(true);
                                        break;
                                    }
                                }
                            }

                            loyaltyPayloadSet.put(customer.getReferenceId(), loyaltyPayload);
                        } else {
                            if (customer != null && customerHasContactInfo && customer.getReferenceId() == null) {
                                throw new Exception("Loyalty customer missing generated reference ID. Customer: "
                                        + customer.getId());
                            } else if (customer != null && customerHasContactInfo
                                    && customer.getReferenceId().length() != LOYALTY_CUSTOMER_ID_LENGTH) {
                                // Square might start returning customer objects
                                // on transactions without merchant adding them
                                throw new Exception("Loyalty customer has invalid reference ID length. Customer: "
                                        + customer.getId());
                            } else if (customer != null && !customerHasContactInfo) {
                                logger.warn("Loyalty customer does not have contact info: " + customer.getId());
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
            LoyaltyEntry entry = new LoyaltyEntry(cal, loyaltyPayload.getStoreId(), loyaltyPayload.getAssociateId(),
                    loyaltyPayload.getCustomer(), loyaltyPayload.isEmailOptIn());
            builder.append(entry.toString() + "\r\n");
        }

        String output = builder.toString();
        String filenameDateStamp = message.getProperty("filenameDateStamp", PropertyScope.INVOCATION);

        // Archive to Google Cloud Storage
        String encryptionKey = message.getProperty("encryptionKey", PropertyScope.INVOCATION);
        String archiveFolder = message.getProperty("archiveFolder", PropertyScope.INVOCATION);
        String fileKey = String.format("%s%s.secure", archiveFolder, filenameDateStamp);

        CloudStorageApi cloudStorage = new CloudStorageApi(storageCredentials);
        cloudStorage.encryptAndUploadObject(encryptionKey, archiveBucket, fileKey, output);

        return output;
    }
}
