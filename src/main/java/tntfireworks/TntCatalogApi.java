package tntfireworks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private static Logger logger = LoggerFactory.getLogger(TntCatalogApi.class);
    private static final int BATCH_UPSERT_SIZE = 5;
    private static final String FIXED_PRICING = "FIXED_PRICING";
    private static final String CATEGORY = "CATEGORY";
    private static final String ITEM = "ITEM";

    // merchant location names to ignore when checking for valid names
    //     - Default master location that is created to store bank account information but not take payments
    //       does not follow TNT nicknaming convention and needs to be captured here to ignore later
    //     - Deactivated locations exist within master accounts and need to be ignored
    private static final String INACTIVE_LOCATION = "DEACTIVATED";
    private static final String DEFAULT_LOCATION = "DEFAULT";
    private SquareClientV2 clientV2;
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
        Preconditions.checkNotNull(clientV2);

        logger.info(logString("Retrieving catalog..."));
        try {
            Catalog sourceCatalog = clientV2.catalog().retrieveCatalog(Catalog.PrimaryKey.SKU, Catalog.PrimaryKey.NAME,
                    Catalog.PrimaryKey.ID, Catalog.PrimaryKey.NAME, Catalog.PrimaryKey.NAME);

            Catalog workingCatalog = new Catalog(sourceCatalog, Catalog.PrimaryKey.SKU, Catalog.PrimaryKey.NAME,
                    Catalog.PrimaryKey.ID, Catalog.PrimaryKey.NAME, Catalog.PrimaryKey.NAME);

            return workingCatalog;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(logString("Square API call to catalog failed"), e);
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

    public TntCatalogApi(SquareClientV2 clientV2, HashMap<String, String> locationMarketingPlanCache,
            HashMap<String, List<CsvItem>> marketingPlanItemsCache) {

        Preconditions.checkNotNull(clientV2);
        Preconditions.checkNotNull(locationMarketingPlanCache);
        Preconditions.checkNotNull(marketingPlanItemsCache);

        this.clientV2 = clientV2;
        this.marketingPlanLocationsCache = generateMarketingPlanLocationsCache(locationMarketingPlanCache, clientV2);
        this.marketingPlanItemsCache = marketingPlanItemsCache;
        catalog = retrieveCatalogFromSquare();
    }

    public TntCatalogApi(SquareClientV2 clientV2) {
        Preconditions.checkNotNull(clientV2);

        this.clientV2 = clientV2;
        catalog = retrieveCatalogFromSquare();
    }

    /*
     * Batch upserts CatalogItem objects found in the local DB into the Square Catalog
     *
     * @return the updated Catalog
     */
    public Catalog batchUpsertItemsIntoCatalog() {
        Preconditions.checkNotNull(catalog);
        Preconditions.checkNotNull(clientV2);
        Preconditions.checkNotNull(marketingPlanLocationsCache);
        Preconditions.checkNotNull(marketingPlanItemsCache);

        clearManagedItemLocations(catalog);
        generateItemUpdates(marketingPlanLocationsCache, marketingPlanItemsCache, catalog);

        logCatalogStats(catalog);

        logger.info(logString("TOTAL ITEMS IN CATALOG: " + catalog.getItems().values().size()));
        CatalogObject[] modifiedItems = catalog.getModifiedItems();
        logger.info(logString("TOTAL MODIFIED ITEMS IN CATALOG: " + modifiedItems.length));

        try {
            logger.info(logString("Upsert latest catalog of items..."));
            clientV2.catalog().setBatchUpsertSize(BATCH_UPSERT_SIZE).batchUpsertObjects(modifiedItems);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(logString("Failure upserting items into catalog"));
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
        Preconditions.checkNotNull(clientV2);
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
     * @return the updated catalog
     */
    public Catalog batchUpsertCategoriesFromDatabaseToSquare() {
        Preconditions.checkNotNull(catalog);
        Preconditions.checkNotNull(clientV2);
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

        batchUpsertCategoriesToSquare(catalog, clientV2);
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
        Preconditions.checkNotNull(clientV2);

        for (CatalogObject catalogObject : catalog.getObjects()) {
            try {
                clientV2.catalog().deleteObject(catalogObject.getId());
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(logString("Failure to delete catalog object " + catalogObject.getId()));
            }
        }

        catalog = retrieveCatalogFromSquare();
        return catalog;
    }

    // per TNT requirements, item is to be deleted if item is absent
    // from any location -> present_at_location_ids = []
    private Catalog clearManagedItemLocations(Catalog catalog) {
        // - loop through catalog items and only update items with > 1 locations
        // - assume custom created items are isolated to 1 location
        for (CatalogObject item : catalog.getItems().values()) {
            if (item.getPresentAtLocationIds() != null && item.getPresentAtLocationIds().length > 1) {
                item.setPresentAtAllLocations(false);
                item.setPresentAtLocationIds(new String[0]);
                item.setAbsentAtLocationIds(new String[0]);

                // - only reset present_at_location_ids for first variation (managed by TNT)
                // - ad-hoc item variations should not be modified
                if (item.getItemData() != null && item.getItemData().getVariations() != null
                        && item.getItemData().getVariations().length > 0) {
                    CatalogObject itemVariation = item.getItemData().getVariations()[0];
                    itemVariation.setPresentAtAllLocations(false);
                    itemVariation.setPresentAtLocationIds(new String[0]);
                    itemVariation.setAbsentAtLocationIds(new String[0]);

                    // No longer need the following line, which overwrites/clears existing location overrides
                    // that may still be relevant. LocationOverrides can exist on CatalogObject even if
                    // CatalogObject is not present at location.
                    // itemVariation.getItemVariationData().setLocationOverrides(new ItemVariationLocationOverride[0]);
                }

                // get all unmanaged variation location ids
                String[] allUnmanagedVariationLocations = getAllUniqueItemVariationLocations(item);

                // reset for unmanaged item locations
                item.setPresentAtLocationIds(allUnmanagedVariationLocations);
            }
            // TODO: need to handle else case for item.getPresentAtLocationIds() == null
            // if 'Available at Future Locations' is selected in Square Dashboard,
            // item.getPresentAtLocationIds() returns null
        }

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

    private void generateCatalogUpsertsForItem(CsvItem csvItem, String[] squareLocationIds, Catalog catalog,
            Map<String, CatalogObject> categories, String taxIds[]) {
        String sku = getSku(csvItem);
        CatalogObject squareItem = getOrCreateSquareItem(catalog, sku);

        /*
         *  add BOGO to price description
         *      - for the 2017 season (and upcoming seasons), TNT will have a "half off" field
         *        in the marketing programs specifying if the item is in a BOGO program
         *      - all prices in the "selling price" column specify the actual selling price, even for
         *        BOGO items (BOGO item with MSRP 49.99 is set with selling price 25.00)
         *      - for any BOGO item, the original item description is appended with BOGO PRICE suffix
         */
        String desc = csvItem.getDescription();
        if (csvItem.getHalfOff().equals(CsvItem.BOGO_TRUE)) {
            desc = String.format("%s - BOGO PRICE", desc);
        }
        squareItem.getItemData().setName(desc);

        // set item variation data
        CatalogItemVariation squareItemVariation = getFirstItemVariation(squareItem);
        squareItemVariation.setSku(sku);
        squareItemVariation.setName(csvItem.getNumber());
        squareItemVariation.setPricingType(FIXED_PRICING);
        Money priceMoney = csvItem.getPriceAsSquareMoney();
        squareItemVariation.setPriceMoney(priceMoney);

        // - set catalog object data
        // - use TntCatalogApi class specific functions to update to ignore custom variations
        enableItemAtLocations(squareItem, squareLocationIds);
        setItemLocationPriceOverride(squareItem, squareLocationIds, squareItemVariation.getPriceMoney(), FIXED_PRICING);

        // set catalog item data
        setSquareCategoryForItem(categories, csvItem, squareItem);
        squareItem.getItemData().setTaxIds(taxIds);

        // add final item to local catalog
        catalog.addItem(squareItem);
    }

    private void setItemLocationPriceOverride(CatalogObject item, String[] locationIds, Money priceMoney,
            String pricingType) {
        // only set price override for first variation
        if (item.getItemData() != null && item.getItemData().getVariations() != null
                && item.getItemData().getVariations().length > 0) {
            CatalogObject variation = item.getItemData().getVariations()[0];
            variation.setLocationPriceOverride(locationIds, priceMoney, pricingType);
        }
    }

    private void enableItemAtLocations(CatalogObject item, String[] locationIds) {
        Set<String> locationsSet = new HashSet<String>();

        if (item.getPresentAtLocationIds() != null) {
            locationsSet.addAll(Arrays.asList(item.getPresentAtLocationIds()));
        }

        if (locationIds != null) {
            locationsSet.addAll(Arrays.asList(locationIds));
        }

        String[] managedLocationsResult = locationsSet.toArray(new String[locationsSet.size()]);
        // only update first variation
        if (item.getItemData() != null && item.getItemData().getVariations() != null
                && item.getItemData().getVariations().length > 0) {
            CatalogObject variation = item.getItemData().getVariations()[0];
            variation.setPresentAtLocationIds(managedLocationsResult);
        }

        // get all managed and unmanaged variation location ids
        String[] allVariationLocations = getAllUniqueItemVariationLocations(item);
        locationsSet.addAll(Arrays.asList(allVariationLocations));

        item.setPresentAtLocationIds(locationsSet.toArray(new String[locationsSet.size()]));
    }

    private String[] getAllUniqueItemVariationLocations(CatalogObject item) {
        Set<String> uniqueLocations = new HashSet<String>();

        // loop through all variations and add locations to set
        for (CatalogObject variation : item.getItemData().getVariations()) {
            // if 0 locations are present, present_at_location_ids is null
            if (variation.getPresentAtLocationIds() != null) {
                uniqueLocations.addAll(Arrays.asList(variation.getPresentAtLocationIds()));
            }
        }

        return uniqueLocations.toArray(new String[uniqueLocations.size()]);
    }

    private void setSquareCategoryForItem(Map<String, CatalogObject> categories, CsvItem csvItem,
            CatalogObject squareItem) {
        // check for matching category, if found, add category id
        if (categories.containsKey(csvItem.getCategory())) {
            String categoryId = categories.get(csvItem.getCategory()).getId();
            squareItem.getItemData().setCategoryId(categoryId);
        } else {
            logger.error(logString("Missing category for itemNumber: " + csvItem.getNumber()));
        }
    }

    private void generateItemUpdatesForMarketingPlan(HashMap<String, List<CsvItem>> marketingPlanItemsCache,
            String marketingPlanId, HashMap<String, List<String>> marketingPlanLocationsCache, Catalog catalog) {
        Map<String, CatalogObject> categories = getCategoriesAsHashmapFromSquare(catalog);
        String[] squareLocationIds = getSquareLocationIds(marketingPlanLocationsCache, marketingPlanId);
        String[] masterTaxIds = catalog.getTaxes().keySet().toArray(new String[catalog.getTaxes().size()]);

        List<CsvItem> marketingPlanItems = marketingPlanItemsCache.get(marketingPlanId);
        if (marketingPlanItems != null && marketingPlanItems.size() > 0) {
            for (CsvItem updateItem : marketingPlanItems) {
                generateCatalogUpsertsForItem(updateItem, squareLocationIds, catalog, categories, masterTaxIds);
            }
        } else {
            throw new IllegalArgumentException(logString("Empty marketing plan: " + marketingPlanId));
        }
    }

    private String[] getTntFireworksCategories() {
        HashSet<String> categoriesSet = new HashSet<String>();

        // add any categories
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
            logger.info(logString(String.format("Delete this catalog object name/token %s/%s:",
                    item.getItemData().getName(), item.getId())));
            try {
                clientV2.catalog().deleteObject(item.getId());
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(logString("Failure deleting unused items"));
            }
        }

    }

    private Map<String, CatalogObject> getCategoriesAsHashmapFromSquare(Catalog catalog) {
        return catalog.getCategories();
    }

    private void addCategoryToLocalCatalog(Catalog catalog, String categoryName) {
        CatalogObject newCategory = new CatalogObject(CATEGORY);
        newCategory.getCategoryData().setName(categoryName);
        catalog.addCategory(newCategory);
    }

    private void batchUpsertCategoriesToSquare(Catalog catalog, SquareClientV2 clientV2) {
        CatalogObject[] modifiedCategories = catalog.getModifiedCategories();

        try {
            logger.info(logString("Updating categories in catalog..."));
            clientV2.catalog().batchUpsertObjects(modifiedCategories);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(logString("Failure to upsert categories"));
        }

        logger.info(logString("Done checking/adding categories"));
    }

    private void addLocationToMarketingPlanLocationsCache(Location location,
            HashMap<String, List<String>> marketingPlanLocationsCache,
            HashMap<String, String> locationMarketingPlanCache) {
        String locationSquareId = location.getId();
        String locationTNTId = location.getName();
        String marketingPlanId = locationMarketingPlanCache.get(locationTNTId);

        if (locationTNTId.length() < 1) {
            throw new IllegalArgumentException(logString("Invalid TNT location number/ID"));
        }

        if (!locationTNTId.contains(INACTIVE_LOCATION) && !locationTNTId.contains(DEFAULT_LOCATION)) {
            if (marketingPlanId == null) {
                logger.error(logString(
                        "Could not find mapping of location number (in existing SQ account) to a location in DB. Missing location in locations file: "
                                + locationTNTId));
            } else {
                List<String> locationsList = marketingPlanLocationsCache.get(marketingPlanId);
                if (locationsList == null) {
                    locationsList = new ArrayList<String>();
                    marketingPlanLocationsCache.put(marketingPlanId, locationsList);
                }
                locationsList.add(locationSquareId);
            }
        }
    }

    private HashMap<String, List<String>> generateMarketingPlanLocationsCache(
            HashMap<String, String> locationMarketingPlanCache, SquareClientV2 clientV2) {
        HashMap<String, List<String>> marketingPlanLocationsCache = new HashMap<String, List<String>>();

        Location[] locations;
        try {
            logger.info(logString("Processing Catalog API updates"));
            locations = clientV2.locations().list();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(logString("Square API call to list locations failed"));
        }

        for (Location location : locations) {
            addLocationToMarketingPlanLocationsCache(location, marketingPlanLocationsCache, locationMarketingPlanCache);
        }

        return marketingPlanLocationsCache;
    }

    private void logCatalogStats(Catalog catalog) {
        logger.info(logString(String.format("CATEGORIES: %d", catalog.getCategories().size())));
        logger.info(logString(String.format("ITEMS: %d", catalog.getItems().size())));
        logger.info(logString(String.format("TAXES: %d", catalog.getTaxes().size())));
        logger.info(logString(String.format("DISCOUNTS: %d", catalog.getDiscounts().size())));
        logger.info(logString(String.format("MODIFIER LISTS: %d", catalog.getModifierLists().size())));
    }

    private String logString(String message) {
        return String.format("[%s] ::: %s", clientV2.getLogInfo(), message);
    }
}
