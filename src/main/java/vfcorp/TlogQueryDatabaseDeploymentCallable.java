package vfcorp;

import java.util.ArrayList;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.springframework.beans.factory.annotation.Value;

public class TlogQueryDatabaseDeploymentCallable implements Callable {
    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}")
    private String databaseUrl;
    @Value("${mysql.user}")
    private String databaseUser;
    @Value("${mysql.password}")
    private String databasePassword;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        String deployment = message.getProperty("deployment", PropertyScope.INVOCATION);
        String whereFilter = String.format("vfcorp_deployments.deployment = '%s'", deployment);

        ArrayList<VfcDeployment> deployments = (ArrayList<VfcDeployment>) Util.getVfcDeployments(databaseUrl,
                databaseUser, databasePassword, whereFilter);

        if (deployments.size() != 1) {
            throw new Exception(String.format("Deployment '%s' not found.", deployment));
        }

        return deployments.get(0);
    }
}
