package util.oauth;

import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.v2.ObtainTokenRequest;
import com.squareup.connect.v2.ObtainTokenResponse;
import com.squareup.connect.v2.SquareClientV2;

import util.SquarePayload;

public class TokenRenewer implements Callable {
    @Value("${api.url}")
    private String apiUrl;
    @Value("${connect.app.id}")
    private String connectAppId;
    @Value("${connect.app.secret}")
    private String connectAppSecret;
    @Value("${encryption.key.tokens}")
    private String encryptionKey;

    private static final String GRANT_TYPE = "refresh_token";
    private static final String API_VERSION = "2022-02-16";

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> m = (Map<String, Object>) eventContext.getMessage().getPayload();

        Integer id = (Integer) m.get("id");
        String encryptedRefreshToken = (String) m.get("encryptedRefreshToken");

        SquarePayload tokenEncryption = new SquarePayload();
        tokenEncryption.setEncryptedRefreshToken(encryptedRefreshToken);

        SquareClientV2 client = new SquareClientV2(apiUrl);
        client.setVersion(API_VERSION);

        try {
            ObtainTokenRequest codeRequest = new ObtainTokenRequest();
            codeRequest.setClientId(connectAppId);
            codeRequest.setClientSecret(connectAppSecret);
            codeRequest.setGrantType(GRANT_TYPE);
            codeRequest.setRefreshToken(tokenEncryption.getRefreshToken(encryptionKey));

            ObtainTokenResponse tokenResponse = client.oauth().obtainToken(codeRequest);

            if (tokenResponse.getAccessToken() != null && tokenResponse.getAccessToken().length() > 0) {
                tokenEncryption.encryptAccessToken(tokenResponse.getAccessToken(), encryptionKey);

                eventContext.getMessage().setProperty("tokenId", id, PropertyScope.INVOCATION);
                eventContext.getMessage().setProperty("encryptedAccessToken", tokenEncryption.getEncryptedAccessToken(),
                        PropertyScope.INVOCATION);
                eventContext.getMessage().setProperty("expiresAt", tokenResponse.getExpiresAt(),
                        PropertyScope.INVOCATION);

                return true;
            }
        } catch (Exception e) {
            eventContext.getMessage().setProperty("error", e.getMessage(), PropertyScope.INVOCATION);
        }

        return false;
    }
}
