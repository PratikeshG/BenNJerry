package tntfireworks.reporting;
import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SettlementsReportAggregatorCallable extends TntReportAggregator implements Callable  {
    private static Logger logger = LoggerFactory.getLogger(SettlementsReportAggregatorCallable.class);

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();
        int reportType = Integer.parseInt(message.getProperty("reportType", PropertyScope.SESSION));

        logger.info("Aggregate settlements report payloads...");
        // build report from payloads
        List<List<SettlementsPayload>> payloadAggregate = (List<List<SettlementsPayload>>)
        		message.getPayload();
        StringBuilder reportBuilder = new StringBuilder();
        boolean addHeader = true;
        String fileDate = "";

        // add file rows
        for (List<SettlementsPayload> masterPayload : payloadAggregate) {
            for (SettlementsPayload locationPayload : masterPayload) {
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
        logger.info("Finished aggregating settlements report payloads");

        String reportName = fileDate + "-report-1.csv";
        String generatedReport = reportBuilder.toString();

        // archive to Google Cloud Storage
        archiveReportToGcp(reportName, generatedReport);

        return attachReport(eventContext.getMessage(), reportName, generatedReport);
    }
}
