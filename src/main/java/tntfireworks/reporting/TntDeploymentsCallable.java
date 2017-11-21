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
import org.springframework.beans.factory.annotation.Value;

import tntfireworks.TntDatabaseApi;
import util.DbConnection;
import util.SquarePayload;

public class TntDeploymentsCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(TntDeploymentsCallable.class);

    @Value("${tntfireworks.activeDeployment}")
    private String activeDeployment;
    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}")
    private String databaseUrl;
    @Value("${mysql.user}")
    private String databaseUser;
    @Value("${mysql.password}")
    private String databasePassword;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();
        logger.info("Retrieving deployments in DB and returning payloads...");

        DbConnection dbConnection = new DbConnection(databaseUrl, databaseUser, databasePassword);
        TntDatabaseApi tntDatabaseApi = new TntDatabaseApi(dbConnection);
        List<SquarePayload> deploymentPayloads = getDeploymentsFromDb(tntDatabaseApi);
        tntDatabaseApi.close();

        logger.info("Returning %s deployments from DB", deploymentPayloads.size());
        return deploymentPayloads;
    }

    private List<SquarePayload> getDeploymentsFromDb(TntDatabaseApi tntDatabaseApi)
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
