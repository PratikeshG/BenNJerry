package vfcorp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.Category;
import com.squareup.connect.Fee;
import com.squareup.connect.Item;
import com.squareup.connect.ItemVariation;
import com.squareup.connect.Money;
import com.squareup.connect.diff.Catalog;
import com.squareup.connect.diff.CatalogChangeRequest;

import vfcorp.rpc.AlternateRecord;
import vfcorp.rpc.DepartmentClassRecord;
import vfcorp.rpc.DepartmentRecord;
import vfcorp.rpc.ItemAdditionalDataRecord;
import vfcorp.rpc.ItemAlternateDescription;
import vfcorp.rpc.ItemRecord;

public class RPC {
    private static Logger logger = LoggerFactory.getLogger(RPC.class);

    public static final String ITEM_RECORD = "01";
    public static final String ALTERNATE_RECORD = "02";
    public static final String DEPARTMENT_RECORD = "03";
    public static final String DEPARTMENT_CLASS_RECORD = "04";
    public static final String ITEM_ALTERNATE_DESCRIPTION = "29";
    public static final String ITEM_ADDITIONAL_DATA_RECORD = "36";

    private LinkedList<Record> rpc;
    private String deploymentId;
    private int itemNumberLookupLength;

    void setItemNumberLookupLength(int itemNumberLookupLength) {
        this.itemNumberLookupLength = itemNumberLookupLength;
    }

    // This method CONSUMES the RPC linked list.
    public Catalog convert(Catalog current) throws Exception {
        Catalog clone = new Catalog(current);

        logger.info("Consuming PLU file..");

        LinkedList<Record> saleRecords = new LinkedList<Record>();

        while (rpc.size() > 0) {
            Record record = rpc.removeFirst();

            if (record != null && record.getId() != null) {
                if (record.getId().equals(ITEM_RECORD)) {
                    // Process sale records last
                    if (record.getValue("Action Type").equals(ItemRecord.ACTION_TYPE_PLACE_ON_SALE)) {
                        saleRecords.add(record);
                    } else {
                        convertItem(clone, record);
                    }
                } else if (record.getId().equals(DEPARTMENT_CLASS_RECORD)) {
                    convertCategory(clone, record);
                }
            }
        }

        // Process sale records
        while (saleRecords.size() > 0) {
            Record saleRecord = saleRecords.removeFirst();
            convertItem(clone, saleRecord);
        }

        return clone;
    }

    // This method CONSUMES the RPC linked list.
    public Catalog convertWithFilter(Catalog current, String deploymentId) throws Exception {
        this.deploymentId = deploymentId;
        Catalog clone = new Catalog(current);

        // Load Filters
        HashMap<String, Boolean> skuFilter = new HashMap<String, Boolean>();
        HashMap<String, Boolean> pluFilter = new HashMap<String, Boolean>();

        // SKUs
        String filterSKUPath = "/vfc-plu-filters/vfcorp-tnf-onhand-sku.csv";
        InputStream iSKU = this.getClass().getResourceAsStream(filterSKUPath);
        BufferedReader brSKU = new BufferedReader(new InputStreamReader(iSKU, "UTF-8"));
        try {
            String line;
            while ((line = brSKU.readLine()) != null) {
                skuFilter.put(line.trim(), new Boolean(true));
            }
        } finally {
            brSKU.close();
        }

        // PLUs
        String filterPLUPath = "/vfc-plu-filters/vfcorp-tnf-onhand-plu.csv";
        InputStream iPLU = this.getClass().getResourceAsStream(filterPLUPath);
        BufferedReader brPLU = new BufferedReader(new InputStreamReader(iPLU, "UTF-8"));
        try {
            String line;
            while ((line = brPLU.readLine()) != null) {
                String[] parts = line.split("\\s+");
                pluFilter.put(parts[0].trim(), new Boolean(true));
            }
        } finally {
            brPLU.close();
        }

        logger.info("Total SKU filtered: " + skuFilter.size());
        logger.info("Total PLU filtered: " + pluFilter.size());
        logger.info("Consuming PLU file..");

        LinkedList<Record> saleRecords = new LinkedList<Record>();

        while (rpc.size() > 0) {
            Record record = rpc.removeFirst();

            if (record != null && record.getId() != null) {
                if (record.getId().equals(ITEM_RECORD)) {
                    // Process sale records last
                    if (record.getValue("Action Type").equals(ItemRecord.ACTION_TYPE_PLACE_ON_SALE)) {
                        saleRecords.add(record);
                    } else {
                        convertItemWithFilter(clone, record, skuFilter, pluFilter);
                    }
                } else if (record.getId().equals(DEPARTMENT_CLASS_RECORD)) {
                    convertCategory(clone, record);
                }
            }
        }

        // Process sale records
        while (saleRecords.size() > 0) {
            Record saleRecord = saleRecords.removeFirst();
            convertItemWithFilter(clone, saleRecord, skuFilter, pluFilter);
        }

        return clone;
    }

    private void convertItem(Catalog clone, Record record) {
        String sku = convertItemNumberIntoSku(record.getValue("Item Number"));

        // SKU is primary key
        if (sku.length() < 1) {
            return;
        }

        String matchingKey = null;
        Item matchingItem = null;
        ItemVariation matchingVariation = null;

        Item item = clone.getItems().get(sku);
        if (item != null) {
            if (sku.equals(item.getVariations()[0].getSku())) {
                matchingKey = sku;
                matchingItem = item;
                matchingVariation = item.getVariations()[0];
            }
        }

        String actionType = record.getValue("Action Type");
        if (ItemRecord.ACTION_TYPE_ADD.equals(actionType) || ItemRecord.ACTION_TYPE_PLACE_ON_SALE.equals(actionType)
                || ItemRecord.ACTION_TYPE_CHANGE_RECORD.equals(actionType)
                || ItemRecord.ACTION_TYPE_CHANGE_FIELD.equals(actionType)) {

            if (matchingItem == null) {
                matchingItem = new Item();
                matchingVariation = new ItemVariation("Regular");
                matchingVariation.setSku(sku);
                matchingItem.setVariations(new ItemVariation[] { matchingVariation });
            }

            int price = Integer.parseInt(record.getValue("Retail Price"));
            if (price > 0) {
                matchingVariation.setPriceMoney(new Money(price));
            }

            if (ItemRecord.ACTION_TYPE_PLACE_ON_SALE.equals(actionType)) {
                matchingVariation.setPriceMoney(new Money(Integer.parseInt(record.getValue("Sale Price"))));
            }

            // Storing on variation to save to payment record details
            String deptCodeClass = record.getValue("Department Number") + record.getValue("Class Number");
            matchingVariation.setName(sku + " (" + deptCodeClass + ")");

            for (Category category : clone.getCategories().values()) {
                if (category.getName().subSequence(0, 8).equals(deptCodeClass)) {
                    matchingItem.setCategory(category);
                    break;
                }
            }

            // Only update name if it looks like it has changed
            String itemName = record.getValue("Description").replaceFirst("\\s+$", "");
            if (!matchingItem.getName().startsWith(itemName)) {
                matchingItem.setName(itemName);
            }

            // Assumes that only one tax exists per catalog. Applies it to all
            // items.
            if (clone.getFees().values().size() > 0) {
                matchingItem.setFees(new Fee[] { (Fee) clone.getFees().values().toArray()[0] });
            }

            // Remove records until an item or category is found
            if (rpc.size() > 0) {
                Record nextRecord = rpc.removeFirst();
                boolean itemOrCategoryRecordFound = false;

                while (!itemOrCategoryRecordFound && rpc.size() > 0) {
                    if (nextRecord.getId().equals(ITEM_RECORD) || nextRecord.getId().equals(DEPARTMENT_CLASS_RECORD)) {
                        rpc.addFirst(nextRecord);
                        itemOrCategoryRecordFound = true;
                    } else if (nextRecord.getId().equals(ITEM_ALTERNATE_DESCRIPTION)) {
                        if (nextRecord.getValue("Action Type").equals("1")) { // add
                            String itemSuffix = nextRecord.getValue("Item Alternate Description").replaceFirst("\\s+$",
                                    "");
                            if (!matchingItem.getName().endsWith(itemSuffix)) {
                                matchingItem.setName(matchingItem.getName() + " - " + itemSuffix);
                            }
                        }
                        nextRecord = rpc.removeFirst();
                    } else {
                        nextRecord = rpc.removeFirst();
                    }
                }
            }

            clone.addItem(matchingItem, CatalogChangeRequest.PrimaryKey.SKU);
        } else if (ItemRecord.ACTION_TYPE_DELETE.equals(actionType) && matchingItem != null) {
            clone.getItems().remove(matchingKey);
        }
    }

    // TODO(bhartard): Remove this hacky shit!
    private void convertItemWithFilter(Catalog clone, Record record, HashMap<String, Boolean> skuFilter,
            HashMap<String, Boolean> pluFilter) throws Exception {
        String sku = convertItemNumberIntoSku(record.getValue("Item Number"));

        // SKU is primary key
        if (sku.length() < 1) {
            return;
        }

        String matchingKey = null;
        Item matchingItem = null;
        ItemVariation matchingVariation = null;

        Item item = clone.getItems().get(sku);
        if (item != null) {
            if (sku.equals(item.getVariations()[0].getSku())) {
                matchingKey = sku;
                matchingItem = item;
                matchingVariation = item.getVariations()[0];
            }
        }

        String actionType = record.getValue("Action Type");
        if (ItemRecord.ACTION_TYPE_ADD.equals(actionType) || ItemRecord.ACTION_TYPE_PLACE_ON_SALE.equals(actionType)
                || ItemRecord.ACTION_TYPE_CHANGE_RECORD.equals(actionType)
                || ItemRecord.ACTION_TYPE_CHANGE_FIELD.equals(actionType)) {

            if (matchingItem == null) {
                matchingItem = new Item();
                matchingVariation = new ItemVariation("Regular");
                matchingVariation.setSku(sku);
                matchingItem.setVariations(new ItemVariation[] { matchingVariation });
            }

            int price = Integer.parseInt(record.getValue("Retail Price"));
            if (price > 0) {
                matchingVariation.setPriceMoney(new Money(price));
            }
            if (ItemRecord.ACTION_TYPE_PLACE_ON_SALE.equals(actionType)) {
                price = Integer.parseInt(record.getValue("Sale Price"));
                matchingVariation.setPriceMoney(new Money(price));
            }

            // Storing on variation to save to payment record details
            String deptCodeClass = record.getValue("Department Number") + record.getValue("Class Number");
            matchingVariation.setName(sku + " (" + deptCodeClass + ")");

            for (Category category : clone.getCategories().values()) {
                if (category.getName().subSequence(0, 8).equals(deptCodeClass)) {
                    matchingItem.setCategory(category);
                    break;
                }
            }

            // Only update name if it looks like it has changed
            String itemName = record.getValue("Description").replaceFirst("\\s+$", "");
            if (!matchingItem.getName().startsWith(itemName)) {
                matchingItem.setName(itemName);
            }

            // Apply item-specific taxes
            if (clone.getFees().values().size() > 0) {
                matchingItem.setFees(TaxRules.getItemTaxesForLocation(deploymentId,
                        clone.getFees().values().toArray(new Fee[0]), price, deptCodeClass));
            }

            // Remove records until an item or category is found
            if (rpc.size() > 0) {
                Record nextRecord = rpc.removeFirst();
                boolean itemOrCategoryRecordFound = false;

                while (!itemOrCategoryRecordFound && rpc.size() > 0) {
                    if (nextRecord.getId().equals(ITEM_RECORD) || nextRecord.getId().equals(DEPARTMENT_CLASS_RECORD)) {
                        rpc.addFirst(nextRecord);
                        itemOrCategoryRecordFound = true;
                    } else if (nextRecord.getId().equals(ITEM_ALTERNATE_DESCRIPTION)) {
                        if (nextRecord.getValue("Action Type").equals("1")) { // add
                            String itemSuffix = nextRecord.getValue("Item Alternate Description").replaceFirst("\\s+$",
                                    "");
                            if (!matchingItem.getName().endsWith(itemSuffix)) {
                                matchingItem.setName(matchingItem.getName() + " - " + itemSuffix);
                            }
                        }
                        nextRecord = rpc.removeFirst();
                    } else {
                        nextRecord = rpc.removeFirst();
                    }
                }
            }

            String filterShortSku = sku;
            String filterMedSku = ("000000000000" + filterShortSku).substring(filterShortSku.length());
            String filterLongSku = ("00000000000000" + filterShortSku).substring(filterShortSku.length());

            String[] bits = matchingItem.getName().split("\\s+");
            String plu = bits[bits.length - 1];

            // ONLY APPLU CHANGES FOR FILTERED ITEMS
            if (skuFilter.containsKey(filterShortSku) || skuFilter.containsKey(filterMedSku)
                    || skuFilter.containsKey(filterLongSku) || pluFilter.containsKey(plu)) {
                clone.addItem(matchingItem, CatalogChangeRequest.PrimaryKey.SKU);
            }
        } else if (ItemRecord.ACTION_TYPE_DELETE.equals(actionType) && matchingItem != null) {
            clone.getItems().remove(matchingKey);
        }
    }

    private void convertCategory(Catalog clone, Record record) {
        String name = record.getValue("Department Number") + record.getValue("Class Number") + " "
                + record.getValue("Class Description").trim();

        String matchingKey = null;
        Category matchingCategory = null;
        for (String key : clone.getCategories().keySet()) {
            Category category = clone.getCategories().get(key);
            if (category.getName().substring(0, 8).equals(name.substring(0, 8))) {
                matchingKey = key;
                matchingCategory = category;
                break;
            }
        }

        String actionType = record.getValue("Action Type");
        if (ItemRecord.ACTION_TYPE_ADD.equals(actionType) || ItemRecord.ACTION_TYPE_CHANGE_RECORD.equals(actionType)
                || ItemRecord.ACTION_TYPE_CHANGE_FIELD.equals(actionType)) {

            if (matchingCategory == null) {
                matchingCategory = new Category();
            }

            matchingCategory.setName(name);
            clone.addCategory(matchingCategory, CatalogChangeRequest.PrimaryKey.NAME);
        } else if (ItemRecord.ACTION_TYPE_DELETE.equals(actionType) && matchingKey != null) {
            clone.getCategories().remove(matchingKey);
        }
    }

    /*
     * They provide 14 digit SKUs, padded with 0s, but SKUs can also start with
     * 0... ugh SKUs that start with 0 are 12 or less characters
     *
     * Examples: 12345678901234 => 12345678901234 123456789012 => 123456789012
     * 00012345678901 => 012345678901 012345678901 => 01234567890
     *
     */
    private String convertItemNumberIntoSku(String itemNumber) {
        String shortItemNumber = itemNumber.substring(0, itemNumberLookupLength);

        if (shortItemNumber.matches("[0-9]+")) {
            // Remove leading zeros
            shortItemNumber = shortItemNumber.replaceFirst("^0+(?!$)", "");

            // But SKUs should be at least 12, pad 0s
            if (shortItemNumber.length() < 12) {
                shortItemNumber = ("000000000000" + shortItemNumber).substring(shortItemNumber.length());
            }
            return shortItemNumber;
        } else {
            // Remove trailing spaces
            return shortItemNumber.replaceFirst("\\s+$", "");
        }
    }

    public void ingest(BufferedInputStream rpc) throws Exception {
        this.rpc = new LinkedList<Record>();

        BufferedReader r = new BufferedReader(new InputStreamReader(rpc, StandardCharsets.UTF_8));
        String rpcLine = "";

        int totalRecordsProcessed = 0;
        logger.info("Ingesting PLU file...");

        while ((rpcLine = r.readLine()) != null) {

            if (totalRecordsProcessed % 5000 == 0) {
                logger.info("Processed: " + totalRecordsProcessed);
            }

            if (rpcLine.length() < 2) {
                continue;
            } else {
                String line = rpcLine.substring(0, 2);

                switch (line) {
                    case ITEM_RECORD:
                        this.rpc.add(new ItemRecord(rpcLine));
                        break;
                    case ALTERNATE_RECORD:
                        this.rpc.add(new AlternateRecord(rpcLine));
                        break;
                    case DEPARTMENT_RECORD:
                        this.rpc.add(new DepartmentRecord(rpcLine));
                        break;
                    case DEPARTMENT_CLASS_RECORD:
                        this.rpc.add(new DepartmentClassRecord(rpcLine));
                        break;
                    case ITEM_ALTERNATE_DESCRIPTION:
                        this.rpc.add(new ItemAlternateDescription(rpcLine));
                        break;
                    case ITEM_ADDITIONAL_DATA_RECORD:
                        this.rpc.add(new ItemAdditionalDataRecord(rpcLine));
                        break;
                }
            }

            totalRecordsProcessed++;
        }

        logger.info("Total records processed: " + totalRecordsProcessed);

        if (totalRecordsProcessed == 0) {
            throw new Exception("No records processed. Invalid input stream.");
        }
    }
}
