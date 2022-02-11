package vfcorp;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import util.CloudStorageApi;

public class WhitelistSyncToCloudCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(WhitelistSyncToCloudCallable.class);

    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}")
    private String databaseUrl;
    @Value("${mysql.user}")
    private String databaseUser;
    @Value("${mysql.password}")
    private String databasePassword;

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

    private static final String WHIETLIST_FILE_PREFIX = "UPC.DTA";
    private static final String ARCHIVE_PATH = "Archive";

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        String brand = (String) message.getProperty("brand", PropertyScope.INVOCATION);
        String sftpPath = (String) message.getProperty("sftpPath", PropertyScope.INVOCATION);
        boolean enabled = message.getProperty("enabled", PropertyScope.INVOCATION).equals("true") ? true : false;

        String deployment = "vfcorp-" + brand;

        if (!enabled) {
            logger.info("Skipping whitelist - " + brand + " - disabled");
            return 0;
        }

        Session session = Util.createSSHSession(sftpHost, sftpUser, sftpPassword, sftpPort);
        logger.info(brand + ": SFTP session created");
        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();
        logger.info(brand + ": SFTP channel created");

        sftpChannel.cd(sftpPath);

        Vector<LsEntry> whitelistFiles = sftpChannel.ls(WHIETLIST_FILE_PREFIX + "*");
        Thread.sleep(120000); // wait 2mins to verify if file ready for processing

        ArrayList<String> filesToProcess = new ArrayList<String>();
        for (LsEntry f : whitelistFiles) {
            String fileName = f.getFilename();

            int oldSize = (int) f.getAttrs().getSize();
            int currentSize = (int) sftpChannel.lstat(fileName).getSize();

            if (oldSize != currentSize) {
                logger.info(String.format("%s not ready for processing", fileName));
            } else {
                filesToProcess.add(fileName);
            }
        }

        Collections.sort(filesToProcess);

        String whitelistFolder = message.getProperty("whitelistFolder", PropertyScope.INVOCATION);
        CloudStorageApi cloudStorage = new CloudStorageApi(storageCredentials);
        String fileKey = null;

        if (filesToProcess.size() > 0) {
            for (int i = 0; i < filesToProcess.size(); i++) {
                String file = filesToProcess.get(i);

                // validate file; only process properly formed files
                if (!isWhitelistValid(sftpChannel, file)) {
                    sftpChannel.rename(file, String.format("%s/invalid_%s", ARCHIVE_PATH, file));
                    continue;
                }

                // Move to Google Cloud Storage for processing
                logger.info(String.format("Saving %s -- %s archive to GCP cloud...", deployment, file));
                InputStream is = sftpChannel.get(file);
                fileKey = String.format("%s/%s", whitelistFolder, file);

                try {
                    cloudStorage.uploadObject(archiveBucket, fileKey, is);
                } catch (RuntimeException e) {
                    logger.error("VFC whitelist error trying to upload to CloudStorage: " + e.getMessage());
                }

                sftpChannel.rename(file, String.format("%s/%s", ARCHIVE_PATH, file));
            }
        }

        sftpChannel.disconnect();
        logger.info("VFC: SFTP channel disconnected");

        session.disconnect();
        logger.info("VFC: SFTP session disconnected");

        return 1;
    }

    private boolean isWhitelistValid(ChannelSftp sftpChannel, String filename)
            throws FileNotFoundException, IOException, SftpException {
        InputStream is = null;
        BufferedInputStream bis = null;
        Reader reader = null;

        try {
            is = sftpChannel.get(filename);
            bis = new BufferedInputStream(is);
            reader = new InputStreamReader(bis, StandardCharsets.UTF_8);

            int count = 0;

            int c;
            while ((c = reader.read()) >= 0) {
                // 0 as char = 48
                // 9 as char = 57
                if (c < 48 || c > 57) {
                    return false;
                }

                count++;
                if (count > 15) {
                    break;
                }
            }
        } finally {
            if (is != null) {
                is.close();
            }
            if (bis != null) {
                bis.close();
            }
            if (reader != null) {
                reader.close();
            }
        }
        return true;
    }
}
