package tntfireworks.reporting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.v2.Tender;
import com.squareup.connect.v2.Transaction;

public class AbnormalTransactionsReportAggregatorCallable extends TntReportAggregator implements Callable {
    private static Logger logger = LoggerFactory.getLogger(AbnormalTransactionsReportAggregatorCallable.class);

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();
        logger.info("Start abnormal transactions report generation");

        // build report from payloads
        List<List<AbnormalTransactionsPayload>> payloadAggregate = (List<List<AbnormalTransactionsPayload>>) message
                .getPayload();
        StringBuilder reportBuilder = new StringBuilder();
        String fileDate = "";

        // retrieve file header and file date from first payload (one location)
        if (!payloadAggregate.isEmpty() && !payloadAggregate.get(0).isEmpty()) {
            reportBuilder.append(payloadAggregate.get(0).get(0).getPayloadHeader());
            fileDate = payloadAggregate.get(0).get(0).getPayloadDate();
        }

        // this block is used to find alert 4 transactions
        for (List<AbnormalTransactionsPayload> masterPayload : payloadAggregate) {
            // this map is used to find cards used multiple times across an
            // entire merchant account
            // (k, v) == (fingerprint, List<Transaction>)
            Map<String, ArrayList<Transaction>> fingerprintToTransactions = new HashMap<String, ArrayList<Transaction>>();

            // this map of location payloads is used to add alert 4 transactions
            // later
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
                if (fingerprintToTransactions.get(key).size() >= AbnormalTransactionsPayload.ALERT_THRESHOLD_4) {
                    for (Transaction transaction : fingerprintToTransactions.get(key)) {
                        if (mappedLocationPayloads.containsKey(transaction.getLocationId())) {
                            mappedLocationPayloads.get(transaction.getLocationId()).addAlert4Entry(transaction);
                        }
                    }
                }
            }

            // add file rows
            for (AbnormalTransactionsPayload locationPayload : masterPayload) {
                for (String fileRow : locationPayload.getRows()) {
                    reportBuilder.append(fileRow);
                }
            }
        }

        // generate report into file
        String reportName = fileDate + "-report-3.csv";
        String generatedReport = reportBuilder.toString();

        // archive to Google Cloud Storage
        archiveReportToGcp(reportName, generatedReport);

        return storeOrAttachReport(eventContext.getMessage(), reportName, generatedReport);
    }
}
