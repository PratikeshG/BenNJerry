package util.oauth;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import com.squareup.connect.OAuthCode;
import com.squareup.connect.OAuthToken;
import com.squareup.connect.SquareClient;

public class CodeToTokenConverter implements Callable {

	private String apiUrl;
	private String multiUnitAppId;
	private String multiUnitAppSecret;
	private String legacyAppId;
	private String legacyAppSecret;

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	public void setMultiUnitAppId(String multiUnitAppId) {
		this.multiUnitAppId = multiUnitAppId;
	}
	
	public void setMultiUnitAppSecret(String multiUnitAppSecret) {
		this.multiUnitAppSecret = multiUnitAppSecret;
	}

	public void setLegacyAppId(String legacyAppId) {
		this.legacyAppId = legacyAppId;
	}

	public void setLegacyAppSecret(String legacyAppSecret) {
		this.legacyAppSecret = legacyAppSecret;
	}

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		// Information that needs to be passed:
		// deployment,connectApp,token,merchantId,locationId,legacy,expiryDate
		
		OAuthCode code = new OAuthCode();
		
		String connectAppId = eventContext.getMessage().getProperty("connectAppId", PropertyScope.INVOCATION);
		
		if (connectAppId.equals(legacyAppId)) {
			code.setClientId(legacyAppId);
			code.setClientSecret(legacyAppSecret);
			eventContext.getMessage().setProperty("legacy", true, PropertyScope.INVOCATION);
		} else if (connectAppId.equals(multiUnitAppId)) {
			code.setClientId(multiUnitAppId);
			code.setClientSecret(multiUnitAppSecret);
			eventContext.getMessage().setProperty("legacy", false, PropertyScope.INVOCATION);
		}
		
		code.setCode(eventContext.getMessage().getProperty("code", PropertyScope.INVOCATION));
		
		SquareClient client = new SquareClient(apiUrl);
		
		OAuthToken token = client.oauth().obtainToken(code);
		
		return token;
	}
}
