package scripts;

import com.squareup.connect.Employee;
import com.squareup.connect.SquareClient;

import util.SquarePayload;

public class EmployeeExport {
    private static String MASTER_ACCOUNT_TOKEN = System.getenv("SCRIPT_MASTER_ACCOUNT_TOKEN");
    private static String MERCHANT_ID = System.getenv("SCRIPT_MERCHANT_ID");
    private static String ENCRYPTION_KEY = System.getenv("SCRIPT_ENCRYPTION_KEY");

    private static String API_URL = System.getenv("SCRIPT_API_URL");

    public static void main(String[] args) throws Exception {
        System.out.println("Running script to export employee records...");

        SquarePayload account = new SquarePayload();
        account.setEncryptedAccessToken(MASTER_ACCOUNT_TOKEN);

        SquareClient clientV1 = new SquareClient(account.getAccessToken(ENCRYPTION_KEY), API_URL, "v1", MERCHANT_ID);
        Employee[] employees = clientV1.employees().list();

        for (Employee employee : employees) {
            String entry = String.format("%s, %s, %s, %s, %s", employee.getId(), employee.getExternalId(),
                    employee.getFirstName(), employee.getLastName(), employee.getStatus(), employee.getEmail());
            System.out.println(entry);
        }

        System.out.println("Done.");
    }
}
