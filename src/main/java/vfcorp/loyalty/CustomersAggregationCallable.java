package vfcorp.loyalty;

import java.util.HashMap;
import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;

import com.squareup.connect.v2.Customer;

public class CustomersAggregationCallable implements Callable {

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
	MuleMessage message = eventContext.getMessage();

	@SuppressWarnings("unchecked")
	List<List<LoyaltyCustomerPayload>> locationCustomerPayloads = (List<List<LoyaltyCustomerPayload>>) message
		.getPayload();

	HashMap<String, LoyaltyCustomerPayload> customerSet = new HashMap<String, LoyaltyCustomerPayload>();

	for (List<LoyaltyCustomerPayload> locationCustomerList : locationCustomerPayloads) {
	    for (LoyaltyCustomerPayload customerPayload : locationCustomerList) {
		Customer c = customerPayload.getCustomer();
		if (c != null && c.getReferenceId() != null) {
		    customerSet.put(c.getReferenceId(), customerPayload);
		} else {
		    throw new Exception("Loyalty customer missing generated reference ID.");
		}
	    }
	}

	StringBuilder builder = new StringBuilder();
	for (String key : customerSet.keySet()) {
	    LoyaltyCustomerPayload loyaltyPayload = customerSet.get(key);
	    LoyaltyEntry entry = new LoyaltyEntry(loyaltyPayload.getStoreId(), loyaltyPayload.getCustomer(),
		    loyaltyPayload.getAssociateId());
	    builder.append(entry.toString() + "\r\n");
	}

	return builder.toString();
    }
}
