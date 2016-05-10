package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

public class SquarePayloadCreator implements Callable {

	@Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
		@SuppressWarnings("unchecked")
		// The payload is a set of entries retrieved from a SQL database, which
		// is expected to have certain parameters.
		List<Map<String,Object>> merchantDatabaseEntries = (List<Map<String,Object>>) eventContext.getMessage().getPayload();
		List<SquarePayload> squarePayloads = new ArrayList<SquarePayload>();
		
		for (Map<String,Object> merchantDatabaseEntry : merchantDatabaseEntries) {
			SquarePayload squarePayload = new SquarePayload();
			
			squarePayload.setAccessToken((String) merchantDatabaseEntry.get("token"));
			squarePayload.setMerchantId((String) merchantDatabaseEntry.get("merchantId"));
			squarePayload.setLocationId((String) merchantDatabaseEntry.get("locationId"));
			squarePayload.setMerchantAlias((String) merchantDatabaseEntry.get("merchantAlias"));
			squarePayload.setLegacy((Boolean) merchantDatabaseEntry.get("legacy"));
			
			String timeMethod = eventContext.getMessage().getProperty("timeMethod", PropertyScope.INVOCATION);
			if ("getPastDayInterval".equals(timeMethod)) {
				int range = Integer.parseInt(eventContext.getMessage().getProperty("range", PropertyScope.INVOCATION));
				int offset = Integer.parseInt(eventContext.getMessage().getProperty("offset", PropertyScope.INVOCATION));
				String timeZone = eventContext.getMessage().getProperty("timeZone", PropertyScope.INVOCATION);
				
				Map<String,String> mm = TimeManager.getPastDayInterval(range, offset, timeZone);
				squarePayload.getParams().put("begin_time", mm.get("begin_time"));
				squarePayload.getParams().put("end_time", mm.get("end_time"));
			} else if ("getPastTimeInterval".equals(timeMethod)) {
				int seconds = Integer.parseInt(eventContext.getMessage().getProperty("seconds", PropertyScope.INVOCATION));
				int offset = Integer.parseInt(eventContext.getMessage().getProperty("offset", PropertyScope.INVOCATION));
				String timeZone = eventContext.getMessage().getProperty("timeZone", PropertyScope.INVOCATION);
				
				Map<String,String> mm = TimeManager.getPastTimeInterval(seconds, offset, timeZone);
				squarePayload.getParams().put("begin_time", mm.get("begin_time"));
				squarePayload.getParams().put("end_time", mm.get("end_time"));
			}
			
			squarePayloads.add(squarePayload);
		}
		
		return squarePayloads;
	}
}