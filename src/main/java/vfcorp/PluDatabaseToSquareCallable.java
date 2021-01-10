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

    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}")
    private String databaseUrl;
    @Value("${mysql.user}")
    private String databaseUser;
    @Value("${mysql.password}")
    private String databasePassword;
    @Value("${api.url}")
    private String apiUrl;
    @Value("${encryption.key.tokens}")
    private String encryptionKey;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        String brand = (String) message.getProperty("brand", PropertyScope.SESSION);
        int itemNumberLookupLength = Integer
                .parseInt(message.getProperty("itemNumberLookupLength", PropertyScope.INVOCATION));

        boolean ignoresSkuCheckDigit = message.getProperty("ignoresSkuCheckDigit", PropertyScope.INVOCATION)
                .equals("true") ? true : false;

        // Retrieve a single deployment for credentials for master account
        VfcDeployment masterAccount = Util.getMasterAccountDeployment(databaseUrl, databaseUser, databasePassword,
                brand);

        SquareClientV2 client = new SquareClientV2(apiUrl,
                masterAccount.getSquarePayload().getAccessToken(encryptionKey));
        client.setLogInfo(masterAccount.getSquarePayload().getMerchantId());

        PluCatalogBuilder catalogBuilder = new PluCatalogBuilder(client, databaseUrl, databaseUser, databasePassword,
                brand);
        catalogBuilder.setItemNumberLookupLength(itemNumberLookupLength);
        catalogBuilder.setPluFiltered(masterAccount.isPluFiltered());
        catalogBuilder.setIgnoresSkuCheckDigit(ignoresSkuCheckDigit);

        catalogBuilder.syncCategoriesFromDatabaseToSquare();
        catalogBuilder.syncItemsFromDatabaseToSquare();

        logger.info(String.format("Done updating brand account: %s", brand));

        return null;
    }
}
