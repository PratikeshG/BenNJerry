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

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import util.TimeManager;

public class PLUPollSyncToDatabaseCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(PLUPollSyncToDatabaseCallable.class);
    private static final String PROCESSING_DIRECTORY = "processing";

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
        ArrayList<VFCDeployment> deployments = (ArrayList<VFCDeployment>) Util.getVFCDeployments(databaseUrl,
                databaseUser, databasePassword, "enablePLU = 1");

        Session session = Util.createSSHSession(sftpHost, sftpUser, sftpPassword, sftpPort);
        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();
        System.out.println("SFTP channel created.");

        List<PLUSyncToDatabaseRequest> syncRequests = getSyncRequests(sftpChannel, deployments);

        sftpChannel.disconnect();
        System.out.println("SFTP channel disconnected.");
        session.disconnect();
        System.out.println("SFTP session disconnected.");

        return syncRequests;
    }

    private List<PLUSyncToDatabaseRequest> getSyncRequests(ChannelSftp channel, List<VFCDeployment> deployments)
            throws SftpException, ParseException, InterruptedException {
        HashMap<String, LsEntry> targetFiles = new HashMap<String, LsEntry>();

        for (VFCDeployment deployment : deployments) {
            channel.cd(deployment.getPluPath());

            Vector<LsEntry> list = channel.ls("plu.chg*");
            LsEntry targetFile = getOldestPLUFile(list);

            if (targetFile != null) {
                targetFiles.put(deployment.getPluPath(), targetFile);
            }
        }

        Thread.sleep(7500); // wait to verify if file ready for processing

        ArrayList<PLUSyncToDatabaseRequest> syncRequests = new ArrayList<PLUSyncToDatabaseRequest>();
        for (VFCDeployment deployment : deployments) {
            channel.cd(deployment.getPluPath());

            LsEntry targetFile = targetFiles.get(deployment.getPluPath());
            if (targetFile != null) {
                String fileName = targetFile.getFilename();

                int oldSize = (int) targetFile.getAttrs().getSize();
                int currentSize = (int) channel.lstat(fileName).getSize();

                if (oldSize != currentSize) {
                    logger.info(String.format("%s not ready for processing.", fileName));
                } else {
                    // Check for existing processing
                    Vector<LsEntry> processingFiles = channel
                            .ls(String.format("./%s/*_plu.chg.*", PROCESSING_DIRECTORY));
                    if (processingFiles.size() > 0) {
                        logger.info(
                                String.format("Can't begin processing %s. Already processing another file.", fileName));
                    } else {
                        // Move for processing
                        String processingFileName = currentDatestamp() + "_" + fileName;
                        channel.rename(fileName, String.format("./%s/%s", PROCESSING_DIRECTORY, processingFileName));

                        logger.info(String.format("Queuing %s for processing (%s)...", fileName, processingFileName));

                        PLUSyncToDatabaseRequest newRequest = new PLUSyncToDatabaseRequest();
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