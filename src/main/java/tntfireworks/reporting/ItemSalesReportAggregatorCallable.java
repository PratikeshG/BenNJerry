package tntfireworks.reporting;

import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemSalesReportAggregatorCallable implements Callable {
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
                for (String fileRow : locationPayload.getPayloadEntries()) {
                    reportBuilder.append(fileRow);
                }
            }
        }

        // generate report into file
        String reportName = fileDate + "-report-7.csv";
        message.setProperty("awsConnectorKey",
                String.format("TNTFireworks/REPORTS/%s", reportName), PropertyScope.INVOCATION);

        return reportBuilder.toString();
    }
}
