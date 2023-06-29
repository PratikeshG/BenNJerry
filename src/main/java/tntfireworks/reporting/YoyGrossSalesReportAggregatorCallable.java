package tntfireworks.reporting;

import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YoyGrossSalesReportAggregatorCallable extends TntReportAggregator implements Callable {
    private static Logger logger = LoggerFactory.getLogger(YoyGrossSalesReportAggregatorCallable.class);

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();
        String adhoc = message.getProperty("adhoc", PropertyScope.SESSION);

        logger.info("Start yoy gross sales report generation (Report 10)");

        // build report from payloads
        List<List<YoyGrossSalesPayload>> payloadAggregate = (List<List<YoyGrossSalesPayload>>) message.getPayload();
        StringBuilder reportBuilder = new StringBuilder();
        String fileDate = "";

        // retrieve file header and file date from first payload (one location)
        if (payloadExists(payloadAggregate)) {
            reportBuilder.append(payloadAggregate.get(0).get(0).getPayloadHeader());
            fileDate = payloadAggregate.get(0).get(0).getPayloadDate();
        }

        // add file rows
        for (List<YoyGrossSalesPayload> masterPayload : payloadAggregate) {
            for (YoyGrossSalesPayload yoyGrossPayload : masterPayload) {
                reportBuilder.append(yoyGrossPayload.getRow());
            }
        }

        // generate report into file
        String reportName = fileDate + "-report-10.csv";
        String generatedReport = reportBuilder.toString();

        // archive to Google Cloud Storage
        archiveReportToGcp(reportName, generatedReport);

        logger.info("Finalizing Report 10 for storing/attachment");

        // if ad-hoc run, store report on SFTP in adhoc directory
        if (adhoc.equals("TRUE")) {
            return storeReport(reportName, generatedReport, TntReportAggregator.ADHOC_DIRECTORY);
        }

        return storeOrAttachReport(eventContext.getMessage(), reportName, generatedReport);
    }

    private boolean payloadExists(List<List<YoyGrossSalesPayload>> payloadAggregate) {
        return !(payloadAggregate.isEmpty() || payloadAggregate.get(0).isEmpty());
    }
}
