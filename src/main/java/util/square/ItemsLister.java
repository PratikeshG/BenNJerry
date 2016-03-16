package util.square;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;

import util.SquarePayload;

import com.squareup.connect.Item;
import com.squareup.connect.SquareClient;

public class ItemsLister implements Callable {

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
		
		Item[] items = client.items().list();
        
        sp.getResults().put(this.getClass().getName(), items);
        
        return sp;
	}
}
