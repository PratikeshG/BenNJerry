package scripts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.Employee;
import com.squareup.connect.SquareClient;

import util.SquarePayload;

public class EmployeeExport {
    private final static String MASTER_ACCOUNT_TOKEN = System.getenv("SCRIPT_MASTER_ACCOUNT_TOKEN");
    private final static String MERCHANT_ID = System.getenv("SCRIPT_MERCHANT_ID");
    private final static String ENCRYPTION_KEY = System.getenv("SCRIPT_ENCRYPTION_KEY");

    private final static String API_URL = System.getenv("SCRIPT_API_URL");

    private static Logger logger = LoggerFactory.getLogger(EncryptDatabaseAccessTokens.class);

    public static void main(String[] args) throws Exception {
        logger.info("Running script to export employee records...");

        SquarePayload account = new SquarePayload();
        account.setEncryptedAccessToken(MASTER_ACCOUNT_TOKEN);

        SquareClient clientV1 = new SquareClient(account.getAccessToken(ENCRYPTION_KEY), API_URL, "v1", MERCHANT_ID);
        Employee[] employees = clientV1.employees().list();

        for (Employee employee : employees) {
            String entry = String.format("%s, %s, %s, %s, %s", employee.getId(), employee.getExternalId(),
                    employee.getFirstName(), employee.getLastName(), employee.getStatus(), employee.getEmail());
            logger.info(entry);
        }

        logger.info("Done.");
    }
}
