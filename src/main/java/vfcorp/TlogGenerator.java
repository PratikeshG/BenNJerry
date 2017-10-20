package vfcorp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.store.ObjectStore;
import org.mule.api.transport.PropertyScope;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.Employee;
import com.squareup.connect.Merchant;
import com.squareup.connect.Payment;
import com.squareup.connect.SquareClient;
import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.CustomerGroup;
import com.squareup.connect.v2.SquareClientV2;
import com.squareup.connect.v2.Tender;
import com.squareup.connect.v2.Transaction;

import util.SquarePayload;
import util.TimeManager;

public class TlogGenerator implements Callable {
    private final int LOYALTY_CUSTOMER_ID_LENGTH = 17;

    @Value("${vfcorp.itemNumberLookupLength}")
    private int itemNumberLookupLength;
    @Value("${api.url}")
    private String apiUrl;
    @Value("${encryption.key.tokens}")
    private String encryptionKey;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        // TODO(bhartard): Refactor these into separate, testable functions
        SquarePayload squarePayload = (SquarePayload) message.getPayload();

        TlogGeneratorPayload tlogGeneratorPayload = new TlogGeneratorPayload();
        tlogGeneratorPayload.setSquarePayload(squarePayload);

        String storeId = message.getProperty("storeId", PropertyScope.INVOCATION);

        // Loyalty settings
        boolean customerLoyaltyEnabled = message.getProperty("enableCustomerLoyalty", PropertyScope.INVOCATION)
                .equals("true") ? true : false;
        String CUSTOMER_GROUP_EMAIL = (String) message.getProperty("customerGroupEmail", PropertyScope.INVOCATION);
        String CUSTOMER_GROUP_NEW = (String) message.getProperty("customerGroupNew", PropertyScope.INVOCATION);
        String CUSTOMER_GROUP_EXISTING = (String) message.getProperty("customerGroupExisting",
                PropertyScope.INVOCATION);

        String deployment = (String) message.getProperty("deploymentId", PropertyScope.SESSION);
        String timeZone = message.getProperty("timeZone", PropertyScope.INVOCATION);

        int offset = Integer.parseInt(message.getProperty("offset", PropertyScope.INVOCATION));
        int range = Integer.parseInt(message.getProperty("range", PropertyScope.INVOCATION));
        tlogGeneratorPayload.setParams(TimeManager.getPastDayInterval(range, offset, timeZone));

        SquareClient squareV1Client = new SquareClient(
                tlogGeneratorPayload.getSquarePayload().getAccessToken(encryptionKey), apiUrl, "v1",
                tlogGeneratorPayload.getSquarePayload().getMerchantId(),
                tlogGeneratorPayload.getSquarePayload().getLocationId());
        SquareClientV2 squareV2Client = new SquareClientV2(apiUrl,
                tlogGeneratorPayload.getSquarePayload().getAccessToken(encryptionKey),
                tlogGeneratorPayload.getSquarePayload().getMerchantId(),
                tlogGeneratorPayload.getSquarePayload().getLocationId());

        // Locations
        Merchant[] locations = squareV1Client.businessLocations().list();
        tlogGeneratorPayload.setLocations(locations);

        // Employees
        Employee[] employees = squareV1Client.employees().list();
        tlogGeneratorPayload.setEmployees(employees);

        // Payments
        Payment[] payments = squareV1Client.payments().list(tlogGeneratorPayload.getParams());
        Map<String, Payment> paymentsCache = new HashMap<String, Payment>();
        List<Payment> nonCashPayments = new ArrayList<Payment>();
        for (Payment payment : payments) {
            boolean hasValidPaymentTender = false;

            // Save payment object for customer lookups
            paymentsCache.put(payment.getId(), payment);
            for (com.squareup.connect.Tender tender : payment.getTender()) {
                paymentsCache.put(tender.getId(), payment);

                if (!tender.getType().equals("CASH") && !tender.getType().equals("NO_SALE")) {
                    hasValidPaymentTender = true;
                }
            }

            // Don't process cash-only payments for TLOGs
            if (hasValidPaymentTender) {
                nonCashPayments.add(payment);
            }
        }
        tlogGeneratorPayload.setPayments(nonCashPayments.toArray(new Payment[0]));

        // Get PCM counters
        Map<String, Integer> nextPreferredCustomerNumbers = new HashMap<String, Integer>();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> preferredCustomerCounters = (List<Map<String, Object>>) message
                .getProperty("preferredCustomerCounters", PropertyScope.SESSION);
        for (Map<String, Object> customerCounter : preferredCustomerCounters) {
            String registerId = (String) customerCounter.get("registerId");
            int nextNumber = (int) customerCounter.get("nextPreferredCustomerNumber");
            nextPreferredCustomerNumbers.put(registerId, nextNumber);
        }

        // Get V2 transactions/customers
        Map<String, Customer> customerPaymentCache = new HashMap<String, Customer>();
        if (customerLoyaltyEnabled) {
            // Get customer transactions
            Map<String, String> v2Params = tlogGeneratorPayload.getParams();
            v2Params.put("sort_order", "ASC"); // v2 default is DESC
            Transaction[] transactions = squareV2Client.transactions().list(v2Params);
            for (Transaction transaction : transactions) {
                for (Tender tender : transaction.getTenders()) {
                    if (tender.getCustomerId() != null) {
                        Customer customer = squareV2Client.customers().retrieve(tender.getCustomerId());

                        // Get customer's loyalty group status
                        boolean loyaltyCustomer = false;
                        boolean emailOptIn = false;
                        if (customer.getGroups() != null) {
                            for (CustomerGroup group : customer.getGroups()) {
                                if (group.getId().equals(CUSTOMER_GROUP_NEW)
                                        || group.getId().equals(CUSTOMER_GROUP_EXISTING)) {
                                    loyaltyCustomer = true;
                                }
                                if (group.getId().equals(CUSTOMER_GROUP_EMAIL)) {
                                    emailOptIn = true;
                                }
                            }
                        }

                        boolean customerProvidedEmail = customer.getEmailAddress() != null
                                && customer.getEmailAddress().length() > 0;
                        boolean customerProvidedPhone = customer.getPhoneNumber() != null
                                && customer.getPhoneNumber().length() > 0;

                        /*
                         * Set preferredCustomerId (if necessary)
                         *
                         * Square currently only returns a customer record when
                         * the merchant adds the customer to the sale. This
                         * could possible change in the future, so may need to
                         * verify they are on the transactions as a loyalty
                         * customer. For now, we are just going to verify the
                         * customer has provided an email
                         */
                        if ((customerProvidedEmail || customerProvidedPhone) && (customer.getReferenceId() == null
                                || customer.getReferenceId().length() != LOYALTY_CUSTOMER_ID_LENGTH)) {
                            String newId = generateNewPreferredCustomerId(nextPreferredCustomerNumbers,
                                    paymentsCache.get(tender.getId()), storeId);

                            customer.setReferenceId(newId);
                            customer = squareV2Client.customers().update(customer);

                            System.out.println("New Customer ID: " + customer.getReferenceId());
                        }

                        // For now, we only track the customer if they have
                        // provided an email address or phone number
                        if (customerProvidedEmail || customerProvidedPhone) {
                            customer.setCompanyName(loyaltyCustomer ? "1" : "0");

                            customerPaymentCache.put(tender.getId(), customer);
                            customerPaymentCache.put(tender.getTransactionId(), customer);
                        }
                    }
                }
            }
        }
        tlogGeneratorPayload.setCustomers(customerPaymentCache);

        String sqlUpdate = generatePreferredCustomerSQLUpdate(nextPreferredCustomerNumbers, deployment, storeId);
        message.setProperty("preferredCustomerSQLUpdate", sqlUpdate.length() > 0 ? true : false,
                PropertyScope.INVOCATION);
        message.setProperty("preferredCustomerSQLStatement", sqlUpdate, PropertyScope.INVOCATION);

        Merchant matchingMerchant = null;
        for (Merchant merchant : tlogGeneratorPayload.getLocations()) {
            if (merchant.getId().equals(tlogGeneratorPayload.getSquarePayload().getLocationId())) {
                matchingMerchant = merchant;
            }
        }

        if (matchingMerchant != null) {

            String deploymentId = (String) message.getProperty("deploymentId", PropertyScope.SESSION) + 1;

            Tlog tlog = new Tlog();
            tlog.setItemNumberLookupLength(itemNumberLookupLength);
            tlog.setDeployment(deploymentId);
            tlog.setTimeZoneId(timeZone);

            // Get Cloudhub default object store
            ObjectStore<String> objectStore = eventContext.getMuleContext().getRegistry()
                    .lookupObject("_defaultUserObjectStore");
            tlog.setObjectStore(objectStore);
            tlog.parse(matchingMerchant, tlogGeneratorPayload.getPayments(), tlogGeneratorPayload.getEmployees(),
                    tlogGeneratorPayload.getCustomers());

            message.setProperty("vfcorpStoreNumber",
                    Util.getStoreNumber(matchingMerchant.getLocationDetails().getNickname()), PropertyScope.INVOCATION);
            message.setProperty("tlog", tlog.toString(), PropertyScope.INVOCATION);
        }

        return null;
    }

    private String generateNewPreferredCustomerId(Map<String, Integer> nextPreferredCustomerIds,
            Payment customerPayment, String storeId) {

        String deviceName = (customerPayment != null && customerPayment.getDevice() != null)
                ? customerPayment.getDevice().getName() : null;
        String registerNumber = Util.getRegisterNumber(deviceName);

        int nextCustomerNumber = nextPreferredCustomerIds.getOrDefault(registerNumber, 1);
        int mod = 0; // Not currently performing any modulus operation

        // Update for next customer
        int updatedCounter = (nextCustomerNumber + 1 > 9999999) ? 1 : nextCustomerNumber + 1;
        nextPreferredCustomerIds.put(registerNumber, updatedCounter);

        return String.format("%s%s5%s%s", storeId, registerNumber, String.format("%07d", nextCustomerNumber), mod);
    }

    private String generatePreferredCustomerSQLUpdate(Map<String, Integer> nextPreferredCustomerIds, String deployment,
            String storeId) {
        String updateStatement = "";

        if (nextPreferredCustomerIds.size() > 0) {
            updateStatement = "INSERT INTO vfcorp_preferred_customer_counter (deployment, storeId, registerId, nextPreferredCustomerNumber) VALUES ";

            ArrayList<String> updates = new ArrayList<String>();
            for (Map.Entry<String, Integer> entry : nextPreferredCustomerIds.entrySet()) {
                updates.add(
                        String.format("('%s', '%s', '%s', %d)", deployment, storeId, entry.getKey(), entry.getValue()));
            }

            Iterator<String> i = updates.iterator();
            if (i.hasNext()) {
                updateStatement += i.next();
                while (i.hasNext()) {
                    updateStatement += ", " + i.next();
                }
            }

            updateStatement += " ON DUPLICATE KEY UPDATE nextPreferredCustomerNumber=VALUES(nextPreferredCustomerNumber);";
        }

        return updateStatement;
    }
}
