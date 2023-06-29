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
import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.SquareClientV2;
import com.squareup.connect.v2.Tender;

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

        SquareClientV2 squareV2Client = new SquareClientV2(apiUrl, deployment.getAccessToken(encryptionKey));
        squareV2Client.setLogInfo(deployment.getMerchantId() + " - " + deployment.getLocationId());

        Location location = squareV2Client.locations().retrieve(deployment.getLocationId());
        if (location == null) {
            throw new Exception("No matching location ID found in loyalty calculation!");
        }

        int offset = Integer.parseInt(message.getProperty("offset", PropertyScope.SESSION));
        int range = Integer.parseInt(message.getProperty("range", PropertyScope.SESSION));
        Map<String, String> params = TimeManager.getPastDayInterval(range, offset, location.getTimezone());

		params.put("location_id", location.getId());
        Payment[] payments = ConnectV2MigrationHelper.getPaymentsV2(squareV2Client,location.getId(), params);
        Order[] orders = ConnectV2MigrationHelper.getOrders(squareV2Client, location.getId(), params, allowCashTransactions);
        Map<String, Payment> tenderToPayment = ConnectV2MigrationHelper.getTenderToPayment(orders, payments, squareV2Client, params);

        HashMap<String, Customer> customers = new HashMap<String, Customer>();
        for (Order order : orders) {
        	if(order.getTenders() != null) {
        		for (Tender tender : order.getTenders()) {
                    if (tender.getCustomerId() != null) {
                        Customer customer = squareV2Client.customers().retrieve(tender.getCustomerId());
                        customers.put(customer.getId(), customer);
                    }
                }
        	}
        }

        LocationTransactionDetails details = new LocationTransactionDetails(location, orders, payments, customers, tenderToPayment);
        return details;
    }
}
