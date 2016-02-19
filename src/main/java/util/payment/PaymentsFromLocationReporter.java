package util.payment;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;

import util.SquarePayload;

import com.squareup.connect.Payment;
import com.squareup.connect.SquareClient;

public class PaymentsFromLocationReporter implements Callable {
	
	private String apiUrl;
	
	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		SquarePayload sp = (SquarePayload) eventContext.getMessage().getPayload();
		
		SquareClient client = new SquareClient(sp.getAccessToken(), apiUrl, sp.getLocationId());
		
        Payment[] payments = client.payments().list(sp.getParams());
        
        sp.getResults().put(eventContext.getFlowConstruct().getName(), payments);
        
        return sp;
	}
}
