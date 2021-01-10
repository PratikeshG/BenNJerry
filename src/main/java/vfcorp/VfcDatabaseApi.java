package vfcorp;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.v2.TeamMember;

import util.TimeManager;

public class VfcDatabaseApi {
    private static Logger logger = LoggerFactory.getLogger(VfcDatabaseApi.class);
    private Connection connection;

    private static String WHITELIST_FILTER_BRAND_FORMAT = "vfcorp-%s";

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

    public int executeUpdate(String query) throws SQLException {
        if (query.isEmpty()) {
            return 0;
        }

        Statement statement = null;
        int response;

        try {
            statement = connection.createStatement();
            response = statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (statement != null) {
                statement.close();
            }
        }

        return response;
    }

    public int setWhitelistForBrand(String deployment, List<String> whitelist)
            throws SQLException, IOException, ParseException {
        return executeUpdate(generateWhitelistUpsertSQL(deployment, whitelist));
    }

    public int setWhitelistForBrandStore(String deployment, String storeId, List<String> whitelist)
            throws SQLException, IOException, ParseException {
        return executeUpdate(generateWhitelistUpsertStoreSQL(deployment, storeId, whitelist));
    }

    public List<String> getWhitelistForBrand(String deployment) throws SQLException, IOException, ParseException {
        ArrayList<String> whitelist = new ArrayList<String>();
        ArrayList<Map<String, String>> executedQuery = executeQuery(generateWhitelistSelectSQL(deployment));

        for (Map<String, String> whitelistRecord : executedQuery) {
            whitelist.add(whitelistRecord.get("productId"));
        }

        return whitelist;
    }

    public List<String> getWhitelistForBrandStore(String deployment, String storeId)
            throws SQLException, IOException, ParseException {
        ArrayList<String> whitelist = new ArrayList<String>();
        ArrayList<Map<String, String>> executedQuery = executeQuery(
                generateWhitelistSelectStoreSQL(deployment, storeId));

        for (Map<String, String> whitelistRecord : executedQuery) {
            whitelist.add(whitelistRecord.get("productId"));
        }

        return whitelist;
    }

    public int deleteEmployeesForBrand(String deployment) throws SQLException, IOException, ParseException {
        return executeUpdate(generateEmployeeDeleteSQL(deployment));
    }

    public int setEmployeesForBrand(String deployment, TeamMember[] employees)
            throws SQLException, IOException, ParseException {
        return executeUpdate(generateEmployeeUpsertSQL(deployment, employees));
    }

    public Map<String, String> getEmployeeIdsForBrand(String deployment)
            throws SQLException, IOException, ParseException {
        Map<String, String> employees = new HashMap<String, String>();
        ArrayList<Map<String, String>> executedQuery = executeQuery(generateEmployeeSelectSQL(deployment));

        for (Map<String, String> employeeRecord : executedQuery) {
            employees.put(employeeRecord.get("employeeId"), employeeRecord.get("externalId"));
        }

        return employees;
    }

    public int deleteWhitelistForBrand(String deployment) throws SQLException, IOException, ParseException {
        return executeUpdate(generateWhitelistDeleteBrandSQL(deployment));
    }

    public int deleteWhitelistForBrandStore(String deployment, String storeId)
            throws SQLException, IOException, ParseException {
        return executeUpdate(generateWhitelistDeleteBrandStoreSQL(deployment, storeId));
    }

    public ArrayList<Map<String, String>> queryDbDeptClass(String brand) throws SQLException, IOException {
        return executeQuery(generatePluDeptClassSQLSelect(brand));
    }

    public ArrayList<Map<String, String>> queryDbItems(String locationId, boolean pluFiltered, String brand)
            throws SQLException, IOException {
        return executeQuery(generatePluItemsSQLSelect(locationId, pluFiltered, brand));
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

    public String generatePluItemsSQLSelect(String locationId, boolean pluFiltered, String brand) throws IOException {
        String query = String.format(
                "SELECT itemNumber, description, alternateDescription, retailPrice, deptNumber, classNumber FROM vfcorp_plu_items WHERE locationId = '%s'",
                locationId);

        if (pluFiltered) {
            logger.info("Applying SKU whitelist filter");
            String filteredQuery = String.format(
                    " AND itemNumber IN (SELECT productId as itemNumber FROM product_whitelist WHERE deployment = '%s')",
                    String.format(WHITELIST_FILTER_BRAND_FORMAT, brand));
            query += filteredQuery;
        }
        logger.info(String.format("Generated query for location %s: %s", locationId, query));

        return query;
    }

    public String generatePluItemsByStoreSQLSelect(String locationId, boolean pluFiltered, String brand, String storeId)
            throws IOException {
        String query = String.format(
                "SELECT itemNumber, description, alternateDescription, retailPrice, deptNumber, classNumber FROM vfcorp_plu_items WHERE locationId = '%s'",
                locationId);

        if (pluFiltered) {
            logger.info("Applying SKU whitelist filter");
            String filteredQuery = String.format(
                    " AND itemNumber IN (SELECT productId as itemNumber FROM product_whitelist WHERE deployment = '%s' AND storeId = '%s')",
                    String.format(WHITELIST_FILTER_BRAND_FORMAT, brand), storeId);
            query += filteredQuery;
        }
        logger.info(String.format("Generated query for location %s: %s", locationId, query));

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

    public String generatePreferredCustomerSQLUpsert(Map<String, Integer> nextPreferredCustomerIds, String deployment,
            String storeId) {
        String updateStatement = "";

        if (nextPreferredCustomerIds.size() > 0) {
            updateStatement = "INSERT INTO vfcorp_preferred_customer_counter (deployment, storeId, registerId, nextPreferredCustomerNumber) VALUES ";

            ArrayList<String> updates = new ArrayList<String>();
            for (Map.Entry<String, Integer> entry : nextPreferredCustomerIds.entrySet()) {
                updates.add(
                        String.format("('%s', '%s', '%s', %d)", deployment, storeId, entry.getKey(), entry.getValue()));
            }

            Iterator<String> i = updates.iterator();
            if (i.hasNext()) {
                updateStatement += i.next();
                while (i.hasNext()) {
                    updateStatement += ", " + i.next();
                }
            }

            updateStatement += " ON DUPLICATE KEY UPDATE nextPreferredCustomerNumber=VALUES(nextPreferredCustomerNumber);";
        }

        return updateStatement;
    }

    public String generateWhitelistDeleteBrandSQL(String deployment) throws IOException, ParseException {
        return "DELETE FROM product_whitelist WHERE deployment = '" + deployment + "'";
    }

    public String generateWhitelistDeleteBrandStoreSQL(String deployment, String storeId)
            throws IOException, ParseException {
        return "DELETE FROM product_whitelist WHERE deployment = '" + deployment + "' AND storeId = '" + storeId + "'";
    }

    public String generateWhitelistSelectSQL(String deployment) throws IOException, ParseException {
        return "SELECT productId FROM product_whitelist WHERE deployment = '" + deployment + "'";
    }

    public String generateWhitelistSelectStoreSQL(String deployment, String storeId)
            throws IOException, ParseException {
        return "SELECT productId FROM product_whitelist WHERE deployment = '" + deployment + "' AND storeId = '"
                + storeId + "'";
    }

    public String generateWhitelistUpsertSQL(String deployment, List<String> whitelist) {
        String updateStatement = "";

        if (whitelist.size() > 0) {
            updateStatement = "INSERT INTO product_whitelist (deployment, productId) VALUES ";

            StringJoiner sj = new StringJoiner(",");
            for (String sku : whitelist) {
                sj.add(String.format("('%s', '%s')", deployment, sku));
            }
            updateStatement += sj.toString() + ";";
        }
        return updateStatement;
    }

    public String generateWhitelistUpsertStoreSQL(String deployment, String storeId, List<String> whitelist) {
        String updateStatement = "";

        if (whitelist.size() > 0) {
            updateStatement = "INSERT INTO product_whitelist (deployment, storeId, productId) VALUES ";

            StringJoiner sj = new StringJoiner(",");
            for (String sku : whitelist) {
                sj.add(String.format("('%s', '%s', '%s')", deployment, storeId, sku));
            }
            updateStatement += sj.toString() + ";";
        }
        return updateStatement;
    }

    public String generateEmployeeDeleteSQL(String deployment) throws IOException, ParseException {
        return "DELETE FROM employee_ids WHERE deployment = '" + deployment + "'";
    }

    public String generateEmployeeSelectSQL(String deployment) throws IOException, ParseException {
        return "SELECT employeeId, externalId FROM employee_ids WHERE deployment = '" + deployment + "'";
    }

    public String generateEmployeeUpsertSQL(String deployment, TeamMember[] employees) {
        String updateStatement = "";

        if (employees.length > 0) {
            updateStatement = "INSERT INTO employee_ids (deployment, employeeId, externalId) VALUES ";

            StringJoiner sj = new StringJoiner(",");
            for (TeamMember employee : employees) {
                sj.add(String.format("('%s', '%s', '%s')", deployment, employee.getId(), employee.getReferenceId()));
            }
            updateStatement += sj.toString() + ";";
        }
        return updateStatement;
    }

    public void close() throws SQLException {
        connection.close();
    }
}
