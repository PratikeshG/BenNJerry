package tntfireworks;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

public class DatabaseToSquareCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(DatabaseToSquareCallable.class);

    private String databaseUrl;
    private String databaseUser;
    private String databasePassword;
    private String sftpHost;
    private int sftpPort;
    private String sftpUser;
    private String sftpPassword;
    private String apiUrl;
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

    public void setSftpHost(String sftpHost) {
        this.sftpHost = sftpHost;
    }

    public void setSftpPort(int sftpPort) {
        this.sftpPort = sftpPort;
    }

    public void setSftpUser(String sftpUser) {
        this.sftpUser = sftpUser;
    }

    public void setSftpPassword(String sftpPassword) {
        this.sftpPassword = sftpPassword;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
    
    public void setActiveDeployment(String activeDeployment) {
        this.activeDeployment = activeDeployment;
    }
    
    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();       
        logger.info("Starting database to Square sync...");
        
        // retrieve payload from prior step
        DatabaseToSquareRequest updateSQRequest = (DatabaseToSquareRequest) message.getPayload();
        message.setProperty("DatabaseToSquareRequest", updateSQRequest, PropertyScope.INVOCATION);
             
        // determine if marketing plans were updated OR locations
        // if locations were updated to db, nothing left to do except archive
        // parse filename - marketing plan or location file
        String filename = "";
        Pattern r = Pattern.compile("\\d+_(\\w+)_\\d+.csv");
        Matcher m = r.matcher(updateSQRequest.getProcessingFilename());
        m.find();
        
        // set name to matched group
        if (m.group(1) != null)
            filename = m.group(1);
        
        if (!filename.equals("locations")) {
            // ResultSets for SQL queries
            ResultSet resultDeployments = null;
            ResultSet resultItems = null;
            ResultSet resultLocations = null;
            
            // set up SQL connection
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
            
            // get deployments from db
            // columns retrieved: connectApp, token
            resultDeployments = submitQuery(conn, generateDeploymentSQLSelect(activeDeployment));
            while (resultDeployments.next()) {
                System.out.println("connectApp: " + resultDeployments.getString(1));
                System.out.println("token: " + resultDeployments.getString(2));
            }
            
            // get locations from db that match the filename/marketing plan
            // columns retrieved: locationNumber, name            
            resultLocations = submitQuery(conn, generateLocationSQLSelect(filename));
            while (resultLocations.next()) {
                System.out.println("locationNum: " + resultLocations.getString(1));
                System.out.println("name: " + resultLocations.getString(2));
            }
            // get items from db of that marketing plan
            // columns retrieved: itemNumber, category, itemDescription, suggestedPrice, upc, currency
            resultItems = submitQuery(conn, generateItemSQLSelect(filename));
            
            // 1. loop through locations in all SQ accounts 
            // 2. determine if location name == 'locationNum (name)' of prior locations ResultSet
            // 3. if so, update that location
    
            conn.close();
        }
        
        // Need to move processingFile to archive
        if (updateSQRequest.isProcessing() && updateSQRequest.getProcessingFilename() != null) {
            archiveProcessingFile(updateSQRequest);
        }
        
        return null;
    }
    
    public String generateDeploymentSQLSelect(String deploymentName) {                
        String query = "SELECT ConnectApp, Token FROM token WHERE deployment='" + deploymentName + "'";
        logger.info("Generated query: " + query);
        return query;
    }
    
    public String generateLocationSQLSelect(String mktPlan) {                
        String query = "SELECT locationNumber, name, mktPlan FROM tntfireworks_locations WHERE mktPlan='" + mktPlan + "'";
        logger.info("Generated query: " + query);
        return query;
    }
    
    public String generateItemSQLSelect(String mktPlan) {                
        String query = "SELECT itemNumber, category, itemDescription, suggestedPrice, upc, currency, mktPlan FROM tntfireworks_marketing_plans WHERE mktPlan='" + mktPlan + "'";
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
    
    private void archiveProcessingFile(DatabaseToSquareRequest updateSQRequest)
            throws JSchException, IOException, SftpException {
        ChannelSftp sftpChannel = SSHUtil.createConnection(sftpHost, sftpPort, sftpUser, sftpPassword); 

        sftpChannel.rename(String.format("%s/%s", updateSQRequest.getProcessingPath(), updateSQRequest.getProcessingFilename()),
                String.format("%s/%s", updateSQRequest.getArchivePath(), updateSQRequest.getProcessingFilename()));

        SSHUtil.closeConnection(sftpChannel);
    }
}
