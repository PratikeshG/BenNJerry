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
            // columns retrieved: itemNumber, category, itemDescription, suggestedPrice, upc, currency
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

                    if (marketingPlanId.length() < 1) {
                        continue;
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
                catalog.clearItemLocations();

                for (String marketingPlanId : marketingPlanLocationsCache.keySet()) {
                    String[] squareLocationIds = marketingPlanLocationsCache.get(marketingPlanId)
                            .toArray(new String[0]);

                    for (CSVItem updateItem : marketingPlanItemsCache.get(marketingPlanId)) {
                        if (updateItem.getUPC().length() < 2) {
                            continue;
                        }

                        CatalogObject matchingItem = catalog.getItem(updateItem.getUPC());
                        if (matchingItem == null) {
                            matchingItem = new CatalogObject("ITEM");
                        }

                        matchingItem.getItemData().setName(updateItem.getDescription());
                        CatalogItemVariation macthingItemVariation = matchingItem.getItemData().getVariations()[0]
                                .getItemVariationData();
                        macthingItemVariation.setSku(updateItem.getUPC());
                        macthingItemVariation.setName(updateItem.getNumber());

                        int price = Integer.parseInt(parsePrice(updateItem.getSuggestedPrice()));
                        macthingItemVariation.setPriceMoney(new Money(price));

                        matchingItem.setLocationPriceOverride(squareLocationIds, new Money(price), "FIXED_PRICING");
                        catalog.addItem(matchingItem);

                        CatalogObject[] o = { matchingItem };
                        catalog.enableObjectsAtLocations(o, squareLocationIds);
                    }
                }

                client.catalog().batchUpsertObjects(catalog.getObjects());
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
