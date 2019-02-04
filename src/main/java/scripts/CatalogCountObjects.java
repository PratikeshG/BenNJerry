package scripts;

import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.SquareClientV2;

import util.SquarePayload;

public class CatalogCountObjects {
    private static String ENCRYPTED_ACCESS_TOKEN = System.getenv("SCRIPT_ENCRYPTED_ACCESS_TOKEN");
    private static String ENCRYPTION_KEY = System.getenv("SCRIPT_ENCRYPTION_KEY");

    private static String API_URL = System.getenv("SCRIPT_API_URL");

    public static void main(String[] args) throws Exception {
        SquarePayload account = new SquarePayload();
        account.setEncryptedAccessToken(ENCRYPTED_ACCESS_TOKEN);

        SquareClientV2 client = new SquareClientV2(API_URL, account.getAccessToken(ENCRYPTION_KEY));

        CatalogObject[] items = client.catalog().listItems();

        System.out.println(items.length);
    }
}
