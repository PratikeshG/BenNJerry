package vfcorp;

import java.util.ArrayList;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

public class TLOGQueryDatabaseDeploymentGroupCallable implements Callable {
    private String databaseUrl;
    private String databaseUser;
    private String databasePassword;

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
        MuleMessage message = eventContext.getMessage();

        String deploymentGroup = message.getProperty("deploymentGroup", PropertyScope.INVOCATION);
        String whereFilter = String.format("deploymentGroup = '%s' AND enableTLOG = 1", deploymentGroup);

        ArrayList<VFCDeployment> deployments = (ArrayList<VFCDeployment>) Util.getVFCDeployments(databaseUrl,
                databaseUser, databasePassword, whereFilter);

        return deployments;
    }
}
