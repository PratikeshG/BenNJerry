package tntfireworks.reporting;

import java.util.ArrayList;
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

    // start of season yyyy-mm-dd
    @Value("${tntfireworks.startOfSeason}")
    private String startOfSeason;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        // get session vars
        int offset = Integer.parseInt(message.getProperty("offset", PropertyScope.SESSION));
        int range = Integer.parseInt(message.getProperty("range", PropertyScope.SESSION));
        int reportType = Integer.parseInt(message.getProperty("reportType", PropertyScope.SESSION));
        String apiUrl = message.getProperty("apiUrl", PropertyScope.SESSION);
        String apiVersion = message.getProperty("apiVersion", PropertyScope.SESSION);

        // compute YTD range if range = 365
        if (range == 365) {
            // - initialize startOfSeason as 03/01/2017 (02 month, 0 day, 2017 year)
            // - use default tz as Los Angeles
            TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
            range = DeploymentDetailsOptimizedCallable.computeSeasonInterval(startOfSeason, tz);
        }

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
            Map<String, String> aggregateIntervalParams = TimeManager.getPastDayInterval(range, offset,
                    location.getTimezone());
            aggregateIntervalParams.put("sort_order", "ASC"); // v2 default is DESC

            squareV1Client.setLocation(location.getId());
            squareV2Client.setLocation(location.getId());

            // get detailed payload data depending on reportType
            Payment[] payments = new Payment[0];
            Transaction[] transactions = new Transaction[0];
            Settlement[] settlements = new Settlement[0];
            Map<String, Employee> employees = new HashMap<String, Employee>();

            switch (reportType) {
                case 1:
                    settlements = getSettlements(squareV1Client, aggregateIntervalParams);
                    break;
                case 2:
                    payments = getPayments(squareV1Client, aggregateIntervalParams);
                    // no break, reportType '2' (transactions report, needs both payments and transactions)
                case 3:
                    transactions = getTransactions(squareV2Client, aggregateIntervalParams);
                    break;
                case 8:
                    payments = getPayments(squareV1Client, aggregateIntervalParams);
                    break;
            }

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

    private Settlement[] getSettlements(SquareClient squareClient, Map<String, String> params) throws Exception {
        // V1 Settlements
        return squareClient.settlements().list(params);
    }

}
