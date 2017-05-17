package tntfireworks;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.DbConnection;

public class TntDatabaseApi {

    private static Logger logger = LoggerFactory.getLogger(TntDatabaseApi.class);
    private DbConnection dbConnection;

    public TntDatabaseApi(DbConnection conn) {
        dbConnection = conn;
    }

    public TntDatabaseApi() {
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
        String query = "SELECT * FROM token WHERE deployment='" + deploymentName + "'";
        logger.info("Generated query: " + query);
        return query;
    }

    public String generateLocationSQLSelect() {
        String query = "SELECT * FROM tntfireworks_locations;";
        logger.info("Generated query: " + query);
        return query;
    }

    public String generateItemSQLSelect() {
        String query = "SELECT itemNumber, category, itemDescription, upc, mktPlan, currency, halfOff, sellingPrice FROM tntfireworks_marketing_plans;";
        logger.info("Generated query: " + query);
        return query;
    }

    public String generateLoadNumberSQLSelect() {
        String query = "SELECT reportName, count FROM tntfireworks_reports_load_number;";
        logger.info("Generated query: " + query);
        return query;
    }

    public void close() throws SQLException {
        dbConnection.close();
    }
}
