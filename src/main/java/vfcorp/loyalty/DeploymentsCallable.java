package vfcorp.loyalty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;

import util.SquarePayload;

public class DeploymentsCallable implements Callable {

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
	MuleMessage message = eventContext.getMessage();

	@SuppressWarnings("unchecked")
	List<Map<String, Object>> merchantDatabaseEntries = (List<Map<String, Object>>) message.getPayload();
	List<SquarePayload> deploymentPayloads = new ArrayList<SquarePayload>();

	for (Map<String, Object> merchantDatabaseEntry : merchantDatabaseEntries) {
	    SquarePayload deploymentPayload = new SquarePayload();

	    deploymentPayload.setAccessToken((String) merchantDatabaseEntry.get("token"));
	    deploymentPayload.setMerchantId((String) merchantDatabaseEntry.get("merchantId"));
	    deploymentPayload.setLocationId((String) merchantDatabaseEntry.get("locationId"));
	    deploymentPayload.setMerchantAlias((String) merchantDatabaseEntry.get("merchantAlias"));
	    deploymentPayload.setLegacy((Boolean) merchantDatabaseEntry.get("legacy"));

	    deploymentPayloads.add(deploymentPayload);
	}

	return deploymentPayloads;
    }
}
