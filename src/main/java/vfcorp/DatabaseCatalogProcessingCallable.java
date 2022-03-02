package vfcorp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.SquareClientV2;

import util.CloudStorageApi;
import util.TimeManager;

public class DatabaseCatalogProcessingCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(DatabaseCatalogProcessingCallable.class);

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

    @Value("${google.storage.bucket.archive}")
    private String storageBucket;
    @Value("${google.storage.account.credentials}")
    private String storageCredentials;

    /* We want to calculate all sales records for the same sale date -- most often "today".
     * We should always calculate for first time zone that has store locations open (EST).
     */
    private static String SALES_TIMEZONE = "America/New_York";

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        String brand = (String) message.getProperty("brand", PropertyScope.SESSION);
        String deploymentId = (String) message.getProperty("deploymentId", PropertyScope.SESSION);
        String retrieveCatalogLastModifiedHoursAgo = (String) message.getProperty("retrieveCatalogLastModifiedHoursAgo",
                PropertyScope.SESSION);
        int lookbackHours = Integer.parseInt(retrieveCatalogLastModifiedHoursAgo);

        String filename = deploymentId + "-catalog.json";

        // Retrieve a single deployment for credentials for master account
        VfcDeployment masterAccount = Util.getVfcDeploymentById(databaseUrl, databaseUser, databasePassword,
                deploymentId);

        SquareClientV2 client = new SquareClientV2(apiUrl,
                masterAccount.getSquarePayload().getAccessToken(encryptionKey));
        client.setLogInfo(masterAccount.getSquarePayload().getMerchantId());

        CloudStorageApi storageApi = new CloudStorageApi(storageCredentials);

        // Any negative number means "pull FULL current catalog"
        if (lookbackHours < 0) {
            CatalogObject[] items = client.catalog().listItems();

            if (items.length > 0) {
                Util.saveTmpFile(filename, new ArrayList<>(Arrays.asList(items)));
                Util.uploadBrandPluFileFromTmpToGCP(storageApi, storageBucket, brand, filename);
            }
        } else {
            String modifiedSinceDate = TimeManager.getIso8601HoursAgo(lookbackHours, SALES_TIMEZONE);

            // Download any CatalogObject item updates since last Item Catalog file cache
            CatalogObject[] modifiedItems = client.catalog().listUpdatedObjects(new String[] { "ITEM" },
                    modifiedSinceDate, true);

            System.out.println(String.format("\nTOTAL MODIFIED ITEMS PULLED FROM SQUARE CATALOG in last %s hours: %s",
                    lookbackHours, modifiedItems.length));

            // Download cached catalog from GCP
            ArrayList<CatalogObject> gcpCatalog = Util.downloadBrandCatalogFileFromGCP(storageApi, storageBucket, brand,
                    filename);
            HashMap<String, CatalogObject> modifiedCatalog = new HashMap<String, CatalogObject>();
            for (CatalogObject o : gcpCatalog) {
                modifiedCatalog.put(o.getId(), o);
            }
            System.out.println("\nCACHED GCP CATALOG SIZE: " + modifiedCatalog.values().size());

            // Merge updated items into the cached catalog
            for (CatalogObject modifiedItem : modifiedItems) {
                if (modifiedItem.isDeleted()) {
                    modifiedCatalog.remove(modifiedItem.getId());
                } else {
                    modifiedCatalog.put(modifiedItem.getId(), modifiedItem);
                }
            }

            // Re-upload to GCP'
            System.out.println("NEW CATALOG SIZE: " + modifiedCatalog.values().size());
            Util.saveTmpFile(filename, new ArrayList<>(modifiedCatalog.values()));
            Util.uploadBrandPluFileFromTmpToGCP(storageApi, storageBucket, brand, filename);
        }

        return 1;
    }
}
