package util.oauth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

public class StateVariableGenerator implements Callable {
	
	private String connectAppStateKey;
	private String apiUrl;
	private String connectAppBridgeId;
	
	public void setConnectAppStateKey(String connectAppStateKey) {
		this.connectAppStateKey = connectAppStateKey;
	}
	
	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	public void setConnectAppBridgeId(String connectAppBridgeId) {
		this.connectAppBridgeId = connectAppBridgeId;
	}

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		String time = String.valueOf(Instant.now().getEpochSecond());
		String text = time + connectAppStateKey;
		
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
		// Convert byte array to hexadecimal string
		StringBuffer state = new StringBuffer();
	    for (int i = 0; i < hash.length; i++) {
	    	state.append(Integer.toHexString(0xFF & hash[i]));
	    }
		
		Map<String,String> m = eventContext.getMessage().getProperty("http.query.params", PropertyScope.INBOUND);
		String deployment = m.get("deployment");
		
		String link = apiUrl + "oauth2/authorize?client_id=" + connectAppBridgeId +
				"&response_type=code&state=" + deployment + "," + state;
		
		eventContext.getMessage().setProperty("state", state, PropertyScope.INVOCATION);
		eventContext.getMessage().setProperty("link", link, PropertyScope.INVOCATION);
		
		return null;
	}
}
