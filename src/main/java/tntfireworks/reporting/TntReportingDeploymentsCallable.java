package tntfireworks.reporting;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import tntfireworks.TntDatabaseApi;
import util.DbConnection;
import util.SquarePayload;

public class TntReportingDeploymentsCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(TntReportingDeploymentsCallable.class);

    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}?autoReconnect=true")
    private String databaseUrl;
    @Value("${mysql.user}")
    private String databaseUser;
    @Value("${mysql.password}")
    private String databasePassword;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();
        // determine if adhoc run
        String adhoc = message.getProperty("adhoc", PropertyScope.SESSION);

        logger.info("Retrieving deployments in DB and returning payloads...");
        DbConnection dbConnection = new DbConnection(databaseUrl, databaseUser, databasePassword);
        TntDatabaseApi tntDatabaseApi = new TntDatabaseApi(dbConnection);
        List<SquarePayload> deploymentPayloads = getReportingDeploymentsFromDb(tntDatabaseApi, adhoc);
        tntDatabaseApi.close();

        logger.info("Returning %s deployments from DB", deploymentPayloads.size());
        return deploymentPayloads;
    }

    private List<SquarePayload> getReportingDeploymentsFromDb(TntDatabaseApi tntDatabaseApi, String adhoc)
            throws SQLException {
        // default: retrieve deployments where reporting is enabled
        String whereFilter = String.format("%s = 1", TntDatabaseApi.DB_DEPLOYMENT_ENABLE_REPORTING_COLUMN);

        // if adhoc run, run reports for deployments where adhoc = 1 regardless of enableReporting setting in DB
        if (adhoc.equals("TRUE")) {
            whereFilter = String.format("%s = 1", TntDatabaseApi.DB_DEPLOYMENT_ENABLE_ADHOC_COLUMN);
        }

        ArrayList<Map<String, String>> dbRows = tntDatabaseApi
                .submitQuery(tntDatabaseApi.generateDeploymentSQLSelect(whereFilter));

        return generateDeploymentPayloads(tntDatabaseApi, dbRows);
    }

    public List<SquarePayload> generateDeploymentPayloads(TntDatabaseApi tntDatabaseApi,
            ArrayList<Map<String, String>> dbRows) throws SQLException {
        // create SquarePayloads from db deployments
        List<SquarePayload> deploymentPayloads = new ArrayList<SquarePayload>();
        for (Map<String, String> row : dbRows) {
            SquarePayload deploymentPayload = new SquarePayload();
            deploymentPayload.setEncryptedAccessToken(row.get(TntDatabaseApi.DB_TOKEN_ENCRYPTED_ACCESS_TOKEN_COLUMN));
            deploymentPayload.setMerchantId(row.get(TntDatabaseApi.DB_TOKEN_MERCHANT_ID_COLUMN));
            deploymentPayload.setMerchantAlias(row.get(TntDatabaseApi.DB_TOKEN_MERCHANT_ALIAS_COLUMN));
            deploymentPayload.setStartOfSeason(row.get(TntDatabaseApi.DB_DEPLOYMENT_SEASON_DATE_COLUMN));
            deploymentPayloads.add(deploymentPayload);
        }

        return deploymentPayloads;
    }
}
