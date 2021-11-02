package vfcorp;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
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
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import util.CloudStorageApi;

public class WhitelistSyncToDatabaseCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(WhitelistSyncToDatabaseCallable.class);

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

    private static final int MINIMUM_STORES_PROVIDED = 100;
    private static final String WHIETLIST_FILE_PREFIX = "UPC.DTA";
    private static final String PROCESSING_PREFIX = "processing_";
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

        if (isProcessingFile(sftpChannel)) {
            return 0;
        }

        Vector<LsEntry> whitelistFiles = sftpChannel.ls(WHIETLIST_FILE_PREFIX + "*");
        Thread.sleep(8000); // wait to verify if file ready for processing

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

        String encryptionKey = message.getProperty("encryptionKey", PropertyScope.INVOCATION);
        String archiveFolder = message.getProperty("archiveFolder", PropertyScope.INVOCATION);
        CloudStorageApi cloudStorage = new CloudStorageApi(storageCredentials);
        String fileToProcess = null;
        String fileKey = null;
        boolean newWhitelistProcessed = false;

        if (filesToProcess.size() > 0) {
            for (int i = 0; i < filesToProcess.size(); i++) {
                String file = filesToProcess.get(i);

                // validate file; only process properly formed files
                if (!isWhitelistValid(sftpChannel, file)) {
                    sftpChannel.rename(file, String.format("%s/invalid_%s", ARCHIVE_PATH, file));
                    continue;
                }

                fileToProcess = String.format("%s%s", PROCESSING_PREFIX, file);
                sftpChannel.rename(file, fileToProcess);

                // Archive to Google Cloud Storage
                logger.info(String.format("Saving %s -- %s archive to GCP cloud...", deployment, fileToProcess));
                InputStream is = sftpChannel.get(fileToProcess);
                fileKey = String.format("%s/%s.secure", archiveFolder, fileToProcess);

                try {
                    cloudStorage.encryptAndUploadObject(encryptionKey, archiveBucket, fileKey, is);
                } catch (RuntimeException e) {
                    logger.error("VFC whitelist error trying to upload to CloudStorage: " + e.getMessage());
                }

                // don't process any more files in this job
                break;
            }
        }

        sftpChannel.disconnect();
        logger.info("VFC: SFTP channel disconnected");

        session.disconnect();
        logger.info("VFC: SFTP session disconnected");

        if (fileToProcess != null) {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
            VfcDatabaseApi databaseApi = new VfcDatabaseApi(conn);

            // Establish new input stream from archived file
            InputStream whitelistInputStream = cloudStorage.downloadAndDecryptObject(encryptionKey, archiveBucket,
                    fileKey);
            BufferedInputStream bis = new BufferedInputStream(whitelistInputStream);

            HashMap<String, HashSet<String>> allStoreWhitelists = new HashMap<String, HashSet<String>>();

            Scanner scanner = new Scanner(bis, "UTF-8");
            while (scanner.hasNextLine()) {
                String whitelistedRow = scanner.nextLine();

                // first five characters are location Id (ex: 00990)
                if (whitelistedRow.length() == 19) {
                    String storeId = whitelistedRow.substring(0, 5);

                    HashSet<String> specificStoreWhitelist = allStoreWhitelists.get(storeId);
                    if (specificStoreWhitelist == null) {
                        specificStoreWhitelist = new HashSet<String>();
                        allStoreWhitelists.put(storeId, specificStoreWhitelist);
                    }

                    String whitelistedUpc = whitelistedRow.substring(5);
                    specificStoreWhitelist.add(whitelistedUpc);
                }
            }
            if (scanner != null) {
                scanner.close();
            }
            if (bis != null) {
                bis.close();
            }
            if (whitelistInputStream != null) {
                whitelistInputStream.close();
            }

            for (String storeId : allStoreWhitelists.keySet()) {
                databaseApi.deleteWhitelistForBrandStore(deployment, storeId);

                ArrayList<String> wl = new ArrayList<String>(allStoreWhitelists.get(storeId));

                int batchSize = 1000;
                for (int b = 0; b < wl.size(); b += batchSize) {
                    databaseApi.setWhitelistForBrandStore(deployment, storeId,
                            wl.subList(b, Math.min(wl.size(), b + batchSize)));
                }
            }

            archiveProcessingFile(sftpPath, fileToProcess, String.format("%s/done_%s", ARCHIVE_PATH, fileToProcess));
            newWhitelistProcessed = true;
        }

        message.setProperty("newWhitelistProcessed", newWhitelistProcessed, PropertyScope.INVOCATION);
        return 1;
    }

    private void archiveProcessingFile(String sftpPath, String srcFile, String destFile)
            throws JSchException, IOException, SftpException {
        Session session = Util.createSSHSession(sftpHost, sftpUser, sftpPassword, sftpPort);
        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();

        sftpChannel.cd(sftpPath);
        sftpChannel.rename(srcFile, destFile);

        sftpChannel.disconnect();
        session.disconnect();
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

    private boolean isProcessingFile(ChannelSftp channel) throws SftpException {
        Vector<LsEntry> processingFiles = channel.ls(String.format("processing_%s*", WHIETLIST_FILE_PREFIX));
        if (processingFiles.size() > 0) {
            logger.info(String.format("Can't begin processing Vans whitelists. Already processing another file: %s",
                    processingFiles.firstElement().getFilename()));
            return true;
        }
        return false;
    }
}
