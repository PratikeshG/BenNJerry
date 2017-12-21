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
        if (payloadExists(payloadAggregate)) {
            reportBuilder.append(payloadAggregate.get(0).get(0).getPayloadHeader());
            fileDate = payloadAggregate.get(0).get(0).getPayloadDate();
        }

        // - payloadAggregate => contains master/merchant account payloads
        // - masterPayload => contains all location payloads
        for (List<AbnormalTransactionsPayload> masterPayload : payloadAggregate) {
            addAlert4TransactionsToMasterPayload(masterPayload);

            // add rows to report/file
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

    private void addAlert4TransactionsToMasterPayload(List<AbnormalTransactionsPayload> masterPayload)
            throws Exception {
        // this map is used to find cards used multiple times across an entire merchant account
        // (k, v) == (fingerprint, List<Transaction>)
        Map<String, ArrayList<Transaction>> fingerprintToTransactions = new HashMap<String, ArrayList<Transaction>>();

        // this map of location payloads is used to add alert 4 transactions later
        // (k, v) == (location_id, AbnormalTransactionsPayload)
        Map<String, AbnormalTransactionsPayload> locationIdToLocationPayload = new HashMap<String, AbnormalTransactionsPayload>();

        // - map location payloads to location ids
        // - map card fingerprints to matching transactions
        for (AbnormalTransactionsPayload locationPayload : masterPayload) {
            locationIdToLocationPayload.put(locationPayload.getLocationId(), locationPayload);
            fingerprintToTransactions = addLocationFingerprints(locationPayload, fingerprintToTransactions);
        }

        // add transactions with fingerprints that qualify as alert 4 into location payloads as
        // alert 4 entries
        for (String fingerprint : fingerprintToTransactions.keySet()) {
            if (isAlert4Fingerprint(fingerprint, fingerprintToTransactions)) {
                addAlert4TransactionsToLocationPayload(fingerprint, fingerprintToTransactions,
                        locationIdToLocationPayload);
            }
        }
    }

    private void addAlert4TransactionsToLocationPayload(String fingerprint,
            Map<String, ArrayList<Transaction>> fingerprintToTransactions,
            Map<String, AbnormalTransactionsPayload> locationIdToLocationPayload) throws Exception {
        for (Transaction transaction : fingerprintToTransactions.get(fingerprint)) {
            if (locationIdToLocationPayload.containsKey(transaction.getLocationId())) {
                locationIdToLocationPayload.get(transaction.getLocationId()).addAlert4Entry(transaction);
            }
        }
    }

    private boolean isAlert4Fingerprint(String fingerprint,
            Map<String, ArrayList<Transaction>> fingerprintToTransactions) {
        return fingerprintToTransactions.get(fingerprint)
                .size() >= AbnormalTransactionsPayload.SAME_CARD_PER_MERCHANT_LIMIT;
    }

    private Map<String, ArrayList<Transaction>> addLocationFingerprints(AbnormalTransactionsPayload locationPayload,
            Map<String, ArrayList<Transaction>> fingerprintToTransactions) {

        for (Transaction transaction : locationPayload.getLocationTransactions()) {
            for (Tender tender : transaction.getTenders()) {
                if (tender.getType().equals(Tender.TENDER_TYPE_CARD)
                        && transaction.getProduct().equals(Transaction.TRANSACTION_PRODUCT_REGISTER)) {
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

        return fingerprintToTransactions;
    }

    private boolean payloadExists(List<List<AbnormalTransactionsPayload>> payloadAggregate) {
        return !(payloadAggregate.isEmpty() || payloadAggregate.get(0).isEmpty());
    }
}
