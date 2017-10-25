package vfcorp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class PluSyncToDatabaseCallable implements Callable {
    private String databaseUrl;
    private String databaseUser;
    private String databasePassword;
    private String sftpHost;
    private int sftpPort;
    private String sftpUser;
    private String sftpPassword;

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public void setDatabaseUser(String databaseUser) {
        this.databaseUser = databaseUser;
    }

    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

    public void setSftpHost(String sftpHost) {
        this.sftpHost = sftpHost;
    }

    public void setSftpPort(int sftpPort) {
        this.sftpPort = sftpPort;
    }

    public void setSftpUser(String sftpUser) {
        this.sftpUser = sftpUser;
    }

    public void setSftpPassword(String sftpPassword) {
        this.sftpPassword = sftpPassword;
    }

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        PluSyncToDatabaseRequest pluSyncToDatabaseRequest = (PluSyncToDatabaseRequest) message
                .getProperty("pluSyncToDatabaseRequest", PropertyScope.INVOCATION);

        String deploymentId = pluSyncToDatabaseRequest.getDeployment().getDeployment();
        String merchantId = pluSyncToDatabaseRequest.getDeployment().getSquarePayload().getMerchantId();
        String locationId = pluSyncToDatabaseRequest.getDeployment().getSquarePayload().getLocationId();

        InputStream is = message.getProperty("pluInputStream", PropertyScope.INVOCATION);
        BufferedInputStream bis = new BufferedInputStream(is);

        PluParser parser = new PluParser();
        parser.setDeploymentId(deploymentId);
        parser.setSyncGroupSize(2500);
        parser.setDatabaseUrl(databaseUrl);
        parser.setDatabaseUser(databaseUser);
        parser.setDatabasePassword(databasePassword);
        parser.syncToDatabase(bis, merchantId, locationId);
        bis.close();

        // Archive file from SFTP processing directory
        archiveProcessingFile(pluSyncToDatabaseRequest.getProcessingFileName(),
                pluSyncToDatabaseRequest.getDeployment().getPluPath());

        return null;
    }

    private void archiveProcessingFile(String fileName, String filePath)
            throws JSchException, IOException, SftpException {
        Session session = Util.createSSHSession(sftpHost, sftpUser, sftpPassword, sftpPort);
        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();

        sftpChannel.rename(filePath + "/processing/" + fileName, filePath + "/archive/" + fileName);

        sftpChannel.disconnect();
        session.disconnect();
    }
}
