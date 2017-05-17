package tntfireworks.reporting;

import java.util.List;

import javax.activation.DataHandler;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.DbConnection;

public class CreditDebitBatchCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(CreditDebitBatchCallable.class);
    private String databaseUrl;
    private String databaseUser;
    private String databasePassword;

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public void setDatabaseUser(String databaseUser) {
        this.databaseUser = databaseUser;
    }

    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();
        logger.info("Start credit debit batch report generation");

        List<List<TntLocationDetails>> deploymentAggregate = (List<List<TntLocationDetails>>) message
                .getPayload();

        DbConnection dbConnection = new DbConnection(databaseUrl, databaseUser, databasePassword);
        CreditDebitBatchFile batchFile = new CreditDebitBatchFile(deploymentAggregate, dbConnection);

        DataHandler dataHandler = new DataHandler(batchFile.generateBatchReport(), "text/plain; charset=UTF-8");
        eventContext.getMessage().addOutboundAttachment(batchFile.getFileDate() + "-credit-debit-batch-report.csv",
                dataHandler);

        return "Credit Debit Batch Report attached.";
    }
}
