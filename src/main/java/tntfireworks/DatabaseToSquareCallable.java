package tntfireworks;

import java.util.HashMap;
import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.base.Preconditions;
import com.squareup.connect.v2.SquareClientV2;

import util.DbConnection;
import util.SquarePayload;

public class DatabaseToSquareCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(DatabaseToSquareCallable.class);

    @Value("${api.url}")
    private String apiUrl;
    @Value("${encryption.key.tokens}")
    private String encryptionKey;
    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}?autoReconnect=true")
    private String databaseUrl;
    @Value("${mysql.user}")
    private String databaseUser;
    @Value("${mysql.password}")
    private String databasePassword;

    private <T> T getSessionProperty(String propertyName, MuleMessage message) {
        return message.getProperty(propertyName, PropertyScope.SESSION);
    }

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        HashMap<String, String> locationMarketingPlanCache = getSessionProperty("locationMarketingPlanCache", message);
        HashMap<String, List<CsvItem>> marketingPlanItemsCache = getSessionProperty("marketingPlanItemsCache", message);
        HashMap<String, List<CsvInventoryAdjustment>> inventoryAdjustmentsCache = getSessionProperty(
                "inventoryAdjustmentsCache", message);
        String syncType = getSessionProperty("syncType", message);
        SquarePayload deployment = (SquarePayload) message.getPayload();

        if (syncType.equals(TntCatalogSyncDeploymentsCallable.SYNC_TYPE_CATALOG)) {
            // perform catalog pricing updates
            logger.info(String.format("Begin processing Catalog API updates for merchant token: %s",
                    deployment.getMerchantId()));
            synchronizeItemsAndCategoriesForDeployment(deployment, locationMarketingPlanCache, marketingPlanItemsCache);
            logger.info(String.format("Done processing Catalog API updates for merchant token: %s",
                    deployment.getMerchantId()));
        }

        if (syncType.equals(TntCatalogSyncDeploymentsCallable.SYNC_TYPE_INVENTORY)) {
            // perform inventory updates
            logger.info(String.format("Begin processing Inventory Adjustment updates for merchant token: %s",
                    deployment.getMerchantId()));

            // set up database connection to delete completed adjustment updates from DB
            DbConnection dbConnection = new DbConnection(databaseUrl, databaseUser, databasePassword);
            TntDatabaseApi tntDatabaseApi = new TntDatabaseApi(dbConnection);

            logger.info("Starting database to Square sync for syncType=" + syncType);
            synchronizeInventoryAdjustmentsForDeployment(deployment, inventoryAdjustmentsCache, tntDatabaseApi);

            // close DB connection
            tntDatabaseApi.close();

            logger.info(String.format("Done processing Inventory Adjustment updates for merchant token: %s",
                    deployment.getMerchantId()));
        }

        return deployment;
    }

    /*
     * Synchronizes items and categories between the Bridge DB and Square for a single Square account (e.g. deployment)
     *
     * @param deployment - SquarePayload object containing accessToken needed by Square API client
     * @param locationMarketingPlanCache - HashMap of Square Location to TNT Markting Plan
     * @param marketingPlanItemsCache - HashMap of TNT Marketing Plan to List of Items (in CsvItem format)
     *
     * This is done in three operations - batch upserting categories, batch upserting items, and finally removing items
     * not present at any location for this deployment
     */
    public void synchronizeItemsAndCategoriesForDeployment(SquarePayload deployment,
            HashMap<String, String> locationMarketingPlanCache,
            HashMap<String, List<CsvItem>> marketingPlanItemsCache) {
        Preconditions.checkNotNull(deployment);
        Preconditions.checkNotNull(locationMarketingPlanCache);
        Preconditions.checkNotNull(marketingPlanItemsCache);

        SquareClientV2 clientV2 = new SquareClientV2(apiUrl, deployment.getAccessToken(encryptionKey));
        clientV2.setLogInfo(deployment.getMerchantId());
        TntCatalogApi tntCatalogApi = new TntCatalogApi(clientV2, locationMarketingPlanCache, marketingPlanItemsCache);

        tntCatalogApi.batchUpsertCategoriesFromDatabaseToSquare();
        tntCatalogApi.batchUpsertItemsIntoCatalog();
        tntCatalogApi.removeItemsNotPresentAtAnyLocations();
    }

    public void synchronizeInventoryAdjustmentsForDeployment(SquarePayload deployment,
            HashMap<String, List<CsvInventoryAdjustment>> inventoryAdjustmentsCache, TntDatabaseApi tntDatabaseApi) {
        Preconditions.checkNotNull(deployment);
        Preconditions.checkNotNull(inventoryAdjustmentsCache);

        SquareClientV2 clientV2 = new SquareClientV2(apiUrl, deployment.getAccessToken(encryptionKey),
                TntInventoryApi.INVENTORY_API_VERSION);
        clientV2.setLogInfo(deployment.getMerchantId());
        TntInventoryApi tntInventoryApi = new TntInventoryApi(clientV2);
        tntInventoryApi.batchUpsertInventoryChangesFromDb(inventoryAdjustmentsCache, tntDatabaseApi);
    }
}
