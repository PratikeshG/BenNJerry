package scripts;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.BankAccount;
import com.squareup.connect.SquareClient;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.SquareClientV2;

import util.SquarePayload;

public class BankAccountsExport {
    private final static String[] ACCOUNT_ENCRYPTED_ACCESS_TOKENS = {}; // modify

    private final static String ENCRYPTION_KEY = System.getenv("SCRIPT_ENCRYPTION_KEY");
    private final static String API_URL = System.getenv("SCRIPT_API_URL");

    private static Logger logger = LoggerFactory.getLogger(BankAccountsExport.class);

    public static void main(String[] args) throws Exception {
        logger.info("Running script to retrieve assigned bank accounts...");

        ArrayList<String> allBankAccounts = new ArrayList<String>();

        for (String encryptedToken : ACCOUNT_ENCRYPTED_ACCESS_TOKENS) {
            SquarePayload account = new SquarePayload();
            account.setEncryptedAccessToken(encryptedToken);

            SquareClientV2 client = new SquareClientV2(API_URL, account.getAccessToken(ENCRYPTION_KEY));
            SquareClient clientV1 = new SquareClient(account.getAccessToken(ENCRYPTION_KEY), API_URL);

            Location[] locations = client.locations().list();
            for (Location location : locations) {
                clientV1.setMerchantId(location.getMerchant_id());
                clientV1.setLocation(location.getId());

                BankAccount[] banks = clientV1.bankAccounts().list();

                for (BankAccount bankAccount : banks) {
                    String entry = String.format("%s, %s, %s, %s", location.getMerchant_id(), location.getId(),
                            location.getName(), bankAccount.getName());
                    allBankAccounts.add(entry);
                }
            }
        }

        for (String entry : allBankAccounts) {
            logger.info(entry);
        }

        logger.info("Done.");
    }
}
