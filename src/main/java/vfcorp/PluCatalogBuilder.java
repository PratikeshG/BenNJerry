package vfcorp;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.v2.Catalog;
import com.squareup.connect.v2.CatalogItemVariation;
import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.ItemVariationLocationOverride;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.Money;
import com.squareup.connect.v2.SquareClientV2;

import util.CloudStorageApi;
import vfcorp.plu.ItemDbRecord;
import vfcorp.plu.ItemSaleDbRecord;

public class PluCatalogBuilder {
    private static Logger logger = LoggerFactory.getLogger(PluCatalogBuilder.class);

    private static final String FIXED_PRICING = "FIXED_PRICING";
    private static final String CATEGORY = "CATEGORY";
    private static final String ITEM = "ITEM";

    private static String DEPLOYMENT_BRAND_TNF = "tnf";
    private static String DEPLOYMENT_BRAND_TNF_CA = "tnfca";
    private static String DEPLOYMENT_BRAND_VANS = "vans";
    private static String DEPLOYMENT_BRAND_VANS_TEST = "test";
    private static String DEPLOYMENT_PREFIX = "vfcorp";
    private static String INVALID_STORE_ID = "00000";

    private static final Set<String> VANS_SKUS_TAX_FREE = new HashSet<String>(
            Arrays.asList(new String[] { "195436643935", "887040993765", "757969465981", "191476107444", "400007022584",
                    "400007022331", "400007022416" }));

    private static final Set<String> TNF_SKUS_TAX_FREE = new HashSet<String>(Arrays
            .asList(new String[] { "040005164508", "040004834909", "040008783039", "040007174632", "040009948465" }));

    private static final Set<String> TNF_SKUS_TAXABLE = new HashSet<String>(Arrays.asList(new String[] {}));

    private static final Set<String> VANS_SKUS_TAXABLE = new HashSet<String>(
            Arrays.asList(new String[] { "706420993945", "196245794955" }));

    private static int BATCH_UPSERT_SIZE = 25;

    private SquareClientV2 client;
    private String databaseUrl;
    private String databaseUser;
    private String databasePassword;
    private String storageBucket;
    private String storageCredentials;
    private String brand;
    private String deployment;
    private boolean ignoresSkuCheckDigit;
    private int itemNumberLookupLength;

    public boolean ignoresSkuCheckDigit() {
        return ignoresSkuCheckDigit;
    }

    public void setIgnoresSkuCheckDigit(boolean ignoresSkuCheckDigit) {
        this.ignoresSkuCheckDigit = ignoresSkuCheckDigit;
    }

    public void setItemNumberLookupLength(int itemNumberLookupLength) {
        this.itemNumberLookupLength = itemNumberLookupLength;
    }

    public PluCatalogBuilder(SquareClientV2 client, String databaseUrl, String databaseUser, String databasePassword,
            String storageBucket, String storageCredentials, String brand, String deployment) {
        this.client = client;
        this.databaseUrl = databaseUrl;
        this.databaseUser = databaseUser;
        this.databasePassword = databasePassword;
        this.storageBucket = storageBucket;
        this.storageCredentials = storageCredentials;
        this.brand = brand;
        this.deployment = deployment;
        this.ignoresSkuCheckDigit = false;
        this.itemNumberLookupLength = 14;
    }

    public void syncItemsFromDatabaseToSquare() throws Exception {
        // Construct current catalog from mix of API requests (taxes, etc.) and cached items (GCP)
        Catalog sourceCatalog = new Catalog(Catalog.PrimaryKey.SKU, Catalog.PrimaryKey.NAME, Catalog.PrimaryKey.ID,
                Catalog.PrimaryKey.NAME, Catalog.PrimaryKey.NAME);
        Map<String, CatalogObject> originalObjectCache = new HashMap<String, CatalogObject>();
        sourceCatalog.setOriginalObjectCache(originalObjectCache);

        CloudStorageApi storageApi = new CloudStorageApi(storageCredentials);

        // Read item catalog from pre-downloaded GCP cache
        String fileName = deployment + "-catalog.json";
        ArrayList<CatalogObject> gcpCatalog = Util.downloadBrandCatalogFileFromGCP(storageApi, storageBucket, brand,
                fileName);
        for (CatalogObject o : gcpCatalog) {
            originalObjectCache.put(o.getId(), o);
            sourceCatalog.addItem(o);
        }

        // Download categories and taxes from API
        CatalogObject[] categories = client.catalog().listCategories();
        for (CatalogObject c : categories) {
            originalObjectCache.put(c.getId(), c);
            sourceCatalog.addCategory(c);
        }
        CatalogObject[] taxes = client.catalog().listTaxes();
        for (CatalogObject t : taxes) {
            originalObjectCache.put(t.getId(), t);
            sourceCatalog.addTax(t);
        }

        Catalog workingCatalog = new Catalog(sourceCatalog, Catalog.PrimaryKey.SKU, Catalog.PrimaryKey.NAME,
                Catalog.PrimaryKey.ID, Catalog.PrimaryKey.NAME, Catalog.PrimaryKey.NAME);

        workingCatalog.clearItemLocations();
        workingCatalog.clearItemTaxes();

        logCatalogStats(brand, workingCatalog, "STARTING CATALOG :: ");

        // For each location, add unique item to catalog and set price (sale) overrides
        Location[] locations = client.locations().list();

        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);

        VfcDatabaseApi databaseApi = new VfcDatabaseApi(conn);

        for (Location location : locations) {
            syncCatalogForLocation(databaseApi, storageApi, workingCatalog, location);
        }

        databaseApi.close();

        // Now that catalog is set, reassign taxes
        for (Location location : locations) {
            assignLocationSpecificTaxes(workingCatalog, location);
        }

        // Remove repeated item meta data, such as superfluous location price overrides
        prepareForUpsert(workingCatalog);
        removeUnassignedLocationOverrides(workingCatalog);

        logCatalogStats(brand, workingCatalog, "CALCULATED CATALOG :: ");

        logInfoForBrand(brand, "TOTAL ITEMS IN SQUARE ACCOUNT: " + workingCatalog.getOriginalItems().values().size());
        logInfoForBrand(brand, "TOTAL ITEMS IN CATALOG: " + workingCatalog.getItems().values().size());
        CatalogObject[] modifiedItems = workingCatalog.getModifiedItems();
        CatalogObject[] modifiedItemsAssignedLocations = itemsWithLocationAssignments(modifiedItems);
        logInfoForBrand(brand, "TOTAL MODIFIED ITEMS IN CATALOG: " + modifiedItems.length);
        logInfoForBrand(brand,
                "TOTAL MODIFIED ITEMS ASSIGNED LOCATIONS IN CATALOG: " + modifiedItemsAssignedLocations.length);

        upsertObjectsToSquare(modifiedItemsAssignedLocations, "item");

        removeInvalidItems(workingCatalog);
    }

    private void prepareForUpsert(Catalog catalog) {
        for (CatalogObject catalogObject : catalog.getItems().values()) {
            catalogObject.minimizePriceOverrides();
        }
    }

    private CatalogObject[] itemsWithLocationAssignments(CatalogObject[] items) {
        ArrayList<CatalogObject> availableObjects = new ArrayList<CatalogObject>();

        for (CatalogObject item : items) {
            if (isObjectPresentAtAnyLocation(item)) {
                availableObjects.add(item);
            }
        }

        CatalogObject[] result = new CatalogObject[availableObjects.size()];
        return result = availableObjects.toArray(result);
    }

    private void removeUnassignedLocationOverrides(Catalog catalog) {
        for (CatalogObject item : catalog.getItems().values()) {
            if (item.isPresentAtAllLocations() || item.getPresentAtLocationIds() == null) {
                continue;
            }

            ArrayList<ItemVariationLocationOverride> itemActiveOverrides = new ArrayList<ItemVariationLocationOverride>();
            ArrayList<String> itemActiveLocations = new ArrayList<String>();

            if (item.getItemData() != null) {
                for (String presentLocationId : item.getPresentAtLocationIds()) {
                    itemActiveLocations.add(presentLocationId);
                }

                for (CatalogObject variation : item.getItemData().getVariations()) {
                    if (variation.getItemVariationData() != null
                            && variation.getItemVariationData().getLocationOverrides() != null) {
                        for (ItemVariationLocationOverride override : variation.getItemVariationData()
                                .getLocationOverrides()) {
                            if (itemActiveLocations.contains(override.getLocationId())) {
                                itemActiveOverrides.add(override);
                            }
                        }

                        variation.getItemVariationData().setLocationOverrides(itemActiveOverrides
                                .toArray(new ItemVariationLocationOverride[itemActiveOverrides.size()]));
                    }
                }
            }
        }
    }

    private String getDeploymentIdForLocation(Location location) {
        String locationId = Util.getStoreNumber(location.getName());
        if (locationId.equals(INVALID_STORE_ID)) {
            return null;
        }

        return String.format("%s-%s-%s", DEPLOYMENT_PREFIX, brand, locationId);
    }

    private void syncCatalogForLocation(VfcDatabaseApi databaseApi, CloudStorageApi storageApi, Catalog catalog,
            Location location) throws Exception {
        String deploymentId = getDeploymentIdForLocation(location);
        if (deploymentId == null) {
            System.out.println("INVALID LOCATION: " + location.getName());
            return; // Skip invalid location
        }

        syncLocationDbItems(storageApi, catalog, location, deploymentId);
        syncLocationDbSalePrices(storageApi, catalog, location);

        if (isPluWhitelistDeployment()) {
            syncWhitelistForLocation(databaseApi, catalog, location, deploymentId);
        }
    }

    private void assignLocationSpecificTaxes(Catalog catalog, Location location) throws Exception {
        String deploymentId = getDeploymentIdForLocation(location);
        if (deploymentId == null) {
            return; // Skip invalid location
        }

        CatalogObject[] locationEnabledTaxes = getEnabledTaxes(
                objectsPresentAtLocation(catalog.getTaxes().values().toArray(new CatalogObject[0]), location.getId()));

        if (locationEnabledTaxes.length > 0) {
            applyLocationSpecificItemTaxes(catalog.getItems().values().toArray(new CatalogObject[0]),
                    locationEnabledTaxes, deploymentId, location.getId());
        }
    }

    private void syncWhitelistForLocation(VfcDatabaseApi databaseApi, Catalog catalog, Location location,
            String deploymentId) throws Exception {

        String storeId = Util.getStoreNumber(location.getName());
        ArrayList<String> productIds = (ArrayList<String>) databaseApi.getWhitelistForBrandStore("vfcorp-" + brand,
                storeId);

        HashSet<String> whitelistedSkus = new HashSet<String>();
        for (String prodId : productIds) {
            whitelistedSkus.add(convertItemNumberToScannableSku(prodId));
        }

        whitelistItemsForLocation(catalog, location, whitelistedSkus);
    }

    private void syncLocationDbItems(CloudStorageApi storageApi, Catalog catalog, Location location,
            String deploymentId) throws Exception {
        String fileKey = location.getId() + "-items.json";
        if (Util.brandPluFileExists(storageApi, storageBucket, brand, fileKey)) {
            ArrayList<ItemDbRecord> gcpItems = Util.downloadBrandItemFileFromGCP(storageApi, storageBucket, brand,
                    fileKey);
            for (ItemDbRecord itemRecord : gcpItems) {
                updateCatalogLocationWithItem(catalog, location, itemRecord, deploymentId);
            }
        } else {
            logInfoForBrand(brand, "PLU SYNC - No item records in database for location: " + location.getId());
        }
    }

    private void syncLocationDbSalePrices(CloudStorageApi storageApi, Catalog catalog, Location location)
            throws Exception {
        String fileKey = location.getId() + "-sales.json";
        if (Util.brandPluFileExists(storageApi, storageBucket, brand, fileKey)) {
            ArrayList<ItemSaleDbRecord> gcpItemSales = Util.downloadBrandItemSaleFileFromGCP(storageApi, storageBucket,
                    brand, fileKey);
            for (ItemSaleDbRecord itemSaleRecord : gcpItemSales) {
                applySalePrice(catalog, location.getId(), itemSaleRecord);
            }
        }
    }

    private static void logCatalogStats(String brand, Catalog catalog, String state) {
        logInfoForBrand(brand, state + "CATEGORIES: " + catalog.getCategories().size());
        logInfoForBrand(brand, state + "ITEMS: " + catalog.getItems().size());
        logInfoForBrand(brand, state + "TAXES: " + catalog.getTaxes().size());
    }

    /* VFC Catalogs are a 1:1 mapping between a CatalogItem and a CatalogItemVariation
     * There should be no duplicate SKUs across CatalogItemVariations.
     *
     * Remove items no longer present at any location, AND
     * Remove items with duplicate SKUs existing in account
     */
    private void removeInvalidItems(Catalog catalog) {
        ArrayList<String> idsToDelete = new ArrayList<String>();

        int totalDuplicateSkus = 0;
        Map<String, List<CatalogObject>> duplicateSkuObjectCache = getDuplicateSkuCache(catalog);

        for (String key : catalog.getItems().keySet()) {
            CatalogObject item = catalog.getItem(key);
            CatalogObject[] duplicateItemsBySku = getDuplicateItemsBySku(duplicateSkuObjectCache, item);

            // removeItemsNotPresentAtAnyLocations
            if (!isObjectPresentAtAnyLocation(item)) {
                idsToDelete.add(item.getId());
            }
            // removeItemsWithDuplicateSkus
            else if (duplicateItemsBySku.length > 0) {
                for (CatalogObject duplicateObject : duplicateItemsBySku) {
                    idsToDelete.add(duplicateObject.getId());
                }
                totalDuplicateSkus += duplicateItemsBySku.length;
            }
        }

        logger.info("TOTAL DUPLCATE SKUS IN CATALOG: " + totalDuplicateSkus);
        logger.info("TOTAL CATALOG IDS TO DELETE: " + idsToDelete.size());

        deleteObjectsFromSquare(idsToDelete.toArray(new String[0]));
    }

    /*
     * Returns a Map of SKU strings to CatalogObjects that share the same CatalogItem SKU
     */
    private Map<String, List<CatalogObject>> getDuplicateSkuCache(Catalog catalog) {
        Map<String, List<CatalogObject>> objectCache = new HashMap<String, List<CatalogObject>>();

        for (CatalogObject originalCatalogItem : catalog.getOriginalItems().values()) {
            String sku = getCatalogItemFirstSku(originalCatalogItem);

            ArrayList<CatalogObject> matchingSkus = (ArrayList<CatalogObject>) objectCache.getOrDefault(sku,
                    new ArrayList<CatalogObject>());
            matchingSkus.add(originalCatalogItem);
            objectCache.put(sku, matchingSkus);
        }

        return objectCache;
    }

    private String getCatalogItemFirstSku(CatalogObject catalogObject) {
        String sku = "";

        if (catalogObject.getItemData() != null && catalogObject.getItemData().getVariations().length > 0
                && catalogObject.getItemData().getVariations()[0].getItemVariationData() != null) {
            sku = catalogObject.getItemData().getVariations()[0].getItemVariationData().getSku();
        }

        return sku;
    }

    /*
     * Returns all CatalogObjects that share the SKU of the finalCatalogItem, but are not the finalCatalogItem object
     */
    private CatalogObject[] getDuplicateItemsBySku(Map<String, List<CatalogObject>> objectCache,
            CatalogObject finalCatalogItem) {
        ArrayList<CatalogObject> itemsWithDuplicateSku = new ArrayList<CatalogObject>();

        String finalCatalogItemId = finalCatalogItem.getId();
        String finalCatalogItemSku = finalCatalogItem.getItemData().getVariations()[0].getItemVariationData().getSku();

        // Duplicate SKU detected on an original account item
        if (objectCache.containsKey(finalCatalogItemSku) && objectCache.get(finalCatalogItemSku) != null) {
            for (CatalogObject originalItem : objectCache.get(finalCatalogItemSku)) {
                if (!originalItem.getId().equals(finalCatalogItemId) && originalItem.getItemData() != null
                        && originalItem.getItemData().getVariations().length > 0
                        && originalItem.getItemData().getVariations()[0].getItemVariationData() != null) {

                    itemsWithDuplicateSku.add(originalItem);
                }
            }
        }

        return itemsWithDuplicateSku.toArray(new CatalogObject[0]);
    }

    private CatalogObject[] objectsPresentAtLocation(CatalogObject[] objects, String locationId) {
        ArrayList<CatalogObject> objectsForLocation = new ArrayList<CatalogObject>();

        for (CatalogObject object : objects) {
            if (object.isPresentAtAllLocations() || (object.getPresentAtLocationIds() != null
                    && Arrays.asList(object.getPresentAtLocationIds()).contains(locationId))) {
                objectsForLocation.add(object);
            }
        }

        return objectsForLocation.toArray(new CatalogObject[0]);
    }

    private CatalogObject[] getEnabledTaxes(CatalogObject[] objects) {
        ArrayList<CatalogObject> enabledTaxes = new ArrayList<CatalogObject>();

        for (CatalogObject object : objects) {
            if (object.getTaxData() != null && object.getTaxData().isEnabled()) {
                enabledTaxes.add(object);
            }
        }

        return enabledTaxes.toArray(new CatalogObject[0]);
    }

    private boolean isObjectPresentAtAnyLocation(CatalogObject object) {
        return (object.isPresentAtAllLocations()
                || (object.getPresentAtLocationIds() != null && object.getPresentAtLocationIds().length > 0));
    }

    public void syncCategoriesFromDatabaseToSquare() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);

        VfcDatabaseApi databaseApi = new VfcDatabaseApi(conn);

        HashSet<String> allDatabaseCategoryNames = (HashSet<String>) getUniqueCategoriesFromDatabase(databaseApi);

        databaseApi.close();

        // Only retrieve Square account categories for now
        Catalog categoriesSourceCatalog = new Catalog(client.catalog().listCategories(), Catalog.PrimaryKey.SKU,
                Catalog.PrimaryKey.NAME, Catalog.PrimaryKey.ID, Catalog.PrimaryKey.NAME, Catalog.PrimaryKey.NAME);
        Catalog categoriesWorkingCatalog = new Catalog(categoriesSourceCatalog, Catalog.PrimaryKey.SKU,
                Catalog.PrimaryKey.NAME, Catalog.PrimaryKey.ID, Catalog.PrimaryKey.NAME, Catalog.PrimaryKey.NAME);

        // Add missing categories
        for (String categoryName : allDatabaseCategoryNames) {
            CatalogObject existingCategory = categoriesWorkingCatalog.getCategories().get(categoryName);
            if (existingCategory == null) {
                CatalogObject newCategory = new CatalogObject(CATEGORY);
                newCategory.getCategoryData().setName(categoryName);
                categoriesWorkingCatalog.addCategory(newCategory);
            }
        }

        CatalogObject[] modifiedCategories = categoriesWorkingCatalog.getModifiedCategories();
        upsertObjectsToSquare(modifiedCategories, "category");
    }

    private void deleteObjectsFromSquare(String[] objectIds) {
        logger.info(String.format("Deleting %d objects from catalog...", objectIds.length));
        try {
            int count = 0;
            for (String idToDelete : objectIds) {
                client.catalog().deleteObject(idToDelete);

                if (count % 250 == 0) {
                    logger.info("250 objects deleted...");
                }
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failure deleting objects");
        }
    }

    private void upsertObjectsToSquare(CatalogObject[] objects, String type) {
        logger.info(String.format("Upserting %s %s objects from catalog...", objects.length, type));
        try {
            client.catalog().setBatchUpsertSize(BATCH_UPSERT_SIZE).batchUpsertObjects(objects);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(String.format("Failure upserting %s %s objects into catalog: %s", objects.length,
                    type, e.getMessage()));
        }
    }

    private Set<String> getUniqueCategoriesFromDatabase(VfcDatabaseApi databaseApi)
            throws ClassNotFoundException, SQLException, IOException {
        HashSet<String> categoriesSet = new HashSet<String>();

        ArrayList<Map<String, String>> records = databaseApi.queryDbDeptClass(brand);

        for (Map<String, String> deptClassRecord : records) {
            String deptNumber = String.format("%-4s", deptClassRecord.get("deptNumber"));
            String classNumber = String.format("%-4s", deptClassRecord.get("classNumber"));
            String categoryName = deptNumber + classNumber + " " + deptClassRecord.get("description").trim();

            categoriesSet.add(categoryName);
        }

        return categoriesSet;
    }

    private void applyLocationSpecificItemTaxes(CatalogObject[] items, CatalogObject[] locationSpecificTaxes,
            String deploymentId, String locationId) throws Exception {
        for (CatalogObject item : items) {
            boolean itemAssignedToLocation = false;

            if (item.getPresentAtLocationIds() != null) {
                for (String assignedLocationId : item.getPresentAtLocationIds()) {
                    if (locationId.equals(assignedLocationId)) {
                        itemAssignedToLocation = true;
                        break;
                    }
                }
            }

            if (item.isPresentAtAllLocations() || itemAssignedToLocation) {
                String[] taxIds = TaxRules.getItemTaxesForLocation(item.getItemData(), locationSpecificTaxes,
                        deploymentId, locationId);
                item.getItemData().appendTaxIds(taxIds);
            }
        }
    }

    private void applySalePrice(Catalog catalog, String locationId, ItemSaleDbRecord record) throws Exception {
        String sku = convertItemNumberToScannableSku(record.getItemNumber());

        CatalogObject item = catalog.getItem(sku);
        if (item == null) {
            return;
        }

        CatalogItemVariation variation = getFirstItemVariation(item);

        if (variation != null && variation.getSku().equals(sku)) {
            int price = Integer.parseInt(record.getSalePrice());
            if (price > 0) {
                item.setLocationPriceOverride(new String[] { locationId }, new Money(price, getAccountCurrency()),
                        FIXED_PRICING);
            }
        }
    }

    private CatalogObject getMatchingOrNewItem(Catalog catalog, String sku) {
        CatalogObject item = catalog.getItem(sku);
        if (item == null) {
            item = new CatalogObject(ITEM);
        }
        return item;
    }

    private void whitelistItemsForLocation(Catalog catalog, Location location, HashSet<String> whitelistedSkus) {
        for (String primaryKey : catalog.getItems().keySet()) {
            CatalogObject item = catalog.getItems().get(primaryKey);
            CatalogItemVariation variation = getFirstItemVariation(item);

            String sku = variation.getSku();

            if (TNF_SKUS_TAX_FREE.contains(sku) || TNF_SKUS_TAXABLE.contains(sku) || VANS_SKUS_TAX_FREE.contains(sku)
                    || VANS_SKUS_TAXABLE.contains(sku)) {
                setPresentAtAllLocations(item, true);
            } else {
                setPresentAtAllLocations(item, false);

                if (whitelistedSkus.contains(sku)) {
                    item.enableAtLocation(location.getId());
                }
            }
        }
    }

    private void updateCatalogLocationWithItem(Catalog catalog, Location location, ItemDbRecord record,
            String deploymentId) throws Exception {
        if (record.getItemNumber().length() < itemNumberLookupLength) {
            // ignore, invalid sku length from database
            return;
        }

        String sku = convertItemNumberToScannableSku(record.getItemNumber());

        CatalogObject updatedItem = getMatchingOrNewItem(catalog, sku);
        CatalogItemVariation updatedVariation = getFirstItemVariation(updatedItem);

        // Item Name
        String description = record.getDescription().replaceFirst("\\s+$", "");
        String altDescription = (record.getAlternateDescription() != null) ? record.getAlternateDescription().trim()
                : sku;
        String itemName = (altDescription.length() > description.length()) ? altDescription : description;
        updatedItem.getItemData().setName(itemName);

        // Variation SKU
        updatedVariation.setPricingType("FIXED_PRICING");
        updatedVariation.setSku(sku);

        // Variation Price
        String rawPrice = (record.getRetailPrice() != null && !record.getRetailPrice().isEmpty())
                ? record.getRetailPrice()
                : "0";
        int price = Integer.parseInt(rawPrice);
        Money locationPriceMoney = new Money(price, getAccountCurrency());
        if (price > 0 || updatedVariation.getPriceMoney() == null) {

            // When price is set to 1c, it should actually be $0.00
            if (price == 1) {
                locationPriceMoney.setAmount(0);
            }

            // We can't discern which location's price is the master price, so just override
            updatedVariation.setPriceMoney(locationPriceMoney);
        }

        // Variation Name
        String deptCodeClass = String.format("%-4s", record.getDeptNumber())
                + String.format("%-4s", record.getClassNumber());
        String variationName = String.format("%s (%s)", sku, deptCodeClass);
        updatedVariation.setName(variationName);

        // Item Category
        for (CatalogObject category : catalog.getCategories().values()) {
            if (category.getCategoryData().getName().subSequence(0, 8).equals(deptCodeClass)) {
                updatedItem.getItemData().setCategoryId(category.getId());
                break;
            }
        }

        // Availability
        String locationId = location.getId();
        setPresentAtAllLocations(updatedItem, true);

        // We need to exclude certain items from MA/RhodeIsland because we can't apply proper dynamic taxation
        // TNF requested these items not show up for sale in the POS
        if (skipItemForTaxReasons(updatedItem, deploymentId)) {
            updatedItem.disableAtLocation(locationId);
        } else {
            if (price > 0) {
                updatedItem.setLocationPriceOverride(locationId, locationPriceMoney, FIXED_PRICING);
            }
        }

        catalog.addItem(updatedItem);
    }

    private void setPresentAtAllLocations(CatalogObject object, boolean val) {
        object.setPresentAtAllLocations(val);
        if (object.getItemData() != null) {
            object.getItemData().setPresentAtAllLocations(val);
        }
    }

    private CatalogItemVariation getFirstItemVariation(CatalogObject item) {
        if (item.getItemData().getVariations() != null) {
            return item.getItemData().getVariations()[0].getItemVariationData();
        }
        return null;
    }

    // Square can't support certain items due to deployment specific taxes. We'll omit them.
    private boolean skipItemForTaxReasons(CatalogObject item, String deploymentId) {
        if ((deploymentId.equals(TaxRules.TNF_BOSTON) || deploymentId.equals(TaxRules.TNF_PEABODY)
                || deploymentId.equals(TaxRules.TNF_BRAINTREE))
                && TaxRules.deptClassIsClothingTaxCategoryTnf(
                        Util.getValueInParenthesis(getFirstItemVariation(item).getName()))
                && getFirstItemVariation(item).getPriceMoney().getAmount() > TaxRules.MA_EXEMPT_THRESHOLD) {
            return true;
        } else if (deploymentId.equals(TaxRules.TNF_RHODE_ISLAND)
                && TaxRules.deptClassIsClothingTaxCategoryTnf(
                        Util.getValueInParenthesis(getFirstItemVariation(item).getName()))
                && getFirstItemVariation(item).getPriceMoney().getAmount() > TaxRules.RI_EXEMPT_THRESHOLD) {
            return true;
        }

        return false;
    }

    /*
     * They provide 14 digit SKUs, padded with 0s, but SKUs can also start with
     * 0... ugh SKUs that start with 0 are 12 or less characters
     *
     * 14 digit SKU examples:
     *
     * 12345678901234 => 12345678901234 (stays the same, fulfills 14-digit
     * criteria) 123456789012 => 00123456789012 (0-pad this one)
     *
     * 12 digit SKU examples:
     *
     * 00012345678901 => 012345678901 (remove 2 0's, need's to fit 12 or less
     * criteria)
     *
     * 012345678901 => 01234567890 (stays the same, <=12)
     *
     */
    private String convertItemNumberIntoSku(String itemNumber) {
        String shortItemNumber = itemNumber.substring(itemNumber.length() - itemNumberLookupLength);

        if (shortItemNumber.matches("[0-9]+")) {
            // Remove leading zeros
            shortItemNumber = shortItemNumber.replaceFirst("^0+(?!$)", "");

            // But SKUs should be at least 12 digits, pad 0s
            if (shortItemNumber.length() < 12) {
                shortItemNumber = ("000000000000" + shortItemNumber).substring(shortItemNumber.length());
            }
            return shortItemNumber;
        } else {
            // Remove trailing spaces
            return shortItemNumber.replaceFirst("\\s+$", "");
        }
    }

    private String convertItemNumberToScannableSku(String itemNumber) {
        if (ignoresSkuCheckDigit && itemNumber.startsWith("00")) {
            return convertItemNumberIntoSku(itemNumber).substring(itemNumber.length() - 12);
        }
        return convertItemNumberIntoSku(itemNumber);
    }

    private String getAccountCurrency() {
        if (isCanadaDeployment()) {
            return "CAD";
        }
        return "USD";
    }

    private boolean isCanadaDeployment() {
        return brand.equals(DEPLOYMENT_BRAND_TNF_CA);
    }

    private boolean isPluWhitelistDeployment() {
        return brand.equals(DEPLOYMENT_BRAND_TNF) || brand.equals(DEPLOYMENT_BRAND_TNF_CA)
                || brand.equals(DEPLOYMENT_BRAND_VANS) || brand.equals(DEPLOYMENT_BRAND_VANS_TEST);
    }

    private static void logInfoForBrand(String brand, String message) {
        logger.info(String.format("[%s] :: %s", brand, message));
    }
}
