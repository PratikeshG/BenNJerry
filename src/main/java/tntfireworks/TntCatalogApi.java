package tntfireworks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.squareup.connect.v2.Catalog;
import com.squareup.connect.v2.CatalogItemVariation;
import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.Money;
import com.squareup.connect.v2.SquareClientV2;

public class TntCatalogApi {

    private static final String FIXED_PRICING = "FIXED_PRICING";
    private static final String CATEGORY = "CATEGORY";
    private static final String ITEM = "ITEM";
    private static final String[] BASE_CATEGORIES = new String[] { "ASSORTMENTS", "BASE FOUNTAINS",
            "CALIFORNIA FOUNTAINS", "CONE FOUNTAINS", "GROUND SPINNERS & CHASERS", "NOVELTIES", "SMOKE", "SPARKLERS",
            "PUNK", "FIRECRACKERS", "HELICOPTERS", "RELOADABLES", "MULTI-AERIALS", "SPECIAL AERIALS", "MISSILES",
            "PARACHUTES", "ROMAN CANDLES", "ROCKETS", "COUNTER CASES & DISPLAYS", "PROMOTIONAL", "SUB ASSESMBLIES",
            "MATERIALS", "TOYS" };

    private SquareClientV2 client;
    public HashMap<String, List<String>> marketingPlanLocationsCache;
    public HashMap<String, List<CsvItem>> marketingPlanItemsCache;
    public Catalog catalog;

    /*
     * Gets the full Catalog of items from Square and updates the local catalog instance var
     * 
     * Note: the primary keys for each CatalogObject type are as follows:
     *   Items: SKU
     *   Categories: Name
     *   Taxes: ID
     *   Discounts: Name
     *   Modifier Lists: Name
     *   
     * @return the catalog from Square
     */
    public Catalog retrieveCatalogFromSquare() {
        Preconditions.checkNotNull(client);

        logger.info("Retrieving catalog...");
        try {
            return client.catalog().retrieveCatalog(Catalog.PrimaryKey.SKU, Catalog.PrimaryKey.NAME,
                    Catalog.PrimaryKey.ID, Catalog.PrimaryKey.NAME, Catalog.PrimaryKey.NAME);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Square API call to catalog failed");
        }
    }

    /* 
     * Getter for local catalog instance var
     * 
     * @return local Catalog
     */
    public Catalog getLocalCatalog() {
        Preconditions.checkNotNull(catalog);

        return catalog;
    }

    public TntCatalogApi(SquareClientV2 client, HashMap<String, String> locationMarketingPlanCache,
            HashMap<String, List<CsvItem>> marketingPlanItemsCache) {
        Preconditions.checkNotNull(client);
        Preconditions.checkNotNull(locationMarketingPlanCache);
        Preconditions.checkNotNull(marketingPlanItemsCache);

        this.client = client;
        marketingPlanLocationsCache = generateMarketingPlanLocationsCache(locationMarketingPlanCache, client);
        this.marketingPlanItemsCache = marketingPlanItemsCache;

        catalog = retrieveCatalogFromSquare();
    }

    /*
     * Batch upserts CatalogItem objects found in the local DB into the Square Catalog
     * 
     * @return the updated Catalog
     */
    public Catalog batchUpsertItemsIntoCatalog() {
        Preconditions.checkNotNull(catalog);
        Preconditions.checkNotNull(client);
        Preconditions.checkNotNull(marketingPlanLocationsCache);
        Preconditions.checkNotNull(marketingPlanItemsCache);

        clearItemLocations(catalog);
        generateItemUpdates(marketingPlanLocationsCache, marketingPlanItemsCache, catalog);
        CatalogObject[] catalogObjects = catalog.getObjects();

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

    /*
     * Removes items in the Square Catalog when not present at any locations
     * 
     * This method does not currently use the Square batch delete operation
     * 
     * @return the updated catalog
     */
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

    /*
     * Upserts the categories found in the MySQL database into Square
     * 
     * The existing categories from Square are pulled and compared with the categories in the local DB.
     * If the category is present in the local DB but not in Square, the category will be loaded into Square.
     * Note: this operation does not remove categories in Square that are no longer in the local DB.
     * 
     * TODO: @wtsang - pull from DB instead of hard-coded categories
     * 
     * @return the updated catalog
     */
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

    /* 
     * Clears the catalog in Square
     * 
     * @return the updated catalog
     */
    public Catalog clearCatalog() {
        Preconditions.checkNotNull(catalog);
        Preconditions.checkNotNull(client);

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
        } else {
            throw new IllegalArgumentException("Missing category for itemNumber " + csvItem.getNumber());
        }
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
        if (marketingPlanItems != null && marketingPlanItems.size() > 0) {
            for (CsvItem updateItem : marketingPlanItems) {
                generateCatalogUpsertsForItem(updateItem, squareLocationIds, catalog, categories);
            }
        } else {
            throw new IllegalArgumentException("Empty marketing plan: " + marketingPlanId);
        }

    }

    private HashSet<String> getBaseTntCategoriesSet() {
        HashSet<String> categoriesSet = new HashSet<String>();

        for (String category : BASE_CATEGORIES) {
            categoriesSet.add(category);
        }

        return categoriesSet;
    }

    private String[] getTntFireworksCategories() {
        // get baseline categories
        HashSet<String> categoriesSet = getBaseTntCategoriesSet();

        // add any additional categories
        for (List<CsvItem> csvItemList : marketingPlanItemsCache.values()) {
            for (CsvItem item : csvItemList) {
                categoriesSet.add(item.getCategory());
            }
        }

        String[] categoriesArray = categoriesSet.toArray(new String[categoriesSet.size()]);
        return categoriesArray;
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
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Square API call to list locations failed");
        }

        for (Location location : locations) {
            addLocationToMarketingPlanLocationsCache(location, marketingPlanLocationsCache, locationMarketingPlanCache);
        }

        return marketingPlanLocationsCache;

    }

    private CatalogObject[] getAllCategories(Catalog catalog) {
        return catalog.getCategories().values().toArray(new CatalogObject[0]);
    }

}
