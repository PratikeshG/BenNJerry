package tntfireworks;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.springframework.beans.factory.annotation.Value;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import util.CloudStorageApi;
import util.DbConnection;

public class SyncToDatabaseCallable implements Callable {
    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}")
    private String databaseUrl;
    @Value("${mysql.user}")
    private String databaseUser;
    @Value("${mysql.password}")
    private String databasePassword;

    @Value("${google.storage.bucket.archive}")
    private String archiveBucket;
    @Value("${google.storage.account.credentials}")
    private String storageCredentials;

    @Value("${tntfireworks.sftp.host}")
    private String sftpHost;
    @Value("${tntfireworks.sftp.port}")
    private int sftpPort;
    @Value("${tntfireworks.sftp.username}")
    private String sftpUser;
    @Value("${tntfireworks.sftp.password}")
    private String sftpPassword;

    @Value("${tntfireworks.encryption.key}")
    private String encryptionKey;

    @Value("${tntfireworks.archive.input.path}")
    private String archivePath;

    private static final int SYNC_GROUP_SIZE = 2500;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();
        SyncToDatabaseRequest request = (SyncToDatabaseRequest) message.getPayload();

        // Retrieve file from SFTP
        ChannelSftp sftpChannel = SshUtil.createConnection(sftpHost, sftpPort, sftpUser, sftpPassword);
        InputStream is = sftpChannel
                .get(String.format("%s/%s", request.getProcessingPath(), request.getProcessingFilename()));

        // Archive to Google Cloud Storage
        String fileKey = String.format("%s/%s.secure", archivePath, request.getProcessingFilename());

        CloudStorageApi cloudStorage = new CloudStorageApi(storageCredentials);
        cloudStorage.encryptAndUploadObject(encryptionKey, archiveBucket, fileKey, is);
        System.out.println("File archived.");

        // Success, close SFTP resources
        SshUtil.closeConnection(sftpChannel);

        // Establish new input stream from archived file
        InputStream archivedInputStream = cloudStorage.downloadAndDecryptObject(encryptionKey, archiveBucket, fileKey);
        BufferedInputStream bis = new BufferedInputStream(archivedInputStream);

        // Sync to database
        DbConnection dbConnection = new DbConnection(databaseUrl, databaseUser, databasePassword);
        InputParser parser = new InputParser(dbConnection, SYNC_GROUP_SIZE);
        parser.syncToDatabase(bis, request.getProcessingFilename());
        bis.close();

        archiveProcessingFile(request.getProcessingPath(), request.getArchivePath(), request.getProcessingFilename());

        return request;
    }

    private void archiveProcessingFile(String processingPath, String archivePath, String processingFilename)
            throws JSchException, IOException, SftpException {
        ChannelSftp sftpChannel = SshUtil.createConnection(sftpHost, sftpPort, sftpUser, sftpPassword);

        sftpChannel.rename(String.format("%s/%s", processingPath, processingFilename),
                String.format("%s/%s", archivePath, processingFilename));

        SshUtil.closeConnection(sftpChannel);
    }

}
