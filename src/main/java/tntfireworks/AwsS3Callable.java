package tntfireworks;

import java.io.InputStream;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;

public class AwsS3Callable implements Callable {  

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        Logger logger = LoggerFactory.getLogger(PollSFTPCallable.class);
        MuleMessage message = eventContext.getMessage();
        
        // state should have following values:
        //     1. copy (copy sftp file to s3)
        //     2. stream (stream file from s3 to db)
        String state = message.getProperty("awsState", PropertyScope.INVOCATION);
        
        if (state.equals("copy")) {
            logger.info("Creating connection used for SFTP server to AWS S3 transfer");
            // obtain SyncToDatabaseRequest object from PollSFTPCallable call           
            SyncToDatabaseRequest request = (SyncToDatabaseRequest) message.getPayload();
            
            // create channel + stream to pass to AWS connector
            ChannelSftp sftpChannel = SSHUtil.createConnection(request.getSftpHost(), request.getSftpPort(), 
                    request.getSftpUser(), request.getSftpPassword()); 
            InputStream is = sftpChannel.get(String.format("%s/%s", request.getProcessingPath(), request.getProcessingFilename()));
            
            // set message properties to be used by AWS connector
            message.setProperty("SyncToDatabaseRequest", request, PropertyScope.INVOCATION);
            message.setProperty("awsConnectorKey",
                    String.format("TNTFireworks/INPUT/%s", request.getProcessingFilename()),
                    PropertyScope.INVOCATION);
            message.setProperty("sftpInputStream", is, PropertyScope.INVOCATION);
            message.setProperty("sftpChannel", sftpChannel, PropertyScope.INVOCATION);

        } else if (state.equals("stream")) {
            logger.info("Closing connection used for SFTP server to AWS S3 transfer");
            ChannelSftp sftpChannel = (ChannelSftp) message.getProperty("sftpChannel", PropertyScope.INVOCATION);
            SSHUtil.closeConnection(sftpChannel);            
        }

        // no need to return any value
        return null;
    }
    
}
