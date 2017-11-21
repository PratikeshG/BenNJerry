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

public class TransactionsReportAggregatorCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(TransactionsReportAggregatorCallable.class);

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
        logger.info("Start transactions report generation");

        // build report from payloads
        List<List<TransactionsPayload>> payloadAggregate = (List<List<TransactionsPayload>>) message
                .getPayload();
        StringBuilder reportBuilder = new StringBuilder();
        boolean addHeader = true;
        String fileDate = "";

        // add file rows
        for (List<TransactionsPayload> masterPayload : payloadAggregate) {
            for (TransactionsPayload locationPayload : masterPayload) {
                if (addHeader) {
                    reportBuilder.append(locationPayload.getPayloadHeader());
                    addHeader = false;
                    fileDate = locationPayload.getPayloadDate();
                }
                for (String fileRow : locationPayload.getRows()) {
                    reportBuilder.append(fileRow);
                }
            }
        }

        // generate report into file
        String reportName = fileDate + "-report-2.csv";
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
}
