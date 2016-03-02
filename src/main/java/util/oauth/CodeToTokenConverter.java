package util.oauth;

import java.io.FileInputStream;
import java.util.Properties;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import com.squareup.connect.OAuthCode;
import com.squareup.connect.OAuthToken;
import com.squareup.connect.SquareClient;

public class CodeToTokenConverter implements Callable {

	private String apiUrl;
	
	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		OAuthCode code = new OAuthCode();
		
		String connectAppId = eventContext.getMessage().getProperty("connectAppId", PropertyScope.INVOCATION);
		Properties properties = new Properties();
		properties.load(new FileInputStream("src/main/resources/development.properties"));
		String secret = properties.getProperty("connect." + connectAppId + ".secret");
		
		code.setClientId(connectAppId);
		code.setClientSecret(secret);
		code.setCode(eventContext.getMessage().getProperty("code", PropertyScope.INVOCATION));
		
		SquareClient client = new SquareClient(apiUrl);
		
		OAuthToken token = client.oauth().obtainToken(code);
		
		return token;
	}
}
