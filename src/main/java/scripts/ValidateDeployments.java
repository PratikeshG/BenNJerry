package scripts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.v2.SquareClientV2;

import util.SquarePayload;

public class ValidateDeployments {

    private final static String[] ACCOUNT_ENCRYPTED_ACCESS_TOKENS = { "" }; // MODIFY

    private final static String ENCRYPTION_KEY = System.getenv("SCRIPT_ENCRYPTION_KEY");

    private final static String API_URL = "https://connect.squareup.com";

    private static Logger logger = LoggerFactory.getLogger(ValidateDeployments.class);

    public static void main(String[] args) throws Exception {
        logger.info("Running script to validate deployments...");

        for (String encryptedToken : ACCOUNT_ENCRYPTED_ACCESS_TOKENS) {
            SquarePayload account = new SquarePayload();
            account.setEncryptedAccessToken(encryptedToken);

            SquareClientV2 client = new SquareClientV2(API_URL, account.getAccessToken(ENCRYPTION_KEY));

            try {
                String id = client.merchants().retrieve().getId();
                System.out.println("GOOD: " + id);
            } catch (Exception e) {
                System.out.println("BAD: " + encryptedToken);
            }
        }

        logger.info("Done.");
    }
}
