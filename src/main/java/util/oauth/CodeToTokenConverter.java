package util.oauth;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import com.squareup.connect.OAuthCode;
import com.squareup.connect.OAuthToken;
import com.squareup.connect.SquareClient;

public class CodeToTokenConverter implements Callable {

	private String apiUrl;
	private String connectAppBridgeId;
	private String connectAppBridgeSecret;
	
	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	public void setConnectAppBridgeId(String connectAppBridgeId) {
		this.connectAppBridgeId = connectAppBridgeId;
	}

	public void setConnectAppBridgeSecret(String connectAppBridgeSecret) {
		this.connectAppBridgeSecret = connectAppBridgeSecret;
	}

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		OAuthCode code = new OAuthCode();
		code.setClientId(connectAppBridgeId);
		code.setClientSecret(connectAppBridgeSecret);
		code.setCode(eventContext.getMessage().getProperty("code", PropertyScope.INVOCATION));
		
		SquareClient client = new SquareClient(null, apiUrl, null);
		
		OAuthToken token = client.oauth().obtainToken(code);
		
		return token;
	}
}
