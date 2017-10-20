package util.oauth;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.OAuthCode;
import com.squareup.connect.OAuthToken;
import com.squareup.connect.SquareClient;

import util.SquarePayload;

public class CodeToTokenConverter implements Callable {
    @Value("${api.url}")
    private String apiUrl;
    @Value("${connect.multiunit.id}")
    private String multiUnitAppId;
    @Value("${connect.multiunit.secret}")
    private String multiUnitAppSecret;
    @Value("${connect.legacy.id}")
    private String legacyAppId;
    @Value("${connect.legacy.secret}")
    private String legacyAppSecret;
    @Value("${encryption.key.tokens}")
    private String encryptionKey;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        // Information that needs to be passed:
        // deployment, connectApp, token, merchantId, legacy, expiryDate

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

        SquarePayload tokenEncryption = new SquarePayload();
        tokenEncryption.encryptAccessToken(token.getAccessToken(), encryptionKey);

        eventContext.getMessage().setProperty("encryptedAccessToken", tokenEncryption.getEncryptedAccessToken(),
                PropertyScope.INVOCATION);
        eventContext.getMessage().setProperty("merchantId", token.getMerchantId(), PropertyScope.INVOCATION);
        eventContext.getMessage().setProperty("expiresAt", token.getExpiresAt(), PropertyScope.INVOCATION);

        return true;
    }
}
