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
        String sessionCookie = null;

        for (String cookie : cookieArray) {
            if (cookie.startsWith("session=")) {
                sessionCookie = cookie.split("=")[1];
                break;
            }
        }

        Map<String, String> m = eventContext.getMessage().getProperty("http.query.params", PropertyScope.INBOUND);
        String deployment = m.get("state").split(",")[0];
        String alias = m.get("state").split(",")[1];
        String locationId = m.get("state").split(",")[2];
        String session = m.get("state").split(",")[3];
        String code = m.get("code");

        if (sessionCookie != null && sessionCookie.equals(session)) {
            eventContext.getMessage().setProperty("deployment", deployment, PropertyScope.INVOCATION);
            eventContext.getMessage().setProperty("locationId", locationId, PropertyScope.INVOCATION);
            eventContext.getMessage().setProperty("alias", alias, PropertyScope.INVOCATION);
            eventContext.getMessage().setProperty("code", code, PropertyScope.INVOCATION);
        }

        return null;
    }
}
