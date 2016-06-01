package util.oauth;

import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import com.squareup.connect.OAuthToken;
import com.squareup.connect.SquareClient;

public class TokenRenewer implements Callable {

	private String apiUrl;
	private String multiunitId;
	private String multiunitSecret;
	private String legacyId;
	private String legacySecret;
	
	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}
	
	public void setMultiunitId(String multiunitId) {
		this.multiunitId = multiunitId;
	}

	public void setMultiunitSecret(String multiunitSecret) {
		this.multiunitSecret = multiunitSecret;
	}

	public void setLegacyId(String legacyId) {
		this.legacyId = legacyId;
	}

	public void setLegacySecret(String legacySecret) {
		this.legacySecret = legacySecret;
	}

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		@SuppressWarnings("unchecked")
		Map<String,Object> m = (Map<String, Object>) eventContext.getMessage().getPayload();
		
		Integer id = (Integer) m.get("id");
		String token = (String) m.get("token");
		String connectApp = (String) m.get("connectApp");
		String deployment = (String) m.get("deployment");
		
		String secret = "";
		if (connectApp != null && connectApp.equals(multiunitId)) {
			secret = multiunitSecret;
		} else if (connectApp != null && connectApp.equals(legacyId)) {
			secret = legacySecret;
		} else {
			throw new Exception("connect app '" + connectApp + "' associated with this token is not an official Managed Integrations application");
		}
		
		SquareClient client = new SquareClient(token, apiUrl);
		OAuthToken newToken = client.oauth().renewToken(connectApp, secret);
		
		eventContext.getMessage().setProperty("oldId", id, PropertyScope.INVOCATION);
		eventContext.getMessage().setProperty("deployment", deployment, PropertyScope.INVOCATION);
		eventContext.getMessage().setProperty("connectApp", connectApp, PropertyScope.INVOCATION);
		
		return newToken;
	}
}
