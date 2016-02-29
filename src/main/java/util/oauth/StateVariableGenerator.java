package util.oauth;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

public class StateVariableGenerator implements Callable {
	
	private String apiUrl;
	private String connectAppBridgeId;
	
	public void setConnectAppStateKey(String connectAppStateKey) {
	}
	
	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	public void setConnectAppBridgeId(String connectAppBridgeId) {
		this.connectAppBridgeId = connectAppBridgeId;
	}

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		SecureRandom random = new SecureRandom();
		String session = new BigInteger(130, random).toString(32);
		
		Map<String,String> m = eventContext.getMessage().getProperty("http.query.params", PropertyScope.INBOUND);
		String deployment = m.get("deployment");
		
		String link = apiUrl + "/oauth2/authorize?client_id=" + connectAppBridgeId +
				"&response_type=code&state=" + deployment + "," + session;
		
		eventContext.getMessage().setProperty("session", session, PropertyScope.INVOCATION);
		eventContext.getMessage().setProperty("link", link, PropertyScope.INVOCATION);
		
		return null;
	}
}
