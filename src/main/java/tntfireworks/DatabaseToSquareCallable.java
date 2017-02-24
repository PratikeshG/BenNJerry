package tntfireworks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.v2.Catalog;
import com.squareup.connect.v2.CatalogItemVariation;
import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.Money;
import com.squareup.connect.v2.SquareClientV2;

import util.SquarePayload;

public class DatabaseToSquareCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(DatabaseToSquareCallable.class);

    private String apiUrl;

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    // TODO(wtsang): generate dynamically by reading all possible values from DB
    private static final String[] CATEGORIES = new String[] { "ASSORTMENTS", "BASE FOUNTAINS", "CALIFORNIA FOUNTAINS",
            "CONE FOUNTAINS", "GROUND SPINNERS & CHASERS", "NOVELTIES", "SMOKE", "SPARKLERS", "PUNK", "FIRECRACKERS",
            "HELICOPTERS", "RELOADABLES", "MULTI-AERIALS", "SPECIAL AERIALS", "MISSILES", "PARACHUTES", "ROMAN CANDLES",
            "ROCKETS", "COUNTER CASES & DISPLAYS", "PROMOTIONAL", "SUB ASSESMBLIES", "MATERIALS", "TOYS" };

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        HashMap<String, String> locationMarketingPlanCache = message.getProperty("locationMarketingPlanCache",
                PropertyScope.SESSION);
        HashMap<String, List<CsvItem>> marketingPlanItemsCache = message.getProperty("marketingPlanItemsCache",
                PropertyScope.SESSION);

        SquarePayload deployment = (SquarePayload) message.getPayload();

        SquareClientV2 client = new SquareClientV2(apiUrl, deployment.getAccessToken());
        HashMap<String, List<String>> marketingPlanLocationsCache = new HashMap<String, List<String>>();

        logger.info(String.format("Processing Catalog API updates for merchant token: %s", deployment.getMerchantId()));

        Location[] locations = client.locations().list();
        for (Location location : locations) {
            String locationSquareId = location.getId();
            String locationTNTId = getValueInParenthesis(location.getName());
            String marketingPlanId = locationMarketingPlanCache.get(locationTNTId);

            String debugDeploymentPlan = String.format(
                    "merchantToken (%s), locationSquareId (%s), locationTNTId (%s), marketingPlanId (%s)",
                    deployment.getMerchantId(), locationSquareId, locationTNTId, marketingPlanId);

            logger.info(debugDeploymentPlan);

            if (locationTNTId.length() < 1) {
                logger.warn(String.format("INVALID LOCATION ID: %s", debugDeploymentPlan));
                continue;
            }

            if (marketingPlanId == null) {
                logger.warn(String.format("NO MARKETING PLAN ID: %s", debugDeploymentPlan));
            }

            List<String> locationsList = marketingPlanLocationsCache.get(marketingPlanId);
            if (locationsList == null) {
                locationsList = new ArrayList<String>();
                marketingPlanLocationsCache.put(marketingPlanId, locationsList);
            }
            locationsList.add(locationSquareId);
        }

        logger.info("Retrieving catalog...");
        Catalog catalog = client.catalog().retrieveCatalog(Catalog.PrimaryKey.SKU, Catalog.PrimaryKey.NAME,
                Catalog.PrimaryKey.ID, Catalog.PrimaryKey.NAME, Catalog.PrimaryKey.NAME);

        // 1. pull existing categories
        // 2. check if all defined categories exist
        // 3. if not, upsert
        HashMap<String, CatalogObject> existingCategories = (HashMap<String, CatalogObject>) catalog.getCategories();

        // check if there are new categories to add
        for (String categoryName : CATEGORIES) {
            // add category if it does not exist in Square
            CatalogObject category = existingCategories.get(categoryName);
            if (category == null) {
                CatalogObject newCategory = new CatalogObject("CATEGORY");
                newCategory.getCategoryData().setName(categoryName);
                catalog.addCategory(newCategory);
            }
        }

        logger.info("Updating categories in catalog...");
        CatalogObject[] allCategories = catalog.getCategories().values().toArray(new CatalogObject[0]);
        client.catalog().batchUpsertObjects(allCategories);
        logger.info("Done checking/adding categories");

        // retrieve latest catalog with all categories now in account
        logger.info("Retrieving updated catalog...");
        catalog = client.catalog().retrieveCatalog(Catalog.PrimaryKey.SKU, Catalog.PrimaryKey.NAME,
                Catalog.PrimaryKey.ID, Catalog.PrimaryKey.NAME, Catalog.PrimaryKey.NAME);

        // reset all item <> location relationships - we will re-add these based on db item info
        catalog.clearItemLocations();

        // get the up-to-date categories
        existingCategories = (HashMap<String, CatalogObject>) catalog.getCategories();

        for (String marketingPlanId : marketingPlanLocationsCache.keySet()) {
            String[] squareLocationIds = marketingPlanLocationsCache.get(marketingPlanId).toArray(new String[0]);

            List<CsvItem> marketingPlanItems = marketingPlanItemsCache.get(marketingPlanId);
            if (marketingPlanItems != null) {
                for (CsvItem updateItem : marketingPlanItems) {
                    String sku = updateItem.getUPC();
                    if (sku == null || sku.length() < 2) {
                        sku = updateItem.getNumber();
                    }

                    CatalogObject matchingItem = catalog.getItem(sku);
                    if (matchingItem == null) {
                        matchingItem = new CatalogObject("ITEM");
                    }

                    matchingItem.getItemData().setName(updateItem.getDescription());
                    CatalogItemVariation macthingItemVariation = matchingItem.getItemData().getVariations()[0]
                            .getItemVariationData();
                    macthingItemVariation.setSku(sku);
                    macthingItemVariation.setName(updateItem.getNumber());

                    int price = Integer.parseInt(parsePrice(updateItem.getSuggestedPrice()));
                    macthingItemVariation.setPriceMoney(new Money(price));

                    matchingItem.enableAtLocations(squareLocationIds);
                    matchingItem.setLocationPriceOverride(squareLocationIds, new Money(price), "FIXED_PRICING");

                    // check for matching category, if found, add category id
                    if (existingCategories.containsKey(updateItem.getCategory())) {
                        String categoryId = existingCategories.get(updateItem.getCategory()).getId();
                        matchingItem.getItemData().setCategoryId(categoryId);
                    }

                    catalog.addItem(matchingItem);
                }
            }
        }

        logger.info("Upsert latest catalog of items...");
        client.catalog().batchUpsertObjects(catalog.getObjects());

        // now list all items in a catalog without locations and delete
        for (String key : catalog.getItems().keySet()) {
            CatalogObject item = catalog.getItem(key);

            if (item.getPresentAtLocationIds() == null || item.getPresentAtLocationIds().length == 0) {
                logger.info(String.format("Delete this catalog object name/token %s/%s:", item.getItemData().getName(),
                        item.getId()));
                client.catalog().deleteObject(item.getId());
            }
        }

        logger.info(String.format("Done processing Catalog API updates for merchant token: %s",
                deployment.getMerchantId()));
        return null;
    }

    private static String parsePrice(String input) {
        input = input.replaceAll("[^\\d]", "");
        if (input.length() < 1) {
            input = "0";
        }
        return input;
    }

    private static String getValueInParenthesis(String input) {
        String value = "";
        if (input != null) {
            int firstIndex = input.indexOf('(');
            int lastIndex = input.indexOf(')');
            if (firstIndex > -1 && lastIndex > -1) {
                value = input.substring(firstIndex + 1, lastIndex);
                value = value.replaceAll("#", "");
            }
        }
        return value;
    }

}