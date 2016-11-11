package vfcorp;

import java.io.BufferedInputStream;
import java.io.InputStream;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

public class PLUSyncToDatabaseCallable implements Callable {
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

        PLUSyncToDatabaseRequest pluSyncToDatabaseRequest = (PLUSyncToDatabaseRequest) message
                .getProperty("pluSyncToDatabaseRequest", PropertyScope.INVOCATION);

        String deploymentId = pluSyncToDatabaseRequest.getDeployment().getDeployment();
        String merchantId = pluSyncToDatabaseRequest.getDeployment().getMerchantId();
        String locationId = pluSyncToDatabaseRequest.getDeployment().getLocationId();

        InputStream is = message.getProperty("pluInputStream", PropertyScope.INVOCATION);
        BufferedInputStream bis = new BufferedInputStream(is);

        PLUParser parser = new PLUParser();
        parser.setDeploymentId(deploymentId);
        parser.setSyncGroupSize(2500);
        parser.setDatabaseUrl(databaseUrl);
        parser.setDatabaseUser(databaseUser);
        parser.setDatabasePassword(databasePassword);
        parser.syncToDatabase(bis, merchantId, locationId);
        bis.close();

        // Submit new request to VM for API updates
        PLUDatabaseToSquareRequest updateRequest = new PLUDatabaseToSquareRequest();
        updateRequest.setDeployment(pluSyncToDatabaseRequest.getDeployment());
        updateRequest.setProcessingFileName(pluSyncToDatabaseRequest.getProcessingFileName());
        updateRequest.setProcessingPluFile(true);

        return updateRequest;
    }
}
