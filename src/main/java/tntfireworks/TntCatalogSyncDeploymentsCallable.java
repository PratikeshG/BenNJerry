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
        HashMap<String, List<CsvInventoryAdjustment>> inventoryAdjustmentsCache = generateInventoryAdjustmentsCache(
                tntDatabaseApi);
        List<SquarePayload> deploymentPayloads = getCatalogSyncDeploymentsFromDb(tntDatabaseApi);

        // set mule session variables
        setMuleLocationMarketingPlanCache(locationMarketingPlanCache, message);
        setMuleMarketingPlanItemCacheFromDb(marketingPlanItemsCache, message);
        setMuleInventoryAdjustmentsCache(inventoryAdjustmentsCache, message);

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

    private void setMuleInventoryAdjustmentsCache(
            HashMap<String, List<CsvInventoryAdjustment>> inventoryAdjustmentsCache, MuleMessage message)
            throws SQLException {
        message.setProperty("inventoryAdjustmentsCache", inventoryAdjustmentsCache, PropertyScope.SESSION);
        logInventoryAdjustments(inventoryAdjustmentsCache);
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

    private void logInventoryAdjustments(HashMap<String, List<CsvInventoryAdjustment>> inventoryAdjustmentsCache) {
        logger.debug("Number of locations: " + inventoryAdjustmentsCache.keySet().size());
        for (String locationNum : inventoryAdjustmentsCache.keySet()) {
            logger.debug(locationNum + ": " + inventoryAdjustmentsCache.get(locationNum).size() + " # of adjustments");
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
            locationMarketingPlanCache.put(row.get(TntDatabaseApi.DB_LOCATION_LOCATION_NUMBER_COLUMN),
                    row.get(TntDatabaseApi.DB_LOCATION_MKT_PLAN_COLUMN));
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
            String mktPlan = row.get(TntDatabaseApi.DB_MKT_PLAN_NAME_COLUMN);
            List<CsvItem> itemList = marketingPlanItemsCache.get(mktPlan);

            if (itemList == null) {
                itemList = new ArrayList<CsvItem>();
                marketingPlanItemsCache.put(mktPlan, itemList);
            }

            CsvItem item = new CsvItem();
            item.setNumber(row.get(TntDatabaseApi.DB_MKT_PLAN_ITEM_NUMBER_COLUMN));
            item.setCategory(row.get(TntDatabaseApi.DB_MKT_PLAN_CATEGORY_COLUMN));
            item.setDescription(row.get(TntDatabaseApi.DB_MKT_PLAN_ITEM_DESCRIPTION_COLUMN));
            item.setUPC(row.get(TntDatabaseApi.DB_MKT_PLAN_UPC_COLUMN));
            item.setMarketingPlan(row.get(TntDatabaseApi.DB_MKT_PLAN_NAME_COLUMN));
            item.setCurrency(row.get(TntDatabaseApi.DB_MKT_PLAN_CURRENCY_COLUMN));
            item.setHalfOff(row.get(TntDatabaseApi.DB_MKT_PLAN_HALF_OFF_COLUMN));
            item.setSellingPrice(row.get(TntDatabaseApi.DB_MKT_PLAN_SELLING_PRICE_COLUMN));
            itemList.add(item);
        }

        return marketingPlanItemsCache;
    }

    public HashMap<String, List<CsvInventoryAdjustment>> generateInventoryAdjustmentsCache(
            TntDatabaseApi tntDatabaseApi) {
        ArrayList<Map<String, String>> rows = tntDatabaseApi
                .submitQuery(tntDatabaseApi.generateInventoryAdjustmentSQLSelect());
        logger.debug("generateInventoryAdjustmentsCache=" + gson.toJson(rows));

        // store inventory adjustment changes per location number
        HashMap<String, List<CsvInventoryAdjustment>> inventoryAdjustmentsCache = new HashMap<String, List<CsvInventoryAdjustment>>();
        for (Map<String, String> row : rows) {
            String locationNum = row.get(tntDatabaseApi.DB_INVENTORY_LOCATION_NUM_COLUMN);

            // retrieve adjustment list for location
            List<CsvInventoryAdjustment> adjustments = inventoryAdjustmentsCache.get(locationNum);

            // if null, initialize new list of adjustments
            if (adjustments == null) {
                adjustments = new ArrayList<CsvInventoryAdjustment>();
                inventoryAdjustmentsCache.put(locationNum, adjustments);
            }

            // create and initialize CsvInventoryAdjustment object from DB values
            CsvInventoryAdjustment adjustment = new CsvInventoryAdjustment();
            adjustment.setId(row.get(tntDatabaseApi.DB_INVENTORY_ID_COLUMN));
            adjustment.setLocationNum(row.get(tntDatabaseApi.DB_INVENTORY_LOCATION_NUM_COLUMN));
            adjustment.setItemNum(row.get(tntDatabaseApi.DB_INVENTORY_ITEM_NUM_COLUMN));
            adjustment.setDescription(row.get(tntDatabaseApi.DB_INVENTORY_ITEM_DESCRIPTION_COLUMN));
            adjustment.setUpc(row.get(tntDatabaseApi.DB_INVENTORY_UPC_COLUMN));
            adjustment.setQtyAdj(row.get(tntDatabaseApi.DB_INVENTORY_QTY_ADJUSTMENT_COLUMN));
            adjustment.setQtyReset(row.get(tntDatabaseApi.DB_INVENTORY_QTY_RESET_COLUMN));
            adjustment.setReset(row.get(tntDatabaseApi.DB_INVENTORY_RESET_COLUMN));
            adjustment.setChangeDate(row.get(tntDatabaseApi.DB_INVENTORY_CHANGE_DATE_COLUMN));
            adjustments.add(adjustment);
        }

        return inventoryAdjustmentsCache;
    }
}
