package scripts;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.SquareClientV2;

import util.SquarePayload;

public class LocationNameExport {
    private final static String[] ACCOUNT_ENCRYPTED_ACCESS_TOKENS = { "" }; // MODIFY

    private final static String OUTPUT_PATH = System.getenv("SCRIPT_OUTPUT_PATH");
    private final static String ENCRYPTION_KEY = System.getenv("SCRIPT_ENCRYPTION_KEY");
    private final static String API_URL = System.getenv("SCRIPT_API_URL");

    private final static String[] HEADERS = { "merchant_id", "location_id", "location_name" };

    private static Logger logger = LoggerFactory.getLogger(LocationNameExport.class);

    public static void main(String[] args) throws Exception {
        logger.info("Running script to retrieve location names...");

        Writer out = new OutputStreamWriter(new FileOutputStream(OUTPUT_PATH), StandardCharsets.ISO_8859_1);

        try (CSVPrinter printer = new CSVPrinter(out,
                CSVFormat.DEFAULT.withHeader(HEADERS).withQuoteMode(QuoteMode.MINIMAL))) {

            for (String encryptedToken : ACCOUNT_ENCRYPTED_ACCESS_TOKENS) {
                SquarePayload account = new SquarePayload();
                account.setEncryptedAccessToken(encryptedToken);

                SquareClientV2 client = new SquareClientV2(API_URL, account.getAccessToken(ENCRYPTION_KEY));

                Location[] locations = client.locations().list();
                for (Location location : locations) {
                    printer.printRecord(location.getMerchant_id(), location.getId(), location.getName());
                }
            }

            printer.close();
        }

        logger.info("Done.");
    }
}
