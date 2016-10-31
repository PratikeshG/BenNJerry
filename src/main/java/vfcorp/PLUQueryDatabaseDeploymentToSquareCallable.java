package vfcorp;

import java.util.ArrayList;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

public class PLUQueryDatabaseDeploymentToSquareCallable implements Callable {
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

        String deployment = message.getProperty("deployment", PropertyScope.INVOCATION);
        String whereFilter = String.format("vfcorp_deployments.deployment = '%s'", deployment);

        ArrayList<VFCDeployment> deployments = (ArrayList<VFCDeployment>) Util.getVFCDeployments(databaseUrl,
                databaseUser, databasePassword, whereFilter);

        if (deployments.size() != 1) {
            throw new Exception(String.format("Deployment '%s' not found.", deployment));
        }

        PLUDatabaseToSquareRequest updateRequest = new PLUDatabaseToSquareRequest();
        updateRequest.setDeployment(deployments.get(0));
        updateRequest.setProcessingPluFile(false);

        return updateRequest;
    }
}
