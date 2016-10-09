package vfcorp.loyalty;

import java.util.HashMap;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

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

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        SquarePayload deployment = (SquarePayload) message.getPayload();
        String apiUrl = message.getProperty("apiUrl", PropertyScope.SESSION);
        String apiVersion = message.getProperty("apiVersion", PropertyScope.SESSION);

        SquareClient squareV1Client = new SquareClient(deployment.getAccessToken(), apiUrl, apiVersion,
                deployment.getMerchantId(), deployment.getLocationId());
        SquareClientV2 squareV2Client = new SquareClientV2(apiUrl, deployment.getAccessToken(),
                deployment.getLocationId());

        Location location = null;

        // We want to get the location store ID and time zone
        // There is currently no retrieveLocation endpoint in V2
        // Need to list locations then find the correct location
        Location[] deploymentLocations = squareV2Client.locations().list();
        for (Location loc : deploymentLocations) {
            if (loc.getId().equals(deployment.getLocationId())) {
                location = loc;
                break;
            }
        }
        if (location == null) {
            throw new Exception("No matching location ID found in loyalty calculation!");
        }

        int offset = Integer.parseInt(message.getProperty("offset", PropertyScope.SESSION));
        int range = Integer.parseInt(message.getProperty("range", PropertyScope.SESSION));
        Map<String, String> params = TimeManager.getPastDayInterval(range, offset, location.getTimezone());

        // V1 Payments
        Payment[] payments = squareV1Client.payments().list(params);

        // V1 Employees
        HashMap<String, Employee> employees = new HashMap<String, Employee>();
        Employee[] employeeList = squareV1Client.employees().list();
        for (Employee employee : employeeList) {
            employees.put(employee.getId(), employee);
        }

        // V2 Transactions
        params.put("sort_order", "ASC"); // v2 default is DESC
        Transaction[] transactions = squareV2Client.transactions().list(params);

        // V2 Customers
        HashMap<String, Customer> customers = new HashMap<String, Customer>();

        for (Transaction transaction : transactions) {
            for (Tender tender : transaction.getTenders()) {
                if (tender.getCustomerId() != null) {
                    Customer customer = squareV2Client.customers().retrieve(tender.getCustomerId());
                    customers.put(customer.getId(), customer);
                }
            }
        }

        LocationTransactionDetails details = new LocationTransactionDetails(location, transactions, payments, employees,
                customers);

        return details;
    }
}
