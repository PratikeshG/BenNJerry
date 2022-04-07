package util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseApi {
    private DbConnection dbConnection;

    public DatabaseApi(DbConnection conn) {
        dbConnection = conn;
    }

    public DatabaseApi(String databaseUrl, String databaseUser, String databasePassword) throws Exception {
        try {
            dbConnection = new DbConnection(databaseUrl, databaseUser, databasePassword);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw e;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public ResultSet getQuerytResultSet(String query) {
        ResultSet resultSet;
        try {
            resultSet = dbConnection.submitQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("SQLException");
        }
        return resultSet;
    }

    public List<Map<String, String>> getQueryResultList(String query) {
        ResultSet resultSet = getQuerytResultSet(query);

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

    public void close() throws SQLException {
        dbConnection.close();
    }

}
