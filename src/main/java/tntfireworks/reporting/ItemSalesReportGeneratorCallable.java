package tntfireworks.reporting;

import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemSalesReportGeneratorCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(ItemSalesReportGeneratorCallable.class);

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();
        logger.info("Start item sales report generation");

        // build report from payloads
        List<List<ItemSalesFile>> payloadAggregate = (List<List<ItemSalesFile>>) message
                .getPayload();
        StringBuilder reportBuilder = new StringBuilder();
        boolean addHeader = true;
        String fileDate = "";

        // add file rows
        for (List<ItemSalesFile> masterPayload : payloadAggregate) {
            for (ItemSalesFile locationPayload : masterPayload) {
                if (addHeader) {
                    reportBuilder.append(locationPayload.getFileHeader());
                    addHeader = false;
                    fileDate = locationPayload.getFileDate();
                }
                for (String fileRow : locationPayload.getFileEntries()) {
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
