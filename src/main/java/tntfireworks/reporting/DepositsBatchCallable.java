package tntfireworks.reporting;

import java.util.List;

import javax.activation.DataHandler;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DepositsBatchCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(DepositsBatchCallable.class);

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();
        logger.info("Start settlement batch report generation");

        List<List<TntLocationDetails>> deploymentAggregate = (List<List<TntLocationDetails>>) message
                .getPayload();

        DepositsBatchFile batchFile = new DepositsBatchFile(deploymentAggregate);

        DataHandler dataHandler = new DataHandler(batchFile.generateBatchReport(), "text/plain; charset=UTF-8");
        eventContext.getMessage().addOutboundAttachment(batchFile.getFileDate() + "-deposits-batch-report.csv",
                dataHandler);

        return "Deposits Batch Report attached.";
    }
}
