package tntfireworks;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.SquarePayload;

public class DeploymentsCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(DeploymentsCallable.class);

    private String databaseUrl;
    private String databaseUser;
    private String databasePassword;
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

    public void setActiveDeployment(String activeDeployment) {
        this.activeDeployment = activeDeployment;
    }

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        logger.info("Starting database to Square sync...");

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
        message.setProperty("locationMarketingPlanCache", locationMarketingPlanCache, PropertyScope.SESSION);

        // get all items from db
        // columns retrieved: itemNumber, category, itemDescription,
        // suggestedPrice, upc, currency
        HashMap<String, List<CsvItem>> marketingPlanItemsCache = new HashMap<String, List<CsvItem>>();
        resultItems = submitQuery(conn, generateItemSQLSelect());
        while (resultItems.next()) {
            String mktPlan = resultItems.getString("mktPlan");
            List<CsvItem> itemList = marketingPlanItemsCache.get(mktPlan);

            if (itemList == null) {
                itemList = new ArrayList<CsvItem>();
                marketingPlanItemsCache.put(mktPlan, itemList);
            }

            CsvItem item = new CsvItem();
            item.setNumber(resultItems.getString("itemNumber"));
            item.setCategory(resultItems.getString("category"));
            item.setDescription(resultItems.getString("itemDescription"));
            item.setSuggestedPrice(resultItems.getString("suggestedPrice"));
            item.setUPC(resultItems.getString("upc"));
            item.setMarketingPlan(resultItems.getString("mktPlan"));
            itemList.add(item);
        }
        message.setProperty("marketingPlanItemsCache", marketingPlanItemsCache, PropertyScope.SESSION);

        System.out.println("plans: " + marketingPlanItemsCache.keySet().size());
        for (String planId : marketingPlanItemsCache.keySet()) {
            System.out.println(planId + ": " + marketingPlanItemsCache.get(planId).size());
        }

        // get deployments from db
        // columns retrieved: connectApp, token
        List<SquarePayload> deploymentPayloads = new ArrayList<SquarePayload>();
        resultDeployments = submitQuery(conn, generateDeploymentSQLSelect(activeDeployment));
        while (resultDeployments.next()) {
            SquarePayload deploymentPayload = new SquarePayload();
            deploymentPayload.setAccessToken(resultDeployments.getString("token"));
            deploymentPayload.setAccessToken(resultDeployments.getString("merchantId"));
            deploymentPayload.setAccessToken(resultDeployments.getString("merchantAlias"));
            deploymentPayloads.add(deploymentPayload);
        }

        conn.close();

        return deploymentPayloads;
    }

    public String generateDeploymentSQLSelect(String deploymentName) {
        String query = "SELECT * FROM token WHERE deployment='" + deploymentName + "'";
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
}
