package tntfireworks.reporting;

import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import util.DbConnection;

public class CreditDebitReportAggregatorCallable extends TntReportAggregator implements Callable {
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
        List<List<CreditDebitPayload>> payloadAggregate = (List<List<CreditDebitPayload>>) message.getPayload();
        StringBuilder reportBuilder = new StringBuilder();
        String fileDate = "";
        int loadNumber = 0;

        // retrieve file header and file date from first payload (one location)
        if (!payloadAggregate.isEmpty() && !payloadAggregate.get(0).isEmpty()) {
            reportBuilder.append(payloadAggregate.get(0).get(0).getPayloadHeader());
            fileDate = payloadAggregate.get(0).get(0).getPayloadDate();
            loadNumber = payloadAggregate.get(0).get(0).loadNumber;
        }

        // add file rows
        for (List<CreditDebitPayload> masterPayload : payloadAggregate) {
            for (CreditDebitPayload locationPayload : masterPayload) {
                reportBuilder.append(locationPayload.getRow());
            }
        }

        // increment load number
        // write new load number into db
        DbConnection dbConnection = new DbConnection(databaseUrl, databaseUser, databasePassword);
        dbConnection.executeQuery(generateLoadNumberSQLUpsert(CreditDebitPayload.DB_REPORT_NAME, (loadNumber + 1)));
        dbConnection.close();

        // generate report into file
        String reportName = fileDate + "-report-8.csv";
        String generatedReport = reportBuilder.toString();

        // archive to Google Cloud Storage
        archiveReportToGcp(reportName, generatedReport);

        return storeOrAttachReport(eventContext.getMessage(), reportName, generatedReport);
    }

    private String generateLoadNumberSQLUpsert(String reportName, int newLoadNumber) {
        String values = String.format("('%s', '%s')", reportName, newLoadNumber);
        String updateStmt = "INSERT INTO tntfireworks_reports_load_number (reportName, count) VALUES " + values;
        updateStmt += " ON DUPLICATE KEY UPDATE count=VALUES(count);";

        return updateStmt;
    }
}
