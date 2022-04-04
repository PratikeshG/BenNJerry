package vfcorp;

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

        message.setProperty("tlogProcessed", false, PropertyScope.INVOCATION);
        message.setProperty("tlogArchiveProcessed", false, PropertyScope.INVOCATION);
        message.setProperty("storeforceTlogArchiveProcessed", false, PropertyScope.INVOCATION);

        // Skip file upload when there is no data
        if (tlog.length() < 1) {
            return tlog;
        }

        return generateTlogSftpVariables(message, tlog, tlogType, archiveTlog, vfcorpStoreNumber, deployment,
                storeforceArchiveDirectory, storeforceTrickleDirectory, sapTrickleDirectory);
    }

    private String generateTlogSftpVariables(MuleMessage message, String tlog, String tlogType, boolean archiveTlog,
            String vfcorpStoreNumber, VfcDeployment deployment, String storeforceArchiveDirectory,
            String storeforceTrickleDirectory, String sapTrickleDirectory)
            throws UnsupportedEncodingException, ParseException {

        // Only put in main SFTP folder if final end of day TLOG generation flow
        if (tlogType.equals("EOD")) {
            message.setProperty("tlogDirectory", deployment.getTlogPath(), PropertyScope.INVOCATION);
            message.setProperty("tlogFilename", getVfcFilename(deployment, vfcorpStoreNumber),
                    PropertyScope.INVOCATION);

            // Also save a copy to Storeforce for EOD
            if (storeforceArchiveDirectory.length() > 0) {
                message.setProperty("storeforceTlogArchiveProcessed", true, PropertyScope.INVOCATION);
                message.setProperty("tlogStoreforceArchiveDirectory", storeforceArchiveDirectory,
                        PropertyScope.INVOCATION);
                message.setProperty("tlogStoreforceArchiveFilename",
                        getStoreforceFilename(deployment, vfcorpStoreNumber), PropertyScope.INVOCATION);
            }

            // Archive copy enabled
            if (archiveTlog) {

                String timeZone = message.getProperty("timeZone", PropertyScope.INVOCATION);

                String archiveDirectory = deployment.getTlogPath() + "/Archive";
                String archiveFilename = getVfcArchiveFilename(deployment, vfcorpStoreNumber, timeZone);

                message.setProperty("tlogArchiveProcessed", true, PropertyScope.INVOCATION);
                message.setProperty("tlogArchiveDirectory", archiveDirectory, PropertyScope.INVOCATION);
                message.setProperty("tlogArchiveFilename", archiveFilename, PropertyScope.INVOCATION);
            }
        } else if (tlogType.equals("SAP")) {
            String sapFilename = getSAPFilename(deployment, vfcorpStoreNumber);

            message.setProperty("tlogDirectory", sapTrickleDirectory, PropertyScope.INVOCATION);
            message.setProperty("tlogFilename", sapFilename, PropertyScope.INVOCATION);

            // Archive copy enabled
            if (archiveTlog) {
                message.setProperty("tlogArchiveProcessed", true, PropertyScope.INVOCATION);
                message.setProperty("tlogArchiveDirectory", sapTrickleDirectory + "/Archive", PropertyScope.INVOCATION);
                message.setProperty("tlogArchiveFilename", sapFilename, PropertyScope.INVOCATION);
            }

        } else if (tlogType.equals("STOREFORCE")) {
            message.setProperty("tlogDirectory", storeforceTrickleDirectory, PropertyScope.INVOCATION);
            message.setProperty("tlogFilename", getStoreforceFilename(deployment, vfcorpStoreNumber),
                    PropertyScope.INVOCATION);
        }

        message.setProperty("tlogProcessed", true, PropertyScope.INVOCATION);
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
