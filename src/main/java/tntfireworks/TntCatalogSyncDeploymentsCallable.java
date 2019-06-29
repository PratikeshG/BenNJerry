package tntfireworks;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import util.DbConnection;
import util.SquarePayload;

public class TntCatalogSyncDeploymentsCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(TntCatalogSyncDeploymentsCallable.class);

    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}")
    private String databaseUrl;
    @Value("${mysql.user}")
    private String databaseUser;
    @Value("${mysql.password}")
    private String databasePassword;

    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.create();

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public void setDatabaseUser(String databaseUser) {
        this.databaseUser = databaseUser;
    }

    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();
        logger.info("Starting database to Square sync...");

        DbConnection dbConnection = new DbConnection(databaseUrl, databaseUser, databasePassword);
        TntDatabaseApi tntDatabaseApi = new TntDatabaseApi(dbConnection);
        List<SquarePayload> deploymentPayloads = setUpDeployments(tntDatabaseApi, message);
        tntDatabaseApi.close();

        return deploymentPayloads;
    }

    public List<SquarePayload> setUpDeployments(TntDatabaseApi tntDatabaseApi, MuleMessage message)
            throws SQLException {
        HashMap<String, String> locationMarketingPlanCache = generateLocationMarketingPlanCache(tntDatabaseApi);
        HashMap<String, List<CsvItem>> marketingPlanItemsCache = generateMarketingPlanItemsCache(tntDatabaseApi);
        List<SquarePayload> deploymentPayloads = getCatalogSyncDeploymentsFromDb(tntDatabaseApi);

        // set mule session variables
        setMuleLocationMarketingPlanCache(locationMarketingPlanCache, message);
        setMuleMarketingPlanItemCacheFromDb(marketingPlanItemsCache, message);

        return deploymentPayloads;
    }

    private void setMuleLocationMarketingPlanCache(HashMap<String, String> locationMarketingPlanCache,
            MuleMessage message) throws SQLException {
        message.setProperty("locationMarketingPlanCache", locationMarketingPlanCache, PropertyScope.SESSION);
    }

    private void setMuleMarketingPlanItemCacheFromDb(HashMap<String, List<CsvItem>> marketingPlanItemsCache,
            MuleMessage message) throws SQLException {
        message.setProperty("marketingPlanItemsCache", marketingPlanItemsCache, PropertyScope.SESSION);
        logMarketingPlans(marketingPlanItemsCache);
    }

    private List<SquarePayload> getCatalogSyncDeploymentsFromDb(TntDatabaseApi tntDatabaseApi) throws SQLException {
        // retrieve deployments from db where catalog sync is enabled
        String whereFilter = String.format("%s = 1", TntDatabaseApi.DB_DEPLOYMENT_ENABLE_CATALOG_SYNC_COLUMN);
        ArrayList<Map<String, String>> dbRows = tntDatabaseApi
                .submitQuery(tntDatabaseApi.generateDeploymentSQLSelect(whereFilter));

        return generateDeploymentPayloads(tntDatabaseApi, dbRows);
    }

    private void logMarketingPlans(HashMap<String, List<CsvItem>> marketingPlanItemsCache) {
        logger.debug("plans: " + marketingPlanItemsCache.keySet().size());
        for (String planId : marketingPlanItemsCache.keySet()) {
            logger.debug(planId + ": " + marketingPlanItemsCache.get(planId).size());
        }
    }

    public List<SquarePayload> generateDeploymentPayloads(TntDatabaseApi tntDatabaseApi,
            ArrayList<Map<String, String>> dbRows) throws SQLException {
        // create SquarePayloads from db deployments
        List<SquarePayload> deploymentPayloads = new ArrayList<SquarePayload>();
        for (Map<String, String> row : dbRows) {
            SquarePayload deploymentPayload = new SquarePayload();
            deploymentPayload.setEncryptedAccessToken(row.get("encryptedAccessToken"));
            deploymentPayload.setMerchantId(row.get("merchantId"));
            deploymentPayload.setMerchantAlias(row.get("merchantAlias"));
            deploymentPayloads.add(deploymentPayload);
        }

        return deploymentPayloads;
    }

    public HashMap<String, String> generateLocationMarketingPlanCache(TntDatabaseApi tntDatabaseApi)
            throws SQLException {
        ArrayList<Map<String, String>> rows = tntDatabaseApi.submitQuery(tntDatabaseApi.generateLocationSQLSelect());
        // columns retrieved: all
        logger.debug("generateLocationMarketingPlanCache=" + gson.toJson(rows));

        HashMap<String, String> locationMarketingPlanCache = new HashMap<String, String>();
        for (Map<String, String> row : rows) {
            locationMarketingPlanCache.put(row.get("locationNumber"), row.get("mktPlan"));
        }

        return locationMarketingPlanCache;
    }

    public HashMap<String, List<CsvItem>> generateMarketingPlanItemsCache(TntDatabaseApi tntDatabaseApi)
            throws SQLException {
        ArrayList<Map<String, String>> rows = tntDatabaseApi.submitQuery(tntDatabaseApi.generateItemSQLSelect());
        // get all items from db
        // columns retrieved: itemNumber, category, itemDescription,
        // upc, currency, halfOff, sellingPrice

        logger.debug("generateMarketingPlanItemsCache=" + gson.toJson(rows));

        HashMap<String, List<CsvItem>> marketingPlanItemsCache = new HashMap<String, List<CsvItem>>();
        for (Map<String, String> row : rows) {
            String mktPlan = row.get("mktPlan");
            List<CsvItem> itemList = marketingPlanItemsCache.get(mktPlan);

            if (itemList == null) {
                itemList = new ArrayList<CsvItem>();
                marketingPlanItemsCache.put(mktPlan, itemList);
            }

            CsvItem item = new CsvItem();
            item.setNumber(row.get("itemNumber"));
            item.setCategory(row.get("category"));
            item.setDescription(row.get("itemDescription"));
            item.setUPC(row.get("upc"));
            item.setMarketingPlan(row.get("mktPlan"));
            item.setCurrency(row.get("currency"));
            item.setHalfOff(row.get("halfOff"));
            item.setSellingPrice(row.get("sellingPrice"));
            itemList.add(item);
        }

        return marketingPlanItemsCache;
    }
}
