package scripts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.SquareClientV2;

import util.SquarePayload;

public class CatalogCountObjects {
    private final static String ENCRYPTED_ACCESS_TOKEN = System.getenv("SCRIPT_ENCRYPTED_ACCESS_TOKEN");
    private final static String ENCRYPTION_KEY = System.getenv("SCRIPT_ENCRYPTION_KEY");
    private final static String API_URL = System.getenv("SCRIPT_API_URL");

    private static Logger logger = LoggerFactory.getLogger(CatalogCountObjects.class);

    public static void main(String[] args) throws Exception {
        SquarePayload account = new SquarePayload();
        account.setEncryptedAccessToken(ENCRYPTED_ACCESS_TOKEN);

        SquareClientV2 client = new SquareClientV2(API_URL, account.getAccessToken(ENCRYPTION_KEY));

        CatalogObject[] items = client.catalog().listItems();

        logger.info("" + items.length);
    }
}
