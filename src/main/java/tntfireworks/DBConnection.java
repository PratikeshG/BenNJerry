package tntfireworks;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {

  private Connection dbConnection;
  private String databaseUrl;
  private String databaseUser;
  private String databasePassword;
  
  public DbConnection(String databaseUrl, String databaseUser, String databasePassword) {
	  this.databaseUrl = databaseUrl;
	  this.databaseUser = databaseUser;
	  this.databasePassword = databasePassword;
  }

  public void getDBConnection() throws ClassNotFoundException, SQLException {
    Class.forName("com.mysql.jdbc.Driver");
    dbConnection = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
  }

  public int executeQuery(String query) throws ClassNotFoundException, SQLException {
    return dbConnection.createStatement().executeUpdate(query);
  }

  public void close() throws SQLException {
	dbConnection.close();
  }
}