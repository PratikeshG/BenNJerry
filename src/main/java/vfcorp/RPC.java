package vfcorp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;

import vfcorp.rpc.AlternateRecord;
import vfcorp.rpc.DepartmentClassRecord;
import vfcorp.rpc.DepartmentRecord;
import vfcorp.rpc.ItemAdditionalDataRecord;
import vfcorp.rpc.ItemAlternateDescription;
import vfcorp.rpc.ItemRecord;

import com.squareup.connect.Category;
import com.squareup.connect.Fee;
import com.squareup.connect.Item;
import com.squareup.connect.ItemVariation;
import com.squareup.connect.Money;
import com.squareup.connect.diff.Catalog;
import com.squareup.connect.diff.CatalogChangeRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		if (ItemRecord.ACTION_TYPE_ADD.equals(actionType) ||
				ItemRecord.ACTION_TYPE_CHANGE_RECORD.equals(actionType) ||
				ItemRecord.ACTION_TYPE_CHANGE_FIELD.equals(actionType)) {

			if (matchingItem == null) {
				matchingItem = new Item();
				matchingVariation = new ItemVariation("Regular");
				matchingVariation.setSku(sku);
				matchingItem.setVariations(new ItemVariation[]{matchingVariation});
			}

			matchingVariation.setPriceMoney(new Money(Integer.parseInt(record.getValue("Retail Price"))));

			// Storing on variation to save to payment record details
			String deptCodeClass = record.getValue("Department Number") + record.getValue("Class Number");
			matchingVariation.setName(sku + " (" + deptCodeClass + ")");

			for (Category category : clone.getCategories().values()) {
				if (category.getName().subSequence(0, 8).equals(deptCodeClass)) {
					matchingItem.setCategory(category);
					break;
				}
			}

			// Assumes that only one tax exists per catalog. Applies it to all items.
			if (clone.getFees().values().size() > 0) {
				matchingItem.setFees(new Fee[]{(Fee) clone.getFees().values().toArray()[0]});
			}

			// Only update name if it looks like it has changed
			String itemName = record.getValue("Description").replaceFirst("\\s+$", "");
			if (!matchingItem.getName().startsWith(itemName)) {
				matchingItem.setName(itemName);
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
							String itemSuffix = nextRecord.getValue("Item Alternate Description").replaceFirst("\\s+$", "");
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
		} else if (ItemRecord.ACTION_TYPE_PLACE_ON_SALE.equals(actionType) && matchingItem != null && matchingVariation != null) {
			// TODO(bhartard): Check if date within sale price date or queue for future use?
			matchingVariation.setPriceMoney(new Money(Integer.parseInt(record.getValue("Sale Price"))));
		} else if (ItemRecord.ACTION_TYPE_DELETE.equals(actionType) && matchingItem != null) {
			clone.getItems().remove(matchingKey);
		}
	}

	// TODO(bhartard): Remove this hacky shit!
	private void convertItemWithFilter(Catalog clone, Record record, HashMap<String, Boolean> skuFilter, HashMap<String, Boolean> pluFilter) {
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
		if (ItemRecord.ACTION_TYPE_ADD.equals(actionType) ||
				ItemRecord.ACTION_TYPE_CHANGE_RECORD.equals(actionType) ||
				ItemRecord.ACTION_TYPE_CHANGE_FIELD.equals(actionType)) {

			if (matchingItem == null) {
				matchingItem = new Item();
				matchingVariation = new ItemVariation("Regular");
				matchingVariation.setSku(sku);
				matchingItem.setVariations(new ItemVariation[]{matchingVariation});
			}

			int price = Integer.parseInt(record.getValue("Retail Price"));
			matchingVariation.setPriceMoney(new Money(price));

			// Storing on variation to save to payment record details
			String deptCodeClass = record.getValue("Department Number") + record.getValue("Class Number");
			matchingVariation.setName(sku + " (" + deptCodeClass + ")");

			for (Category category : clone.getCategories().values()) {
				if (category.getName().subSequence(0, 8).equals(deptCodeClass)) {
					matchingItem.setCategory(category);
					break;
				}
			}

			// New York, NY - TNF Stores #12, 18, 516
			// 0% on clothing & footwear below $110 per item
			// 8.875% on clothing & footwear $110 and above per item
			// 8.875% on all other items (non- clothing/footwear)
			String NYC_BROADWAY = "vfcorp-tnf-00012";
			String NYC_WOOSTER = "vfcorp-tnf-00018";
			String NYC_FIFTH = "vfcorp-tnf-00516";

			// White Plains, NY - Westchester Co. - TNF Store #28
			// 4.375% on clothing & footwear below $110 per item
			// 8.375% on clothing & footwear $110 and above per item
			// 8.375% on all other items (non-clothing/footwear)
			String NY_WHITEPLAINS = "vfcorp-tnf-00028";

			// Victor, NY - Ontario Co. - TNF Store #58
			// 3.5% on clothing & footwear below $110 per item
			// 7.5% on clothing & footwear $110 and above per item
			// 7.5% on all other items (non-clothing/footwear)
			String NY_ONTARIO = "vfcorp-tnf-00058";

			// Central Valley, NY - Orange, Co. - TNF Store #64
			// 4.125% on clothing & footwear below $110 per item
			// 8.125% on clothing & footwear $110 and above per item
			// 8.125% on all other items (non-clothing/footwear)
			String NY_WOODBURY = "vfcorp-tnf-00064";

			// Riverhead, NY  - Suffolk Co. - TNF Store #319
			// 4.625% on clothing & footwear below $110 per item
			// 8.625% on clothing & footwear $110 and above per item
			// 8.625% on all other items (non-clothing/footwear)
			String NY_RIVERHEAD = "vfcorp-tnf-00319";

			// Boston
			// No sales tax on clothing (and shoes) that costs less than $175.
			// It it costs more than $175, you pay 6.25% on the amount over 175
			String BOSTON = "vfcorp-tnf-00014";

			// Apply unique taxation of items
			if (clone.getFees().values().size() == 1 && (deploymentId.equals(NYC_BROADWAY) || deploymentId.equals(NYC_WOOSTER) || deploymentId.equals(NYC_FIFTH))) {
				Fee nycTax = (Fee) clone.getFees().values().toArray()[0];
				if (price < 11000) {
					// TODO(bhartard): Apply tax to items not in mentioned categories
				} else {
					matchingItem.setFees(new Fee[]{nycTax});
				}
			} if (clone.getFees().values().size() == 1 && deploymentId.equals(BOSTON)) {
				// Don't appl y any taxes for now, since we can't correctly apply MA tax rules
				Fee maTax = (Fee) clone.getFees().values().toArray()[0];
				if (price < 17500) {
				} else {
				}
			} else if (clone.getFees().values().size() == 2 &&
					(deploymentId.equals(NY_WHITEPLAINS) || deploymentId.equals(NY_ONTARIO) || deploymentId.equals(NY_WOODBURY) || deploymentId.equals(NY_RIVERHEAD))) {
				Fee lowTax = getLowerTax((Fee) clone.getFees().values().toArray()[0], (Fee) clone.getFees().values().toArray()[1]);
				Fee highTax = getHigherTax((Fee) clone.getFees().values().toArray()[0], (Fee) clone.getFees().values().toArray()[1]);
				if (price < 11000) {
					matchingItem.setFees(new Fee[]{lowTax});
				} else {
					matchingItem.setFees(new Fee[]{highTax});
				}
			} else if (clone.getFees().values().size() > 0) {
				// Assumes that only one tax exists per normal location catalog. Applies it to all items.
				matchingItem.setFees(new Fee[]{(Fee) clone.getFees().values().toArray()[0]});
			}

			// Only update name if it looks like it has changed
			String itemName = record.getValue("Description").replaceFirst("\\s+$", "");
			if (!matchingItem.getName().startsWith(itemName)) {
				matchingItem.setName(itemName);
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
							String itemSuffix = nextRecord.getValue("Item Alternate Description").replaceFirst("\\s+$", "");
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
			String plu = bits[bits.length-1];
			
			// ONLY APPLU CHANGES FOR FILTERED ITEMS
			if (skuFilter.containsKey(filterShortSku) || skuFilter.containsKey(filterMedSku) || skuFilter.containsKey(filterLongSku) || pluFilter.containsKey(plu)) {
				clone.addItem(matchingItem, CatalogChangeRequest.PrimaryKey.SKU);
			}
		} else if (ItemRecord.ACTION_TYPE_PLACE_ON_SALE.equals(actionType) && matchingItem != null && matchingVariation != null) {
			// TODO(bhartard): Check if date within sale price date or queue for future use?
			matchingVariation.setPriceMoney(new Money(Integer.parseInt(record.getValue("Sale Price"))));
		} else if (ItemRecord.ACTION_TYPE_DELETE.equals(actionType) && matchingItem != null) {
			clone.getItems().remove(matchingKey);
		}
	}
	
	private Fee getLowerTax(Fee fee1, Fee fee2) {
		if (Float.parseFloat(fee1.getRate()) < Float.parseFloat(fee2.getRate())) {
			return fee1;
		}
		return fee2;
	}

	private Fee getHigherTax(Fee fee1, Fee fee2) {
		if (Float.parseFloat(fee1.getRate()) >= Float.parseFloat(fee2.getRate())) {
			return fee1;
		}
		return fee2;
	}

	private void convertCategory(Catalog clone, Record record) {
		String name = record.getValue("Department Number") + record.getValue("Class Number") + " " + record.getValue("Class Description").trim();

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
		if (ItemRecord.ACTION_TYPE_ADD.equals(actionType) ||
				ItemRecord.ACTION_TYPE_CHANGE_RECORD.equals(actionType) ||
				ItemRecord.ACTION_TYPE_CHANGE_FIELD.equals(actionType)) {

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
	 * They provide 14 digit SKUs, padded with 0s, but SKUs can also start with 0... ugh
	 * SKUs that start with 0 are 12 or less characters
	 *
	 * Examples:
	 * 12345678901234 => 12345678901234
	 * 123456789012 => 123456789012
	 * 00012345678901 => 012345678901
	 * 012345678901 => 01234567890
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

				switch(line) {
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
