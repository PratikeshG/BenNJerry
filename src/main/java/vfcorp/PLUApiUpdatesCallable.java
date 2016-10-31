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
import java.util.HashSet;
import java.util.StringJoiner;
import java.util.TimeZone;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.jdbc.ResultSet;
import com.squareup.connect.Category;
import com.squareup.connect.Discount;
import com.squareup.connect.Fee;
import com.squareup.connect.Item;
import com.squareup.connect.ItemVariation;
import com.squareup.connect.Money;
import com.squareup.connect.Page;
import com.squareup.connect.PageCell;
import com.squareup.connect.SquareClient;
import com.squareup.connect.diff.Catalog;
import com.squareup.connect.diff.CatalogChangeRequest;

import util.SquarePayload;
import util.TimeManager;

public class PLUApiUpdatesCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(PLUApiUpdatesCallable.class);

    private String databaseUrl;
    private String databaseUser;
    private String databasePassword;
    private int itemNumberLookupLength;

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public void setDatabaseUser(String databaseUser) {
        this.databaseUser = databaseUser;
    }

    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

    public void setItemNumberLookupLength(int itemNumberLookupLength) {
        this.itemNumberLookupLength = itemNumberLookupLength;
    }

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        String deploymentId = message.getProperty("deployment", PropertyScope.INVOCATION);
        String apiUrl = message.getProperty("apiUrl", PropertyScope.SESSION);
        String timeZone = message.getProperty("timeZone", PropertyScope.INVOCATION);
        boolean filteredPlu = message.getProperty("filteredPlu", PropertyScope.INVOCATION).equals("true") ? true
                : false;
        SquarePayload payload = (SquarePayload) message.getPayload();

        SquareClient client = new SquareClient(payload.getAccessToken(), apiUrl, "v1", payload.getMerchantId(),
                payload.getLocationId());

        Catalog currentCatalog = getCurrentCatalog(client);

        Connection conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
        Catalog proposedCatalog = getProposedCatalog(conn, currentCatalog, deploymentId, payload.getLocationId(),
                timeZone, filteredPlu);
        conn.close();

        CatalogChangeRequest ccr = diffAndLogChanges(currentCatalog, proposedCatalog, deploymentId);

        logger.info(String.format("(%s) Updating account...", deploymentId));
        ccr.setSquareClient(client);
        ccr.call();

        // TODO(bhartard): Determine how to handle fav page/cells after
        // migration to V2 APIs
        updateFavoritesGrid(client);

        logger.info(String.format("(%s) Done updating account.", deploymentId));

        return null;
    }

    private CatalogChangeRequest diffAndLogChanges(Catalog current, Catalog proposed, String deploymentId) {
        logger.info(String.format("(%s) Current categories total: %d", deploymentId, current.getCategories().size()));
        logger.info(String.format("(%s) Current discounts total: %d", deploymentId, current.getDiscounts().size()));
        logger.info(String.format("(%s) Current fees total: %d", deploymentId, current.getFees().size()));
        logger.info(String.format("(%s) Current items total: %d", deploymentId, current.getItems().size()));

        logger.info(String.format("(%s) Proposed categories total: %d", deploymentId, proposed.getCategories().size()));
        logger.info(String.format("(%s) Proposed discounts total: %d", deploymentId, proposed.getDiscounts().size()));
        logger.info(String.format("(%s) Proposed fees total: %d", deploymentId, proposed.getFees().size()));
        logger.info(String.format("(%s) Proposed items total: %d", deploymentId, proposed.getItems().size()));

        logger.info(String.format("(%s) Performing diff...", deploymentId));
        CatalogChangeRequest ccr = CatalogChangeRequest.diff(current, proposed, CatalogChangeRequest.PrimaryKey.SKU,
                CatalogChangeRequest.PrimaryKey.NAME);
        logger.info(String.format("(%s) Diff complete.", deploymentId));

        logger.info(String.format("(%s) Diff new mappings total: %d", deploymentId,
                ccr.getMappingsToApply().keySet().size()));
        logger.info(String.format("(%s) Diff create total: %d", deploymentId, ccr.getObjectsToCreate().size()));
        logger.info(String.format("(%s) Diff update total: %d", deploymentId, ccr.getObjectsToUpdate().size()));

        return ccr;
    }

    private void updateFavoritesGrid(SquareClient client) throws Exception {
        Page[] pages = client.pages().list();
        PageCell discountCell = discountCell();

        HashSet<Object> ignoreFields = new HashSet<Object>();
        ignoreFields.add(PageCell.Field.PAGE_ID);

        String pageId = "";
        PageCell currentCell = null;

        for (Page page : pages) {
            if (page.getPageIndex() == 0) {
                pageId = page.getId();
                for (PageCell cell : page.getCells()) {
                    if (cell.getRow() == 0 && cell.getColumn() == 0) {
                        currentCell = cell;
                        break;
                    }
                }
            }
        }

        if (pageId.length() > 0 && (currentCell == null || !currentCell.equals(discountCell, ignoreFields))) {
            logger.info("Updating discount favorites cell...");
            client.cells().update(pageId, discountCell);
        }
    }

    private PageCell discountCell() {
        PageCell cell = new PageCell();
        cell.setRow(0);
        cell.setColumn(0);
        cell.setObjectType("PLACEHOLDER");
        cell.setPlaceholderType("DISCOUNTS_CATEGORY");
        return cell;
    }

    private Catalog getCurrentCatalog(SquareClient client) throws Exception {
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

    private Catalog getProposedCatalog(Connection conn, Catalog current, String deploymentId, String locationId,
            String timeZone, boolean filtered) throws Exception {
        Catalog catalog = new Catalog(current);

        // Categories
        ResultSet dbCategoryCursor = getDBDeptClass(conn, locationId);
        while (dbCategoryCursor.next()) {
            convertCategory(catalog, dbCategoryCursor);
        }

        // Discounts
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
            applySalePrice(catalog, dbItemSaleCursor);
        }

        return catalog;
    }

    private void applySalePrice(Catalog catalog, ResultSet record) throws Exception {
        String sku = convertItemNumberIntoSku(record.getString("itemNumber"));

        Item item = catalog.getItems().get(sku);
        ItemVariation variation = (item != null) ? item.getVariations()[0] : null;

        if (variation != null && variation.getSku().equals(sku)) {
            int price = Integer.parseInt(record.getString("salePrice"));
            if (price > 0) {
                variation.setPriceMoney(new Money(price));
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
                + "     STR_TO_DATE('" + nowDate + "', '%m%d%Y') < STR_TO_DATE(dateSaleEnds, '%m%d%Y') "
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
