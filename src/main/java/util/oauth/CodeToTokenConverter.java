package util.oauth;

import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.v2.ObtainTokenRequest;
import com.squareup.connect.v2.ObtainTokenResponse;
import com.squareup.connect.v2.SquareClientV2;

import util.SquarePayload;

public class CodeToTokenConverter {
    @Value("${api.url}")
    private String apiUrl;
    @Value("${connect.app.id}")
    private String connectAppId;
    @Value("${connect.app.secret}")
    private String connectAppSecret;
    @Value("${encryption.key.tokens}")
    private String encryptionKey;

    private static final String GRANT_TYPE = "authorization_code";
    private static final String API_VERSION = "2022-02-16";

    public Object onCall(Object eventContext) throws Exception {
        ObtainTokenRequest codeRequest = new ObtainTokenRequest();
        codeRequest.setClientId(connectAppId);
        codeRequest.setClientSecret(connectAppSecret);
       // codeRequest.setCode(eventContext.getMessage().getProperty("code", PropertyScope.INVOCATION));
        codeRequest.setGrantType(GRANT_TYPE);

        SquareClientV2 client = new SquareClientV2(apiUrl);
        client.setVersion(API_VERSION);

        ObtainTokenResponse tokenResponse = client.oauth().obtainToken(codeRequest);

        SquarePayload tokenEncryption = new SquarePayload();
        tokenEncryption.encryptAccessToken(tokenResponse.getAccessToken(), encryptionKey);
        tokenEncryption.encryptRefreshToken(tokenResponse.getRefreshToken(), encryptionKey);

//        eventContext.getMessage().setProperty("connectAppId", connectAppId, PropertyScope.INVOCATION);
//        eventContext.getMessage().setProperty("encryptedAccessToken", tokenEncryption.getEncryptedAccessToken(),PropertyScope.INVOCATION);
//        eventContext.getMessage().setProperty("encryptedRefreshToken", tokenEncryption.getEncryptedRefreshToken(),PropertyScope.INVOCATION);
//        eventContext.getMessage().setProperty("merchantId", tokenResponse.getMerchantId(), PropertyScope.INVOCATION);
//        eventContext.getMessage().setProperty("expiresAt", tokenResponse.getExpiresAt(), PropertyScope.INVOCATION);

        return true;
    }
}
