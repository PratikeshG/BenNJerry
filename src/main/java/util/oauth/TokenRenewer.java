package util.oauth;

import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.OAuthToken;
import com.squareup.connect.SquareClient;

import util.SquarePayload;

public class TokenRenewer implements Callable {
    @Value("${api.url}")
    private String apiUrl;
    @Value("${connect.multiunit.id}")
    private String multiunitId;
    @Value("${connect.multiunit.secret}")
    private String multiunitSecret;
    @Value("${connect.legacy.id}")
    private String legacyId;
    @Value("${connect.legacy.secret}")
    private String legacySecret;
    @Value("${encryption.key.tokens}")
    private String encryptionKey;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> m = (Map<String, Object>) eventContext.getMessage().getPayload();

        Integer id = (Integer) m.get("id");
        String encryptedAccessToken = (String) m.get("encryptedAccessToken");
        String connectApp = (String) m.get("connectApp");

        String secret = "";
        if (connectApp != null && connectApp.equals(multiunitId)) {
            secret = multiunitSecret;
        } else if (connectApp != null && connectApp.equals(legacyId)) {
            secret = legacySecret;
        } else {
            throw new Exception("connect app '" + connectApp
                    + "' associated with this token is not an official Managed Integrations application");
        }

        SquarePayload tokenEncryption = new SquarePayload();
        tokenEncryption.setEncryptedAccessToken(encryptedAccessToken);

        SquareClient client = new SquareClient(tokenEncryption.getAccessToken(encryptionKey), apiUrl);
        OAuthToken newToken = client.oauth().renewToken(connectApp, secret);

        tokenEncryption.encryptAccessToken(newToken.getAccessToken(), encryptionKey);

        eventContext.getMessage().setProperty("tokenId", id, PropertyScope.INVOCATION);
        eventContext.getMessage().setProperty("encryptedAccessToken", tokenEncryption.getEncryptedAccessToken(),
                PropertyScope.INVOCATION);
        eventContext.getMessage().setProperty("expiresAt", newToken.getExpiresAt(), PropertyScope.INVOCATION);

        return true;
    }
}
