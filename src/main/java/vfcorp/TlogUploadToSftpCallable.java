package vfcorp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.TimeZone;

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
import util.TimeManager;

public class TlogUploadToSftpCallable implements Callable {
    private static final String DEFAULT_TIMEZONE = "America/New_York";
    private static final String BRAND_VANS = "vans";
    private static final String BRAND_TEST = "test";
    private static final String VFC_TLOG_PREFIX = "SA";
    private static final String VFC_TLOG_SUFFIX = ".NEW";
    private static final String VANS_EOD_TLOG_PREFIX = "sls07";
    private static final String VANS_EOD_TLOG_SUFFIX = "HHmm";
    private static final String VANS_STOREFORCE_TLOG_PREFIX = "sas";
    private static final String VANS_STOREFORCE_TLOG_SUFFIX = ".new";

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

    private static final int RETRY_COUNT = 10;
    private static final int RETRY_DELAY_MS = 60000; // 1 minute

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        String tlog = message.getProperty("tlog", PropertyScope.INVOCATION);

        boolean isStoreforceTrickle = message.getProperty("storeforceTrickle", PropertyScope.SESSION).equals("true")
                ? true : false;

        boolean archiveTlog = message.getProperty("enableTlogArchive", PropertyScope.INVOCATION).equals("true") ? true
                : false;

        String vfcorpStoreNumber = message.getProperty("vfcorpStoreNumber", PropertyScope.INVOCATION);
        VfcDeployment deployment = message.getProperty("tlogVfcDeployment", PropertyScope.INVOCATION);

        String storeforceArchiveDirectory = message.getProperty("storeforceArchiveDirectory", PropertyScope.INVOCATION);
        String storeforceTrickleDirectory = message.getProperty("storeforceTrickleDirectory", PropertyScope.INVOCATION);

        String fileName = getVfcFilename(deployment, vfcorpStoreNumber);

        // Archive complete TLOGs to Google Cloud Storage
        if (!isStoreforceTrickle) {
            String encryptionKey = message.getProperty("encryptionKey", PropertyScope.INVOCATION);
            String cloudArchiveFolder = message.getProperty("cloudArchiveFolder", PropertyScope.INVOCATION);
            String fileKey = String.format("%s%s.secure", cloudArchiveFolder, fileName);

            CloudStorageApi cloudStorage = new CloudStorageApi(storageCredentials);
            cloudStorage.encryptAndUploadObject(encryptionKey, archiveBucket, fileKey, tlog);
        }

        return uploadTlogsWithRetries(message, tlog, isStoreforceTrickle, archiveTlog, vfcorpStoreNumber, deployment,
                storeforceArchiveDirectory, storeforceTrickleDirectory);
    }

    private Object uploadTlogsWithRetries(MuleMessage message, String tlog, boolean isStoreforceTrickle,
            boolean archiveTlog, String vfcorpStoreNumber, VfcDeployment deployment, String storeforceArchiveDirectory,
            String storeforceTrickleDirectory) throws InterruptedException, Exception {
        Exception lastException = null;
        for (int i = 0; i < RETRY_COUNT; i++) {
            try {
                return uploadTntTlogsViaSftp(message, tlog, isStoreforceTrickle, archiveTlog, vfcorpStoreNumber,
                        deployment, storeforceArchiveDirectory, storeforceTrickleDirectory);

            } catch (Exception e) {
                lastException = e;
                lastException.printStackTrace();
                Thread.sleep(RETRY_DELAY_MS);
            }
        }

        throw lastException;
    }

    private Object uploadTntTlogsViaSftp(MuleMessage message, String tlog, boolean isStoreforceTrickle,
            boolean archiveTlog, String vfcorpStoreNumber, VfcDeployment deployment, String storeforceArchiveDirectory,
            String storeforceTrickleDirectory)
            throws JSchException, IOException, UnsupportedEncodingException, SftpException, ParseException {
        Session session = Util.createSSHSession(sftpHost, sftpUser, sftpPassword, sftpPort);
        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();

        // Only put in main SFTP folder if final end of day TLOG generation flow
        if (!isStoreforceTrickle) {
            InputStream tlogUploadStream = new ByteArrayInputStream(tlog.getBytes("UTF-8"));
            sftpChannel.cd(deployment.getTlogPath());
            sftpChannel.put(tlogUploadStream, getVfcFilename(deployment, vfcorpStoreNumber), ChannelSftp.OVERWRITE);

            // Archive copy enabled
            if (archiveTlog) {
                InputStream archiveUploadStream = new ByteArrayInputStream(tlog.getBytes("UTF-8"));
                String timeZone = message.getProperty("timeZone", PropertyScope.INVOCATION);

                String archiveFilename = getVfcArchiveFilename(deployment, vfcorpStoreNumber, timeZone);

                String archiveDirectory = deployment.getTlogPath() + "/Archive";
                sftpChannel.cd(archiveDirectory);
                sftpChannel.put(archiveUploadStream, archiveFilename, ChannelSftp.OVERWRITE);
            }
        }

        // If deployment has Storeforce enabled, save copy of TLOG to SF archive directory
        if (storeforceArchiveDirectory.length() > 0) {
            InputStream storeforceUploadStream = new ByteArrayInputStream(tlog.getBytes("UTF-8"));

            // TEMPORARY: Save EoD to Storeforce directory
            if (!isStoreforceTrickle) {
                sftpChannel.cd(storeforceArchiveDirectory);
            } else {
                sftpChannel.cd(storeforceTrickleDirectory);
            }

            sftpChannel.put(storeforceUploadStream, getStoreforceFilename(deployment, vfcorpStoreNumber),
                    ChannelSftp.OVERWRITE);
        }

        sftpChannel.disconnect();
        session.disconnect();

        return tlog;
    }

    private boolean isVansDeployment(VfcDeployment deployment) {
        return deployment.getDeployment().contains(BRAND_VANS) || deployment.getDeployment().contains(BRAND_TEST);
    }

    private String getStoreforceFilename(VfcDeployment deployment, String vfcorpStoreNumber) {
        if (isVansDeployment(deployment)) {
            return String.format("%s%s%s", VANS_STOREFORCE_TLOG_PREFIX, fourDigitStoreNumber(vfcorpStoreNumber),
                    VANS_STOREFORCE_TLOG_SUFFIX);
        }
        return String.format("%s%s%s", VFC_TLOG_PREFIX, vfcorpStoreNumber, VFC_TLOG_SUFFIX);
    }

    private String getVfcFilename(VfcDeployment deployment, String vfcorpStoreNumber) throws ParseException {
        if (isVansDeployment(deployment)) {
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone(DEFAULT_TIMEZONE));
            String dateSuffix = TimeManager.toSimpleDateTimeInTimeZone(TimeManager.toIso8601(c, DEFAULT_TIMEZONE),
                    DEFAULT_TIMEZONE, VANS_EOD_TLOG_SUFFIX);
            return String.format("%s%s%s", VANS_EOD_TLOG_PREFIX, vfcorpStoreNumber, dateSuffix);
        }
        return String.format("%s%s%s", VFC_TLOG_PREFIX, vfcorpStoreNumber, VFC_TLOG_SUFFIX);
    }

    private String getVfcArchiveFilename(VfcDeployment deployment, String vfcorpStoreNumber, String timeZone)
            throws ParseException {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        String currentDate = TimeManager.toSimpleDateTimeInTimeZone(TimeManager.toIso8601(c, timeZone), timeZone,
                "yyyy-MM-dd-HH-mm-ss");

        return String.format("%s_%s", currentDate, getVfcFilename(deployment, vfcorpStoreNumber));
    }

    private String fourDigitStoreNumber(String storeNumber) {
        return storeNumber.substring(storeNumber.length() - 4);
    }
}
