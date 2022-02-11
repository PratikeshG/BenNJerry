package util;

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

public class SequentialRecordsApi {
    private static Logger logger = LoggerFactory.getLogger(SequentialRecordsApi.class);
    private Connection connection;

    public SequentialRecordsApi(Connection connection) {
        this.connection = connection;
    }

    public SequentialRecordsApi() {
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

    public ArrayList<Map<String, String>> queryRecordNumbersForLocation(String locationId)
            throws SQLException, IOException {
        return executeQuery(generateRecordNumbersForLocationSQLSelect(locationId));
    }

    public ArrayList<Map<String, String>> queryRecordsById(List<String> recordIds) throws SQLException, IOException {
        return executeQuery(generateRecordsByDeviceSQLSelect(recordIds));
    }

    public int updateRecordNumbersForLocation(List<SequentialRecord> records, String locationId)
            throws SQLException, IOException {
        return executeUpdate(generateRecordsByDeviceSQLUpsert(records, locationId));
    }

    public String generateRecordNumbersForLocationSQLSelect(String locationId) throws IOException {
        String query = String.format(
                "SELECT MAX(recordNumber) as lastRecordNumber, deviceId FROM orders_by_device WHERE locationId = '%s' GROUP BY deviceId;",
                locationId);

        logger.debug(String.format("Generated sequential record numbers for location SELECT query: %s", query));

        return query;
    }

    public String generateRecordsByDeviceSQLSelect(List<String> recordIds) throws IOException {
        String query = String.format(
                "SELECT recordId, recordNumber, deviceId, locationId, recordCreatedAt FROM orders_by_device WHERE recordId IN (%s) GROUP BY recordId, recordNumber, deviceId, locationId, recordCreatedAt;",
                getRecordIdsQueryString(recordIds));

        logger.debug(String.format("Generated sequential record SELECT query: %s", query));

        return query;
    }

    public String generateRecordsByDeviceSQLUpsert(List<SequentialRecord> records, String locationId) {
        String updateStatement = "";

        if (records.size() > 0) {
            updateStatement = "INSERT INTO orders_by_device (locationId, recordId, deviceId, recordNumber, recordCreatedAt) VALUES ";

            ArrayList<String> updates = new ArrayList<String>();
            for (SequentialRecord record : records) {
                String createdDate = record.getRecordCreatedAt().replace("Z", "").split("\\.", 2)[0];
                updates.add(String.format("('%s', '%s', '%s', %d, '%s')", locationId, record.getRecordId(),
                        record.getDeviceId(), record.getRecordNumber(), createdDate));
            }

            Iterator<String> i = updates.iterator();
            if (i.hasNext()) {
                updateStatement += i.next();
                while (i.hasNext()) {
                    updateStatement += ", " + i.next();
                }
            }

            // do nothing for existing records
            updateStatement += " ON DUPLICATE KEY UPDATE locationId = locationId;";
        }

        return updateStatement;
    }

    private String getRecordIdsQueryString(List<String> recordIds) throws IOException {
        if (recordIds.size() < 1) {
            return "''";
        }

        StringJoiner sj = new StringJoiner(",");
        for (String recordId : recordIds) {
            sj.add(String.format("'%s'", recordId));
        }
        return sj.toString();
    }

    public void close() throws SQLException {
        connection.close();
    }
}
