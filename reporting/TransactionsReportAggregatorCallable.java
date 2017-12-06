package tntfireworks.reporting;

import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionsReportAggregatorCallable extends TntReportAggregator implements Callable {
    private static Logger logger = LoggerFactory.getLogger(TransactionsReportAggregatorCallable.class);

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();
        logger.info("Start transactions report generation");

        // build report from payloads
        List<List<TransactionsPayload>> payloadAggregate = (List<List<TransactionsPayload>>) message.getPayload();
        StringBuilder reportBuilder = new StringBuilder();
        String fileDate = "";

        // retrieve file header and file date from first payload (one location)
        if (!payloadAggregate.isEmpty() && !payloadAggregate.get(0).isEmpty()) {
            reportBuilder.append(payloadAggregate.get(0).get(0).getPayloadHeader());
            fileDate = payloadAggregate.get(0).get(0).getPayloadDate();
        }

        // add file rows
        for (List<TransactionsPayload> masterPayload : payloadAggregate) {
            for (TransactionsPayload locationPayload : masterPayload) {
                for (String fileRow : locationPayload.getRows()) {
                    reportBuilder.append(fileRow);
                }
            }
        }

        // generate report into file
        String reportName = fileDate + "-report-2.csv";
        String generatedReport = reportBuilder.toString();

        // archive to Google Cloud Storage
        archiveReportToGcp(reportName, generatedReport);

        return storeOrAttachReport(eventContext.getMessage(), reportName, generatedReport);
    }
}
