package transaction;

import java.util.HashMap;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import com.squareup.connect.Payment;
import com.squareup.connect.SquareClient;

public class PaymentsFromLocationReporter implements Callable {

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		SquareAccount sa = (SquareAccount) eventContext.getMessage().getPayload();
		
		// TODO(colinlam): make this configurable
		SquareClient client = new SquareClient(sa.getAccessToken(), "https://connect.squareupstaging.com/v1", sa.getSquareId());
		
		String beginTime = eventContext.getMessage().getProperty("begin_time", PropertyScope.SESSION, "");
		String endTime = eventContext.getMessage().getProperty("end_time", PropertyScope.SESSION, "");
		String order = eventContext.getMessage().getProperty("order", PropertyScope.SESSION, "");
		String limit = eventContext.getMessage().getProperty("limit", PropertyScope.SESSION, "");
		
		HashMap<String,String> map = new HashMap<String,String>();
		if (!beginTime.equals("")) {
			map.put("begin_time", beginTime);			
		}
		if (!endTime.equals("")) {
			map.put("end_time", endTime);			
		}
		if (!order.equals("")) {
			map.put("order", order);			
		}
		if (!limit.equals("")) {
			map.put("limit", limit);			
		}
		
        Payment[] payments = client.payments().list(map);
        
        return payments;
	}
}
