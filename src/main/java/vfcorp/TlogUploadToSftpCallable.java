package vfcorp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.TimeZone;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;

import util.TimeManager;

public class TlogUploadToSftpCallable implements Callable {
    private static final String TLOG_PREFIX = "SA";
    private static final String TLOG_SUFFIX = ".NEW";

    private String sftpHost;
    private int sftpPort;
    private String sftpUser;
    private String sftpPassword;

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

        String tlog = message.getProperty("tlog", PropertyScope.INVOCATION);
        boolean archiveTlog = message.getProperty("enableTlogArchive", PropertyScope.INVOCATION).equals("true") ? true
                : false;
        String vfcorpStoreNumber = message.getProperty("vfcorpStoreNumber", PropertyScope.INVOCATION);
        VfcDeployment deployment = message.getProperty("tlogVfcDeployment", PropertyScope.INVOCATION);
        String storeforceArchiveDirectory = message.getProperty("storeforceArchiveDirectory", PropertyScope.INVOCATION);

        Session session = Util.createSSHSession(sftpHost, sftpUser, sftpPassword, sftpPort);
        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();

        String uploadPattern = TLOG_PREFIX + vfcorpStoreNumber + TLOG_SUFFIX;
        InputStream tlogUploadStream = new ByteArrayInputStream(tlog.getBytes("UTF-8"));
        sftpChannel.cd(deployment.getTlogPath());
        sftpChannel.put(tlogUploadStream, uploadPattern, ChannelSftp.OVERWRITE);

        // Archive enabled
        if (archiveTlog) {
            InputStream archiveUploadStream = new ByteArrayInputStream(tlog.getBytes("UTF-8"));

            String timeZone = message.getProperty("timeZone", PropertyScope.INVOCATION);
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
            String currentDate = TimeManager.toSimpleDateTimeInTimeZone(TimeManager.toIso8601(c, timeZone), timeZone,
                    "yyyy-MM-dd-HH-mm-ss");

            String archiveUploadPattern = currentDate + "_" + TLOG_PREFIX + vfcorpStoreNumber + TLOG_SUFFIX;

            String archiveDirectory = deployment.getTlogPath() + "/Archive";
            sftpChannel.cd(archiveDirectory);

            try {
                sftpChannel.put(archiveUploadStream, archiveUploadPattern, ChannelSftp.OVERWRITE);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage() + " " + archiveDirectory + " " + archiveUploadPattern);
            }
            sftpChannel.put(archiveUploadStream, archiveUploadPattern, ChannelSftp.OVERWRITE);
        }

        // If deployment has storeforce enabled, save another copy of TLOG to SF archive directory
        if (storeforceArchiveDirectory.length() > 0) {
            InputStream storeforceUploadStream = new ByteArrayInputStream(tlog.getBytes("UTF-8"));

            sftpChannel.cd(storeforceArchiveDirectory);
            sftpChannel.put(storeforceUploadStream, uploadPattern, ChannelSftp.OVERWRITE);
        }

        sftpChannel.disconnect();
        session.disconnect();

        return tlog;
    }
}
