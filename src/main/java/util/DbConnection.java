package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbConnection {

    private static Logger logger = LoggerFactory.getLogger(DbConnection.class);
    private Connection dbConnection;
    private String databaseUrl;
    private String databaseUser;
    private String databasePassword;

    private static final int RETRY_COUNT = 10;
    private static final int RETRY_DELAY_MS = 5000; // 5 seconds

    public DbConnection() {

    }

    public DbConnection(String databaseUrl, String databaseUser, String databasePassword) throws Exception {
        this.databaseUrl = databaseUrl;
        this.databaseUser = databaseUser;
        this.databasePassword = databasePassword;
        this.dbConnection = connect();
    }

    public Connection getConnection() {
        return this.dbConnection;
    }

    private Connection connect() throws Exception {
        Connection conn = null;
        for (int i = 0;; i++) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
                break;
            } catch (Exception e) {
                String err = String.format("ERROR trying to connect to database: %s", e.toString());
                if (i < RETRY_COUNT) {
                    logger.info(err);
                    Thread.sleep(RETRY_DELAY_MS);
                } else {
                    throw new Exception(err);
                }
            }

        }
        return conn;
    }

    public int executeQuery(String query) throws ClassNotFoundException, SQLException {
        return dbConnection.createStatement().executeUpdate(query);
    }

    public void close() throws SQLException {
        dbConnection.close();
    }

    public ResultSet submitQuery(String query) throws SQLException {
        if (query.isEmpty()) {
            return null;
        }

        Statement stmt = dbConnection.createStatement();
        return stmt.executeQuery(query);
    }

}
