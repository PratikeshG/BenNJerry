package util.square;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;

import util.SquarePayload;

import com.squareup.connect.Merchant;
import com.squareup.connect.SquareClient;

public class LocationsLister implements Callable {

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
		
		if (!sp.isLegacy()) {
			SquareClient client = new SquareClient(sp.getAccessToken(), apiUrl, apiVersion, sp.getMerchantId(), sp.getLocationId());
			
	        Merchant[] merchants = client.businessLocations().list();
	        
	        sp.getResults().put(this.getClass().getName(), merchants);
		}
        
        return sp;
	}
}
