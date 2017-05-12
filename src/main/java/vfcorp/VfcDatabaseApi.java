package vfcorp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.TimeManager;

public class VfcDatabaseApi {

    private static Logger logger = LoggerFactory.getLogger(VfcDatabaseApi.class);
    private Connection connection;

    public VfcDatabaseApi(Connection connection) {
        this.connection = connection;
    }

    public VfcDatabaseApi() {
    }

    public ArrayList<Map<String, String>> executeQuery(String query) throws SQLException {
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
        } catch (SQLException e) {
            if (statement != null) {
                statement.close();
            }

            e.printStackTrace();
            throw e;
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
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
        }
    }

    public ArrayList<Map<String, String>> queryDbDeptClass(String brand) throws SQLException, IOException {
        return executeQuery(generatePluDeptClassSQLSelect(brand));
    }

    public ArrayList<Map<String, String>> queryDbItems(String locationId, boolean pluFiltered)
            throws SQLException, IOException {
        return executeQuery(generatePluItemsSQLSelect(locationId, pluFiltered));
    }

    public ArrayList<Map<String, String>> queryDbItemSaleEvents(String locationId, String timeZone)
            throws SQLException, IOException, ParseException {
        return executeQuery(generatePluItemSaleEventsSQLSelect(locationId, timeZone));
    }

    public String generatePluDeptClassSQLSelect(String brand) throws IOException {
        String query = String.format(
                "SELECT deptNumber, classNumber, description FROM vfcorp_plu_dept_class WHERE deployment LIKE 'vfcorp-%s-%%' GROUP BY deptNumber, classNumber, description",
                brand);

        logger.debug(String.format("Generated query for brand (%s): %s", brand, query));

        return query;
    }

    public String generatePluItemsSQLSelect(String locationId, boolean pluFiltered) throws IOException {
        String query = String.format("SELECT * FROM vfcorp_plu_items WHERE locationId = '%s'", locationId);

        if (pluFiltered) {
            logger.info("Applying SKU whitelist filter");
            query += String.format(" AND itemNumber IN (%s)", getFilteredSKUQueryString());
        }
        logger.debug(String.format("Generated query for location %s: %s", locationId, query));

        return query;
    }

    public String generatePluItemSaleEventsSQLSelect(String locationId, String timeZone)
            throws IOException, ParseException {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        String nowDate = TimeManager.toSimpleDateTimeInTimeZone(cal, timeZone, "MMddyyyy");

        String query = "SELECT events.itemNumber as itemNumber, events.salePrice as salePrice "
                + "FROM vfcorp_plu_sale_events as events " + "JOIN " + "     (SELECT itemNumber, MAX(id) as id "
                + "     FROM vfcorp_plu_sale_events " + "     WHERE locationId = '" + locationId + "' AND "
                + "     STR_TO_DATE('" + nowDate + "', '%m%d%Y') >= STR_TO_DATE(dateSaleBegins, '%m%d%Y') AND "
                + "     STR_TO_DATE('" + nowDate + "', '%m%d%Y') <= STR_TO_DATE(dateSaleEnds, '%m%d%Y') "
                + "     GROUP BY itemNumber) as newest ON events.id = newest.id";

        logger.debug(String.format("Generated query for location %s: %s", locationId, query));

        return query;
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

    public void close() throws SQLException {
        connection.close();
    }
}
