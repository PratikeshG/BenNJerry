package tntfireworks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.connect.v2.Catalog;
import com.squareup.connect.v2.CatalogItemVariation;
import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.Money;
import com.squareup.connect.v2.SquareClientV2;

public class TntCatalogApi {

    // constants
    private static final String FIXED_PRICING = "FIXED_PRICING";
    private static final String CATEGORY = "CATEGORY";
    private static final String ITEM = "ITEM";

    // instance vars
    private SquareClientV2 client;
    public HashMap<String, List<String>> marketingPlanLocationsCache;
    public HashMap<String, List<CsvItem>> marketingPlanItemsCache;
    public Catalog catalog;

    // constructor
    public TntCatalogApi(SquareClientV2 client, HashMap<String, String> locationMarketingPlanCache,
            HashMap<String, List<CsvItem>> marketingPlanItemsCache) {
        Preconditions.checkNotNull(client);
        Preconditions.checkNotNull(locationMarketingPlanCache);
        Preconditions.checkNotNull(marketingPlanItemsCache);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        System.out.println("locationMarketingPlanCache:");
        System.out.println(gson.toJson(locationMarketingPlanCache));
        System.out.println("marketingPlanItemsCache");
        System.out.println(gson.toJson(marketingPlanItemsCache));

        this.client = client;
        marketingPlanLocationsCache = generateMarketingPlanLocationsCache(locationMarketingPlanCache, client);
        this.marketingPlanItemsCache = marketingPlanItemsCache;

        catalog = retrieveCatalogFromSquare();
    }

    // public methods
    public Catalog batchUpsertItemsIntoCatalog() {
        Preconditions.checkNotNull(catalog);
        Preconditions.checkNotNull(client);
        Preconditions.checkNotNull(marketingPlanLocationsCache);
        Preconditions.checkNotNull(marketingPlanItemsCache);

        clearItemLocations(catalog);
        generateItemUpdates(marketingPlanLocationsCache, marketingPlanItemsCache, catalog);
        System.out.println("ItemsToInsertJson");
        CatalogObject[] catalogObjects = catalog.getObjects();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        System.out.println(gson.toJson(catalogObjects));
        try {
            logger.info("Upsert latest catalog of items...");
            client.catalog().batchUpsertObjects(catalogObjects);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failure upserting items into catalog");
        }
        catalog = retrieveCatalogFromSquare();
        return catalog;
    }

    public Catalog removeItemsNotPresentAtAnyLocations() {
        Preconditions.checkNotNull(catalog);
        Preconditions.checkNotNull(client);
        Preconditions.checkNotNull(marketingPlanLocationsCache);
        Preconditions.checkNotNull(marketingPlanItemsCache);

        for (String key : catalog.getItems().keySet()) {
            CatalogObject item = catalog.getItem(key);
            deleteItemIfNotPresentAtAnyLocation(item);
        }
        catalog = retrieveCatalogFromSquare();
        return catalog;
    }

    public Catalog batchUpsertCategoriesFromDatabaseToSquare() {
        Preconditions.checkNotNull(catalog);
        Preconditions.checkNotNull(client);
        Preconditions.checkNotNull(marketingPlanLocationsCache);
        Preconditions.checkNotNull(marketingPlanItemsCache);

        String[] categories = getTntFireworksCategories();

        Map<String, CatalogObject> existingCategories = getCategoriesAsHashmapFromSquare(catalog);

        for (String categoryName : categories) {
            CatalogObject category = existingCategories.get(categoryName);
            if (category == null) {
                addCategoryToLocalCatalog(catalog, categoryName);
            }
        }

        batchUpsertCategoriesToSquare(catalog, client);
        catalog = retrieveCatalogFromSquare();
        return catalog;
    }

    public Catalog clearCatalog() {

        for (CatalogObject catalogObject : catalog.getObjects()) {
            try {
                client.catalog().deleteObject(catalogObject.getId());
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("failure to delete catalog object " + catalogObject.getId());
            }
        }

        catalog = retrieveCatalogFromSquare();
        return catalog;
    }

    private Catalog clearItemLocations(Catalog catalog) {
        catalog.clearItemLocations();
        return catalog;
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
            squareItem = new CatalogObject(ITEM);
        }
        return squareItem;
    }

    private CatalogItemVariation getFirstItemVariation(CatalogObject squareItem) {
        return squareItem.getItemData().getVariations()[0].getItemVariationData();
    }

    private void setSquareCategoryForItem(Map<String, CatalogObject> categories, CsvItem csvItem,
            CatalogObject squareItem) {
        // check for matching category, if found, add category id
        if (categories.containsKey(csvItem.getCategory())) {
            String categoryId = categories.get(csvItem.getCategory()).getId();
            squareItem.getItemData().setCategoryId(categoryId);
        } else
            throw new IllegalArgumentException("Missing category for itemNumber " + csvItem.getNumber());
    }

    private void generateCatalogUpsertsForItem(CsvItem csvItem, String[] squareLocationIds, Catalog catalog,
            Map<String, CatalogObject> categories) {
        String sku = getSku(csvItem);

        CatalogObject squareItem = getOrCreateSquareItem(catalog, sku);

        squareItem.getItemData().setName(csvItem.getDescription());
        CatalogItemVariation squareItemVariation = getFirstItemVariation(squareItem);
        squareItemVariation.setSku(sku);
        squareItemVariation.setName(csvItem.getNumber());
        Money priceMoney = csvItem.getPriceAsSquareMoney();
        squareItemVariation.setPriceMoney(priceMoney);

        squareItem.enableAtLocations(squareLocationIds);
        squareItem.setLocationPriceOverride(squareLocationIds, squareItemVariation.getPriceMoney(), FIXED_PRICING);
        setSquareCategoryForItem(categories, csvItem, squareItem);

        catalog.addItem(squareItem);
    }

    private void generateItemUpdatesForMarketingPlan(HashMap<String, List<CsvItem>> marketingPlanItemsCache,
            String marketingPlanId, HashMap<String, List<String>> marketingPlanLocationsCache, Catalog catalog) {
        Map<String, CatalogObject> categories = getCategoriesAsHashmapFromSquare(catalog);
        String[] squareLocationIds = getSquareLocationIds(marketingPlanLocationsCache, marketingPlanId);

        List<CsvItem> marketingPlanItems = marketingPlanItemsCache.get(marketingPlanId);
        if (marketingPlanItems != null) {
            for (CsvItem updateItem : marketingPlanItems) {
                generateCatalogUpsertsForItem(updateItem, squareLocationIds, catalog, categories);
            }
        }

    }

    private String[] getTntFireworksCategories() {
        // TODO(wtsang): generate dynamically by reading all possible values from DB
        return new String[] { "ASSORTMENTS", "BASE FOUNTAINS", "CALIFORNIA FOUNTAINS", "CONE FOUNTAINS",
                "GROUND SPINNERS & CHASERS", "NOVELTIES", "SMOKE", "SPARKLERS", "PUNK", "FIRECRACKERS", "HELICOPTERS",
                "RELOADABLES", "MULTI-AERIALS", "SPECIAL AERIALS", "MISSILES", "PARACHUTES", "ROMAN CANDLES", "ROCKETS",
                "COUNTER CASES & DISPLAYS", "PROMOTIONAL", "SUB ASSESMBLIES", "MATERIALS", "TOYS" };
    }

    private void generateItemUpdates(HashMap<String, List<String>> marketingPlanLocationsCache,
            HashMap<String, List<CsvItem>> marketingPlanItemsCache, Catalog catalog) {
        for (String marketingPlanId : marketingPlanLocationsCache.keySet()) {
            generateItemUpdatesForMarketingPlan(marketingPlanItemsCache, marketingPlanId, marketingPlanLocationsCache,
                    catalog);
        }
    }

    private void deleteItemIfNotPresentAtAnyLocation(CatalogObject item) {
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

    private static Logger logger = LoggerFactory.getLogger(TntCatalogApi.class);

    private Map<String, CatalogObject> getCategoriesAsHashmapFromSquare(Catalog catalog) {
        return catalog.getCategories();
    }

    private void addCategoryToLocalCatalog(Catalog catalog, String categoryName) {
        CatalogObject newCategory = new CatalogObject(CATEGORY);
        newCategory.getCategoryData().setName(categoryName);
        catalog.addCategory(newCategory);
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

    private static String getValueInParenthesisAndStripHashtag(String input) {
        Preconditions.checkArgument(input.contains("("));
        Preconditions.checkArgument(input.contains(")"));
        Preconditions.checkArgument(input.indexOf("(") < input.indexOf(")"));

        int firstIndex = input.indexOf('(');
        int lastIndex = input.indexOf(')');
        String value = input.substring(firstIndex + 1, lastIndex);
        return value.replaceAll("#", "");
    }

    private void addLocationToMarketingPlanLocationsCache(Location location,
            HashMap<String, List<String>> marketingPlanLocationsCache,
            HashMap<String, String> locationMarketingPlanCache) {
        String locationSquareId = location.getId();
        String locationTNTId = getValueInParenthesisAndStripHashtag(location.getName());
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

    private HashMap<String, List<String>> generateMarketingPlanLocationsCache(
            HashMap<String, String> locationMarketingPlanCache, SquareClientV2 client) {
        HashMap<String, List<String>> marketingPlanLocationsCache = new HashMap<String, List<String>>();

        Location[] locations;
        try {
            logger.info("Processing Catalog API updates");
            locations = client.locations().list();

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();

            System.out.println(gson.toJson(locations));

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Square API call to list locations failed");
        }

        for (Location location : locations) {
            addLocationToMarketingPlanLocationsCache(location, marketingPlanLocationsCache, locationMarketingPlanCache);
        }

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        System.out.println(gson.toJson(marketingPlanLocationsCache));

        return marketingPlanLocationsCache;

    }

    private CatalogObject[] getAllCategories(Catalog catalog) {
        return catalog.getCategories().values().toArray(new CatalogObject[0]);
    }

    public Catalog retrieveCatalogFromSquare() {
        logger.info("Retrieving catalog...");
        try {
            return client.catalog().retrieveCatalog(Catalog.PrimaryKey.SKU, Catalog.PrimaryKey.NAME,
                    Catalog.PrimaryKey.ID, Catalog.PrimaryKey.NAME, Catalog.PrimaryKey.NAME);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Square API call to catalog failed");
        }
    }

    public Catalog getLocalCatalog() {
        return catalog;
    }

}
