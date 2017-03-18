package tntfireworks;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import util.DbConnection;

public class SyncToDatabaseCallable implements Callable {
    private String databaseUrl;
    private String databaseUser;
    private String databasePassword;
    private String sftpHost;
    private int sftpPort;
    private String sftpUser;
    private String sftpPassword;
    private static final int SYNC_GROUP_SIZE = 2500;

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

        SyncToDatabaseRequest request = (SyncToDatabaseRequest) message.getProperty("syncToDatabaseRequest",
                PropertyScope.INVOCATION);

        InputStream is = message.getProperty("s3InputStream", PropertyScope.INVOCATION);
        BufferedInputStream bis = new BufferedInputStream(is);

        DbConnection dbConnection = new DbConnection(databaseUrl, databaseUser, databasePassword);
        InputParser parser = new InputParser(dbConnection, SYNC_GROUP_SIZE);
        parser.syncToDatabase(bis, request.getProcessingFilename());
        bis.close();

        archiveProcessingFile(request.getProcessingPath(), request.getArchivePath(), request.getProcessingFilename());

        return message.getPayload();
    }

    private void archiveProcessingFile(String processingPath, String archivePath, String processingFilename)
            throws JSchException, IOException, SftpException {
        ChannelSftp sftpChannel = SshUtil.createConnection(sftpHost, sftpPort, sftpUser, sftpPassword);

        sftpChannel.rename(String.format("%s/%s", processingPath, processingFilename),
                String.format("%s/%s", archivePath, processingFilename));

        SshUtil.closeConnection(sftpChannel);
    }

}
