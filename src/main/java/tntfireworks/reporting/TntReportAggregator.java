package tntfireworks.reporting;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.activation.DataHandler;
import org.mule.api.MuleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.jcraft.jsch.ChannelSftp;

import tntfireworks.SshUtil;
import util.CloudStorageApi;

public class TntReportAggregator {
    private static Logger logger = LoggerFactory.getLogger(TntReportAggregator.class);
    // max report size in bytes
    // Amazon SES attachment limit 10MB, set limit to 9.5MB
    private static final int MAX_REPORT_SIZE = 9500000;
    // charset
    private static final String CHARSET = "UTF-8";
    protected static final String ADHOC_DIRECTORY = "/Adhoc";

    // glcoud storage values
    @Value("${google.storage.bucket.archive}")
    private String archiveBucket;
    @Value("${google.storage.account.credentials}")
    private String storageCredentials;
    @Value("${tntfireworks.archive.output.path}")
    private String archivePath;
    @Value("${tntfireworks.encryption.key}")
    private String encryptionKey;

    // sftp values
    @Value("${tntfireworks.sftp.host}")
    private String sftpHost;
    @Value("${tntfireworks.sftp.port}")
    private int sftpPort;
    @Value("${tntfireworks.sftp.username}")
    private String sftpUser;
    @Value("${tntfireworks.sftp.password}")
    private String sftpPassword;
    @Value("${tntfireworks.sftp.basepath}")
    private String sftpBasePath;
    @Value("${tntfireworks.sftp.reportpath}")
    private String sftpReportPath;

    protected void archiveReportToGcp(String reportName, String generatedReport) throws Exception {
        String fileKey = String.format("%s/%s.secure", archivePath, reportName);

        // Archive to Google Cloud Storage
        CloudStorageApi cloudStorage = new CloudStorageApi(storageCredentials);
        cloudStorage.encryptAndUploadObject(encryptionKey, archiveBucket, fileKey, generatedReport);
        logger.info("Finished archiving report to Google Cloud");
    }

    protected String storeOrAttachReport(MuleMessage muleMessage, String reportName, String generatedReport)
            throws Exception {
        String emailBody = "";

        // if file size is too large to send in email attachment, send to SFTP
        if (generatedReport.getBytes(CHARSET).length > MAX_REPORT_SIZE) {
            emailBody = storeReport(reportName, generatedReport);
        } else {
            emailBody = attachReport(muleMessage, reportName, generatedReport);
        }

        return emailBody;
    }

    protected String attachReport(MuleMessage muleMessage, String reportName, String generatedReport) throws Exception {
        DataHandler dataHandler = new DataHandler(generatedReport, String.format("text/plain; charset=%s", CHARSET));
        muleMessage.addOutboundAttachment(reportName, dataHandler);
        logger.info("Attached report to Mule message");

        return String.format("See attached report: %s", reportName);
    }

    protected String storeReport(String reportName, String generatedReport, String directory) throws Exception {
        String reportFullPath = sftpBasePath + sftpReportPath + directory;
        logger.info(String.format("Storing report at %s", reportFullPath));

        // store file in defined sftp location
        ChannelSftp sftpChannel = SshUtil.createConnection(sftpHost, sftpPort, sftpUser, sftpPassword);
        InputStream is = new ByteArrayInputStream(generatedReport.getBytes(CHARSET));
        sftpChannel.put(is, String.format("%s/%s", reportFullPath, reportName));
        SshUtil.closeConnection(sftpChannel);
        logger.info("Sent report to SFTP");

        return String.format("%s sent to SFTP.", reportName);
    }

    protected String storeReport(String reportName, String generatedReport) throws Exception {
    	return storeReport(reportName, generatedReport, "");
    }
}
