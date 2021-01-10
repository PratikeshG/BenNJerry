package jockey;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.springframework.beans.factory.annotation.Value;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import util.CloudStorageApi;

public class ArchiveAndUploadToSftpCallable implements Callable {
    @Value("${jockey.sftp.ip}")
    private String sftpHost;
    @Value("${jockey.sftp.port}")
    private int sftpPort;
    @Value("${jockey.sftp.user}")
    private String sftpUser;
    @Value("${jockey.sftp.password}")
    private String sftpPassword;
    @Value("${jockey.sftp.path}")
    private String sftpPath;

    @Value("${jockey.encryption.key}")
    private String encryptionKey;

    @Value("${google.storage.bucket.archive}")
    private String archiveBucket;
    @Value("${google.storage.account.credentials}")
    private String storageCredentials;

    private static final int RETRY_COUNT = 10;
    private static final int RETRY_DELAY_MS = 60000; // 1 minute

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        String payload = eventContext.getMessage().getPayloadAsString();
        String fileName = eventContext.getMessage().getProperty("fileName", PropertyScope.INVOCATION);

        // Archive complete TLOGs to Google Cloud Storage
        String archiveFileKey = String.format("Jockey/%s.secure", fileName);
        CloudStorageApi cloudStorage = new CloudStorageApi(storageCredentials);
        cloudStorage.encryptAndUploadObject(encryptionKey, archiveBucket, archiveFileKey, payload);

        return uploadSftpWithRetry(payload, fileName);
    }

    private Object uploadSftpWithRetry(String payload, String fileName) throws InterruptedException, Exception {
        Exception lastException = null;
        for (int i = 0; i < RETRY_COUNT; i++) {
            try {
                return uploadViaSftp(payload, fileName);

            } catch (Exception e) {
                lastException = e;
                lastException.printStackTrace();
                Thread.sleep(RETRY_DELAY_MS);
            }
        }

        throw lastException;
    }

    private Object uploadViaSftp(String payload, String fileName) throws JSchException, IOException, SftpException {
        Session session = Util.createSSHSession(sftpHost, sftpUser, sftpPassword, sftpPort);
        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();

        InputStream uploadStream = new ByteArrayInputStream(payload.getBytes("UTF-8"));
        sftpChannel.cd(sftpPath);
        sftpChannel.put(uploadStream, fileName, ChannelSftp.OVERWRITE);

        // archive
        InputStream archiveStream = new ByteArrayInputStream(payload.getBytes("UTF-8"));
        sftpChannel.cd(sftpPath + "/archive");
        sftpChannel.put(archiveStream, fileName, ChannelSftp.OVERWRITE);

        sftpChannel.disconnect();
        session.disconnect();

        return payload;
    }
}
