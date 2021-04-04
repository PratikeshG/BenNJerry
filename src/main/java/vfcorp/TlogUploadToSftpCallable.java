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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final String VANS_SAP_TLOG_PREFIX = "SA";
    private static final String VANS_SAP_TLOG_DATE = "MMddyyyyHHmmss";
    private static final String VANS_SAP_TLOG_SUFFIX = ".vans_us.Processed";

    private static Logger logger = LoggerFactory.getLogger(TlogUploadToSftpCallable.class);

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
        String tlogType = message.getProperty("tlogType", PropertyScope.SESSION);

        boolean archiveTlog = message.getProperty("enableTlogArchive", PropertyScope.INVOCATION).equals("true") ? true
                : false;

        String vfcorpStoreNumber = message.getProperty("vfcorpStoreNumber", PropertyScope.INVOCATION);
        VfcDeployment deployment = message.getProperty("tlogVfcDeployment", PropertyScope.INVOCATION);

        String storeforceArchiveDirectory = message.getProperty("storeforceArchiveDirectory", PropertyScope.INVOCATION);
        String storeforceTrickleDirectory = message.getProperty("storeforceTrickleDirectory", PropertyScope.INVOCATION);
        String sapTrickleDirectory = message.getProperty("sapTrickleDirectory", PropertyScope.INVOCATION);

        String fileName = getVfcFilename(tlogType, deployment, vfcorpStoreNumber);

        // Archive complete TLOGs to Google Cloud Storage
        if (tlogType.equals("EOD")) {
            String encryptionKey = message.getProperty("encryptionKey", PropertyScope.INVOCATION);
            String cloudArchiveFolder = message.getProperty("cloudArchiveFolder", PropertyScope.INVOCATION);
            String fileKey = String.format("%s%s.secure", cloudArchiveFolder, fileName);

            CloudStorageApi cloudStorage = new CloudStorageApi(storageCredentials);
            cloudStorage.encryptAndUploadObject(encryptionKey, archiveBucket, fileKey, tlog);
        }

        return uploadTlogsWithRetries(message, tlog, tlogType, archiveTlog, vfcorpStoreNumber, deployment,
                storeforceArchiveDirectory, storeforceTrickleDirectory, sapTrickleDirectory);
    }

    private Object uploadTlogsWithRetries(MuleMessage message, String tlog, String tlogType, boolean archiveTlog,
            String vfcorpStoreNumber, VfcDeployment deployment, String storeforceArchiveDirectory,
            String storeforceTrickleDirectory, String sapTrickleDirectory) throws InterruptedException, Exception {
        Exception lastException = null;
        for (int i = 0; i < RETRY_COUNT; i++) {
            try {
                return uploadVfcTlogsViaSftp(message, tlog, tlogType, archiveTlog, vfcorpStoreNumber, deployment,
                        storeforceArchiveDirectory, storeforceTrickleDirectory, sapTrickleDirectory);

            } catch (Exception e) {
                lastException = e;
                lastException.printStackTrace();
                Thread.sleep(RETRY_DELAY_MS);
            }
        }

        throw lastException;
    }

    private Object uploadVfcTlogsViaSftp(MuleMessage message, String tlog, String tlogType, boolean archiveTlog,
            String vfcorpStoreNumber, VfcDeployment deployment, String storeforceArchiveDirectory,
            String storeforceTrickleDirectory, String sapTrickleDirectory)
            throws JSchException, IOException, UnsupportedEncodingException, SftpException, ParseException {
        Session session = Util.createSSHSession(sftpHost, sftpUser, sftpPassword, sftpPort);
        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();

        // Only put in main SFTP folder if final end of day TLOG generation flow
        if (tlogType.equals("EOD")) {
            InputStream tlogUploadStream = new ByteArrayInputStream(tlog.getBytes("UTF-8"));
            sftpChannel.cd(deployment.getTlogPath());
            sftpChannel.put(tlogUploadStream, getVfcFilename(tlogType, deployment, vfcorpStoreNumber),
                    ChannelSftp.OVERWRITE);

            // Also save a copy to Storeforce for EOD
            if (storeforceArchiveDirectory.length() > 0) {
                InputStream storeforceUploadStream = new ByteArrayInputStream(tlog.getBytes("UTF-8"));
                sftpChannel.cd(storeforceArchiveDirectory);
                sftpChannel.put(storeforceUploadStream, getStoreforceFilename(deployment, vfcorpStoreNumber),
                        ChannelSftp.OVERWRITE);
            }

            // Archive copy enabled
            if (archiveTlog) {
                InputStream archiveUploadStream = new ByteArrayInputStream(tlog.getBytes("UTF-8"));
                String timeZone = message.getProperty("timeZone", PropertyScope.INVOCATION);

                String archiveFilename = getVfcArchiveFilename(tlogType, deployment, vfcorpStoreNumber, timeZone);

                String archiveDirectory = deployment.getTlogPath() + "/Archive";
                sftpChannel.cd(archiveDirectory);
                sftpChannel.put(archiveUploadStream, archiveFilename, ChannelSftp.OVERWRITE);
            }
        } else if (tlogType.equals("SAP")) {
            logger.debug(deployment + ": tlogType is SAP");

            // Skip SAP trickle when there is no new transactions
            if (tlog.length() < 1) {
                logger.debug(deployment + ": tlogType EMPTY - skipping");
                return tlog;
            }

            InputStream saptrickleUploadStream = new ByteArrayInputStream(tlog.getBytes("UTF-8"));
            sftpChannel.cd(sapTrickleDirectory);
            sftpChannel.put(saptrickleUploadStream, getVfcFilename(tlogType, deployment, vfcorpStoreNumber),
                    ChannelSftp.OVERWRITE);
        } else if (tlogType.equals("STOREFORCE")) {
            InputStream storeforceUploadStream = new ByteArrayInputStream(tlog.getBytes("UTF-8"));
            sftpChannel.cd(storeforceTrickleDirectory);
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

    private String getVfcFilename(String tlogType, VfcDeployment deployment, String vfcorpStoreNumber)
            throws ParseException {
        if (isVansDeployment(deployment)) {
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone(DEFAULT_TIMEZONE));

            if (tlogType.equals("SAP")) {
                String dateSuffix = TimeManager.toSimpleDateTimeInTimeZone(TimeManager.toIso8601(c, DEFAULT_TIMEZONE),
                        DEFAULT_TIMEZONE, VANS_SAP_TLOG_DATE);
                return String.format("%s%s%s%s", VANS_SAP_TLOG_PREFIX, vfcorpStoreNumber, dateSuffix,
                        VANS_SAP_TLOG_SUFFIX);
            } else { // EOD
                String dateSuffix = TimeManager.toSimpleDateTimeInTimeZone(TimeManager.toIso8601(c, DEFAULT_TIMEZONE),
                        DEFAULT_TIMEZONE, VANS_EOD_TLOG_SUFFIX);
                return String.format("%s%s%s", VANS_EOD_TLOG_PREFIX, vfcorpStoreNumber, dateSuffix);
            }
        }
        return String.format("%s%s%s", VFC_TLOG_PREFIX, vfcorpStoreNumber, VFC_TLOG_SUFFIX);
    }

    private String getVfcArchiveFilename(String tlogType, VfcDeployment deployment, String vfcorpStoreNumber,
            String timeZone) throws ParseException {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        String currentDate = TimeManager.toSimpleDateTimeInTimeZone(TimeManager.toIso8601(c, timeZone), timeZone,
                "yyyy-MM-dd-HH-mm-ss");

        return String.format("%s_%s", currentDate, getVfcFilename(tlogType, deployment, vfcorpStoreNumber));
    }

    private String fourDigitStoreNumber(String storeNumber) {
        return storeNumber.substring(storeNumber.length() - 4);
    }
}
