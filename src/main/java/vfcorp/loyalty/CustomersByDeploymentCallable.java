package vfcorp.loyalty;

import java.util.ArrayList;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.SquareClient;
import com.squareup.connect.v2.Tender;
import com.squareup.connect.v2.Transaction;

import util.SquarePayload;
import util.TimeManager;
import vfcorp.Util;

public class CustomersByDeploymentCallable implements Callable {

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
	MuleMessage message = eventContext.getMessage();

	SquarePayload deployment = (SquarePayload) message.getPayload();
	String apiUrl = message.getProperty("apiUrl", PropertyScope.SESSION);

	SquareClient squareClient = new SquareClient(apiUrl, deployment.getAccessToken(), deployment.getLocationId());

	String storeId = "0000";
	String timeZone = "America/New_York";

	// We want to get the location store ID and time zone
	// There is currently no retrieveLocation endpoint in V2
	// Need to list locations then find the correct location
	Location[] deploymentLocations = squareClient.locations().list();
	boolean locationFound = false;
	for (Location loc : deploymentLocations) {
	    if (loc.getId().equals(deployment.getLocationId())) {
		locationFound = true;
		storeId = Util.getStoreNumber(loc.getName());
		timeZone = loc.getTimezone();

		System.out.println(storeId);
		System.out.println(loc.getName());
		System.out.println(timeZone);

		break;
	    }
	}
	if (!locationFound) {
	    throw new Exception("No matching location ID found in loyalty calculation!");
	}

	// Get all customers from transactions during the previous day
	int offset = Integer.parseInt(message.getProperty("offset", PropertyScope.SESSION));
	int range = Integer.parseInt(message.getProperty("range", PropertyScope.SESSION));
	Map<String, String> params = TimeManager.getPastDayInterval(range, offset, timeZone);
	params.put("sort_order", "ASC"); // v2 default is DESC

	ArrayList<LoyaltyCustomerPayload> customerPayloads = new ArrayList<LoyaltyCustomerPayload>();

	Transaction[] transactions = squareClient.transactions().list(params);
	for (Transaction transaction : transactions) {
	    for (Tender tender : transaction.getTenders()) {
		if (tender.getCustomerId() != null) {
		    Customer customer = squareClient.customers().retrieve(tender.getCustomerId());

		    LoyaltyCustomerPayload customerPayload = new LoyaltyCustomerPayload();
		    customerPayload.setStoreId(storeId);
		    customerPayload.setCustomer(customer);
		    customerPayload.setAssociateId("");

		    customerPayloads.add(customerPayload);
		}
	    }
	}

	return customerPayloads;
    }
}
