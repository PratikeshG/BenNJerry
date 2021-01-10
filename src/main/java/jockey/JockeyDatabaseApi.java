package jockey;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JockeyDatabaseApi {
    private static Logger logger = LoggerFactory.getLogger(JockeyDatabaseApi.class);
    private Connection connection;

    public JockeyDatabaseApi(Connection connection) {
        this.connection = connection;
    }

    public JockeyDatabaseApi() {
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

    public ArrayList<Map<String, String>> queryOrderNumbersForLocation(String locationId)
            throws SQLException, IOException {
        return executeQuery(generateOrderNumbersForLocationSQLSelect(locationId));
    }

    public ArrayList<Map<String, String>> queryOrdersById(List<String> orderIds) throws SQLException, IOException {
        return executeQuery(generateOrdersByDeviceSQLSelect(orderIds));
    }

    public int updateOrderNumbersForLocation(List<SalesOrder> orders, String locationId)
            throws SQLException, IOException {
        return executeUpdate(generateOrdersByDeviceSQLUpsert(orders, locationId));
    }

    public String generateOrderNumbersForLocationSQLSelect(String locationId) throws IOException {
        String query = String.format(
                "SELECT MAX(orderNumber) as lastOrderNumber, deviceId FROM orders_by_device WHERE locationId = '%s' GROUP BY deviceId;",
                locationId);

        logger.debug(String.format("Generated Jockey Order Numbers for Location Select query: %s", query));

        return query;
    }

    public String generateOrdersByDeviceSQLSelect(List<String> orderIds) throws IOException {
        String query = String.format(
                "SELECT orderId, orderNumber, deviceId FROM orders_by_device WHERE orderId IN (%s) GROUP BY orderId, orderNumber, deviceId;",
                getOrderIdsQueryString(orderIds));

        logger.debug(String.format("Generated Jockey Order Select query: %s", query));

        return query;
    }

    public String generateOrdersByDeviceSQLUpsert(List<SalesOrder> salesOrders, String locationId) {
        String updateStatement = "";

        if (salesOrders.size() > 0) {
            updateStatement = "INSERT INTO orders_by_device (locationId, orderId, deviceId, orderNumber, orderCreatedAt) VALUES ";

            ArrayList<String> updates = new ArrayList<String>();
            for (SalesOrder order : salesOrders) {
                int n = Integer.parseInt(order.getTransactionNumber());
                System.out.println(order.getThirdPartyOrderId() + " " + order.getDateCreated());
                String createdDate = order.getDateCreated().split("\\.", 2)[0];

                updates.add(String.format("('%s', '%s', '%s', %d, '%s')", locationId, order.getThirdPartyOrderId(),
                        order.getRegisterNumber(), n, createdDate));
            }

            Iterator<String> i = updates.iterator();
            if (i.hasNext()) {
                updateStatement += i.next();
                while (i.hasNext()) {
                    updateStatement += ", " + i.next();
                }
            }

            // do nothing for existing orders
            updateStatement += " ON DUPLICATE KEY UPDATE locationId = locationId;";
        }

        return updateStatement;
    }

    private String getOrderIdsQueryString(List<String> orderIds) throws IOException {
        if (orderIds.size() < 1) {
            return "''";
        }

        StringJoiner sj = new StringJoiner(",");
        for (String orderId : orderIds) {
            sj.add(String.format("'%s'", orderId));
        }
        return sj.toString();
    }

    public void close() throws SQLException {
        connection.close();
    }
}
