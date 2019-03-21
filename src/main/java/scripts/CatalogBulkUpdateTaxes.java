package scripts;

import java.util.ArrayList;

import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.SquareClientV2;

import util.SquarePayload;

public class CatalogBulkUpdateTaxes {
    private final static String ENCRYPTED_ACCESS_TOKEN = System.getenv("SCRIPT_ENCRYPTED_ACCESS_TOKEN");
    private final static String ENCRYPTION_KEY = System.getenv("SCRIPT_ENCRYPTION_KEY");

    private final static String[] TAXES_TO_ENABLE = { "" }; // MODIFY
    private final static String[] TAXES_TO_DISABLE = { "" }; // MODIFY

    private final static String API_URL = System.getenv("SCRIPT_API_URL");

    public static void main(String[] args) throws Exception {
        SquarePayload account = new SquarePayload();
        account.setEncryptedAccessToken(ENCRYPTED_ACCESS_TOKEN);

        SquareClientV2 client = new SquareClientV2(API_URL, account.getAccessToken(ENCRYPTION_KEY));

        CatalogObject[] items = client.catalog().listItems();

        ArrayList<String> itemIds = new ArrayList<String>();
        for (CatalogObject item : items) {
            itemIds.add(item.getId());
        }

        client.catalog().updateItemTaxes(itemIds.toArray(new String[itemIds.size()]), TAXES_TO_ENABLE,
                TAXES_TO_DISABLE);

        System.out.println("DONE");
    }
}
