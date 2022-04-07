package vfcorp;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.springframework.beans.factory.annotation.Value;

public class TlogQueryDatabaseDeploymentCallable implements Callable {
    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}?autoReconnect=true")
    private String databaseUrl;
    @Value("${mysql.user}")
    private String databaseUser;
    @Value("${mysql.password}")
    private String databasePassword;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        String deploymentId = message.getProperty("deployment", PropertyScope.INVOCATION);

        VfcDeployment deployment = Util.getVfcDeploymentById(databaseUrl, databaseUser, databasePassword, deploymentId);

        if (deployment.getDeployment().length() < 1) {
            throw new Exception(String.format("Deployment '%s' not found.", deployment));
        }

        return deployment;
    }
}
