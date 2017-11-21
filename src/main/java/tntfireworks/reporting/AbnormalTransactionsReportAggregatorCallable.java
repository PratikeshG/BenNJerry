package tntfireworks.reporting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.v2.Tender;
import com.squareup.connect.v2.Transaction;

import util.CloudStorageApi;

public class AbnormalTransactionsReportAggregatorCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(AbnormalTransactionsReportAggregatorCallable.class);

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
        logger.info("Start abnormal transactions report generation");

        // build report from payloads
        List<List<AbnormalTransactionsPayload>> payloadAggregate = (List<List<AbnormalTransactionsPayload>>) message
                .getPayload();
        StringBuilder reportBuilder = new StringBuilder();
        boolean addHeader = true;
        String fileDate = "";

        // this block is used to find alert 4 transactions
        for (List<AbnormalTransactionsPayload> masterPayload : payloadAggregate) {
            // this map is used to find cards used multiple times across an entire merchant account
            // (k, v) == (fingerprint, List<Transaction>)
            Map<String, ArrayList<Transaction>> fingerprintToTransactions = new HashMap<String, ArrayList<Transaction>>();

            // this map of location payloads is used to add alert 4 transactions later
            // (k, v) == (location_id, AbnormalTransactionsPayload)
            Map<String, AbnormalTransactionsPayload> mappedLocationPayloads = new HashMap<String, AbnormalTransactionsPayload>();

            for (AbnormalTransactionsPayload locationPayload : masterPayload) {
                mappedLocationPayloads.put(locationPayload.getLocationId(), locationPayload);

                for (Transaction transaction : locationPayload.getLocationTransactions()) {
                    for (Tender tender : transaction.getTenders()) {
                        if (tender.getType().equals("CARD") && transaction.getProduct().equals("REGISTER")) {
                            if (tender.getCardDetails() != null && tender.getCardDetails().getCard() != null) {
                                String fingerprint = tender.getCardDetails().getCard().getFingerprint();

                                ArrayList<Transaction> transactions;
                                if (fingerprintToTransactions.containsKey(fingerprint)) {
                                    transactions = fingerprintToTransactions.get(fingerprint);
                                } else {
                                    transactions = new ArrayList<Transaction>();
                                }
                                transactions.add(transaction);
                                fingerprintToTransactions.put(fingerprint, transactions);
                            }
                        }
                    }
                }
            }

            // add alert 4 transactions into location payloads
            for (String key : fingerprintToTransactions.keySet()) {
                if (fingerprintToTransactions.size() >= AbnormalTransactionsPayload.ALERT_THRESHOLD_4) {
                    for (Transaction transaction : fingerprintToTransactions.get(key)) {
                        if (mappedLocationPayloads.containsKey(transaction.getLocationId())) {
                            mappedLocationPayloads.get(transaction.getLocationId()).addAlert4Entry(transaction);
                        }
                    }
                }
            }

            // add file rows
            for (AbnormalTransactionsPayload locationPayload : masterPayload) {
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
        String reportName = fileDate + "-report-3.csv";
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
