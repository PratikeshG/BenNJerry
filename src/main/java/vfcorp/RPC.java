package vfcorp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedList;

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

	public static final String ITEM_RECORD = "01";
	public static final String ALTERNATE_RECORD = "02";
	public static final String DEPARTMENT_RECORD = "03";
	public static final String DEPARTMENT_CLASS_RECORD = "04";
	public static final String ITEM_ALTERNATE_DESCRIPTION = "29";
	public static final String ITEM_ADDITIONAL_DATA_RECORD = "36";
	
	private LinkedList<Record> rpc;
	private boolean onlyAddsCheck;
	private boolean suspiciousNumberOfRecordsCheck;
	private int itemNumberLookupLength;
	private int suspiciousNumberOfRecords;
	
	public void setOnlyAddsCheck(boolean onlyAddsCheck) {
		this.onlyAddsCheck = onlyAddsCheck;
	}
	
	public void setSuspiciousNumberOfRecordsCheck(boolean suspiciousNumberOfRecordsCheck) {
		this.suspiciousNumberOfRecordsCheck = suspiciousNumberOfRecordsCheck;
	}
	
	public void setItemNumberLookupLength(int itemNumberLookupLength) {
		this.itemNumberLookupLength = itemNumberLookupLength;
	}
	
	public void setSuspiciousNumberOfRecords(int suspiciousNumberOfRecords) {
		this.suspiciousNumberOfRecords = suspiciousNumberOfRecords;
	}
	
	public Catalog convert(Catalog current, CatalogChangeRequest.PrimaryKey primaryKey) throws Exception {
		Catalog clone = new Catalog(current);
		
		Iterator<Record> i = rpc.iterator();
		while (i.hasNext()) {
			Record record = i.next();
			
			if (record != null && record.getId() != null) {
				if (record.getId().equals(ITEM_RECORD)) {
					convertItem(primaryKey, clone, i, record);
				} else if (record.getId().equals(DEPARTMENT_CLASS_RECORD)) {
					convertCategory(primaryKey, clone, record);
				}
			}
		}
		
		performHealthChecks();
		
		return clone;
	}
	
	private void convertItem(CatalogChangeRequest.PrimaryKey primaryKey, Catalog clone, Iterator<Record> i, Record record) {
		String sku = convertItemNumberIntoSku(record.getValue("Item Number"));
		
		String matchingKey = null;
		Item matchingItem = null;
		ItemVariation matchingVariation = null;
		for (String key : clone.getItems().keySet()) {
			Item item = clone.getItems().get(key);
			if (sku.equals(item.getVariations()[0].getSku())) {
				matchingKey = key;
				matchingItem = item;
				matchingVariation = item.getVariations()[0];
				break;
			}
		}
		
		if (ItemRecord.ACTION_TYPE_ADD.equals(record.getValue("Action Type")) ||
				ItemRecord.ACTION_TYPE_CHANGE_RECORD.equals(record.getValue("Action Type")) ||
				ItemRecord.ACTION_TYPE_CHANGE_FIELD.equals(record.getValue("Action Type"))) {
			
			if (matchingItem == null) {
				matchingItem = new Item();
				matchingVariation = new ItemVariation("Regular");
				matchingVariation.setSku(sku);
				matchingItem.setVariations(new ItemVariation[]{matchingVariation});
			}
			
			matchingVariation.setPriceMoney(new Money(Integer.parseInt(record.getValue("Retail Price"))));
			matchingVariation.setUserData(record.getValue("Department Number") + record.getValue("Class Number"));
			
			for (Category category : clone.getCategories().values()) {
				if (category.getName().subSequence(0, 8).equals(record.getValue("Department Number") + record.getValue("Class Number"))) {
					matchingItem.setCategory(category);
				}
			}
			
			// Assumes that only one tax exists per catalog. Applies it to all items.
			if (clone.getFees().values().size() > 0) {
				matchingItem.setFees(new Fee[]{(Fee) clone.getFees().values().toArray()[0]});
			}
			
			matchingItem.setName(record.getValue("Description").replaceFirst("\\s+$", ""));
			
			for (int j = 0; j < 3; j++) {
				Record next = i.next();
				if (next.getId().equals(ITEM_ALTERNATE_DESCRIPTION)) {
					matchingItem.setName(matchingItem.getName() + " - " + next.getValue("Item Alternate Description").replaceFirst("\\s+$", ""));
				}
			}
			
			clone.addItem(matchingItem, primaryKey);
		} else if (ItemRecord.ACTION_TYPE_DELETE.equals(record.getValue("Action Type")) && matchingItem != null) {
			clone.getItems().remove(matchingKey);
		}
	}

	private void convertCategory(CatalogChangeRequest.PrimaryKey primaryKey, Catalog clone, Record record) {
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
		
		if (matchingCategory == null) {
			if (ItemRecord.ACTION_TYPE_ADD.equals(record.getValue("Action Type")) ||
					ItemRecord.ACTION_TYPE_CHANGE_RECORD.equals(record.getValue("Action Type")) ||
					ItemRecord.ACTION_TYPE_CHANGE_FIELD.equals(record.getValue("Action Type"))) {
				Category category = new Category();
				category.setName(name);
				clone.addCategory(category, CatalogChangeRequest.PrimaryKey.NAME);
			}
		} else {
			if ((ItemRecord.ACTION_TYPE_ADD.equals(record.getValue("Action Type")) ||
					ItemRecord.ACTION_TYPE_CHANGE_RECORD.equals(record.getValue("Action Type")) ||
					ItemRecord.ACTION_TYPE_CHANGE_FIELD.equals(record.getValue("Action Type"))) &&
					!matchingKey.equals(name)) {
				Category category = new Category();
				category.setName(name);
				clone.addCategory(category, primaryKey);
				clone.getCategories().remove(matchingKey);
			} else if (ItemRecord.ACTION_TYPE_DELETE.equals(record.getValue("Action Type"))) {
				clone.getCategories().remove(name);
			}
		}
	}
	
	private String convertItemNumberIntoSku(String itemNumber) {
		String shortItemNumber = itemNumber.substring(0, itemNumberLookupLength);
		
		if (shortItemNumber.matches("[0-9]+")) {
			// Remove leading zeros
			return shortItemNumber.replaceFirst("^0+(?!$)", "");
		} else {
			// Remove trailing spaces
			return shortItemNumber.replaceFirst("\\s+$", "");
		}
	}

	private void performHealthChecks() throws Exception {
		// Check to see if this is a suspicious number of records
		if (suspiciousNumberOfRecordsCheck) {
			if (rpc.size() > suspiciousNumberOfRecords) {
				throw new Exception("A suspicious number of records was detected for this PLU: " + rpc.size() + " records");
			}
		}
		
		// Check to see if this PLU contains only adds
		if (onlyAddsCheck) {
			boolean onlyAdds = true;
			for (Record record : rpc) {
				if (record != null && record.getId() != null) {
					if (record.getId().equals(ITEM_RECORD) || record.getId().equals(DEPARTMENT_CLASS_RECORD)) {
						if (!ItemRecord.ACTION_TYPE_ADD.equals(record.getValue("Action Type"))) {
							onlyAdds = false;
							break;
						}
					}
				}
			}
			
			if (onlyAdds) {
				throw new Exception("Items were only added in the current PLU; no items were updated or deleted");
			}
		}
	}
	
	public void ingest(BufferedInputStream rpc) throws IOException {
		this.rpc = new LinkedList<Record>();
		
		BufferedReader r = new BufferedReader(new InputStreamReader(rpc, StandardCharsets.UTF_8));
		String rpcLine = "";
		
		while ((rpcLine = r.readLine()) != null) {
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
		}
	}
}
