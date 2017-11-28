package tntfireworks.reporting;

import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemSalesReportAggregatorCallable extends TntReportAggregator implements Callable {
    private static Logger logger = LoggerFactory.getLogger(ItemSalesReportAggregatorCallable.class);

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();
        logger.info("Start item sales report generation");

        // build report from payloads
        List<List<ItemSalesPayload>> payloadAggregate = (List<List<ItemSalesPayload>>) message
                .getPayload();
        StringBuilder reportBuilder = new StringBuilder();
        boolean addHeader = true;
        String fileDate = "";

        // add file rows
        for (List<ItemSalesPayload> masterPayload : payloadAggregate) {
            for (ItemSalesPayload locationPayload : masterPayload) {
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
        String reportName = fileDate + "-report-7.csv";
        String generatedReport = reportBuilder.toString();

        // archive to Google Cloud Storage
        archiveReportToGcp(reportName, generatedReport);

        return attachReport(eventContext.getMessage(), reportName, generatedReport);
    }
}
