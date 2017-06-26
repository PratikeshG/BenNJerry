package tntfireworks.reporting;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import tntfireworks.TntDatabaseApi;
import util.DbConnection;
import util.SquarePayload;

public class TntDeploymentsCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(TntDeploymentsCallable.class);

    private String databaseUrl;
    private String databaseUser;
    private String databasePassword;
    private String activeDeployment;
    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.create();

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
        logger.info("Retrieving deployments in DB and returning payloads...");

        DbConnection dbConnection = new DbConnection(databaseUrl, databaseUser, databasePassword);
        TntDatabaseApi tntDatabaseApi = new TntDatabaseApi(dbConnection);
        List<SquarePayload> deploymentPayloads = getDeploymentsFromDb(tntDatabaseApi, activeDeployment);
        tntDatabaseApi.close();

        logger.info("Returning %s deployments from DB", deploymentPayloads.size());
        return deploymentPayloads;
    }

    public List<SquarePayload> getDeploymentsFromDb(TntDatabaseApi tntDatabaseApi, String deployment)
            throws SQLException {

        ArrayList<Map<String, String>> rows = tntDatabaseApi
                .submitQuery(tntDatabaseApi.generateDeploymentSQLSelect(activeDeployment));

        // columns retrieved: connectApp, token
        List<SquarePayload> deploymentPayloads = new ArrayList<SquarePayload>();
        for (Map<String, String> row : rows) {
            SquarePayload deploymentPayload = new SquarePayload();
            deploymentPayload.setAccessToken(row.get("token"));
            deploymentPayload.setMerchantId(row.get("merchantId"));
            deploymentPayload.setMerchantAlias(row.get("merchantAlias"));
            deploymentPayloads.add(deploymentPayload);
        }
        return deploymentPayloads;
    }
}
