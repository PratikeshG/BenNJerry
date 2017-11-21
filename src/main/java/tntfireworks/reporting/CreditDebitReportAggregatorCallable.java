package tntfireworks.reporting;

import java.util.List;

import javax.activation.DataHandler;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import util.CloudStorageApi;
import util.DbConnection;

public class CreditDebitReportAggregatorCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(CreditDebitReportAggregatorCallable.class);

    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}")
    private String databaseUrl;
    @Value("${mysql.user}")
    private String databaseUser;
    @Value("${mysql.password}")
    private String databasePassword;

    @Value("${google.storage.bucket.archive}")
    private String archiveBucket;
    @Value("${google.storage.account.credentials}")
    private String storageCredentials;
    @Value("${tntfireworks.archive.output.path}")
    private String archivePath;
    @Value("${tntfireworks.encryption.key}")
    private String encryptionKey;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();
        logger.info("Start credit-debit report generation");

        // build report from payloads
        List<List<CreditDebitPayload>> payloadAggregate = (List<List<CreditDebitPayload>>) message
                .getPayload();
        StringBuilder reportBuilder = new StringBuilder();
        boolean addHeader = true;
        String fileDate = "";
        int loadNumber = 0;

        // add file rows
        for (List<CreditDebitPayload> masterPayload : payloadAggregate) {
            for (CreditDebitPayload locationPayload : masterPayload) {
                if (addHeader) {
                    reportBuilder.append(locationPayload.getPayloadHeader());
                    addHeader = false;
                    fileDate = locationPayload.getPayloadDate();
                	loadNumber = locationPayload.loadNumber;
                }
                reportBuilder.append(locationPayload.getRow());
            }
        }

        // increment load number
	    // write new load number into db
		DbConnection dbConnection = new DbConnection(databaseUrl, databaseUser, databasePassword);
	    dbConnection.executeQuery(generateLoadNumberSQLUpsert(CreditDebitPayload.DB_REPORT_NAME, (loadNumber + 1)));
	    dbConnection.close();

        // generate report into file
        String reportName = fileDate + "-report-8.csv";
        String generatedReport = reportBuilder.toString();

        // Archive to Google Cloud Storage
        String fileKey = String.format("%s%s.secure", archivePath, reportName);
        CloudStorageApi cloudStorage = new CloudStorageApi(storageCredentials);
        cloudStorage.encryptAndUploadObject(encryptionKey, archiveBucket, fileKey, generatedReport);

        DataHandler dataHandler = new DataHandler(generatedReport, "text/plain; charset=UTF-8");
        eventContext.getMessage().addOutboundAttachment(reportName, dataHandler);

        // empty return
        return "See attachment.";
    }

    private String generateLoadNumberSQLUpsert(String reportName, int newLoadNumber) {
        String values = String.format("('%s', '%s')", reportName, newLoadNumber);
        String updateStmt = "INSERT INTO tntfireworks_reports_load_number (reportName, count) VALUES " + values;
        updateStmt += " ON DUPLICATE KEY UPDATE count=VALUES(count);";

        return updateStmt;
    }
}
