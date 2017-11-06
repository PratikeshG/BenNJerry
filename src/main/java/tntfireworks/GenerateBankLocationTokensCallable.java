package tntfireworks;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.activation.DataHandler;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.SquareClientV2;

import util.DbConnection;
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
    private String databaseUrl;
    private String databaseUser;
    private String databasePassword;
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

    public void setActiveDeployment(String activeDeployment) {
        this.activeDeployment = activeDeployment;
    }

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();
        String apiUrl = message.getProperty("apiUrl", PropertyScope.SESSION);

        // get access tokens for merchant accounts
        DbConnection dbConnection = new DbConnection(databaseUrl, databaseUser, databasePassword);
        TntDatabaseApi tntDatabaseApi = new TntDatabaseApi(dbConnection);
        ArrayList<Map<String, String>> tokenRows = tntDatabaseApi
                .submitQuery(tntDatabaseApi.generateDeploymentSQLSelect(activeDeployment));
        tntDatabaseApi.close();

        // create source token to destination token mappings
        HashMap<String, ArrayList<String>> sourceToDestinations = new HashMap<String, ArrayList<String>>();

        // loop through each master account
        for (Map<String, String> row : tokenRows) {
            String sourceToken = "";
            ArrayList<String> destinationTokens = new ArrayList<String>();
            int totalDefaultLocations = 0;

            // loop through locations in each master
            SquareClientV2 clientV2 = new SquareClientV2(apiUrl, row.get("token"));
            for (Location location : clientV2.locations().list()) {
                if (location.getName().contains("DEFAULT")) {
                    totalDefaultLocations++;
                    sourceToken = location.getId();
                } else if (!location.getName().contains("DEACTIVATED")) {
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
}
