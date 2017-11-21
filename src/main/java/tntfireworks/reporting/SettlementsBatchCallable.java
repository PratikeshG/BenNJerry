package tntfireworks.reporting;

import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import util.DbConnection;

public class SettlementsBatchCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(SettlementsBatchCallable.class);

    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}")
    private String databaseUrl;
    @Value("${mysql.user}")
    private String databaseUser;
    @Value("${mysql.password}")
    private String databasePassword;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();
        logger.info("Start settlements batch report generation");

        List<List<TntLocationDetails>> deploymentAggregate = (List<List<TntLocationDetails>>) message
                .getPayload();

        DbConnection dbConnection = new DbConnection(databaseUrl, databaseUser, databasePassword);
        SettlementsBatchFile batchFile = new SettlementsBatchFile(deploymentAggregate, dbConnection);

        // generate report into file
        String reportName = batchFile.getFileDate() + "-report-1.csv";
        String batchReport = batchFile.generateBatchReport();
        message.setProperty("awsConnectorKey",
                String.format("TNTFireworks/REPORTS/%s", reportName), PropertyScope.INVOCATION);

        return batchReport;
    }

}
