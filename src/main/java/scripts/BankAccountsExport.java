package scripts;

import java.util.ArrayList;

import com.squareup.connect.BankAccount;
import com.squareup.connect.SquareClient;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.SquareClientV2;

import util.SquarePayload;

public class BankAccountsExport {
    private static String[] ACCOUNTS = {};

    private static String ENCRYPTION_KEY = System.getenv("SCRIPT_ENCRYPTION_KEY");
    private static String API_URL = System.getenv("SCRIPT_API_URL");

    public static void main(String[] args) throws Exception {
        System.out.println("Running script to retrieve assigned bank accounts...");

        ArrayList<String> allLocations = new ArrayList<String>();
        int done = 0;

        for (String encryptedToken : ACCOUNTS) {
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
                    allLocations.add(entry);
                }

                done++;
            }
        }

        for (String entry : allLocations) {
            System.out.println(entry);
        }

        System.out.println("Done.");
    }
}
