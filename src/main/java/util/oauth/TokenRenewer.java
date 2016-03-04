package util.oauth;

import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import com.squareup.connect.OAuthToken;
import com.squareup.connect.SquareClient;

public class TokenRenewer implements Callable {

	private String apiUrl;
	
	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}
	
	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		@SuppressWarnings("unchecked")
		Map<String,Object> m = (Map<String, Object>) eventContext.getMessage().getPayload();
		
		Integer id = (Integer) m.get("id");
		String token = (String) m.get("token");
		String connectApp = (String) m.get("connectApp");
		String deployment = (String) m.get("deployment");
		
		Properties properties = new Properties();
		properties.load(new FileInputStream("src/main/resources/development.properties"));
		String secret = properties.getProperty("connect." + connectApp + ".secret");
		
		SquareClient client = new SquareClient(token, apiUrl);
		OAuthToken newToken = client.oauth().renewToken(connectApp, secret);
		
		eventContext.getMessage().setProperty("oldId", id, PropertyScope.INVOCATION);
		eventContext.getMessage().setProperty("deployment", deployment, PropertyScope.INVOCATION);
		eventContext.getMessage().setProperty("connectApp", connectApp, PropertyScope.INVOCATION);
		
		return newToken;
	}
}
