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

import com.squareup.connect.Employee;
import com.squareup.connect.Payment;
import com.squareup.connect.SquareClient;
import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.SquareClientV2;
import com.squareup.connect.v2.Tender;
import com.squareup.connect.v2.Transaction;

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
        Payment[] allPayments = squareV1Client.payments().list(params);
        List<Payment> validPayments = new ArrayList<Payment>();
        for (Payment payment : allPayments) {
            boolean hasValidPaymentTender = false;
            for (com.squareup.connect.Tender tender : payment.getTender()) {
                if (!tender.getType().equals("CASH") && !tender.getType().equals("NO_SALE")) {
                    hasValidPaymentTender = true;
                }
            }
            if (hasValidPaymentTender) {
                validPayments.add(payment);
            }
        }

        // V1 Employees
        HashMap<String, Employee> employees = new HashMap<String, Employee>();
        Employee[] employeeList = squareV1Client.employees().list();
        for (Employee employee : employeeList) {
            employees.put(employee.getId(), employee);
        }

        // V2 Transactions - ignore no-sales and cash-only transactions
        params.put("sort_order", "ASC"); // v2 default is DESC
        Transaction[] allTransactions = squareV2Client.transactions().list(location.getId(), params);
        List<Transaction> validTransactions = new ArrayList<Transaction>();
        for (Transaction transaction : allTransactions) {
            boolean hasValidTransactionTender = false;
            for (com.squareup.connect.v2.Tender tender : transaction.getTenders()) {
                if (!tender.getType().equals("CASH") && !tender.getType().equals("NO_SALE")) {
                    hasValidTransactionTender = true;
                }
            }
            if (hasValidTransactionTender) {
                validTransactions.add(transaction);
            }

        }

        // V2 Customers
        HashMap<String, Customer> customers = new HashMap<String, Customer>();
        for (Transaction transaction : validTransactions) {
            for (Tender tender : transaction.getTenders()) {
                if (tender.getCustomerId() != null) {
                    Customer customer = squareV2Client.customers().retrieve(tender.getCustomerId());
                    customers.put(customer.getId(), customer);
                }
            }
        }

        // V1 YTD Payments - ignore no-sale and cash-only payments
        Map<String, String> yearToDateParams = TimeManager.getYearToDateInterval(location.getTimezone());
        Payment[] allYearToDatePayments = squareV1Client.payments().list(yearToDateParams);
        List<Payment> validYearToDatePayments = new ArrayList<Payment>();
        for (Payment payment : allYearToDatePayments) {
            boolean hasValidPaymentTender = false;
            for (com.squareup.connect.Tender tender : payment.getTender()) {
                if (!tender.getType().equals("CASH") && !tender.getType().equals("NO_SALE")) {
                    hasValidPaymentTender = true;
                }
            }
            if (hasValidPaymentTender) {
                validYearToDatePayments.add(payment);
            }
        }

        LocationTransactionDetails details = new LocationTransactionDetails(location,
                validTransactions.toArray(new Transaction[0]), validPayments.toArray(new Payment[0]), employees,
                customers);
        details.setYearToDatePayments(validYearToDatePayments.toArray(new Payment[0]));

        return details;
    }
}
