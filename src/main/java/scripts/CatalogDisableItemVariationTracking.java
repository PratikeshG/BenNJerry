package scripts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.squareup.connect.v2.CatalogItemVariation;
import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.ItemVariationLocationOverride;
import com.squareup.connect.v2.SquareClientV2;

import util.SquarePayload;

public class CatalogDisableItemVariationTracking {

    private static String ENCRYPTED_ACCESS_TOKEN = System.getenv("SCRIPT_ENCRYPTED_ACCESS_TOKEN");
    private static String ENCRYPTION_KEY = System.getenv("SCRIPT_ENCRYPTION_KEY");

    private static String API_URL = "https://connect.squareup.com";
    private static List<String> varsWhitelist = Arrays.asList(new String[] {});

    public static void main(String[] args) throws Exception {
        SquarePayload account = new SquarePayload();
        account.setEncryptedAccessToken(ENCRYPTED_ACCESS_TOKEN);

        SquareClientV2 client = new SquareClientV2(API_URL, account.getAccessToken(ENCRYPTION_KEY));

        CatalogObject[] itemVariations = client.catalog().listItemVariations();

        ArrayList<CatalogObject> variationsToModify = new ArrayList<CatalogObject>();
        for (CatalogObject catalogObject : itemVariations) {
            if (varsWhitelist.contains(catalogObject.getId())) {
                variationsToModify.add(catalogObject);

                CatalogItemVariation itemVariation = catalogObject.getItemVariationData();

                itemVariation.setTrackInventory(false);

                if (itemVariation.getLocationOverrides() != null) {
                    for (ItemVariationLocationOverride locationOverride : itemVariation.getLocationOverrides()) {
                        if (locationOverride.isTrackInventory()) {
                            locationOverride.setTrackInventory(false);
                        }
                    }
                }
            }
        }

        CatalogObject[] objectsToModify = variationsToModify.toArray(new CatalogObject[variationsToModify.size()]);
        client.catalog().batchUpsertObjects(objectsToModify);
    }
}
