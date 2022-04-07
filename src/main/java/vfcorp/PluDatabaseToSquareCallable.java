package vfcorp;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.v2.SquareClientV2;

public class PluDatabaseToSquareCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(PluDatabaseToSquareCallable.class);

    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}?autoReconnect=true")
    private String databaseUrl;
    @Value("${mysql.user}")
    private String databaseUser;
    @Value("${mysql.password}")
    private String databasePassword;
    @Value("${api.url}")
    private String apiUrl;
    @Value("${encryption.key.tokens}")
    private String encryptionKey;

    @Value("${google.storage.bucket.archive}")
    private String storageBucket;
    @Value("${google.storage.account.credentials}")
    private String storageCredentials;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        String deploymentId = (String) message.getProperty("deploymentId", PropertyScope.SESSION);
        String brand = (String) message.getProperty("brand", PropertyScope.SESSION);
        int itemNumberLookupLength = Integer
                .parseInt(message.getProperty("itemNumberLookupLength", PropertyScope.INVOCATION));

        boolean ignoresSkuCheckDigit = message.getProperty("ignoresSkuCheckDigit", PropertyScope.INVOCATION)
                .equals("true") ? true : false;

        // Retrieve a single deployment for credentials for master account
        VfcDeployment masterAccount = Util.getVfcDeploymentById(databaseUrl, databaseUser, databasePassword,
                deploymentId);

        SquareClientV2 client = new SquareClientV2(apiUrl,
                masterAccount.getSquarePayload().getAccessToken(encryptionKey));
        client.setLogInfo(masterAccount.getSquarePayload().getMerchantId());

        PluCatalogBuilder catalogBuilder = new PluCatalogBuilder(client, databaseUrl, databaseUser, databasePassword,
                storageBucket, storageCredentials, brand, deploymentId);
        catalogBuilder.setItemNumberLookupLength(itemNumberLookupLength);
        catalogBuilder.setIgnoresSkuCheckDigit(ignoresSkuCheckDigit);

        catalogBuilder.syncCategoriesFromDatabaseToSquare();
        catalogBuilder.syncItemsFromDatabaseToSquare();

        logger.info(String.format("[%s] :: Done updating brand account: %s", client.getLogInfo(), brand));

        return null;
    }
}
