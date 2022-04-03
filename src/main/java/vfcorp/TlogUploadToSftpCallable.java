package vfcorp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;

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
    private static final String SAP_TLOG_PREFIX = "SA";
    private static final String SAP_TLOG_DATE = "MMddyyyyHHmmss";
    private static final String SAP_TLOG_SUFFIX = ".Processed";
    private static final String VANS_SAP_TLOG_SUFFIX = ".vans_us";
    private static final String TNF_SAP_TLOG_SUFFIX = ".tnf_us";
    private static final String TNFCA_SAP_TLOG_SUFFIX = ".tnf_ca";

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

    private static final int RETRY_COUNT = 20;
    private static final int RETRY_DELAY_MAX_SECONDS = 80;

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

        // Skip file upload when there is no data
        if (tlog.length() < 1) {
            logger.debug(deployment.getDeployment() + ": EMPTY tlog - skipping " + tlogType);
            return tlog;
        }

        return uploadTlogsWithRetries(message, tlog, tlogType, archiveTlog, vfcorpStoreNumber, deployment,
                storeforceArchiveDirectory, storeforceTrickleDirectory, sapTrickleDirectory);
    }

    private Object uploadTlogsWithRetries(MuleMessage message, String tlog, String tlogType, boolean archiveTlog,
            String vfcorpStoreNumber, VfcDeployment deployment, String storeforceArchiveDirectory,
            String storeforceTrickleDirectory, String sapTrickleDirectory) throws InterruptedException, Exception {
        Exception lastException = null;

        Session session = null;
        ChannelSftp sftpChannel = null;
        Object uploadObject = null;

        for (int i = 0; i < RETRY_COUNT; i++) {
            try {
                session = Util.createSSHSession(sftpHost, sftpUser, sftpPassword, sftpPort);
                sftpChannel = (ChannelSftp) session.openChannel("sftp");

                sftpChannel.connect();

                uploadObject = uploadVfcTlogsViaSftp(message, sftpChannel, tlog, tlogType, archiveTlog,
                        vfcorpStoreNumber, deployment, storeforceArchiveDirectory, storeforceTrickleDirectory,
                        sapTrickleDirectory);
                break;
            } catch (Exception e) {
                if (sftpChannel != null) {
                    sftpChannel.disconnect();
                }
                if (session != null) {
                    session.disconnect();
                }

                lastException = e;
                logger.warn(String.format("Error connecting to SFTP for %s TLOG upload - %s",
                        deployment.getDeployment(), lastException.toString()));

                int randomSeconds = ThreadLocalRandom.current().nextInt(1, RETRY_DELAY_MAX_SECONDS + 1);
                Thread.sleep(randomSeconds * 1000); // sleep() accepts milliseconds
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
        return uploadObject;
    }

    private Object uploadVfcTlogsViaSftp(MuleMessage message, ChannelSftp sftpChannel, String tlog, String tlogType,
            boolean archiveTlog, String vfcorpStoreNumber, VfcDeployment deployment, String storeforceArchiveDirectory,
            String storeforceTrickleDirectory, String sapTrickleDirectory)
            throws JSchException, IOException, UnsupportedEncodingException, SftpException, ParseException {

        // Only put in main SFTP folder if final end of day TLOG generation flow
        if (tlogType.equals("EOD")) {
            InputStream tlogUploadStream = new ByteArrayInputStream(tlog.getBytes("UTF-8"));
            sftpChannel.cd(deployment.getTlogPath());
            sftpChannel.put(tlogUploadStream, getVfcFilename(deployment, vfcorpStoreNumber), ChannelSftp.OVERWRITE);

            if (tlogUploadStream != null) {
                tlogUploadStream.close();
            }

            // Also save a copy to Storeforce for EOD
            if (storeforceArchiveDirectory.length() > 0) {
                InputStream storeforceUploadStream = new ByteArrayInputStream(tlog.getBytes("UTF-8"));
                sftpChannel.cd(storeforceArchiveDirectory);
                sftpChannel.put(storeforceUploadStream, getStoreforceFilename(deployment, vfcorpStoreNumber),
                        ChannelSftp.OVERWRITE);

                if (storeforceUploadStream != null) {
                    storeforceUploadStream.close();
                }
            }

            // Archive copy enabled
            if (archiveTlog) {
                InputStream archiveUploadStream = new ByteArrayInputStream(tlog.getBytes("UTF-8"));
                String timeZone = message.getProperty("timeZone", PropertyScope.INVOCATION);

                String archiveFilename = getVfcArchiveFilename(deployment, vfcorpStoreNumber, timeZone);

                String archiveDirectory = deployment.getTlogPath() + "/Archive";
                sftpChannel.cd(archiveDirectory);
                sftpChannel.put(archiveUploadStream, archiveFilename, ChannelSftp.OVERWRITE);

                if (archiveUploadStream != null) {
                    archiveUploadStream.close();
                }
            }
        } else if (tlogType.equals("SAP")) {
            String sapFilename = getSAPFilename(deployment, vfcorpStoreNumber);
            InputStream saptrickleUploadStream = new ByteArrayInputStream(tlog.getBytes("UTF-8"));
            sftpChannel.cd(sapTrickleDirectory);
            sftpChannel.put(saptrickleUploadStream, sapFilename, ChannelSftp.OVERWRITE);

            if (saptrickleUploadStream != null) {
                saptrickleUploadStream.close();
            }

            // Archive copy enabled
            if (archiveTlog) {
                InputStream archiveUploadStream = new ByteArrayInputStream(tlog.getBytes("UTF-8"));
                sftpChannel.cd(sapTrickleDirectory + "/Archive");
                sftpChannel.put(archiveUploadStream, sapFilename, ChannelSftp.OVERWRITE);

                if (archiveUploadStream != null) {
                    archiveUploadStream.close();
                }
            }
        } else if (tlogType.equals("STOREFORCE")) {
            InputStream storeforceUploadStream = new ByteArrayInputStream(tlog.getBytes("UTF-8"));
            sftpChannel.cd(storeforceTrickleDirectory);
            sftpChannel.put(storeforceUploadStream, getStoreforceFilename(deployment, vfcorpStoreNumber),
                    ChannelSftp.OVERWRITE);

            if (storeforceUploadStream != null) {
                storeforceUploadStream.close();
            }
        }

        return tlog;
    }

    private boolean isVansDeployment(VfcDeployment deployment) {
        return deployment.getDeployment().contains(BRAND_VANS) || deployment.getDeployment().contains(BRAND_TEST);
    }

    private String getSAPFilename(VfcDeployment deployment, String vfcorpStoreNumber) throws ParseException {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone(DEFAULT_TIMEZONE));

        // Brand specific SAP trickle format
        String dateSuffix = TimeManager.toSimpleDateTimeInTimeZone(TimeManager.toIso8601(c, DEFAULT_TIMEZONE),
                DEFAULT_TIMEZONE, SAP_TLOG_DATE);

        String brandSuffix = getBrandSAPSuffix(deployment);

        return String.format("%s%s%s%s%s", SAP_TLOG_PREFIX, vfcorpStoreNumber, dateSuffix, brandSuffix,
                SAP_TLOG_SUFFIX);
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
            // Vans unique EOD tlog format
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone(DEFAULT_TIMEZONE));

            String dateSuffix = TimeManager.toSimpleDateTimeInTimeZone(TimeManager.toIso8601(c, DEFAULT_TIMEZONE),
                    DEFAULT_TIMEZONE, VANS_EOD_TLOG_SUFFIX);
            return String.format("%s%s%s", VANS_EOD_TLOG_PREFIX, vfcorpStoreNumber, dateSuffix);

        }

        // default EOD format
        return String.format("%s%s%s", VFC_TLOG_PREFIX, vfcorpStoreNumber, VFC_TLOG_SUFFIX);
    }

    private String getBrandSAPSuffix(VfcDeployment deployment) {
        String suffix = "";

        if (deployment.getDeployment().startsWith("vfcorp-tnf-")) {
            suffix = TNF_SAP_TLOG_SUFFIX;
        } else if (deployment.getDeployment().startsWith("vfcorp-tnfca-")) {
            suffix = TNFCA_SAP_TLOG_SUFFIX;
        } else if (isVansDeployment(deployment)) {
            suffix = VANS_SAP_TLOG_SUFFIX;
        }

        return suffix;
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
