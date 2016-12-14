package tntfireworks;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.squareup.connect.v2.Catalog;
import com.squareup.connect.v2.CatalogItemVariation;
import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.Money;
import com.squareup.connect.v2.SquareClientV2;

public class DatabaseToSquareCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(DatabaseToSquareCallable.class);

    // TODO(wtsang): generate dynamically by reading all possible values from DB
    private static final String[] CATEGORIES = new String[] { "ASSORTMENTS", "BASE FOUNTAINS", "CALIFORNIA FOUNTAINS",
            "CONE FOUNTAINS", "GROUND SPINNERS & CHASERS", "NOVELTIES", "SMOKE", "SPARKLERS", "PUNK", "FIRECRACKERS",
            "HELICOPTERS", "RELOADABLES", "MULTI-AERIALS", "SPECIAL AERIALS", "MISSILES", "PARACHUTES", "ROMAN CANDLES",
            "ROCKETS", "COUNTER CASES & DISPLAYS", "PROMOTIONAL", "SUB ASSESMBLIES", "MATERIALS", "TOYS" };

    private String databaseUrl;
    private String databaseUser;
    private String databasePassword;
    private String sftpHost;
    private int sftpPort;
    private String sftpUser;
    private String sftpPassword;
    private String apiUrl;
    private String activeDeployment;

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public void setDatabaseUser(String databaseUser) {
        this.databaseUser = databaseUser;
    }

    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

    public void setSftpHost(String sftpHost) {
        this.sftpHost = sftpHost;
    }

    public void setSftpPort(int sftpPort) {
        this.sftpPort = sftpPort;
    }

    public void setSftpUser(String sftpUser) {
        this.sftpUser = sftpUser;
    }

    public void setSftpPassword(String sftpPassword) {
        this.sftpPassword = sftpPassword;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public void setActiveDeployment(String activeDeployment) {
        this.activeDeployment = activeDeployment;
    }

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();
        logger.info("Starting database to Square sync...");

        // retrieve payload from prior step
        DatabaseToSquareRequest updateSQRequest = (DatabaseToSquareRequest) message.getPayload();
        message.setProperty("DatabaseToSquareRequest", updateSQRequest, PropertyScope.INVOCATION);

        // determine if marketing plans were updated OR locations
        // if locations were updated to db, nothing left to do except archive
        // parse filename - marketing plan or location file
        String filename = "";
        Pattern r = Pattern.compile("\\d+_(\\w+)_\\d+.csv");
        Matcher m = r.matcher(updateSQRequest.getProcessingFilename());
        m.find();

        // set name to matched group
        if (m.group(1) != null) {
            filename = m.group(1);
        }

        if (!filename.equals("locations")) {
            // ResultSets for SQL queries
            ResultSet resultDeployments = null;
            ResultSet resultItems = null;
            ResultSet resultLocations = null;

            // set up SQL connection
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);

            // get locations from db
            // columns retrieved: locationNumber, name
            HashMap<String, String> locationMarketingPlanCache = new HashMap<String, String>();
            resultLocations = submitQuery(conn, generateLocationSQLSelect());
            while (resultLocations.next()) {
                locationMarketingPlanCache.put(resultLocations.getString("locationNumber"),
                        resultLocations.getString("mktPlan"));
            }

            // get all items from db
            // columns retrieved: itemNumber, category, itemDescription,
            // suggestedPrice, upc, currency
            HashMap<String, List<CSVItem>> marketingPlanItemsCache = new HashMap<String, List<CSVItem>>();
            resultItems = submitQuery(conn, generateItemSQLSelect());
            while (resultItems.next()) {
                String mktPlan = resultItems.getString("mktPlan");
                List<CSVItem> itemList = marketingPlanItemsCache.get(mktPlan);

                if (itemList == null) {
                    itemList = new ArrayList<CSVItem>();
                    marketingPlanItemsCache.put(mktPlan, itemList);
                }

                CSVItem item = new CSVItem();
                item.setNumber(resultItems.getString("itemNumber"));
                item.setCategory(resultItems.getString("category"));
                item.setDescription(resultItems.getString("itemDescription"));
                item.setSuggestedPrice(resultItems.getString("suggestedPrice"));
                item.setUPC(resultItems.getString("upc"));
                item.setMarketingPlan(resultItems.getString("mktPlan"));
                itemList.add(item);
            }

            System.out.println("plans: " + marketingPlanItemsCache.keySet().size());
            for (String planId : marketingPlanItemsCache.keySet()) {
                System.out.println(planId + ": " + marketingPlanItemsCache.get(planId).size());
            }

            // get deployments from db
            // columns retrieved: connectApp, token
            resultDeployments = submitQuery(conn, generateDeploymentSQLSelect(activeDeployment));
            while (resultDeployments.next()) {
                SquareClientV2 client = new SquareClientV2(apiUrl, resultDeployments.getString("token"));
                HashMap<String, List<String>> marketingPlanLocationsCache = new HashMap<String, List<String>>();

                Location[] locations = client.locations().list();
                for (Location location : locations) {
                    String locationSquareId = location.getId();
                    String locationTNTId = getValueInParenthesis(location.getName());
                    String marketingPlanId = locationMarketingPlanCache.get(locationTNTId);

                    logger.info(String.format("locationSquareId (%s), locationTNTId (%s), marketingPlanId (%s)",
                            locationSquareId, locationTNTId, marketingPlanId));

                    if (locationTNTId.length() < 1) {
                        logger.warn(String.format(
                                "INVALID LOCATION ID: locationSquareId (%s), locationTNTId (%s), marketingPlanId (%s)",
                                locationSquareId, locationTNTId, marketingPlanId));
                        continue;
                    }

                    if (marketingPlanId == null) {
                        logger.warn(String.format(
                                "NO MARKETING PLAN ID: locationSquareId (%s), locationTNTId (%s), marketingPlanId (%s)",
                                locationSquareId, locationTNTId, marketingPlanId));
                    }

                    List<String> locationsList = marketingPlanLocationsCache.get(marketingPlanId);
                    if (locationsList == null) {
                        locationsList = new ArrayList<String>();
                        marketingPlanLocationsCache.put(marketingPlanId, locationsList);
                    }
                    locationsList.add(locationSquareId);
                }

                Catalog catalog = client.catalog().retrieveCatalog(Catalog.PrimaryKey.SKU, Catalog.PrimaryKey.NAME,
                        Catalog.PrimaryKey.ID, Catalog.PrimaryKey.NAME, Catalog.PrimaryKey.NAME);

                // 1. pull existing categories
                // 2. check if all defined categories exist
                // 3. if not, upsert
                HashMap<String, CatalogObject> existingCategories = (HashMap<String, CatalogObject>) catalog
                        .getCategories();

                // check if there are new categories to add
                for (String categoryName : CATEGORIES) {
                    // add category if it does not exist in Square
                    CatalogObject category = existingCategories.get(categoryName);
                    if (category == null) {
                        CatalogObject newCategory = new CatalogObject("CATEGORY");
                        newCategory.getCategoryData().setName(categoryName);
                        catalog.addCategory(newCategory);
                    }
                }

                // upsert new categories
                CatalogObject[] allCategories = catalog.getCategories().values().toArray(new CatalogObject[0]);
                client.catalog().batchUpsertObjects(allCategories);
                logger.info("Done checking/adding categories");

                // retrieve latest catalog with all categories now in account
                catalog = client.catalog().retrieveCatalog(Catalog.PrimaryKey.SKU, Catalog.PrimaryKey.NAME,
                        Catalog.PrimaryKey.ID, Catalog.PrimaryKey.NAME, Catalog.PrimaryKey.NAME);

                // reset all item <> location relationships - we will re-add these based on db item info
                catalog.clearItemLocations();

                // get the up-to-date categories
                existingCategories = (HashMap<String, CatalogObject>) catalog.getCategories();

                for (String marketingPlanId : marketingPlanLocationsCache.keySet()) {
                    String[] squareLocationIds = marketingPlanLocationsCache.get(marketingPlanId)
                            .toArray(new String[0]);

                    List<CSVItem> marketingPlanItems = marketingPlanItemsCache.get(marketingPlanId);
                    if (marketingPlanItems != null) {
                        for (CSVItem updateItem : marketingPlanItems) {
                            String sku = updateItem.getUPC();
                            if (sku == null || sku.length() < 2) {
                                sku = updateItem.getNumber();
                            }

                            CatalogObject matchingItem = catalog.getItem(sku);
                            if (matchingItem == null) {
                                matchingItem = new CatalogObject("ITEM");
                            }

                            matchingItem.getItemData().setName(updateItem.getDescription());
                            CatalogItemVariation macthingItemVariation = matchingItem.getItemData().getVariations()[0]
                                    .getItemVariationData();
                            macthingItemVariation.setSku(sku);
                            macthingItemVariation.setName(updateItem.getNumber());

                            int price = Integer.parseInt(parsePrice(updateItem.getSuggestedPrice()));
                            macthingItemVariation.setPriceMoney(new Money(price));

                            matchingItem.enableAtLocations(squareLocationIds);
                            matchingItem.setLocationPriceOverride(squareLocationIds, new Money(price), "FIXED_PRICING");

                            // check for matching category, if found, add category id
                            if (existingCategories.containsKey(updateItem.getCategory())) {
                                String categoryId = existingCategories.get(updateItem.getCategory()).getId();
                                matchingItem.getItemData().setCategoryId(categoryId);
                            }

                            catalog.addItem(matchingItem);
                        }
                    }
                }

                client.catalog().batchUpsertObjects(catalog.getObjects());
                logger.info("Done processing for account.");
            }

            conn.close();
        }

        // Need to move processingFile to archive
        if (updateSQRequest.isProcessing() && updateSQRequest.getProcessingFilename() != null) {
            archiveProcessingFile(updateSQRequest);
        }

        return null;
    }

    public String generateDeploymentSQLSelect(String deploymentName) {
        String query = "SELECT token FROM token WHERE deployment='" + deploymentName + "'";
        logger.info("Generated query: " + query);
        return query;
    }

    public String generateLocationSQLSelect() {
        String query = "SELECT locationNumber, name, mktPlan FROM tntfireworks_locations;";
        logger.info("Generated query: " + query);
        return query;
    }

    public String generateItemSQLSelect() {
        String query = "SELECT itemNumber, category, itemDescription, suggestedPrice, upc, mktPlan FROM tntfireworks_marketing_plans;";
        logger.info("Generated query: " + query);
        return query;
    }

    public ResultSet submitQuery(Connection conn, String query) throws SQLException {
        if (query.isEmpty()) {
            return null;
        }

        Statement stmt = conn.createStatement();
        return stmt.executeQuery(query);
    }

    private void archiveProcessingFile(DatabaseToSquareRequest updateSQRequest)
            throws JSchException, IOException, SftpException {
        ChannelSftp sftpChannel = SSHUtil.createConnection(sftpHost, sftpPort, sftpUser, sftpPassword);

        sftpChannel.rename(
                String.format("%s/%s", updateSQRequest.getProcessingPath(), updateSQRequest.getProcessingFilename()),
                String.format("%s/%s", updateSQRequest.getArchivePath(), updateSQRequest.getProcessingFilename()));

        SSHUtil.closeConnection(sftpChannel);
    }

    private static String parsePrice(String input) {
        input = input.replaceAll("[^\\d]", "");
        if (input.length() < 1) {
            input = "0";
        }
        return input;
    }

    private static String getValueInParenthesis(String input) {
        String value = "";
        if (input != null) {
            int firstIndex = input.indexOf('(');
            int lastIndex = input.indexOf(')');
            if (firstIndex > -1 && lastIndex > -1) {
                value = input.substring(firstIndex + 1, lastIndex);
                value = value.replaceAll("#", "");
            }
        }
        return value;
    }

}
