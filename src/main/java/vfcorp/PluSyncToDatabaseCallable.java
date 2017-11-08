package vfcorp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.springframework.beans.factory.annotation.Value;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import util.CloudStorageApi;

public class PluSyncToDatabaseCallable implements Callable {
    @Value("${vfcorp.sftp.host}")
    private String sftpHost;
    @Value("${vfcorp.sftp.port}")
    private int sftpPort;
    @Value("${vfcorp.sftp.username}")
    private String sftpUser;
    @Value("${vfcorp.sftp.password}")
    private String sftpPassword;

    @Value("${google.storage.bucket.archive}")
    private String archiveBucket;
    @Value("${google.storage.account.credentials}")
    private String storageCredentials;

    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}")
    private String databaseUrl;
    @Value("${mysql.user}")
    private String databaseUser;
    @Value("${mysql.password}")
    private String databasePassword;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        PluSyncToDatabaseRequest request = (PluSyncToDatabaseRequest) message.getPayload();

        Session session = Util.createSSHSession(sftpHost, sftpUser, sftpPassword, sftpPort);
        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();
        System.out.println("SFTP archive channel created.");

        System.out.println("Saving archive stream...");
        sftpChannel.cd(request.getDeployment().getPluPath() + "/processing");
        InputStream is = sftpChannel.get(request.getProcessingFileName());

        // Archive to Google Cloud Storage
        String encryptionKey = message.getProperty("encryptionKey", PropertyScope.INVOCATION);
        String archiveFolder = message.getProperty("archiveFolder", PropertyScope.INVOCATION);
        String fileKey = String.format("%s/%s.secure", archiveFolder, request.getProcessingFileName());

        CloudStorageApi cloudStorage = new CloudStorageApi(storageCredentials);
        cloudStorage.encryptAndUploadObject(encryptionKey, archiveBucket, fileKey, is);
        System.out.println("File archived.");

        // Success, close SFTP resources
        if (sftpChannel != null && sftpChannel.isConnected()) {
            sftpChannel.disconnect();
            System.out.println("SFTP channel disconnected.");

            if (session != null && session.isConnected()) {
                session.disconnect();
                System.out.println("SFTP session disconnected.");
            }
        }

        // Establish new input stream from archived file
        InputStream pluInputStream = cloudStorage.downloadAndDecryptObject(encryptionKey, archiveBucket, fileKey);
        BufferedInputStream bis = new BufferedInputStream(pluInputStream);

        // Sync to DB
        String deploymentId = request.getDeployment().getDeployment();
        String merchantId = request.getDeployment().getSquarePayload().getMerchantId();
        String locationId = request.getDeployment().getSquarePayload().getLocationId();

        PluParser parser = new PluParser();
        parser.setDeploymentId(deploymentId);
        parser.setSyncGroupSize(2500);
        parser.setDatabaseUrl(databaseUrl);
        parser.setDatabaseUser(databaseUser);
        parser.setDatabasePassword(databasePassword);
        parser.syncToDatabase(bis, merchantId, locationId);
        bis.close();
        System.out.println("PLU processed.");

        // Archive file on SFTP from temp processing directory
        archiveProcessingFile(request.getProcessingFileName(), request.getDeployment().getPluPath());

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
