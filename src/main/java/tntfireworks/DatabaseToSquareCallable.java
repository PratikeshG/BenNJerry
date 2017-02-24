package tntfireworks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.squareup.connect.v2.Catalog;
import com.squareup.connect.v2.CatalogItemVariation;
import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.Money;
import com.squareup.connect.v2.SquareClientV2;

import util.SquareDeploymentCredentials;

public class DatabaseToSquareCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(DatabaseToSquareCallable.class);

    private String apiUrl;

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    private String[] getTntFireworksCategories() {
        // TODO(wtsang): generate dynamically by reading all possible values from DB
        return new String[] { "ASSORTMENTS", "BASE FOUNTAINS", "CALIFORNIA FOUNTAINS", "CONE FOUNTAINS",
                "GROUND SPINNERS & CHASERS", "NOVELTIES", "SMOKE", "SPARKLERS", "PUNK", "FIRECRACKERS", "HELICOPTERS",
                "RELOADABLES", "MULTI-AERIALS", "SPECIAL AERIALS", "MISSILES", "PARACHUTES", "ROMAN CANDLES", "ROCKETS",
                "COUNTER CASES & DISPLAYS", "PROMOTIONAL", "SUB ASSESMBLIES", "MATERIALS", "TOYS" };
    }

    private HashMap<String, List<String>> generateMarketingPlanLocationsCache(
            HashMap<String, String> locationMarketingPlanCache, SquareClientV2 client,
            SquareDeploymentCredentials deployment) {
        HashMap<String, List<String>> marketingPlanLocationsCache = new HashMap<String, List<String>>();

        Location[] locations;
        try {
            logger.info(
                    String.format("Processing Catalog API updates for merchant token: %s", deployment.getMerchantId()));
            locations = client.locations().list();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Square API call to list locations failed");
        }

        for (Location location : locations) {
            addLocationToMarketingPlanLocationsCache(location, marketingPlanLocationsCache, locationMarketingPlanCache);
        }
        return marketingPlanLocationsCache;

    }

    private void addLocationToMarketingPlanLocationsCache(Location location,
            HashMap<String, List<String>> marketingPlanLocationsCache,
            HashMap<String, String> locationMarketingPlanCache) {
        String locationSquareId = location.getId();
        String locationTNTId = getValueInParenthesis(location.getName());
        String marketingPlanId = locationMarketingPlanCache.get(locationTNTId);

        if (locationTNTId.length() < 1) {
            throw new IllegalArgumentException("Invalid location id ");
        }

        if (marketingPlanId == null) {
            throw new IllegalArgumentException("Invalid marketing plan id");
        }

        List<String> locationsList = marketingPlanLocationsCache.get(marketingPlanId);
        if (locationsList == null) {
            locationsList = new ArrayList<String>();
            marketingPlanLocationsCache.put(marketingPlanId, locationsList);
        }
        locationsList.add(locationSquareId);

    }

    private Catalog getCatalog(SquareClientV2 client) {
        logger.info("Retrieving catalog...");
        try {
            return client.catalog().retrieveCatalog(Catalog.PrimaryKey.SKU, Catalog.PrimaryKey.NAME,
                    Catalog.PrimaryKey.ID, Catalog.PrimaryKey.NAME, Catalog.PrimaryKey.NAME);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Square API call to catalog failed");
        }
    }

    private void upsertCategoriesFromDatabaseToSquare(Catalog catalog, SquareClientV2 client) {

        HashMap<String, CatalogObject> existingCategories = getCategoriesAsHashmapFromSquare(catalog);

        for (String categoryName : this.getTntFireworksCategories()) {
            CatalogObject category = existingCategories.get(categoryName);
            if (category == null) {
                addCategoryToLocalCatalog(catalog, categoryName);
            }
        }

        batchUpsertCategoriesToSquare(catalog, client);
    }

    private void batchUpsertCategoriesToSquare(Catalog catalog, SquareClientV2 client) {
        logger.info("Updating categories in catalog...");
        CatalogObject[] allCategories = getAllCategories(catalog);
        try {
            client.catalog().batchUpsertObjects(allCategories);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("failure to upsert categories");
        }
        logger.info("Done checking/adding categories");

    }

    private CatalogObject[] getAllCategories(Catalog catalog) {
        return catalog.getCategories().values().toArray(new CatalogObject[0]);
    }

    private void addCategoryToLocalCatalog(Catalog catalog, String categoryName) {
        CatalogObject newCategory = new CatalogObject("CATEGORY");
        newCategory.getCategoryData().setName(categoryName);
        catalog.addCategory(newCategory);
    }

    private void updateCatalogWithNewCategories(Catalog catalog, SquareClientV2 client) {
        upsertCategoriesFromDatabaseToSquare(catalog, client);
        logger.info("Retrieving updated catalog...");
        try {
            catalog = client.catalog().retrieveCatalog(Catalog.PrimaryKey.SKU, Catalog.PrimaryKey.NAME,
                    Catalog.PrimaryKey.ID, Catalog.PrimaryKey.NAME, Catalog.PrimaryKey.NAME);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("failure to retreive updated catalog");
        }

    }

    private void clearItemLocations(Catalog catalog) {
        catalog.clearItemLocations();
    }

    private HashMap<String, CatalogObject> getCategoriesAsHashmapFromSquare(Catalog catalog) {
        return (HashMap<String, CatalogObject>) catalog.getCategories();
    }

    private String[] getSquareLocationIds(HashMap<String, List<String>> marketingPlanLocationsCache,
            String marketingPlanId) {
        return marketingPlanLocationsCache.get(marketingPlanId).toArray(new String[0]);
    }

    private String getSku(CsvItem csvItem) {
        String sku = csvItem.getUPC();
        if (sku == null || sku.length() < 2) {
            sku = csvItem.getNumber();
        }
        return sku;
    }

    private CatalogObject getOrCreateSquareItem(Catalog catalog, String sku) {
        CatalogObject squareItem = catalog.getItem(sku);
        if (squareItem == null) {
            squareItem = new CatalogObject("ITEM");
        }
        return squareItem;
    }

    private CatalogItemVariation getFirstItemVariation(CatalogObject squareItem) {
        return squareItem.getItemData().getVariations()[0].getItemVariationData();
    }

    private void setSquareCategoryForItem(HashMap<String, CatalogObject> categories, CsvItem csvItem,
            CatalogObject squareItem) {
        // check for matching category, if found, add category id
        if (categories.containsKey(csvItem.getCategory())) {
            String categoryId = categories.get(csvItem.getCategory()).getId();
            squareItem.getItemData().setCategoryId(categoryId);
        } else
            throw new IllegalArgumentException("Missing category");
    }

    private void generateCatalogUpsertsForItem(CsvItem csvItem, String[] squareLocationIds, Catalog catalog,
            HashMap<String, CatalogObject> categories) {
        String sku = getSku(csvItem);

        CatalogObject squareItem = getOrCreateSquareItem(catalog, sku);

        squareItem.getItemData().setName(csvItem.getDescription());
        CatalogItemVariation squareItemVariation = getFirstItemVariation(squareItem);
        squareItemVariation.setSku(sku);
        squareItemVariation.setName(csvItem.getNumber());
        Money priceMoney = csvItem.getPriceAsSquareMoney();
        squareItemVariation.setPriceMoney(priceMoney);

        squareItem.enableAtLocations(squareLocationIds);
        squareItem.setLocationPriceOverride(squareLocationIds, squareItemVariation.getPriceMoney(), "FIXED_PRICING");
        setSquareCategoryForItem(categories, csvItem, squareItem);

        catalog.addItem(squareItem);
    }

    private void generateItemUpdatesForMarketingPlan(HashMap<String, List<CsvItem>> marketingPlanItemsCache,
            String marketingPlanId, HashMap<String, List<String>> marketingPlanLocationsCache, Catalog catalog) {
        HashMap<String, CatalogObject> categories = getCategoriesAsHashmapFromSquare(catalog);
        String[] squareLocationIds = getSquareLocationIds(marketingPlanLocationsCache, marketingPlanId);

        List<CsvItem> marketingPlanItems = marketingPlanItemsCache.get(marketingPlanId);
        if (marketingPlanItems != null) {
            for (CsvItem updateItem : marketingPlanItems) {
                generateCatalogUpsertsForItem(updateItem, squareLocationIds, catalog, categories);
            }
        }

    }

    private void generateItemUpdates(HashMap<String, List<String>> marketingPlanLocationsCache,
            HashMap<String, List<CsvItem>> marketingPlanItemsCache, Catalog catalog) {
        for (String marketingPlanId : marketingPlanLocationsCache.keySet()) {
            generateItemUpdatesForMarketingPlan(marketingPlanItemsCache, marketingPlanId, marketingPlanLocationsCache,
                    catalog);
        }
    }

    private void batchUpsertItemsIntoCatalog(Catalog catalog, SquareClientV2 client) {
        try {
            logger.info("Upsert latest catalog of items...");
            client.catalog().batchUpsertObjects(catalog.getObjects());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failure upserting items into catalog");
        }
    }

    private void removeItemsNotPresentAtAnyLocations(Catalog catalog, SquareClientV2 client) {
        for (String key : catalog.getItems().keySet()) {
            CatalogObject item = catalog.getItem(key);
            deleteItem(item, client);
        }
    }

    private void deleteItem(CatalogObject item, SquareClientV2 client) {
        if (item.getPresentAtLocationIds() == null || item.getPresentAtLocationIds().length == 0) {
            logger.info(String.format("Delete this catalog object name/token %s/%s:", item.getItemData().getName(),
                    item.getId()));
            try {
                client.catalog().deleteObject(item.getId());
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failure deleting unused items");
            }
        }

    }

    private void syncronizeItemsAndCategoriesForDeployment(SquareDeploymentCredentials deployment,
            HashMap<String, String> locationMarketingPlanCache,
            HashMap<String, List<CsvItem>> marketingPlanItemsCache) {
        SquareClientV2 client = new SquareClientV2(apiUrl, deployment.getAccessToken());

        String merchantToken = deployment.getMerchantId();
        logger.info(String.format("Begin processing Catalog API updates for merchant token: %s", merchantToken));

        HashMap<String, List<String>> marketingPlanLocationsCache = generateMarketingPlanLocationsCache(
                locationMarketingPlanCache, client, deployment);

        Catalog catalog = getCatalog(client);

        updateCatalogWithNewCategories(catalog, client);
        clearItemLocations(catalog);
        generateItemUpdates(marketingPlanLocationsCache, marketingPlanItemsCache, catalog);
        batchUpsertItemsIntoCatalog(catalog, client);
        removeItemsNotPresentAtAnyLocations(catalog, client);

        logger.info(String.format("Done processing Catalog API updates for merchant token: %s", merchantToken));
    }

    private <T> T getSessionProperty(String propertyName, MuleMessage message) {
        return message.getProperty(propertyName, PropertyScope.SESSION);
    }

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        HashMap<String, String> locationMarketingPlanCache = getSessionProperty("locationMarketingPlanCache", message);
        HashMap<String, List<CsvItem>> marketingPlanItemsCache = getSessionProperty("marketingPlanItemsCache", message);
        SquareDeploymentCredentials deployment = (SquareDeploymentCredentials) message.getPayload();

        syncronizeItemsAndCategoriesForDeployment(deployment, locationMarketingPlanCache, marketingPlanItemsCache);

        return null;
    }

    private static String getValueInParenthesis(String input) {
        Preconditions.checkNotNull(input);

        int firstIndex = input.indexOf('(');
        int lastIndex = input.indexOf(')');
        if (firstIndex > -1 && lastIndex > -1) {
            String value = input.substring(firstIndex + 1, lastIndex);
            return value.replaceAll("#", "");
        } else {
            throw new IllegalArgumentException("Does not contain ( or )");
        }

    }

}