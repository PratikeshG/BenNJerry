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
import java.util.Calendar;
import java.util.HashMap;
import java.util.StringJoiner;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.jdbc.ResultSet;
import com.squareup.connect.Category;
import com.squareup.connect.Discount;
import com.squareup.connect.Fee;
import com.squareup.connect.Item;
import com.squareup.connect.ItemVariation;
import com.squareup.connect.Money;
import com.squareup.connect.SquareClient;
import com.squareup.connect.diff.Catalog;
import com.squareup.connect.diff.CatalogChangeRequest;

import util.TimeManager;

public class PLUCatalogBuilder {
    private static Logger logger = LoggerFactory.getLogger(PLUCatalogBuilder.class);

    private String databaseUrl;
    private String databaseUser;
    private String databasePassword;
    private int itemNumberLookupLength;

    public void setItemNumberLookupLength(int itemNumberLookupLength) {
        this.itemNumberLookupLength = itemNumberLookupLength;
    }

    public PLUCatalogBuilder(String databaseUrl, String databaseUser, String databasePassword) {
        this.databaseUrl = databaseUrl;
        this.databaseUser = databaseUser;
        this.databasePassword = databasePassword;
        this.itemNumberLookupLength = 14;
    }

    public Catalog newCatalogFromSquare(SquareClient client) throws Exception {
        Catalog catalog = new Catalog();

        Item[] items = client.items().list();
        for (Item item : items) {
            catalog.addItem(item, CatalogChangeRequest.PrimaryKey.SKU);
        }

        Category[] categories = client.categories().list();
        for (Category category : categories) {
            catalog.addCategory(category, CatalogChangeRequest.PrimaryKey.NAME);
        }

        Fee[] fees = client.fees().list();
        for (Fee fee : fees) {
            catalog.addFee(fee); // default primary is ID
        }

        Discount[] discounts = client.discounts().list();
        for (Discount discount : discounts) {
            catalog.addDiscount(discount, CatalogChangeRequest.PrimaryKey.NAME);
        }

        return catalog;
    }

    public Catalog newCatalogFromDatabase(Catalog current, String deploymentId, String locationId, String timeZone,
            boolean filtered) throws Exception {

        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
        Catalog catalog = new Catalog(current);

        // Categories
        ResultSet dbDeptClassCursor = getDBDeptClass(conn, locationId);
        while (dbDeptClassCursor.next()) {
            convertCategory(catalog, dbDeptClassCursor);
        }

        // Ignore existing discounts and only load expected defaults
        catalog.setDiscounts(new HashMap<String, Discount>());
        for (Discount discount : getDefaultDiscounts()) {
            catalog.addDiscount(discount, CatalogChangeRequest.PrimaryKey.NAME);
        }

        // Items
        ResultSet dbItemCursor = getDBItems(conn, locationId, filtered);
        int rowcount = 0;
        if (dbItemCursor.last()) {
            rowcount = dbItemCursor.getRow();
            dbItemCursor.beforeFirst();
        }
        logger.info(String.format("(%s) Total items from DB: %d", deploymentId, rowcount));
        while (dbItemCursor.next()) {
            convertItem(catalog, dbItemCursor, deploymentId);
        }

        // Sale Events
        ResultSet dbItemSaleCursor = getDBItemSaleEvents(conn, locationId, timeZone);
        while (dbItemSaleCursor.next()) {
            applySalePrice(catalog, dbItemSaleCursor, deploymentId);
        }

        conn.close();

        return catalog;
    }

    private void applySalePrice(Catalog catalog, ResultSet record, String deploymentId) throws Exception {
        String sku = convertItemNumberIntoSku(record.getString("itemNumber"));

        Item item = catalog.getItems().get(sku);
        ItemVariation variation = (item != null) ? item.getVariations()[0] : null;

        if (variation != null && variation.getSku().equals(sku)) {
            int price = Integer.parseInt(record.getString("salePrice"));
            if (price > 0) {
                variation.setPriceMoney(new Money(price));

                // re-calculate item-specific taxes
                if (catalog.getFees().values().size() > 0) {
                    String deptCodeClass = Util.getValueInParenthesis(variation.getName());
                    item.setFees(TaxRules.getItemTaxesForLocation(deploymentId,
                            catalog.getFees().values().toArray(new Fee[0]), price, deptCodeClass));
                }
            }
        }
    }

    private Item getMatchingOrNewItem(Catalog catalog, String sku) {
        Item matchingItem = null;

        Item item = catalog.getItems().get(sku);
        if (item != null) {
            if (sku.equals(item.getVariations()[0].getSku())) {
                matchingItem = item;
            }
        }

        if (matchingItem == null) {
            matchingItem = new Item();
            ItemVariation newVariation = new ItemVariation("Regular");
            newVariation.setSku(sku);
            matchingItem.setVariations(new ItemVariation[] { newVariation });
        }

        return matchingItem;
    }

    private void convertItem(Catalog catalog, ResultSet record, String deploymentId) throws Exception {
        String sku = convertItemNumberIntoSku(record.getString("itemNumber"));

        Item matchingItem = getMatchingOrNewItem(catalog, sku);
        ItemVariation matchingVariation = matchingItem.getVariations()[0];

        int price = Integer.parseInt(record.getString("retailPrice"));
        if (price > 0) {
            matchingVariation.setPriceMoney(new Money(price));
        }

        String deptCodeClass = String.format("%-4s", record.getString("deptNumber"))
                + String.format("%-4s", record.getString("classNumber"));
        matchingVariation.setName(String.format("%s (%s)", sku, deptCodeClass));

        for (Category category : catalog.getCategories().values()) {
            if (category.getName().subSequence(0, 8).equals(deptCodeClass)) {
                matchingItem.setCategory(category);
                break;
            }
        }

        // Only update name if it looks like it has changed
        String itemName = record.getString("description").replaceFirst("\\s+$", "");
        if (!matchingItem.getName().startsWith(itemName)) {
            String altDescription = (record.getString("alternateDescription") != null)
                    ? record.getString("alternateDescription").trim() : "";
            itemName = (altDescription.length() > itemName.length()) ? altDescription : itemName;
            matchingItem.setName(itemName);
        }

        // Apply item-specific taxes
        if (catalog.getFees().values().size() > 0) {
            matchingItem.setFees(TaxRules.getItemTaxesForLocation(deploymentId,
                    catalog.getFees().values().toArray(new Fee[0]), price, deptCodeClass));
        }

        catalog.addItem(matchingItem, CatalogChangeRequest.PrimaryKey.SKU);
    }

    private void convertCategory(Catalog catalog, ResultSet record) throws SQLException {
        String deptNumber = String.format("%-4s", record.getString("deptNumber"));
        String classNumber = String.format("%-4s", record.getString("classNumber"));
        String categoryName = deptNumber + classNumber + " " + record.getString("description").trim();

        Category matchingCategory = null;
        for (String key : catalog.getCategories().keySet()) {
            Category category = catalog.getCategories().get(key);
            if (category.getName().substring(0, 8).equals(categoryName.substring(0, 8))) {
                matchingCategory = category;
                break;
            }
        }

        if (matchingCategory == null) {
            matchingCategory = new Category();
        }

        matchingCategory.setName(categoryName);
        catalog.addCategory(matchingCategory, CatalogChangeRequest.PrimaryKey.NAME);
    }

    private ResultSet executeQuery(Connection conn, String query) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet result = (ResultSet) stmt.executeQuery(query);

        return result;
    }

    private ResultSet getDBDeptClass(Connection conn, String locationId) throws SQLException {
        String query = String.format("SELECT * FROM vfcorp_plu_dept_class WHERE locationId = '%s'", locationId);
        return executeQuery(conn, query);
    }

    private ResultSet getDBItems(Connection conn, String locationId, boolean filtered)
            throws SQLException, IOException {
        String query = String.format("SELECT * FROM vfcorp_plu_items WHERE locationId = '%s'", locationId);

        if (filtered) {
            logger.info("Applying SKU filter... ");
            query += String.format(" AND itemNumber IN (%s)", getFilteredSKUQueryString());
        }

        return executeQuery(conn, query);
    }

    private ResultSet getDBItemSaleEvents(Connection conn, String locationId, String timeZone)
            throws SQLException, IOException, ParseException {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        String nowDate = TimeManager.toSimpleDateTimeInTimeZone(cal, timeZone, "MMddyyyy");

        String query = "SELECT events.itemNumber as itemNumber, events.salePrice as salePrice "
                + "FROM vfcorp_plu_sale_events as events " + "JOIN " + "     (SELECT itemNumber, MAX(id) as id "
                + "     FROM vfcorp_plu_sale_events " + "     WHERE locationId = '" + locationId + "' AND "
                + "     STR_TO_DATE('" + nowDate + "', '%m%d%Y') >= STR_TO_DATE(dateSaleBegins, '%m%d%Y') AND "
                + "     STR_TO_DATE('" + nowDate + "', '%m%d%Y') <= STR_TO_DATE(dateSaleEnds, '%m%d%Y') "
                + "     GROUP BY itemNumber) as newest ON events.id = newest.id";

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

        logger.info("Total SKU filtered: " + skuFilter.size());

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

    private Discount[] getDefaultDiscounts() {
        Discount[] discounts = new Discount[] { newDiscount("40% Associate Discount [21000]", "FIXED", "0.40", 0),
                newDiscount("50% Associate Discount [21000]", "FIXED", "0.50", 0),

                newDiscount("Item % Customer Accommodation [00122]", "VARIABLE_PERCENTAGE", "0", 0),
                newDiscount("Item % Damaged [00123]", "VARIABLE_PERCENTAGE", "0", 0),
                newDiscount("Item % In Store Coupon [00124]", "VARIABLE_PERCENTAGE", "0", 0),
                newDiscount("Item % Mall Coupon [00125]", "VARIABLE_PERCENTAGE", "0", 0),
                newDiscount("Item % Other [00126]", "VARIABLE_PERCENTAGE", "0", 0),
                newDiscount("Item % Post Card Promo [00120]", "VARIABLE_PERCENTAGE", "0", 0),
                newDiscount("Item % Price Match [00121]", "VARIABLE_PERCENTAGE", "0", 0),

                newDiscount("Transaction % Customer Accommodation [01142]", "VARIABLE_PERCENTAGE", "0", 0),
                newDiscount("Transaction % Damaged [01143]", "VARIABLE_PERCENTAGE", "0", 0),
                newDiscount("Transaction % In Store Coupon [01144]", "VARIABLE_PERCENTAGE", "0", 0),
                newDiscount("Transaction % Mall Coupon [01145]", "VARIABLE_PERCENTAGE", "0", 0),
                newDiscount("Transaction % Military [01150]", "VARIABLE_PERCENTAGE", "0", 0),
                newDiscount("Transaction % Other [01146]", "VARIABLE_PERCENTAGE", "0", 0),
                newDiscount("Transaction % Post Card Promo [01140]", "VARIABLE_PERCENTAGE", "0", 0),
                newDiscount("Transaction % Price Match [01141]", "VARIABLE_PERCENTAGE", "0", 0),

                newDiscount("Transaction $ Customer Accommodation [01132]", "VARIABLE_AMOUNT", null, 0),
                newDiscount("Transaction $ Damaged [01133]", "VARIABLE_AMOUNT", null, 0),
                newDiscount("Transaction $ In Store Coupon [01134]", "VARIABLE_AMOUNT", null, 0),
                newDiscount("Transaction $ Mall Coupon [01135]", "VARIABLE_AMOUNT", null, 0),
                newDiscount("Transaction $ Other [01136]", "VARIABLE_AMOUNT", null, 0),
                newDiscount("Transaction $ Post Card Promo [01130]", "VARIABLE_AMOUNT", null, 0),
                newDiscount("Transaction $ Price Match [01131]", "VARIABLE_AMOUNT", null, 0) };

        return discounts;
    }

    private Discount newDiscount(String name, String type, String rate, int amount) {
        Discount d = new Discount();

        d.setName(name);
        d.setDiscountType(type);

        // Do not include this field for amount-based discounts.
        if (rate != null) {
            d.setRate(rate);
        } else {
            // Do not include this field for rate-based discounts.
            Money money = new Money(amount, "USD");
            d.setAmountMoney(money);
        }

        return d;
    }
}
