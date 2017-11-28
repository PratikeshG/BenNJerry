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
    private static final int MAX_REPORT_SIZE = 10000000;
    // charset
    private static final String CHARSET = "UTF-8";

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

    protected String attachReport(MuleMessage muleMessage, String reportName, String generatedReport) throws Exception {
        String emailBody = "";

        // if file size is too large to send in email attachment, send to SFTP
        if (generatedReport.getBytes(CHARSET).length > MAX_REPORT_SIZE) {
            String reportFullPath = sftpBasePath + sftpReportPath;

            // store file in defined sftp location
            ChannelSftp sftpChannel = SshUtil.createConnection(sftpHost, sftpPort, sftpUser, sftpPassword);
            InputStream is = new ByteArrayInputStream(generatedReport.getBytes(CHARSET));
            sftpChannel.put(is, String.format("%s/%s", reportFullPath, reportName));
            SshUtil.closeConnection(sftpChannel);
            logger.info("Sent report to SFTP");

            emailBody = String.format("%s sent to SFTP due to file size.", reportName);
        } else {
            DataHandler dataHandler = new DataHandler(generatedReport, String.format("text/plain; charset=%s", CHARSET));
            muleMessage.addOutboundAttachment(reportName, dataHandler);
            emailBody = String.format("See attached report: %s", reportName);
            logger.info("Attached report to Mule message");
        }

        return emailBody;
    }
}
