package util.oauth;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.v2.OAuthUriParams;
import com.squareup.connect.v2.SquareClientV2;

public class StateVariableGenerator {
    @Value("${api.url}")
    private String apiUrl;
    @Value("${connect.app.id}")
    private String connectAppId;

    public Object onCall() throws Exception {
        SecureRandom random = new SecureRandom();
        String session = new BigInteger(130, random).toString(32);

        Map<String, String> m = null;//eventContext.getMessage().getProperty("http.query.params", PropertyScope.INBOUND);
        String deployment = m.get("deployment");
        String[] scope = m.get("scope").split(" ");
        String alias = m.get("alias");
        String locationId = m.get("locationId");

        OAuthUriParams params = new OAuthUriParams();
        params.setClientId(connectAppId);
        params.setState(deployment + "," + alias + "," + locationId + "," + session);
        params.setSession(false);
        params.setScope(scope);

        SquareClientV2 client = new SquareClientV2(apiUrl);
        String link = client.oauth().authorizeUrl(params);

       // eventContext.getMessage().setProperty("session", session, PropertyScope.INVOCATION);
      //  eventContext.getMessage().setProperty("link", link, PropertyScope.INVOCATION);

        return null;
    }
}
