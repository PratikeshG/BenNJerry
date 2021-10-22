package tntfireworks;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.squareup.connect.v2.Catalog;
import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.InventoryAdjustment;
import com.squareup.connect.v2.InventoryChange;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.SquareClientV2;

public class TntInventoryApi {
    private static Logger logger = LoggerFactory.getLogger(TntInventoryApi.class);
    private static final int BATCH_UPSERT_SIZE = 5;
    private static final String INACTIVE_LOCATION = "DEACTIVATED";
    private static final String DEFAULT_LOCATION = "DEFAULT";
    static final String INVENTORY_API_VERSION = "2018-09-18";

    private Catalog catalog;
    private SquareClientV2 clientV2;

    public TntInventoryApi(SquareClientV2 clientV2) {
        this.clientV2 = clientV2;
        catalog = retrieveCatalogFromSquare();
    }

    public Catalog retrieveCatalogFromSquare() {
        Preconditions.checkNotNull(clientV2);

        logger.info(logString("Retrieving catalog..."));
        try {
            Catalog sourceCatalog = clientV2.catalog().retrieveCatalog(Catalog.PrimaryKey.SKU, Catalog.PrimaryKey.NAME,
                    Catalog.PrimaryKey.ID, Catalog.PrimaryKey.NAME, Catalog.PrimaryKey.NAME);

            Catalog workingCatalog = new Catalog(sourceCatalog, Catalog.PrimaryKey.SKU, Catalog.PrimaryKey.NAME,
                    Catalog.PrimaryKey.ID, Catalog.PrimaryKey.NAME, Catalog.PrimaryKey.NAME);

            return workingCatalog;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(logString("Square API call to catalog failed"), e);
        }
    }

    private String logString(String message) {
        return String.format("[%s] ::: %s", clientV2.getLogInfo(), message);
    }

    public void batchUpsertItemsIntoCatalog() {
        Preconditions.checkNotNull(catalog);
        Preconditions.checkNotNull(clientV2);

        logger.info(logString("TOTAL ITEMS IN CATALOG: " + catalog.getItems().values().size()));
        CatalogObject[] modifiedItems = catalog.getModifiedItems();
        logger.info(logString("TOTAL MODIFIED ITEMS IN CATALOG: " + modifiedItems.length));

        try {
            logger.info(logString("Upsert latest catalog of items..."));
            clientV2.catalog().setBatchUpsertSize(BATCH_UPSERT_SIZE).batchUpsertObjects(modifiedItems);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(logString("Failure upserting items into catalog"));
        }
        catalog = retrieveCatalogFromSquare();
    }

    public void batchUpsertInventoryChangesFromDb(
            HashMap<String, List<CsvInventoryAdjustment>> inventoryAdjustmentsCache, TntDatabaseApi tntDatabaseApi) {
        ArrayList<InventoryChange> changes = getInventoryChangesForLocations(inventoryAdjustmentsCache,
                getDeploymentLocations());
        logger.info(logString("Found " + changes.size() + " number of inventory changes for all locations"));

        // Upsert set inventory tracking changes if any
        // catalog is modified in getInventoryChangesForLocations()
        batchUpsertItemsIntoCatalog();

        // Upsert inventory changes
        batchUpsertInventoryAdjustmentsToSquare(changes, tntDatabaseApi);
    }

    public void batchUpsertInventoryAdjustmentsToSquare(ArrayList<InventoryChange> changes,
            TntDatabaseApi tntDatabaseApi) {
        logger.info(logString("NUMBER OF INVENTORY CHANGES TO BE SUBMITTED: " + changes.size()));

        // submit only 1 adjustment at a time and remove from DB on success
        // this way we can recover from any request errors and rely on the  DB
        // to hold all pending adjustments
        for (InventoryChange change : changes) {
            InventoryChange[] singleChange = new InventoryChange[] { change };
            try {
                logger.info(logString("Submitting inventory change to Square..."));
                clientV2.inventory().batchChangeInventory(singleChange);
                logger.info(logString("Successully submitted adjustment, removing adjustment entry from DB..."));
                tntDatabaseApi.executeQuery(
                        tntDatabaseApi.generateInventoryAdjustmentSQLDelete(change.getAdjustment().getReference_id()));
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(logString("Failure to upsert inventory changes"));
            }
        }

        logString("Done updating inventory changes");
    }

    private Location[] getDeploymentLocations() {
        try {
            logger.info(logString("Retrieving account location objects"));
            return clientV2.locations().list();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(logString("Square API call to list locations failed"));
        }
    }

    private ArrayList<InventoryChange> getInventoryChangesForLocations(
            HashMap<String, List<CsvInventoryAdjustment>> inventoryAdjustmentsCache, Location[] locations) {
        ArrayList<InventoryChange> changes = new ArrayList<InventoryChange>();

        // iterate through each location in Square and create InventoryChanges for each location if adjustments are found
        for (Location location : locations) {
            String tntLocationNum = location.getName();
            List<CsvInventoryAdjustment> adjustments = inventoryAdjustmentsCache.get(tntLocationNum);

            if (tntLocationNum.length() < 1) {
                throw new IllegalArgumentException(logString("Invalid TNT location number/ID"));
            }

            // skip location if no adjustments found in inventory adjustment entries from DB
            if (hasAdjustments(adjustments)) {
                if (isValidLocation(tntLocationNum)) {
                    logger.info(logString(
                            "LocationNum " + tntLocationNum + " expecting " + adjustments.size() + " adjustments."));
                    changes.addAll(getInventoryChangesForSingleLocation(location, adjustments));
                }
            }
        }

        return changes;
    }

    private ArrayList<InventoryChange> getInventoryChangesForSingleLocation(Location location,
            List<CsvInventoryAdjustment> adjustments) {
        ArrayList<InventoryChange> changes = new ArrayList<InventoryChange>();

        // iterate through each adjustment and find corresonding sku
        for (CsvInventoryAdjustment csvAdjustment : adjustments) {
            CatalogObject item = catalog.getItem(csvAdjustment.getUpc());

            // if item is not found, no need to perform adjustment update
            if (item != null) {

                // per TNT, only perform adjustment on first variation with matching SKU
                // CatalogObject item.get(SKU) also only check SKU against first variation
                if (hasValidItemVariation(item)) {
                    CatalogObject variation = item.getItemData().getVariations()[0];

                    if (variationIsPresentAtLocation(location, variation.getPresentAtLocationIds())) {
                        logger.info(logString(
                                "Found matching item variation for inventory adjustment: " + variation.getId()));

                        // update item variation override for inventory tracking
                        variation.setLocationTrackInventoryOverride(location.getId(), true);

                        // create inventory change
                        InventoryAdjustment adjustment = createInventoryAdjustment(location.getId(), variation.getId(),
                                csvAdjustment);
                        changes.add(createInventoryChange(adjustment));
                    }
                }
            }
        }

        logger.info(logString(
                "Returning " + changes.size() + " number of inventory changes for location " + location.getId()));
        return changes;
    }

    private boolean isValidLocation(String tntLocationNum) {
        if (!tntLocationNum.contains(INACTIVE_LOCATION) && !tntLocationNum.contains(DEFAULT_LOCATION)) {
            return true;
        }
        return false;
    }

    private boolean hasAdjustments(List<CsvInventoryAdjustment> adjustments) {
        if (adjustments != null) {
            return true;
        }
        return false;
    }

    private InventoryAdjustment createInventoryAdjustment(String locationId, String variationId,
            CsvInventoryAdjustment csvAdjustment) {
        // check for corrections to inventory count
        if (hasNegativeAdjustment(csvAdjustment)) {
            return createNegativeInventoryAdjustment(locationId, variationId, csvAdjustment);
        }

        return createPositiveInventoryAdjustment(locationId, variationId, csvAdjustment);
    }

    private InventoryAdjustment createPositiveInventoryAdjustment(String locationId, String variationId,
            CsvInventoryAdjustment csvAdjustment) {
        InventoryAdjustment adjustment = new InventoryAdjustment();
        adjustment.setFrom_state("NONE");
        adjustment.setTo_state("IN_STOCK");
        adjustment.setLocation_id(locationId);
        adjustment.setCatalog_object_id(variationId);
        adjustment.setQuantity(csvAdjustment.getQtyAdj());
        adjustment.setOccurred_at(getCurrentTimeStamp());
        adjustment.setReference_id(csvAdjustment.getId());
        return adjustment;
    }

    private InventoryAdjustment createNegativeInventoryAdjustment(String locationId, String variationId,
            CsvInventoryAdjustment csvAdjustment) {
        // convert "negative" String value to positive
        String adjustmentQty = String.valueOf(Math.abs(Integer.parseInt(csvAdjustment.getQtyAdj())));

        InventoryAdjustment adjustment = new InventoryAdjustment();
        adjustment.setFrom_state("IN_STOCK");
        adjustment.setTo_state("WASTE");
        adjustment.setLocation_id(locationId);
        adjustment.setCatalog_object_id(variationId);
        adjustment.setQuantity(adjustmentQty);
        adjustment.setOccurred_at(getCurrentTimeStamp());
        adjustment.setReference_id(csvAdjustment.getId());
        return adjustment;
    }

    private boolean hasNegativeAdjustment(CsvInventoryAdjustment csvAdjustment) {
        if (Integer.parseInt(csvAdjustment.getQtyAdj()) < 0) {
            return true;
        }

        return false;
    }

    private String getCurrentTimeStamp() {
        // RFC3339 Format
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(new Date());
    }

    private InventoryChange createInventoryChange(InventoryAdjustment adjustment) {
        InventoryChange change = new InventoryChange();
        change.setType("ADJUSTMENT");
        change.setAdjustment(adjustment);
        return change;
    }

    private boolean hasValidItemVariation(CatalogObject item) {
        return (item.getItemData() != null && item.getItemData().getVariations() != null
                && item.getItemData().getVariations().length > 0 && item.getItemData().getVariations()[0] != null
                && item.getItemData().getVariations()[0].getItemVariationData() != null);
    }

    private boolean variationIsPresentAtLocation(Location location, String[] locationIds) {
        for (String locationId : locationIds) {
            if (location.getId().equals(locationId)) {
                return true;
            }
        }
        return false;
    }
}