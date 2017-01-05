package tntfireworks.reporting;

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

import com.squareup.connect.Employee;
import com.squareup.connect.Payment;
import com.squareup.connect.Settlement;
import com.squareup.connect.SquareClient;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.SquareClientV2;
import com.squareup.connect.v2.Transaction;

import util.SquarePayload;
import util.TimeManager;

public class DeploymentDetailsCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(DeploymentDetailsCallable.class);

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        // get session vars
        int offset = Integer.parseInt(message.getProperty("offset", PropertyScope.SESSION));
        int range = Integer.parseInt(message.getProperty("range", PropertyScope.SESSION));
        String apiUrl = message.getProperty("apiUrl", PropertyScope.SESSION);
        String apiVersion = message.getProperty("apiVersion", PropertyScope.SESSION);

        // get deployment from queue-splitter
        SquarePayload deployment = (SquarePayload) message.getPayload();

        // initialize connect v1/v2 api clients
        SquareClient squareV1Client = new SquareClient(deployment.getAccessToken(), apiUrl, apiVersion,
                deployment.getMerchantId());
        SquareClientV2 squareV2Client = new SquareClientV2(apiUrl, deployment.getAccessToken());

        // retrieve individual location details and store into abstracted object       
        logger.info("Retrieving location details for merchant: %s", deployment.getMerchantId());
        List<TntLocationDetails> deploymentDetails = new ArrayList<TntLocationDetails>();

        for (Location location : squareV2Client.locations().list()) {
            Map<String, String> params = TimeManager.getPastDayInterval(range, offset, location.getTimezone());
            squareV1Client.setLocation(location.getId());
            squareV2Client.setLocation(location.getId());

            // get detailed payment / transaction data
            Payment[] payments = getPayments(squareV1Client, params);
            Transaction[] transactions = getTransactions(squareV2Client, params);
            Settlement[] settlements = getSettlements(squareV1Client, params);
            Map<String, Employee> employees = getEmployees(squareV1Client);

            deploymentDetails.add(
                    new TntLocationDetails(location, transactions, payments, settlements, employees,
                            deployment.getMerchantId()));
        }

        return deploymentDetails;
    }

    private Payment[] getPayments(SquareClient squareClient, Map<String, String> params)
            throws Exception {
        // V1 Payments - ignore no-sale and cash-only payments
        Payment[] allPayments = squareClient.payments().list(params);
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

    private Transaction[] getTransactions(SquareClientV2 squareClient, Map<String, String> params)
            throws Exception {
        // V2 Transactions - ignore no-sales and cash-only transactions
        params.put("sort_order", "ASC"); // v2 default is DESC
        Transaction[] allTransactions = squareClient.transactions().list(params);
        List<Transaction> transactions = new ArrayList<Transaction>();

        for (Transaction transaction : allTransactions) {
            boolean hasValidTransactionTender = false;
            for (com.squareup.connect.v2.Tender tender : transaction.getTenders()) {
                if (!tender.getType().equals("CASH") && !tender.getType().equals("NO_SALE")) {
                    hasValidTransactionTender = true;
                }
            }
            if (hasValidTransactionTender) {
                transactions.add(transaction);
            }
        }
        return transactions.toArray(new Transaction[0]);
    }

    private Map<String, Employee> getEmployees(SquareClient squareClient) throws Exception {
        Map<String, Employee> employeeMap = new HashMap<String, Employee>();

        for (Employee employee : squareClient.employees().list()) {
            employeeMap.put(employee.getId(), employee);
        }

        return employeeMap;
    }

    private Settlement[] getSettlements(SquareClient squareClient, Map<String, String> params) throws Exception {
        // V1 Settlements
        return squareClient.settlements().list(params);
    }

}
