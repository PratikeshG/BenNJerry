package tntfireworks.reporting;

import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.DbConnection;

public class AbnormalTransactionsCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(AbnormalTransactionsCallable.class);
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
        logger.info("Start abnormal transactions report generation");

        List<List<TntLocationDetails>> deploymentAggregate = (List<List<TntLocationDetails>>) message
                .getPayload();

        // get offset for dayTimeInterval calculation
        int offset = Integer.parseInt(message.getProperty("offset", PropertyScope.SESSION));

        // process report data
        DbConnection dbConnection = new DbConnection(databaseUrl, databaseUser, databasePassword);
        AbnormalTransactionsFile alertFile = new AbnormalTransactionsFile(deploymentAggregate, dbConnection, offset);

        // generate report into file
        String reportName = alertFile.getFileDate() + "-report-3.csv";
        String batchReport = alertFile.generateBatchReport();
        message.setProperty("awsConnectorKey",
                String.format("TNTFireworks/REPORTS/%s", reportName), PropertyScope.INVOCATION);

        return batchReport;
    }
}
