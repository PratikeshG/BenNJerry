package tntfireworks.reporting;

import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocationSalesReportAggregatorCallable extends TntReportAggregator implements Callable {
    private static Logger logger = LoggerFactory.getLogger(LocationSalesReportAggregatorCallable.class);

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();
        logger.info("Start location sales report generation");

        // build report from payloads
        List<List<LocationSalesPayload>> payloadAggregate = (List<List<LocationSalesPayload>>) message.getPayload();
        StringBuilder reportBuilder = new StringBuilder();
        String fileDate = "";

        // retrieve file header and file date from first payload (one location)
        if (payloadExists(payloadAggregate)) {
            reportBuilder.append(payloadAggregate.get(0).get(0).getPayloadHeader());
            fileDate = payloadAggregate.get(0).get(0).getPayloadDate();
        }

        // add file rows
        for (List<LocationSalesPayload> masterPayload : payloadAggregate) {
            for (LocationSalesPayload locationPayload : masterPayload) {
                reportBuilder.append(locationPayload.getRow());
            }
        }

        // generate report into file
        String reportName = fileDate + "-report-5.csv";
        String generatedReport = reportBuilder.toString();

        // archive to Google Cloud Storage
        archiveReportToGcp(reportName, generatedReport);

        return storeOrAttachReport(eventContext.getMessage(), reportName, generatedReport);
    }

    private boolean payloadExists(List<List<LocationSalesPayload>> payloadAggregate) {
        return !(payloadAggregate.isEmpty() || payloadAggregate.get(0).isEmpty());
    }
}
