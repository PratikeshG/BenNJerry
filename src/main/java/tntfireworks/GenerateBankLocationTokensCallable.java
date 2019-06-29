package tntfireworks;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.activation.DataHandler;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.SquareClientV2;

import util.DbConnection;
import util.SquarePayload;
import util.TimeManager;

/*
 * GenerateBankLocationTokensCallable.java
 *
 * - This callable class generates a csv file with two columns: source location token, destination location token
 * - The purpose of this class is to provide a csv file that can be used in SqWeb to clone bank account information from
 *   one location to another
 * - For TNT accounts, there should only be 1 source location created per master (denoted by 'DEFAULT - ' in the location name)
 *   and 'DEACTIVATED' locations should be ignored
 *
 */
public class GenerateBankLocationTokensCallable implements Callable {
    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}")
    private String databaseUrl;
    @Value("${mysql.user}")
    private String databaseUser;
    @Value("${mysql.password}")
    private String databasePassword;
    @Value("${encryption.key.tokens}")
    private String encryptionKey;
    @Value("${api.url}")
    private String apiUrl;

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public void setDatabaseUser(String databaseUser) {
        this.databaseUser = databaseUser;
    }

    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        // get deployments from database
        ArrayList<Map<String, String>> dbRows = getDeploymentsFromDb();

        // create source token to destination token mappings
        Map<String, ArrayList<String>> sourceToDestinations = getSourceToDestinationMap(dbRows);

        // write to string builder as csv file with columns "source token, destination token"
        StringBuilder builder = new StringBuilder();
        for (String source : sourceToDestinations.keySet()) {
            for (String destination : sourceToDestinations.get(source)) {
                builder.append(String.format("%s,%s\n", source, destination));
            }
        }

        // Calculate day of report generated
        String timezone = "America/Los_Angeles";
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timezone));
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String currentDate = TimeManager.toSimpleDateTimeInTimeZone(TimeManager.toIso8601(cal, timezone), timezone,
                "yyyy-MM-dd");

        DataHandler dataHandler = new DataHandler(builder.toString(), "text/plain; charset=UTF-8");
        eventContext.getMessage().addOutboundAttachment(currentDate + "-location-tokens.csv", dataHandler);

        // empty return
        return "See attachment.";
    }

    private Map<String, ArrayList<String>> getSourceToDestinationMap(ArrayList<Map<String, String>> dbRows)
            throws Exception {
        Map<String, ArrayList<String>> sourceToDestinations = new HashMap<String, ArrayList<String>>();
        // loop through each merchant account
        for (Map<String, String> row : dbRows) {
            // init SquarePayload with db values
            SquarePayload deploymentPayload = new SquarePayload();
            deploymentPayload.setEncryptedAccessToken(row.get("encryptedAccessToken"));
            deploymentPayload.setMerchantId(row.get("merchantId"));
            deploymentPayload.setMerchantAlias(row.get("merchantAlias"));

            // loop through locations in each master
            SquareClientV2 clientV2 = new SquareClientV2(apiUrl, deploymentPayload.getAccessToken(encryptionKey));
            String sourceToken = "";
            ArrayList<String> destinationTokens = new ArrayList<String>();
            int totalDefaultLocations = 0;
            for (Location location : clientV2.locations().list()) {
                if (location.getName().contains("DEFAULT")) {
                    totalDefaultLocations++;
                    sourceToken = location.getId();
                } else if (location.getStatus().equals(Location.LOCATION_STATUS_ACTIVE)) {
                    destinationTokens.add(location.getId());
                }
            }

            // check only 1 source location token
            if (totalDefaultLocations == 1) {
                // add source token and destination list into map
                if (!sourceToDestinations.containsKey(sourceToken)) {
                    sourceToDestinations.put(sourceToken, destinationTokens);
                } else {
                    throw new Exception("ERROR: duplicate source location token found.");
                }
            } else {
                throw new Exception("ERROR: there should only be one source location per master account.");
            }
        }

        return sourceToDestinations;
    }

    private ArrayList<Map<String, String>> getDeploymentsFromDb() throws SQLException, ClassNotFoundException {
        // get all access tokens for tnt merchant accounts (catalog and reporting)
        String whereFilter = String.format("%s = 1 and %s = 1", TntDatabaseApi.DB_DEPLOYMENT_ENABLE_CATALOG_SYNC_COLUMN,
                TntDatabaseApi.DB_DEPLOYMENT_ENABLE_REPORTING_COLUMN);

        DbConnection dbConnection = new DbConnection(databaseUrl, databaseUser, databasePassword);
        TntDatabaseApi tntDatabaseApi = new TntDatabaseApi(dbConnection);
        ArrayList<Map<String, String>> dbRows = tntDatabaseApi
                .submitQuery(tntDatabaseApi.generateDeploymentSQLSelect(whereFilter));
        tntDatabaseApi.close();

        return dbRows;
    }
}
