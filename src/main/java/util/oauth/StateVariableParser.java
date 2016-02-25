package util.oauth;

import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

public class StateVariableParser implements Callable {

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		String cookies = eventContext.getMessage().getProperty("Cookie", PropertyScope.INBOUND);
		String[] cookieArray = cookies.split("; ");
		String stateCookie = null;
		
		for (String cookie : cookieArray) {
			if (cookie.startsWith("state=")) {
				stateCookie = cookie.split("=")[1];
				break;
			}
		}
		
		Map<String,String> m = eventContext.getMessage().getProperty("http.query.params", PropertyScope.INBOUND);
		String deployment = m.get("state").split(",")[0];
		String state = m.get("state").split(",")[1];
		String code = m.get("code");
		
		if (stateCookie != null && stateCookie.equals(state)) {
			eventContext.getMessage().setProperty("deployment", deployment, PropertyScope.INVOCATION);
			eventContext.getMessage().setProperty("code", code, PropertyScope.INVOCATION);
		}
		
		return null;
	}
}
