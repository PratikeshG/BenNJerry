package scripts;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.Employee;
import com.squareup.connect.SquareClient;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.SquareClientV2;

import util.SquarePayload;

public class EmployeeExport {
    private final static String MASTER_ACCOUNT_TOKEN = System.getenv("SCRIPT_MASTER_ACCOUNT_TOKEN");
    private final static String MERCHANT_ID = System.getenv("SCRIPT_MERCHANT_ID");
    private final static String ENCRYPTION_KEY = System.getenv("SCRIPT_ENCRYPTION_KEY");
    private final static String OUTPUT_PATH = System.getenv("SCRIPT_OUTPUT_PATH");

    private final static String API_URL = System.getenv("SCRIPT_API_URL");

    private final static String[] HEADERS = { "id", "external_id", "first_name", "last_name", "status", "email",
            "authorized_locations" };

    private static Logger logger = LoggerFactory.getLogger(EncryptDatabaseAccessTokens.class);

    public static void main(String[] args) throws Exception {
        logger.info("Running script to export employee records...");

        SquarePayload account = new SquarePayload();
        account.setEncryptedAccessToken(MASTER_ACCOUNT_TOKEN);

        HashMap<String, String> locationNamesCache = new HashMap<String, String>();
        SquareClientV2 clientV2 = new SquareClientV2(API_URL, account.getAccessToken(ENCRYPTION_KEY));
        Location[] locations = clientV2.locations().list();
        for (Location location : locations) {
            locationNamesCache.put(location.getId(), location.getName());
        }

        SquareClient clientV1 = new SquareClient(account.getAccessToken(ENCRYPTION_KEY), API_URL, "v1", MERCHANT_ID);
        Employee[] employees = clientV1.employees().list();

        Writer out = new OutputStreamWriter(new FileOutputStream(OUTPUT_PATH), StandardCharsets.ISO_8859_1);

//        try (CSVPrinter printer = new CSVPrinter(out,CSVFormat.DEFAULT.withHeader(HEADERS).withQuoteMode(QuoteMode.MINIMAL))) {
//
//            for (Employee employee : employees) {
//                ArrayList<String> authorizedLocations = new ArrayList<String>();
//
//                for (String locationId : employee.getAuthorizedLocationIds()) {
//                    authorizedLocations.add(locationNamesCache.get(locationId));
//                }
//
//                printer.printRecord(employee.getId(), employee.getExternalId(), employee.getFirstName(),
//                        employee.getLastName(), employee.getStatus(), employee.getEmail(),
//                        String.join(", ", authorizedLocations));
//            }
//
//            printer.close();
//        }

        logger.info("Done.");
    }
}
