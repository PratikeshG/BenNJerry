package vfcorp;

import java.io.BufferedInputStream;
import java.io.InputStream;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;

import util.CloudStorageApi;

public class PluSyncToDatabaseCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(PluSyncToDatabaseCallable.class);

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

    private static final int RETRY_COUNT = 5;
    private static final int RETRY_DELAY_MS = 60000; // 1 minute

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        PluSyncToDatabaseRequest request = (PluSyncToDatabaseRequest) message.getPayload();

        // Archive to Google Cloud Storage
        String encryptionKey = message.getProperty("encryptionKey", PropertyScope.INVOCATION);
        String archiveFolder = message.getProperty("archiveFolder", PropertyScope.INVOCATION);
        String fileKey = String.format("%s/%s.secure", archiveFolder, request.getProcessingFileName());

        CloudStorageApi cloudStorage = new CloudStorageApi(storageCredentials);

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

        // Archive file on SFTP from temp processing directory
        Exception lastException = null;
        Session session = null;
        ChannelSftp sftpChannel = null;

        for (int i = 0; i < RETRY_COUNT; i++) {
            try {
                session = Util.createSSHSession(sftpHost, sftpUser, sftpPassword, sftpPort);
                sftpChannel = (ChannelSftp) session.openChannel("sftp");

                sftpChannel.connect();

                String fileName = request.getProcessingFileName();
                String filePath = request.getDeployment().getPluPath();
                sftpChannel.rename(filePath + "/processing/" + fileName, filePath + "/archive/" + fileName);
                break;
            } catch (Exception e) {
                lastException = e;
                Thread.sleep(RETRY_DELAY_MS);
            } finally {
                if (sftpChannel != null) {
                    sftpChannel.disconnect();
                }
                if (session != null) {
                    session.disconnect();
                }
            }
        }

        if (lastException != null) {
            throw lastException;
        }

        logger.info("PLU processed.");
        return null;
    }
}
