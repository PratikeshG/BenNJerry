package tntfireworks;

import java.io.BufferedInputStream;
import java.io.InputStream;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

public class SyncToDatabaseCallable implements Callable {
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

        SyncToDatabaseRequest request = (SyncToDatabaseRequest) message
                .getProperty("SyncToDatabaseRequest", PropertyScope.INVOCATION);

        InputStream is = message.getProperty("s3InputStream", PropertyScope.INVOCATION);
        BufferedInputStream bis = new BufferedInputStream(is);

        InputParser parser = new InputParser();
        parser.setSyncGroupSize(2500);
        parser.setDatabaseUrl(databaseUrl);
        parser.setDatabaseUser(databaseUser);
        parser.setDatabasePassword(databasePassword);
        parser.syncToDatabase(bis, request.getProcessingFilename());
        bis.close();
            
        /*
        // Submit new request to VM for API updates
        DatabaseToSquareRequest updateRequest = new DatabaseToSquareRequest();
        updateRequest.setProcessingFileName(SyncToDatabaseRequest.getProcessingFilename());
        updateRequest.setProcessingFile(true);
        */

        // submit new request to VM to generate import files for marketing plan
        //return updateRequest;        
        
        return new Object();
    }
}
