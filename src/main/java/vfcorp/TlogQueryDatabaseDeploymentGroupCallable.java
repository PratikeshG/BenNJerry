package vfcorp;

import java.util.ArrayList;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.springframework.beans.factory.annotation.Value;

public class TlogQueryDatabaseDeploymentGroupCallable implements Callable {
    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}")
    private String databaseUrl;
    @Value("${mysql.user}")
    private String databaseUser;
    @Value("${mysql.password}")
    private String databasePassword;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        String deploymentGroup = message.getProperty("deploymentGroup", PropertyScope.INVOCATION);
        String whereFilter = String.format("deploymentGroup = '%s' AND enableTLOG = 1", deploymentGroup);

        ArrayList<VfcDeployment> deployments = (ArrayList<VfcDeployment>) Util.getVfcDeployments(databaseUrl,
                databaseUser, databasePassword, whereFilter);

        return deployments;
    }
}
