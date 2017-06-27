package tntfireworks.reporting;

import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocationSalesReportGeneratorCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(LocationSalesReportGeneratorCallable.class);

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();
        logger.info("Start location sales report generation");

        // build report from payloads
        List<List<LocationSalesFile>> payloadAggregate = (List<List<LocationSalesFile>>) message
                .getPayload();
        StringBuilder reportBuilder = new StringBuilder();
        boolean addHeader = true;
        String fileDate = "";

        // add file rows
        for (List<LocationSalesFile> masterPayload : payloadAggregate) {
            for (LocationSalesFile locationPayload : masterPayload) {
                if (addHeader) {
                    reportBuilder.append(locationPayload.getFileHeader());
                    addHeader = false;
                    fileDate = locationPayload.getFileDate();
                }
                reportBuilder.append(locationPayload.getFileEntry());
            }
        }

        // generate report into file
        String reportName = fileDate + "-report-5.csv";
        message.setProperty("awsConnectorKey",
                String.format("TNTFireworks/REPORTS/%s", reportName), PropertyScope.INVOCATION);

        return reportBuilder.toString();
    }
}
