package urbanspace;

import java.sql.ResultSet;
import java.util.ArrayList;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import urbanspace.models.UrbanspaceDeployment;
import util.DatabaseApi;

public class DeploymentsCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(DeploymentsCallable.class);

    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}")
    private String databaseUrl;
    @Value("${mysql.user}")
    private String databaseUser;
    @Value("${mysql.password}")
    private String databasePassword;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {

        ArrayList<UrbanspaceDeployment> activeDeployments = new ArrayList<UrbanspaceDeployment>();

        DatabaseApi databaseApi = new DatabaseApi(databaseUrl, databaseUser, databasePassword);

        ResultSet queryResult = databaseApi.getQuerytResultSet(Constants.ACTIVE_DEPLOYMENTS_QUERY);
        while (queryResult.next()) {
            UrbanspaceDeployment deployment = new UrbanspaceDeployment();

            String d = queryResult.getString(Constants.DEPLOYMENT);

            deployment.setDeployment(d);
            deployment.setName(queryResult.getString(Constants.NAME));
            deployment.setActive(queryResult.getBoolean(Constants.ACTIVE));
            deployment.setTimeZone(queryResult.getString(Constants.TIMEZONE));
            deployment.setRange(queryResult.getInt(Constants.RANGE));
            deployment.setOffset(queryResult.getInt(Constants.OFFSET));
            deployment.setEmails(queryResult.getString(Constants.EMAILS));

            logger.info("Active Urbanspace Deployment: " + d);

            activeDeployments.add(deployment);
        }

        databaseApi.close();

        return activeDeployments;
    }
}
