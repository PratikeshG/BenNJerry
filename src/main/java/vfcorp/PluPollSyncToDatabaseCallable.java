package vfcorp;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.Session;

import util.CloudStorageApi;
import util.TimeManager;

public class PluPollSyncToDatabaseCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(PluPollSyncToDatabaseCallable.class);

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

    @Value("${vfcorp.vans.encryption.key}")
    private String encryptionKeyVans;
    @Value("${vfcorp.tnf.encryption.key}")
    private String encryptionKeyTnf;
    @Value("${vfcorp.tnfca.encryption.key}")
    private String encryptionKeyTnfca;
    @Value("${vfcorp.test.encryption.key}")
    private String encryptionKeyTest;
    @Value("${vfcorp.kipling.encryption.key}")
    private String encryptionKeyKipling;
    @Value("${vfcorp.vfo.encryption.key}")
    private String encryptionKeyVfo;
    @Value("${vfcorp.nautica.encryption.key}")
    private String encryptionKeyNautica;

    @Value("${google.storage.bucket.archive}")
    private String archiveBucket;
    @Value("${google.storage.account.credentials}")
    private String storageCredentials;

    private static final int MAX_REQUESTS = 50;
    private static final int RETRY_COUNT = 10;
    private static final int RETRY_DELAY_MS = 60000; // 1 minute

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        ArrayList<VfcDeployment> deployments = (ArrayList<VfcDeployment>) Util.getVfcDeployments(databaseUrl,
                databaseUser, databasePassword, "enablePLU = 1");

        // SFTP RETRY BLOCK
        Exception lastException = null;
        Session session = null;
        ChannelSftp sftpChannel = null;
        Object returnObject = null;

        for (int i = 0; i < RETRY_COUNT; i++) {
            try {
                session = Util.createSSHSession(sftpHost, sftpUser, sftpPassword, sftpPort);
                sftpChannel = (ChannelSftp) session.openChannel("sftp");

                sftpChannel.connect();

                returnObject = getSyncRequests(sftpChannel, deployments);
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
        return returnObject;
    }

    private List<PluSyncToDatabaseRequest> getSyncRequests(ChannelSftp channel, List<VfcDeployment> deployments)
            throws Exception {
        HashMap<String, LsEntry> targetFiles = new HashMap<String, LsEntry>();

        for (VfcDeployment deployment : deployments) {
            channel.cd(deployment.getPluPath());

            // default PLU pattern for store-based PLUs (separate folders)
            String pluPattern = "plu.chg*";

            // automated whitelist PLU pattern (unified folder)
            if (Util.isPluWhitelistDeployment(getBrand(deployment.getDeployment()))) {
                pluPattern = "PLU" + deployment.getStoreId() + ".CHG*";
            }

            Vector<LsEntry> list = channel.ls(pluPattern);
            LsEntry targetFile = getOldestPLUFile(list);

            if (targetFile != null) {
                targetFiles.put(deployment.getDeployment(), targetFile);
            }
        }

        Thread.sleep(7500); // wait to verify if file ready for processing

        ArrayList<PluSyncToDatabaseRequest> syncRequests = new ArrayList<PluSyncToDatabaseRequest>();
        for (VfcDeployment deployment : deployments) {
            channel.cd(deployment.getPluPath());

            LsEntry targetFile = targetFiles.get(deployment.getDeployment());
            if (targetFile != null) {
                String fileName = targetFile.getFilename();

                int oldSize = (int) targetFile.getAttrs().getSize();
                int currentSize = 0;

                try {
                    currentSize = (int) channel.lstat(fileName).getSize();
                } catch (Exception e) {
                    logger.info(String.format("%s not found after delayed directory read (%s)", fileName,
                            deployment.getDeployment()));
                    continue;
                }

                if (oldSize != currentSize) {
                    logger.info(
                            String.format("%s not ready for processing (%s)", fileName, deployment.getDeployment()));
                } else {
                    // Check for existing processing
                    Vector<LsEntry> processingFiles = channel
                            .ls(String.format("%s/%s_*", Constants.PROCESSING_DIRECTORY, deployment.getStoreId()));
                    if (processingFiles.size() > 0) {
                        logger.info(String.format(
                                "Can't begin processing %s. Already processing another file for deployment %s",
                                fileName, deployment.getDeployment()));
                    } else {
                        // Move for processing
                        String processingFileName = deployment.getStoreId() + Constants.PROCESSING_FILE_DELIMITER
                                + currentDatestamp() + Constants.PROCESSING_FILE_DELIMITER + fileName;
                        String processingFileLocation = String.format("%s/%s", Constants.PROCESSING_DIRECTORY,
                                processingFileName);

                        try {
                            channel.rename(fileName, processingFileLocation);
                        } catch (Exception e) {
                            logger.info(String.format("Can't begin processing %s. Failed renaming file for processing.",
                                    fileName));
                            continue;
                        }

                        logger.info(String.format("Queuing %s for processing (%s) for deployment %s...", fileName,
                                processingFileName, deployment.getDeployment()));

                        // Archive to Google Cloud Storage
                        logger.info(String.format("Saving %s -- %s archive to GCP cloud...", deployment.getDeployment(),
                                fileName));
                        InputStream is = channel.get(processingFileLocation);

                        String archiveFolder = getArchiveFolder(getBrand(deployment.getDeployment()),
                                deployment.getStoreId());
                        String encryptionKey = getEncryptionKey(deployment.getDeployment());
                        String fileKey = String.format("%s/%s.secure", archiveFolder, processingFileName);

                        CloudStorageApi cloudStorage = new CloudStorageApi(storageCredentials);

                        try {
                            cloudStorage.encryptAndUploadObject(encryptionKey, archiveBucket, fileKey, is);
                        } catch (RuntimeException e) {
                            logger.error(
                                    "VFC SFTP connection error trying to upload to CloudStorage: " + e.getMessage());
                        }

                        PluSyncToDatabaseRequest newRequest = new PluSyncToDatabaseRequest();
                        newRequest.setOriginalFileName(fileName);
                        newRequest.setProcessingFileName(processingFileName);
                        newRequest.setDeployment(deployment);
                        syncRequests.add(newRequest);

                        if (syncRequests.size() > MAX_REQUESTS) {
                            break;
                        }
                    }
                }
            }
        }

        return syncRequests;
    }

    private String getEncryptionKey(String deployment) throws Exception {
        if (deployment.startsWith("vfcorp-kipling-")) {
            return encryptionKeyKipling;
        } else if (deployment.startsWith("vfcorp-nautica-")) {
            return encryptionKeyNautica;
        } else if (deployment.startsWith("vfcorp-vfo-")) {
            return encryptionKeyVfo;
        } else if (deployment.startsWith("vfcorp-test-")) {
            return encryptionKeyTest;
        } else if (deployment.startsWith("vfcorp-vans-")) {
            return encryptionKeyVans;
        } else if (deployment.startsWith("vfcorp-tnfca-")) {
            return encryptionKeyTnfca;
        } else if (deployment.startsWith("vfcorp-tnf-")) {
            return encryptionKeyTnf;
        }
        throw new Exception("Invalid deployment " + deployment);
    }

    private String getArchiveFolder(String brand, String storeId) {
        return String.format("%s/%s/PLU", brand, storeId);
    }

    private String getBrand(String deployment) throws Exception {
        if (deployment.startsWith("vfcorp-kipling-")) {
            return "Kipling";
        } else if (deployment.startsWith("vfcorp-nautica-")) {
            return "Nautica";
        } else if (deployment.startsWith("vfcorp-vfo-")) {
            return "VFO";
        } else if (deployment.startsWith("vfcorp-test-")) {
            return "Test";
        } else if (deployment.startsWith("vfcorp-vans-")) {
            return "Vans";
        } else if (deployment.startsWith("vfcorp-tnf")) {
            return "TNF";
        }
        throw new Exception("Invalid deployment " + deployment);
    }

    private String currentDatestamp() throws ParseException {
        String tz = "America/Los_Angeles";
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone(tz));
        return TimeManager.toSimpleDateTimeInTimeZone(c, tz, "yyyyMMddHHmmss");
    }

    private ChannelSftp.LsEntry getOldestPLUFile(Vector<ChannelSftp.LsEntry> list) throws ParseException {
        if (list.size() == 0) {
            return null;
        } else if (list.size() == 1) {
            return list.get(0);
        }

        ChannelSftp.LsEntry oldestFile = null;
        Date oldestFileDate = null;
        for (ChannelSftp.LsEntry entry : list) {
            // DEFAULT
            // plu.chg.00001 (initial load, store ID)
            String currentFileName = entry.getFilename();
            Date currentFileDate = new Date();

            if (currentFileName.endsWith(".ex")) {
                currentFileName = currentFileName.substring(0, currentFileName.length() - 3);
            }

            // plu.chg.10042016 (TNF daily upload)
            if (currentFileName.length() == 16) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy");
                currentFileDate = sdf.parse(currentFileName.substring(currentFileName.length() - 8));
            }

            // plu.chg.20161004558833 (TNF daily incremental)
            if (currentFileName.length() == 22) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                currentFileDate = sdf.parse(currentFileName.substring(currentFileName.length() - 14));
            }

            // pluSSSSS.chg.xMMDDHHMMSSmmm (whitelist PLU format)
            if (currentFileName.length() == 27) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmssSSS");
                currentFileDate = sdf.parse(currentFileName.substring(currentFileName.length() - 13));
            }

            if (oldestFile == null || oldestFileDate.compareTo(currentFileDate) > 0) {
                oldestFile = entry;
                oldestFileDate = currentFileDate;
            }
        }

        return oldestFile;
    }
}
