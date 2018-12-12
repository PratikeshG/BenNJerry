package tntfireworks;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import util.DbConnection;

public class TntDatabaseApi {
    private static Logger logger = LoggerFactory.getLogger(TntDatabaseApi.class);

    // table names
    public static final String DB_TOKEN = "token";
    public static final String DB_LOAD_NUMBER = "tntfireworks_reports_load_number";
    public static final String DB_MKT_PLAN = "tntfireworks_marketing_plans";
    public static final String DB_LOCATION = "tntfireworks_locations";

    // load number is specifically used for tntfireworks report 8 (settlements report) and used
    // internally by TNT; the load number is tracked in a database
    public static final String DB_LOAD_NUMBER_REPORT_NAME_COLUMN = "reportName";
    public static final String DB_LOAD_NUMBER_COUNT_COLUMN = "count";
    public static final String DB_LOAD_NUMBER_REPORT8_NAME = "CreditDebitBatch";

    // Marketing Plan DB constants
    public static final String DB_MKT_PLAN_CATEGORY_COLUMN = "category";
    public static final String DB_MKT_PLAN_NAME_COLUMN = "mktPlan";
    public static final String DB_MKT_PLAN_CURRENCY_COLUMN = "currency";
    public static final String DB_MKT_PLAN_HALF_OFF_COLUMN = "halfOff";
    public static final String DB_MKT_PLAN_SELLING_PRICE_COLUMN = "sellingPrice";
    public static final String DB_MKT_PLAN_UPC_COLUMN = "upc";
    public static final String DB_MKT_PLAN_ITEM_NUMBER_COLUMN = "itemNumber";
    public static final String DB_MKT_PLAN_ITEM_DESCRIPTION_COLUMN = "itemDescription";

    // Locations DB constants
    public static final String DB_LOCATION_LOCATION_NUMBER_COLUMN = "locationNumber";
    public static final String DB_LOCATION_CITY_COLUMN = "city";
    public static final String DB_LOCATION_STATE_COLUMN = "state";
    public static final String DB_LOCATION_RBU_COLUMN = "rbu";
    public static final String DB_LOCATION_ZIP_COLUMN = "zip";
    public static final String DB_LOCATION_SA_NAME_COLUMN = "saName";
    public static final String DB_LOCATION_SA_NUMBER_COLUMN = "saNum";

    private DbConnection dbConnection;

    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}")
    private String databaseUrl;
    @Value("${mysql.user}")
    private String databaseUser;
    @Value("${mysql.password}")
    private String databasePassword;

    public TntDatabaseApi(DbConnection conn) {
        dbConnection = conn;
    }

    public TntDatabaseApi() {
        // initialize db connection
        try {
            dbConnection = new DbConnection(databaseUrl, databaseUser, databasePassword);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating DbConnection: " + e);
        }
    }

    public ArrayList<Map<String, String>> submitQuery(String query) {
        ResultSet resultSet;
        try {
            resultSet = dbConnection.submitQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("SQLException");
        }

        ArrayList<Map<String, String>> rows = new ArrayList<>();
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int numColumns = metaData.getColumnCount();
            while (resultSet.next()) {
                Map<String, String> row = new HashMap<>();
                for (int i = 1; i < numColumns + 1; i++) {
                    row.put(metaData.getColumnName(i), resultSet.getString(i));
                }
                rows.add(row);
            }
            return rows;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public String generateDeploymentSQLSelect(String deploymentName) {
        String query = String.format("SELECT * FROM %s WHERE deployment='%s'", DB_TOKEN, deploymentName);
        logger.info("Generated query: " + query);
        return query;
    }

    public String generateLocationSQLSelect() {
        String query = String.format("SELECT * FROM %s", DB_LOCATION);
        logger.info("Generated query: " + query);
        return query;
    }

    public String generateItemSQLSelect() {
        String query = String.format("SELECT %s, %s, %s, %s, %s, %s, %s, %s FROM %s", DB_MKT_PLAN_ITEM_NUMBER_COLUMN,
                DB_MKT_PLAN_CATEGORY_COLUMN, DB_MKT_PLAN_ITEM_DESCRIPTION_COLUMN, DB_MKT_PLAN_UPC_COLUMN,
                DB_MKT_PLAN_NAME_COLUMN, DB_MKT_PLAN_CURRENCY_COLUMN, DB_MKT_PLAN_HALF_OFF_COLUMN,
                DB_MKT_PLAN_SELLING_PRICE_COLUMN, DB_MKT_PLAN);
        logger.info("Generated query: " + query);
        return query;
    }

    public String generateLoadNumberSQLSelect() {
        String query = String.format("SELECT %s, %s FROM %s", DB_LOAD_NUMBER_REPORT_NAME_COLUMN,
                DB_LOAD_NUMBER_COUNT_COLUMN, DB_LOAD_NUMBER);
        logger.info("Generated query: " + query);
        return query;
    }

    public void close() throws SQLException {
        dbConnection.close();
    }
}
