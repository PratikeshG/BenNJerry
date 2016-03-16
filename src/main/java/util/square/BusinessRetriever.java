package util.square;
import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;

import util.SquarePayload;

import com.squareup.connect.Merchant;
import com.squareup.connect.SquareClient;


public class BusinessRetriever implements Callable {

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
		
		SquareClient client = new SquareClient(sp.getAccessToken(), apiUrl, apiVersion, sp.getMerchantId(), sp.getLocationId());
		
        Merchant merchant = client.businessLocations().retrieve();
        
        sp.getResults().put(this.getClass().getName(), merchant);
        
        return sp;
	}
}
