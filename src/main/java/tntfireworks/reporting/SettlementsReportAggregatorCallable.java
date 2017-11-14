package tntfireworks.reporting;
import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SettlementsReportAggregatorCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(SettlementsReportAggregatorCallable.class);

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();
        logger.info("Start settlements report generation");

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
                reportBuilder.append(locationPayload.getPayloadEntries());
            }
        }

        // generate report into file
        String reportName = fileDate + "-report-1.csv";
        message.setProperty("awsConnectorKey",
                String.format("TNTFireworks/REPORTS/%s", reportName), PropertyScope.INVOCATION);

        return reportBuilder.toString();
    }
}
