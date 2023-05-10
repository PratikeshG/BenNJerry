package vfcorp.reporting;

import java.util.ArrayList;
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

import com.squareup.connect.v2.Payment;
import com.squareup.connect.SquareClient;
import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.SquareClientV2;
import com.squareup.connect.v2.Tender;
import com.squareup.connect.v2.Transaction;

import util.ConnectV2MigrationHelper;
import util.SquarePayload;
import util.TimeManager;

public class DetailsByDeploymentCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(DetailsByDeploymentCallable.class);

    @Value("${encryption.key.tokens}")
    private String encryptionKey;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        SquarePayload deployment = (SquarePayload) message.getPayload();
        String apiUrl = message.getProperty("apiUrl", PropertyScope.SESSION);
        String apiVersion = message.getProperty("apiVersion", PropertyScope.SESSION);

        boolean allowCashTransactions = message.getProperty("allowCashTransactions", PropertyScope.SESSION)
                .equals("true") ? true : false;

        SquareClient squareV1Client = new SquareClient(deployment.getAccessToken(encryptionKey), apiUrl, apiVersion,
                deployment.getMerchantId(), deployment.getLocationId());
        SquareClientV2 squareV2Client = new SquareClientV2(apiUrl, deployment.getAccessToken(encryptionKey));
        squareV2Client.setLogInfo(deployment.getMerchantId() + " - " + deployment.getLocationId());

        Location location = squareV2Client.locations().retrieve(deployment.getLocationId());
        if (location == null) {
            throw new Exception("No matching location ID found in loyalty calculation!");
        }

        int offset = Integer.parseInt(message.getProperty("offset", PropertyScope.SESSION));
        int range = Integer.parseInt(message.getProperty("range", PropertyScope.SESSION));
        Map<String, String> params = TimeManager.getPastDayInterval(range, offset, location.getTimezone());

        // V1 Payments - ignore no-sale and cash-only payments
        com.squareup.connect.Payment[] v1allPayments = squareV1Client.payments().list(params);
        List<com.squareup.connect.Payment> v1validPayments = new ArrayList<com.squareup.connect.Payment>();
        for (com.squareup.connect.Payment payment : v1allPayments) {
            boolean hasValidPaymentTender = false;
            for (com.squareup.connect.Tender tender : payment.getTender()) {

                if (allowCashTransactions) {
                    if (!tender.getType().equals("NO_SALE")) {
                        hasValidPaymentTender = true;
                    }
                } else {
                    if (!tender.getType().equals("CASH") && !tender.getType().equals("NO_SALE")) {
                        hasValidPaymentTender = true;
                    }
                }
            }
            if (hasValidPaymentTender) {
                v1validPayments.add(payment);
            }
        }

        // V2 Transactions - ignore no-sales and cash-only transactions
        params.put("sort_order", "ASC"); // v2 default is DESC
        Transaction[] allTransactions = squareV2Client.transactions().list(location.getId(), params);
        List<Transaction> validTransactions = new ArrayList<Transaction>();
        for (Transaction transaction : allTransactions) {
            boolean hasValidTransactionTender = false;
            for (com.squareup.connect.v2.Tender tender : transaction.getTenders()) {
                if (allowCashTransactions) {
                    if (!tender.getType().equals("NO_SALE")) {
                        hasValidTransactionTender = true;
                    }
                } else {
                    if (!tender.getType().equals("CASH") && !tender.getType().equals("NO_SALE")) {
                        hasValidTransactionTender = true;
                    }
                }
            }
            if (hasValidTransactionTender) {
                validTransactions.add(transaction);
            }
        }

        // V2 Customers
//        HashMap<String, Customer> customers = new HashMap<String, Customer>();
//        for (Transaction transaction : validTransactions) {
//            for (Tender tender : transaction.getTenders()) {
//                if (tender.getCustomerId() != null) {
//                    Customer customer = squareV2Client.customers().retrieve(tender.getCustomerId());
//                    customers.put(customer.getId(), customer);
//                }
//            }
//        }

		params.put("location_id", location.getId());
        Payment[] payments = ConnectV2MigrationHelper.getPaymentsV2(squareV2Client,location.getId(), params);
        Order[] orders = ConnectV2MigrationHelper.getOrders(squareV2Client, location.getId(), params, allowCashTransactions);
        Map<String, Payment> tenderToPayment = ConnectV2MigrationHelper.getTenderToPayment(orders, payments, squareV2Client, params);

        HashMap<String, Customer> customers = new HashMap<String, Customer>();
        for (Order order : orders) {
            for (Tender tender : order.getTenders()) {
                if (tender.getCustomerId() != null) {
                    Customer customer = squareV2Client.customers().retrieve(tender.getCustomerId());
                    customers.put(customer.getId(), customer);
                }
            }
        }


//        LocationTransactionDetails details = new LocationTransactionDetails(location,
//                validTransactions.toArray(new Transaction[0]), v1validPayments.toArray(new com.squareup.connect.Payment[0]), customers);
        LocationTransactionDetails details = new LocationTransactionDetails(location, orders, payments, customers, tenderToPayment);
        return details;
    }
}
