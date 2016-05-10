package util.square;

import java.util.LinkedList;
import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;

import util.SquarePayload;

import com.squareup.connect.Payment;
import com.squareup.connect.Refund;
import com.squareup.connect.SquareClient;

public class RefundPaymentsRetriever implements Callable {

	private String apiUrl;
	private String apiVersion;
	
	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}
	
	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		SquarePayload sp = (SquarePayload) eventContext.getMessage().getPayload();
		if (sp.getResults().get("util.square.RefundsLister") != null) {
			Refund[] squareRefunds = (Refund[]) sp.getResults().get("util.square.RefundsLister");
			
			SquareClient client = new SquareClient(sp.getAccessToken(), apiUrl, apiVersion, sp.getMerchantId(), sp.getLocationId());
			
	        List<Payment> squareRefundPayments = new LinkedList<Payment>();
	        
	        for (Refund squareRefund : squareRefunds) {
	        	squareRefundPayments.add(client.payments().retrieve(squareRefund.getPaymentId()));
	        }
	        
	        sp.getResults().put(this.getClass().getName(), squareRefundPayments.toArray(new Payment[squareRefundPayments.size()]));
		}
		
		return sp;
	}
}
