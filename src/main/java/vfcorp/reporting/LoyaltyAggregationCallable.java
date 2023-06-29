package vfcorp.reporting;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.v2.Payment;
import com.squareup.connect.v2.Tender;
import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.CustomerGroup;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.Transaction;

import util.CloudStorageApi;
import vfcorp.Util;
import vfcorp.VfcDatabaseApi;

public class LoyaltyAggregationCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(LoyaltyAggregationCallable.class);

    private final int LOYALTY_CUSTOMER_ID_LENGTH = 17;

    @Value("${google.storage.bucket.archive}")
    private String archiveBucket;
    @Value("${google.storage.account.credentials}")
    private String storageCredentials;

    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}")
    private String databaseUrl;
    @Value("${mysql.user}")
    private String databaseUser;
    @Value("${mysql.password}")
    private String databasePassword;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        @SuppressWarnings("unchecked")
        List<LocationTransactionDetails> transactionDetailsByLocation = (List<LocationTransactionDetails>) message
                .getProperty("transactionDetailsByLocation", PropertyScope.INVOCATION);

        String CUSTOMER_GROUP_EMAIL = (String) message.getProperty("customerGroupEmail", PropertyScope.SESSION);
        String brand = (String) message.getProperty("brand", PropertyScope.SESSION);

        HashMap<String, LoyaltyEntryPayload> loyaltyPayloadSet = new HashMap<String, LoyaltyEntryPayload>();

        boolean throwCustomerReferenceIdMissingError = false;
        boolean throwCutomerReferenceIdLengthError = false;
        boolean throwCustomerNotFoundError = false;

        // Employees
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
        VfcDatabaseApi databaseApi = new VfcDatabaseApi(conn);
        Map<String, String> employees = databaseApi.getEmployeeIdsForBrand(getEmployeeDeploymentFromBrand(brand));

        for (LocationTransactionDetails locationTransactionDetails : transactionDetailsByLocation) {
            String storeId = Util.getStoreNumber(locationTransactionDetails.getLocation().getName());

            Map<String, String> tenderTeamMemberMapping = new HashMap<String, String>();
            Map<String, Payment> tenderToPayment = locationTransactionDetails.getTenderToPayment();
            for (Order order : locationTransactionDetails.getOrders()) {
            	if(order.getTenders() != null) {
            		for (Tender tender : order.getTenders()) {
                        if (tenderToPayment.containsKey(tender.getId())) {
                        	Payment payment = tenderToPayment.get(tender.getId());
                        	if(payment.getTeamMemberId() != null) {
                                tenderTeamMemberMapping.put(tender.getId(), payment.getTeamMemberId());
                        	}
                        }
                    }
            	}
            }

            for (Order order : locationTransactionDetails.getOrders()) {
            	if(order.getTenders() != null) {
            		for (Tender tender : order.getTenders()) {
                        if (tender.getCustomerId() != null) {
                            Customer customer = locationTransactionDetails.getCustomers().get(tender.getCustomerId());

                            boolean customerHasContactInfo = (customer.getEmailAddress() != null
                                    && customer.getEmailAddress().length() > 0)
                                    || (customer.getPhoneNumber() != null && customer.getPhoneNumber().length() > 0);

                            if (customer != null && customer.getReferenceId() != null
                                    && customer.getReferenceId().length() == LOYALTY_CUSTOMER_ID_LENGTH) {

                                String employeeId = tenderTeamMemberMapping.get(tender.getId());
                                String associateId = employees.get(employeeId);

                                LoyaltyEntryPayload loyaltyPayload = new LoyaltyEntryPayload();
                                loyaltyPayload.setStoreId(storeId);
                                loyaltyPayload.setAssociateId(associateId);
                                loyaltyPayload.setCustomer(customer);

                                if (brand.equals("kipling") || brand.equals("vans") || brand.equals("test")) {
                                    loyaltyPayload.setEmailOptIn(true);
                                } else {
                                    if (customer.getGroups() != null) {
                                        for (CustomerGroup group : customer.getGroups()) {
                                            if (group.getId().equals(CUSTOMER_GROUP_EMAIL)) {
                                                loyaltyPayload.setEmailOptIn(true);
                                                break;
                                            }
                                        }
                                    }
                                }

                                loyaltyPayloadSet.put(customer.getReferenceId(), loyaltyPayload);
                            } else {
                                if (customer != null && customerHasContactInfo && customer.getReferenceId() == null) {
                                    throwCustomerReferenceIdMissingError = true;
                                    logger.warn("Loyalty customer missing generated reference ID. Customer: "
                                            + customer.getId());
                                } else if (customer != null && customerHasContactInfo
                                        && customer.getReferenceId().length() != LOYALTY_CUSTOMER_ID_LENGTH) {
                                    // Square might start returning customer objects
                                    // on transactions without merchant adding them
                                    throwCutomerReferenceIdLengthError = true;
                                    logger.warn("Loyalty customer has invalid reference ID length. Customer: "
                                            + customer.getId());
                                } else if (customer != null && !customerHasContactInfo) {
                                    logger.warn("Loyalty customer does not have contact info: " + customer.getId());
                                } else {
                                    throwCustomerNotFoundError = true;
                                    logger.warn("Loyalty customer not found in cache for tender: " + tender.getId());
                                }
                            }
                        }
                    }
            	}
            }
        }

        if (throwCustomerReferenceIdMissingError) {
            throw new Exception("Loyalty customer(s) missing generated reference IDs.");
        } else if (throwCutomerReferenceIdLengthError) {
            throw new Exception("Loyalty customer(s) has invalid reference ID length.");
        } else if (throwCustomerNotFoundError) {
            throw new Exception("Loyalty customer(s) not found in cache for tender(s).");
        }

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
        cal.add(Calendar.DAY_OF_YEAR, -1);

        StringBuilder builder = new StringBuilder();
        for (String key : loyaltyPayloadSet.keySet()) {
            LoyaltyEntryPayload loyaltyPayload = loyaltyPayloadSet.get(key);
            LoyaltyEntry entry = new LoyaltyEntry(cal, loyaltyPayload.getStoreId(), loyaltyPayload.getAssociateId(),
                    loyaltyPayload.getCustomer(), loyaltyPayload.isEmailOptIn());

            if (brand.equals("kipling")) {
                entry.setBrandString("5");
                entry.setSourceDatabaseId("58888");
            } else if (brand.equals("vans") || brand.equals("test")) {
                entry.setBrandString("7");
                entry.setSourceDatabaseId("999");

                // remove leading zeros
                entry.setCustomerNumber(entry.getCustomerNumber().replaceFirst("^0+(?!$)", ""));

                // Vans should not have +1 prefix
                String phone = entry.getTelephoneNumber();
                if (phone.length() == 11 && phone.startsWith("1")) {
                    entry.setTelephoneNumber(phone.substring(1));
                }
            }

            builder.append(entry.toString() + "\r\n");
        }

        String output = builder.toString();
        String filenameDateStamp = message.getProperty("filenameDateStamp", PropertyScope.SESSION);

        // Archive to Google Cloud Storage
        String encryptionKey = message.getProperty("encryptionKey", PropertyScope.SESSION);
        String archiveFolder = message.getProperty("archiveFolder", PropertyScope.SESSION);
        String fileKey = String.format("%s%s.secure", archiveFolder, filenameDateStamp);

        CloudStorageApi cloudStorage = new CloudStorageApi(storageCredentials);
        cloudStorage.encryptAndUploadObject(encryptionKey, archiveBucket, fileKey, output);

        return output;
    }

    private String getEmployeeDeploymentFromBrand(String brand) {
        return "vfcorp-" + brand;
    }
}
