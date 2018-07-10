package vfcorp;

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
import org.mule.api.lifecycle.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

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

    private static final int RETRY_COUNT = 10;
    private static final int RETRY_DELAY_MS = 60000; // 1 minute

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        ArrayList<VfcDeployment> deployments = (ArrayList<VfcDeployment>) Util.getVfcDeployments(databaseUrl,
                databaseUser, databasePassword, "enablePLU = 1");

        Exception lastException = null;
        for (int i = 0; i < RETRY_COUNT; i++) {
            try {
                Session session = Util.createSSHSession(sftpHost, sftpUser, sftpPassword, sftpPort);
                logger.info("VFC: SFTP session created");

                ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
                sftpChannel.connect();
                logger.info("VFC: SFTP channel created");

                List<PluSyncToDatabaseRequest> syncRequests = getSyncRequests(sftpChannel, deployments);

                sftpChannel.disconnect();
                logger.info("VFC: SFTP channel disconnected");

                session.disconnect();
                logger.info("VFC: SFTP session disconnected");

                return syncRequests;
            } catch (Exception e) {
                lastException = e;
                lastException.printStackTrace();
                Thread.sleep(RETRY_DELAY_MS);
            }
        }

        throw lastException;
    }

    private List<PluSyncToDatabaseRequest> getSyncRequests(ChannelSftp channel, List<VfcDeployment> deployments)
            throws SftpException, ParseException, InterruptedException {
        HashMap<String, LsEntry> targetFiles = new HashMap<String, LsEntry>();

        for (VfcDeployment deployment : deployments) {
            channel.cd(deployment.getPluPath());

            Vector<LsEntry> list = channel.ls("plu.chg*");
            LsEntry targetFile = getOldestPLUFile(list);

            if (targetFile != null) {
                targetFiles.put(deployment.getPluPath(), targetFile);
            }
        }

        Thread.sleep(7500); // wait to verify if file ready for processing

        ArrayList<PluSyncToDatabaseRequest> syncRequests = new ArrayList<PluSyncToDatabaseRequest>();
        for (VfcDeployment deployment : deployments) {
            channel.cd(deployment.getPluPath());

            LsEntry targetFile = targetFiles.get(deployment.getPluPath());
            if (targetFile != null) {
                String fileName = targetFile.getFilename();

                int oldSize = (int) targetFile.getAttrs().getSize();
                int currentSize = (int) channel.lstat(fileName).getSize();

                if (oldSize != currentSize) {
                    logger.info(
                            String.format("%s not ready for processing (%s)", fileName, deployment.getDeployment()));
                } else {
                    // Check for existing processing
                    Vector<LsEntry> processingFiles = channel
                            .ls(String.format("%s/*_plu.chg.*", Constants.PROCESSING_DIRECTORY));
                    if (processingFiles.size() > 0) {
                        logger.info(String.format(
                                "Can't begin processing %s. Already processing another file for deployment %s",
                                fileName, deployment.getDeployment()));
                    } else {
                        // Move for processing
                        String processingFileName = currentDatestamp() + Constants.PROCESSING_FILE_DELIMITER + fileName;
                        channel.rename(fileName,
                                String.format("%s/%s", Constants.PROCESSING_DIRECTORY, processingFileName));

                        logger.info(String.format("Queuing %s for processing (%s) for deployment %s...", fileName,
                                processingFileName, deployment.getDeployment()));

                        PluSyncToDatabaseRequest newRequest = new PluSyncToDatabaseRequest();
                        newRequest.setOriginalFileName(fileName);
                        newRequest.setProcessingFileName(processingFileName);
                        newRequest.setDeployment(deployment);

                        syncRequests.add(newRequest);
                    }
                }
            }
        }

        return syncRequests;
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

            // plu.chg.10042016 (daily upload)
            if (entry.getFilename().length() == 16) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy");
                currentFileDate = sdf.parse(currentFileName.substring(currentFileName.length() - 8));
            }

            // plu.chg.20161004558833 (daily incremental)
            if (entry.getFilename().length() == 22) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                currentFileDate = sdf.parse(currentFileName.substring(currentFileName.length() - 14));
            }

            if (oldestFile == null || oldestFileDate.compareTo(currentFileDate) > 0) {
                oldestFile = entry;
                oldestFileDate = currentFileDate;
            }
        }

        return oldestFile;
    }
}