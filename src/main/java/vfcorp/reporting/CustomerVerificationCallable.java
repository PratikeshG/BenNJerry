package vfcorp.reporting;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.Payment;
import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.SquareClientV2;
import com.squareup.connect.v2.Transaction;

import util.SquarePayload;
import vfcorp.Util;
import vfcorp.VfcDatabaseApi;

public class CustomerVerificationCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(CustomerVerificationCallable.class);

    private final int LOYALTY_CUSTOMER_ID_LENGTH = 17;

    @Value("${api.url}")
    private String apiUrl;
    @Value("${encryption.key.tokens}")
    private String encryptionKey;

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

        @SuppressWarnings("unchecked")
        List<SquarePayload> payloads = (List<SquarePayload>) message.getProperty("squarePayloads",
                PropertyScope.SESSION);

        Map<String, SquarePayload> squarePayloads = new HashMap<String, SquarePayload>();
        for (SquarePayload p : payloads) {
            squarePayloads.put(p.getLocationId(), p);
        }

        // Get PCM counters
        Map<String, Map<String, Integer>> nextPreferredCustomerNumbers = new HashMap<String, Map<String, Integer>>();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> preferredCustomerCounters = (List<Map<String, Object>>) message
                .getProperty("preferredCustomerCounters", PropertyScope.SESSION);
        for (Map<String, Object> customerCounter : preferredCustomerCounters) {
            String pcmStoreId = (String) customerCounter.get("storeId");
            String registerId = (String) customerCounter.get("registerId");
            int nextNumber = (int) customerCounter.get("nextPreferredCustomerNumber");

            Map<String, Integer> newPCM = nextPreferredCustomerNumbers.get(pcmStoreId);
            if (newPCM == null) {
                newPCM = new HashMap<String, Integer>();
                nextPreferredCustomerNumbers.put(pcmStoreId, newPCM);
            }

            newPCM.put(registerId, nextNumber);
        }

        // Establish database connection for PCM counters
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
        VfcDatabaseApi databaseApi = new VfcDatabaseApi(conn);

        // Process Customers for each location
        for (LocationTransactionDetails locationTransactionDetails : transactionDetailsByLocation) {
            SquareClientV2 squareV2Client = new SquareClientV2(apiUrl,
                    squarePayloads.get(locationTransactionDetails.getLocation().getId()).getAccessToken(encryptionKey),
                    squarePayloads.get(locationTransactionDetails.getLocation().getId()).getMerchantId(),
                    squarePayloads.get(locationTransactionDetails.getLocation().getId()).getLocationId());

            String storeId = Util.getStoreNumber(locationTransactionDetails.getLocation().getName());

            Map<String, Integer> customerCounters = nextPreferredCustomerNumbers.get(storeId);
            if (customerCounters == null) {
                customerCounters = new HashMap<String, Integer>();
            }

            Map<String, Payment> paymentsCache = new HashMap<String, Payment>();
            for (Payment payment : locationTransactionDetails.getPayments()) {
                // Save payment object for customer lookups
                paymentsCache.put(payment.getId(), payment);
                for (com.squareup.connect.Tender tender : payment.getTender()) {
                    paymentsCache.put(tender.getId(), payment);
                }
            }

            for (Transaction transaction : locationTransactionDetails.getTransactions()) {
                for (com.squareup.connect.v2.Tender tender : transaction.getTenders()) {
                    if (tender.getCustomerId() != null) {
                        Customer customer = locationTransactionDetails.getCustomers().get(tender.getCustomerId());

                        boolean customerHasContactInfo = (customer.getEmailAddress() != null
                                && customer.getEmailAddress().length() > 0)
                                || (customer.getPhoneNumber() != null && customer.getPhoneNumber().length() > 0);

                        if (customer != null && customerHasContactInfo && (customer.getReferenceId() == null
                                || customer.getReferenceId().length() != LOYALTY_CUSTOMER_ID_LENGTH)) {
                            String newId = generateNewPreferredCustomerId(customerCounters,
                                    paymentsCache.get(tender.getId()), storeId);

                            customer.setReferenceId(newId);
                            customer = squareV2Client.customers().update(customer);

                            logger.info("New Customer: " + customer.getId());
                            logger.info("New Customer ID: " + customer.getReferenceId());
                        }
                    }
                }
            }

            String deploymentId = String.format("vfcorp-tnf-%s", storeId);
            String sqlUpdate = databaseApi.generatePreferredCustomerSQLUpsert(customerCounters, deploymentId, storeId);
            databaseApi.executeUpdate(sqlUpdate);
        }

        databaseApi.close();

        return 1;
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

        logger.info("update counter: " + updatedCounter);
        return String.format("%s%s5%s%s", storeId, registerNumber, String.format("%07d", nextCustomerNumber), mod);
    }
}
