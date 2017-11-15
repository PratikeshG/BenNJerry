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

public class CreditDebitReportAggregatorCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(CreditDebitReportAggregatorCallable.class);

    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}")
    private String databaseUrl;
    @Value("${mysql.user}")
    private String databaseUser;
    @Value("${mysql.password}")
    private String databasePassword;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();
        logger.info("Start credit-debit report generation");

        // build report from payloads
        List<List<CreditDebitPayload>> payloadAggregate = (List<List<CreditDebitPayload>>) message
                .getPayload();
        StringBuilder reportBuilder = new StringBuilder();
        boolean addHeader = true;
        String fileDate = "";
        int loadNumber = 0;

        // add file rows
        for (List<CreditDebitPayload> masterPayload : payloadAggregate) {
            for (CreditDebitPayload locationPayload : masterPayload) {
                if (addHeader) {
                    reportBuilder.append(locationPayload.getPayloadHeader());
                    addHeader = false;
                    fileDate = locationPayload.getPayloadDate();
                	loadNumber = locationPayload.loadNumber;
                }
                reportBuilder.append(locationPayload.getRow());
            }
        }

        // increment load number
	    // write new load number into db
		DbConnection dbConnection = new DbConnection(databaseUrl, databaseUser, databasePassword);
	    dbConnection.executeQuery(generateLoadNumberSQLUpsert(CreditDebitPayload.DB_REPORT_NAME, (loadNumber + 1)));
	    dbConnection.close();

        // generate report into file
        String fileName = fileDate + "-report-8.csv";
        message.setProperty("awsConnectorKey",
                String.format("TNTFireworks/REPORTS/%s", fileName), PropertyScope.INVOCATION);
	    return reportBuilder.toString();
    }

    private String generateLoadNumberSQLUpsert(String reportName, int newLoadNumber) {
        String values = String.format("('%s', '%s')", reportName, newLoadNumber);
        String updateStmt = "INSERT INTO tntfireworks_reports_load_number (reportName, count) VALUES " + values;
        updateStmt += " ON DUPLICATE KEY UPDATE count=VALUES(count);";

        return updateStmt;
    }
}
