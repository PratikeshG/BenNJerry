package vfcorp;

import java.util.Arrays;
import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.SquareClientV2;

public class DatabaseItemsLocationsCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(DatabaseItemsLocationsCallable.class);

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

        String deploymentId = (String) message.getProperty("deploymentId", PropertyScope.SESSION);

        // Retrieve a single deployment for credentials for master account
        VfcDeployment masterAccount = Util.getVfcDeploymentById(databaseUrl, databaseUser, databasePassword,
                deploymentId);

        SquareClientV2 client = new SquareClientV2(apiUrl,
                masterAccount.getSquarePayload().getAccessToken(encryptionKey));
        client.setLogInfo(masterAccount.getSquarePayload().getMerchantId());

        message.setProperty("pluFiltered", masterAccount.isPluFiltered(), PropertyScope.SESSION);

        List<Location> locations = Arrays.asList(client.locations().list());
        return locations;
    }
}
