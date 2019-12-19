package util.oauth;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.OAuthURIParams;
import com.squareup.connect.SquareClient;

public class StateVariableGenerator implements Callable {
    @Value("${api.url}")
    private String apiUrl;
    @Value("${connect.legacy.id}")
    private String legacyAppId;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        SecureRandom random = new SecureRandom();
        String session = new BigInteger(130, random).toString(32);

        Map<String, String> m = eventContext.getMessage().getProperty("http.query.params", PropertyScope.INBOUND);
        String deployment = m.get("deployment");
        String connectAppId = m.get("client_id");

        OAuthURIParams params = new OAuthURIParams();
        params.setClientId(connectAppId);
        params.setResponseType("code");
        params.setState(deployment + "," + session + "," + connectAppId);
        params.setSession(false);

        if (connectAppId.equals(legacyAppId)) {
            params.setScope(new String[] { "MERCHANT_PROFILE_READ", "SETTLEMENTS_READ", "PAYMENTS_READ", "ITEMS_READ",
                    "ITEMS_WRITE" });
        } else {
            params.setScope(new String[] { "MERCHANT_PROFILE_READ", "SETTLEMENTS_READ", "PAYMENTS_READ", "ITEMS_READ",
                    "ITEMS_WRITE", "INVENTORY_READ", "INVENTORY_WRITE", "EMPLOYEES_READ", "TIMECARDS_READ",
                    "CUSTOMERS_READ", "CUSTOMERS_WRITE", "ORDERS_READ" });
        }

        SquareClient client = new SquareClient(apiUrl);
        String link = client.oauth().authorizeUrl(params);

        eventContext.getMessage().setProperty("session", session, PropertyScope.INVOCATION);
        eventContext.getMessage().setProperty("link", link, PropertyScope.INVOCATION);

        return null;
    }
}
