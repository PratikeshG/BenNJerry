package tntfireworks.reporting;

import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import tntfireworks.TntDatabaseApi;
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
        String adhoc = message.getProperty("adhoc", PropertyScope.SESSION);

        logger.info("Start credit-debit report generation");

        // build report from payloads
        List<List<CreditDebitPayload>> payloadAggregate = (List<List<CreditDebitPayload>>) message.getPayload();
        StringBuilder reportBuilder = new StringBuilder();
        String fileDate = "";
        int loadNumber = 0;

        // retrieve file header and file date from first payload (one location)
        if (payloadExists(payloadAggregate)) {
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
        dbConnection.executeQuery(
                generateLoadNumberSQLUpsert(TntDatabaseApi.DB_LOAD_NUMBER_REPORT8_NAME, (loadNumber + 1)));
        dbConnection.close();

        // generate report into file
        String reportName = fileDate + "-report-8.csv";
        String generatedReport = reportBuilder.toString();

        // archive to Google Cloud Storage
        archiveReportToGcp(reportName, generatedReport);

        // if ad-hoc run, store report on SFTP in adhoc directory
        if (adhoc.equals("TRUE")) {
        	return storeReport(reportName, generatedReport,TntReportAggregator.ADHOC_DIRECTORY);
        }

        // report 8 is SFTP only
        return storeReport(reportName, generatedReport);
    }

    private String generateLoadNumberSQLUpsert(String reportName, int newLoadNumber) {
        String values = String.format("('%s', '%s')", reportName, newLoadNumber);
        String updateStmt = "INSERT INTO tntfireworks_reports_load_number (reportName, count) VALUES " + values;
        updateStmt += " ON DUPLICATE KEY UPDATE count=VALUES(count);";

        return updateStmt;
    }

    private boolean payloadExists(List<List<CreditDebitPayload>> payloadAggregate) {
        return !(payloadAggregate.isEmpty() || payloadAggregate.get(0).isEmpty());
    }
}
