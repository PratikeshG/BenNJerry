package tntfireworks.reporting;

import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemSalesReportAggregatorCallable extends TntReportAggregator implements Callable {
    private static Logger logger = LoggerFactory.getLogger(ItemSalesReportAggregatorCallable.class);

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();
        String adhoc = message.getProperty("adhoc", PropertyScope.SESSION);

        logger.info("Start item sales report generation");

        // build report from payloads
        List<List<ItemSalesPayload>> payloadAggregate = (List<List<ItemSalesPayload>>) message.getPayload();
        StringBuilder reportBuilder = new StringBuilder();
        String fileDate = "";

        // retrieve file header and file date from first payload (one location)
        if (payloadExists(payloadAggregate)) {
            reportBuilder.append(payloadAggregate.get(0).get(0).getPayloadHeader());
            fileDate = payloadAggregate.get(0).get(0).getPayloadDate();
        }

        // add file rows
        for (List<ItemSalesPayload> masterPayload : payloadAggregate) {
            for (ItemSalesPayload locationPayload : masterPayload) {
                for (String fileRow : locationPayload.getRows()) {
                    reportBuilder.append(fileRow);
                }
            }
        }

        // generate report into file
        String reportName = fileDate + "-report-7.csv";
        String generatedReport = reportBuilder.toString();

        // archive to Google Cloud Storage
        archiveReportToGcp(reportName, generatedReport);

        // if ad-hoc run, store report on SFTP in adhoc directory
        if (adhoc.equals("TRUE")) {
        	return storeReport(reportName, generatedReport,TntReportAggregator.ADHOC_DIRECTORY);
        }

        // report 7 is SFTP only
        return storeReport(reportName, generatedReport);
    }

    private boolean payloadExists(List<List<ItemSalesPayload>> payloadAggregate) {
        return !(payloadAggregate.isEmpty() || payloadAggregate.get(0).isEmpty());
    }
}
