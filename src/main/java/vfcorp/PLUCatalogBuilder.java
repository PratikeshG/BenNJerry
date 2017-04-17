package vfcorp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.jdbc.ResultSet;
import com.squareup.connect.v2.Catalog;
import com.squareup.connect.v2.CatalogItemVariation;
import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.Money;
import com.squareup.connect.v2.SquareClientV2;

import util.TimeManager;

public class PLUCatalogBuilder {
    private static Logger logger = LoggerFactory.getLogger(PLUCatalogBuilder.class);

    private static final String FIXED_PRICING = "FIXED_PRICING";
    private static final String CATEGORY = "CATEGORY";
    private static final String ITEM = "ITEM";

    private static String DEPLOYMENT_PREFIX = "vfcorp";
    private static String INVALID_STORE_ID = "00000";

    private SquareClientV2 client;
    private String databaseUrl;
    private String databaseUser;
    private String databasePassword;
    private String brand;
    private boolean pluFiltered;
    private int itemNumberLookupLength;

    public void setItemNumberLookupLength(int itemNumberLookupLength) {
        this.itemNumberLookupLength = itemNumberLookupLength;
    }

    public void setPluFiltered(boolean pluFiltered) {
        this.pluFiltered = pluFiltered;
    }

    public PLUCatalogBuilder(SquareClientV2 client, String databaseUrl, String databaseUser, String databasePassword,
            String brand) {
        this.client = client;
        this.databaseUrl = databaseUrl;
        this.databaseUser = databaseUser;
        this.databasePassword = databasePassword;
        this.brand = brand;
        this.pluFiltered = true;
        this.itemNumberLookupLength = 14;
    }

    public void syncItemsFromDatabaseToSquare() throws Exception {
        Catalog sourceCatalog = client.catalog().retrieveCatalog(Catalog.PrimaryKey.SKU, Catalog.PrimaryKey.NAME,
                Catalog.PrimaryKey.ID, Catalog.PrimaryKey.NAME, Catalog.PrimaryKey.NAME);

        Catalog workingCatalog = new Catalog(sourceCatalog, Catalog.PrimaryKey.SKU, Catalog.PrimaryKey.NAME,
                Catalog.PrimaryKey.ID, Catalog.PrimaryKey.NAME, Catalog.PrimaryKey.NAME);

        workingCatalog.clearItemLocations();
        workingCatalog.clearItemTaxes();

        logCatalogStats(workingCatalog);

        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);

        // For each location, add unique item to catalog and set price (sale) overrides
        Location[] locations = client.locations().list();

        for (Location location : locations) {
            syncCatalogForLocation(conn, workingCatalog, location);
        }

        conn.close();

        // Now that catalog is set, reassign taxes
        for (Location location : locations) {
            assignLocationSpecificTaxes(workingCatalog, location);
        }

        // Remove repeated item meta data, such as superfluous location price overrides
        prepareForUpsert(workingCatalog);

        logger.info("TOTAL ITEMS IN CATALOG: " + workingCatalog.getItems().values().size());
        CatalogObject[] modifiedItems = workingCatalog.getModifiedItems();
        logger.info("TOTAL MODIFIED ITEMS IN CATALOG: " + modifiedItems.length);

        upsertObjectsToSquare(modifiedItems, "item");
        removeItemsNotPresentAtAnyLocations(workingCatalog);
    }

    private void prepareForUpsert(Catalog catalog) {
        for (CatalogObject catalogObject : catalog.getItems().values()) {
            catalogObject.minimizePriceOverrides();
        }
    }

    private String getDeploymentIdForLocation(Location location) {
        String locationId = Util.getStoreNumber(location.getName());
        if (locationId.equals(INVALID_STORE_ID)) {
            return null;
        }

        return String.format("%s-%s-%s", DEPLOYMENT_PREFIX, brand, locationId);
    }

    private void syncCatalogForLocation(Connection conn, Catalog catalog, Location location) throws Exception {
        String deploymentId = getDeploymentIdForLocation(location);
        if (deploymentId == null) {
            System.out.println("INVALID LOCATION: " + location.getName());
            return; // Skip invalid location
        }

        syncLocationDbItems(conn, catalog, location, deploymentId);
        syncLocationDbSalePrices(conn, catalog, location);
    }

    private void assignLocationSpecificTaxes(Catalog catalog, Location location) throws Exception {
        String deploymentId = getDeploymentIdForLocation(location);
        if (deploymentId == null) {
            return; // Skip invalid location
        }

        CatalogObject[] locationTaxes = objectsPresentAtLocation(
                catalog.getTaxes().values().toArray(new CatalogObject[0]), location.getId());

        if (locationTaxes.length > 0) {
            applyLocationSpecificItemTaxes(catalog.getItems().values().toArray(new CatalogObject[0]), locationTaxes,
                    deploymentId, location.getId());
        }
    }

    private void syncLocationDbItems(Connection conn, Catalog catalog, Location location, String deploymentId)
            throws Exception {
        ResultSet dbItemCursor = queryDBItems(conn, location.getId());
        while (dbItemCursor.next()) {
            updateCatalogLocationWithItem(catalog, location, dbItemCursor, deploymentId);
        }
    }

    private void syncLocationDbSalePrices(Connection conn, Catalog catalog, Location location) throws Exception {
        ResultSet dbItemSaleCursor = queryDBItemSaleEvents(conn, location.getId(), location.getTimezone());
        while (dbItemSaleCursor.next()) {
            applySalePrice(catalog, location.getId(), dbItemSaleCursor);
        }
    }

    private static void logCatalogStats(Catalog catalog) {
        logger.info("CATEGORIES: " + catalog.getCategories().size());
        logger.info("ITEMS: " + catalog.getItems().size());
        logger.info("TAXES: " + catalog.getTaxes().size());
        logger.info("DISCOUNTS: " + catalog.getDiscounts().size());
        logger.info("MODIFIER LISTS: " + catalog.getModifierLists().size());
    }

    private void removeItemsNotPresentAtAnyLocations(Catalog catalog) {
        ArrayList<String> idsToDelete = new ArrayList<String>();

        for (String key : catalog.getItems().keySet()) {
            CatalogObject item = catalog.getItem(key);

            if (!isObjectPresentAtAnyLocation(item)) {
                idsToDelete.add(item.getId());
            }
        }

        deleteObjectsFromSquare(idsToDelete.toArray(new String[0]));
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

    private boolean isObjectPresentAtAnyLocation(CatalogObject object) {
        return (object.isPresentAtAllLocations()
                || (object.getPresentAtLocationIds() != null && object.getPresentAtLocationIds().length > 0));
    }

    public void syncCategoriesFromDatabaseToSquare() throws Exception {
        HashSet<String> allDatabaseCategoryNames = (HashSet<String>) getUniqueCategoriesFromDatabase();

        // Only retrieve Square account categories for now
        Catalog categoriesCatalog = retrieveCategoriesFromSquare();

        // Add missing categories
        for (String categoryName : allDatabaseCategoryNames) {
            CatalogObject existingCategory = categoriesCatalog.getCategories().get(categoryName);
            if (existingCategory == null) {
                CatalogObject newCategory = new CatalogObject(CATEGORY);
                newCategory.getCategoryData().setName(categoryName);
                categoriesCatalog.addCategory(newCategory);
            }
        }

        upsertObjectsToSquare(categoriesCatalog.getCategories().values().toArray(new CatalogObject[0]), "category");
    }

    private Catalog retrieveCategoriesFromSquare() throws Exception {
        Catalog catalog = new Catalog(client.catalog().listCategories(), Catalog.PrimaryKey.SKU,
                Catalog.PrimaryKey.NAME, Catalog.PrimaryKey.ID, Catalog.PrimaryKey.NAME, Catalog.PrimaryKey.NAME);
        return catalog;
    }

    private void deleteObjectsFromSquare(String[] objectIds) {
        logger.info(String.format("Deleteing %d objects from catalog...", objectIds.length));
        try {
            client.catalog().batchDeleteObjects(objectIds);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failure deleting objects");
        }
    }

    private void upsertObjectsToSquare(CatalogObject[] objects, String type) {
        logger.info(String.format("Upserting %s objects from catalog...", type));
        try {
            client.catalog().batchUpsertObjects(objects);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(
                    String.format("Failure upserting %s objects into catalog: %s", type, e.getMessage()));
        }
    }

    private Set<String> getUniqueCategoriesFromDatabase() throws ClassNotFoundException, SQLException {
        HashSet<String> categoriesSet = new HashSet<String>();

        ResultSet dbDeptClassCursor = queryDBDeptClass(brand);
        while (dbDeptClassCursor.next()) {
            String deptNumber = String.format("%-4s", dbDeptClassCursor.getString("deptNumber"));
            String classNumber = String.format("%-4s", dbDeptClassCursor.getString("classNumber"));
            String categoryName = deptNumber + classNumber + " " + dbDeptClassCursor.getString("description").trim();

            categoriesSet.add(categoryName);
        }

        return categoriesSet;
    }

    private ResultSet queryDBDeptClass(String brand) throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);

        String query = String.format(
                "SELECT deptNumber, classNumber, description FROM vfcorp_plu_dept_class WHERE deployment LIKE 'vfcorp-%s-%%' GROUP BY deptNumber, classNumber, description",
                brand);

        return executeQuery(conn, query);
    }

    private void applyLocationSpecificItemTaxes(CatalogObject[] items, CatalogObject[] locationSpecificTaxes,
            String deploymentId, String locationId) throws Exception {
        for (CatalogObject item : items) {
            String[] taxIds = TaxRules.getItemTaxesForLocation(item.getItemData(), locationSpecificTaxes, deploymentId,
                    locationId);
            item.getItemData().appendTaxIds(taxIds);
        }
    }

    private void applySalePrice(Catalog catalog, String locationId, ResultSet record) throws Exception {
        String sku = convertItemNumberIntoSku(record.getString("itemNumber"));

        CatalogObject item = catalog.getItem(sku);
        if (item == null) {
            return;
        }

        CatalogItemVariation variation = getFirstItemVariation(item);

        if (variation != null && variation.getSku().equals(sku)) {
            int price = Integer.parseInt(record.getString("salePrice"));
            if (price > 0) {
                item.setLocationPriceOverride(new String[] { locationId }, new Money(price), FIXED_PRICING);
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

    private void updateCatalogLocationWithItem(Catalog catalog, Location location, ResultSet record,
            String deploymentId) throws Exception {
        String sku = convertItemNumberIntoSku(record.getString("itemNumber"));

        CatalogObject updatedItem = getMatchingOrNewItem(catalog, sku);
        CatalogItemVariation updatedVariation = getFirstItemVariation(updatedItem);

        // Item Name
        String description = record.getString("description").replaceFirst("\\s+$", "");
        String altDescription = (record.getString("alternateDescription") != null)
                ? record.getString("alternateDescription").trim() : "";
        String itemName = (altDescription.length() > description.length()) ? altDescription : description;
        updatedItem.getItemData().setName(itemName);

        // Variation SKU
        updatedVariation.setSku(sku);

        // Variation Price
        int price = Integer.parseInt(record.getString("retailPrice"));
        Money locationPriceMoney = new Money(price);
        if (price > 0) {
            // We can't discern which location's price is the master price, just override
            updatedVariation.setPriceMoney(locationPriceMoney);
        }

        // Variation Name
        String deptCodeClass = String.format("%-4s", record.getString("deptNumber"))
                + String.format("%-4s", record.getString("classNumber"));
        updatedVariation.setName(String.format("%s (%s)", sku, deptCodeClass));

        // Item Category
        for (CatalogObject category : catalog.getCategories().values()) {
            if (category.getCategoryData().getName().subSequence(0, 8).equals(deptCodeClass)) {
                updatedItem.getItemData().setCategoryId(category.getId());
                break;
            }
        }

        // Availability
        String locationId = location.getId();
        setPresentAtAllLocations(updatedItem);

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

    private void setPresentAtAllLocations(CatalogObject object) {
        object.setPresentAtAllLocations(true);
        if (object.getItemData() != null) {
            object.getItemData().setPresentAtAllLocations(true);
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
                && TaxRules.deptClassIsClothingTaxCategory(
                        Util.getValueInParenthesis(getFirstItemVariation(item).getName()))
                && getFirstItemVariation(item).getPriceMoney().getAmount() > TaxRules.MA_EXEMPT_THRESHOLD) {
            return true;
        } else if (deploymentId.equals(TaxRules.TNF_RHODE_ISLAND)
                && TaxRules.deptClassIsClothingTaxCategory(
                        Util.getValueInParenthesis(getFirstItemVariation(item).getName()))
                && getFirstItemVariation(item).getPriceMoney().getAmount() > TaxRules.RI_EXEMPT_THRESHOLD) {
            return true;
        }

        return false;
    }

    private ResultSet executeQuery(Connection conn, String query) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet result = (ResultSet) stmt.executeQuery(query);

        return result;
    }

    private ResultSet queryDBItems(Connection conn, String locationId)
            throws SQLException, IOException, ClassNotFoundException {
        String query = String.format("SELECT * FROM vfcorp_plu_items WHERE locationId = '%s'", locationId);

        if (pluFiltered) {
            logger.info("Applying SKU whitelist filter... ");
            query += String.format(" AND itemNumber IN (%s)", getFilteredSKUQueryString());
        }

        logger.info("Querying items DB for location " + locationId);
        return executeQuery(conn, query);
    }

    private ResultSet queryDBItemSaleEvents(Connection conn, String locationId, String timeZone)
            throws SQLException, IOException, ParseException {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        String nowDate = TimeManager.toSimpleDateTimeInTimeZone(cal, timeZone, "MMddyyyy");

        String query = "SELECT events.itemNumber as itemNumber, events.salePrice as salePrice "
                + "FROM vfcorp_plu_sale_events as events " + "JOIN " + "     (SELECT itemNumber, MAX(id) as id "
                + "     FROM vfcorp_plu_sale_events " + "     WHERE locationId = '" + locationId + "' AND "
                + "     STR_TO_DATE('" + nowDate + "', '%m%d%Y') >= STR_TO_DATE(dateSaleBegins, '%m%d%Y') AND "
                + "     STR_TO_DATE('" + nowDate + "', '%m%d%Y') <= STR_TO_DATE(dateSaleEnds, '%m%d%Y') "
                + "     GROUP BY itemNumber) as newest ON events.id = newest.id";

        logger.info("Querying item sales DB for loaction " + locationId);
        return executeQuery(conn, query);
    }

    private String getFilteredSKUQueryString() throws IOException {
        HashMap<String, Boolean> skuFilter = new HashMap<String, Boolean>();

        String filterSKUPath = "/vfc-plu-filters/vfcorp-tnf-onhand-sku.csv";
        InputStream iSKU = this.getClass().getResourceAsStream(filterSKUPath);
        BufferedReader brSKU = new BufferedReader(new InputStreamReader(iSKU, "UTF-8"));
        try {
            String line;
            while ((line = brSKU.readLine()) != null) {
                skuFilter.put(line.trim(), new Boolean(true));
            }
        } finally {
            brSKU.close();
        }

        logger.info("Total SKU whitelist filtered: " + skuFilter.size());

        StringJoiner sj = new StringJoiner(",");
        for (String sku : skuFilter.keySet()) {
            sj.add(String.format("'%s'", sku));
        }
        return sj.toString();
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
        String shortItemNumber = itemNumber.substring(0, itemNumberLookupLength);

        if (shortItemNumber.matches("[0-9]+")) {
            // Remove leading zeros
            shortItemNumber = shortItemNumber.replaceFirst("^0+(?!$)", "");

            // But SKUs should be at least 12, pad 0s
            if (shortItemNumber.length() < 12) {
                shortItemNumber = ("000000000000" + shortItemNumber).substring(shortItemNumber.length());
            }
            return shortItemNumber;
        } else {
            // Remove trailing spaces
            return shortItemNumber.replaceFirst("\\s+$", "");
        }
    }
}
