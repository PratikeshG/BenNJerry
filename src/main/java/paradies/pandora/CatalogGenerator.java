package paradies.pandora;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.squareup.connect.Discount;
import com.squareup.connect.Fee;
import com.squareup.connect.Item;
import com.squareup.connect.ItemVariation;
import com.squareup.connect.Money;
import com.squareup.connect.diff.Catalog;

import paradies.Tax;
import paradies.TaxTable;
import paradies.Util;

public class CatalogGenerator {

	public static final int INDEX_START_UPC = 0;
	public static final int INDEX_START_NAME = 16;
	public static final int INDEX_START_SKU = 38;
	public static final int INDEX_START_DEPT = 51;
	public static final int INDEX_START_COST = 56;
	public static final int INDEX_START_PRICE = 64;
	public static final int INDEX_START_TAX = 72;

	public static final String NO_SCAN_KEY_SKU = "99990430";

	private String currencyCode;
	private String storeId;
	private TaxTable taxTable;

	public CatalogGenerator(String storeId, String currencyCode) throws Exception {
		this.storeId = storeId;
		this.currencyCode = currencyCode;
		this.taxTable = new TaxTable(storeId);
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public String getStoreId() {
		return storeId;
	}

	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}

	public TaxTable getTaxTable() {
		return taxTable;
	}

	public void setTaxTable(TaxTable taxTable) {
		this.taxTable = taxTable;
	}

	public Catalog parsePayload(byte[] payload, Catalog currentCatalog) throws IOException {

		InputStream stream = new ByteArrayInputStream(payload);
        BufferedReader bfReader = new BufferedReader(new InputStreamReader(stream));

        ArrayList<CatalogEntry> payloadEntries = new ArrayList<CatalogEntry>();

        String line = "";
        while((line = bfReader.readLine()) != null) {
        	CatalogEntry entry = new CatalogEntry();

        	entry.setUpc(line.substring(INDEX_START_UPC, INDEX_START_NAME).trim());
        	entry.setName(line.substring(INDEX_START_NAME, INDEX_START_SKU).trim());
        	entry.setSku(line.substring(INDEX_START_SKU, INDEX_START_DEPT).trim());
        	entry.setDeptCode(line.substring(INDEX_START_DEPT, INDEX_START_COST).trim());
        	entry.setCost(line.substring(INDEX_START_COST, INDEX_START_PRICE).trim());
        	entry.setPrice(line.substring(INDEX_START_PRICE, INDEX_START_TAX).trim());
        	entry.setTaxCode(line.substring(INDEX_START_TAX).trim());

        	payloadEntries.add(entry);
        }

        if(stream != null) {
        	stream.close();
        }
 
        // Clone existing catalog and make incremental changes from input file
		Catalog newCatalog = new Catalog(currentCatalog);

		addNewCatalogDiscounts(newCatalog);
		addNewCatalogFees(newCatalog);
		
		// Get tax<>code mappings for items file
		// catalog taxes have been assigned Square IDs
		HashMap<String, Fee> taxesByCode = getActiveTableTaxesByCode();
		HashMap<String, Fee> catalogTaxesByCode = new HashMap<String, Fee>();
		for (Fee f : newCatalog.getFees().values()) {
			for (String code : taxesByCode.keySet()) {
				if (taxesByCode.get(code).getName().equals(f.getName())) {
					catalogTaxesByCode.put(code, f);
					break;
				}
			}
		}
		
		addNewCatalogItems(newCatalog, payloadEntries, catalogTaxesByCode);

		return newCatalog;
	}

	private void applyItemExemptions(List<Item> items) {
		for (Item item : items) {
			ItemVariation itemVariation = item.getVariations()[0];

			// No Scan Item
			if (itemVariation.getSku().equals(NO_SCAN_KEY_SKU)) {
				item.setName("NO SCAN");
				itemVariation.setPricingType("VARIABLE_PRICING");
				itemVariation.setPriceMoney(null);
				break;
			}
		}
	}

	private void addNewCatalogItems(Catalog newCatalog, List<CatalogEntry> newCatalogEntries, Map<String, Fee> taxesByCode) {
		// Get current unique items by SKU
		HashMap<String, Item> catalogItemCache = new HashMap<String, Item>();
		for (Item newI : newCatalog.getItems().values()) {
			if (newI.getVariations().length == 1) {
				catalogItemCache.put(newI.getVariations()[0].getSku(), newI);
			}
		}

		// Convert file line entries into Items
		ArrayList<Item> newItems = new ArrayList<Item>();
		for (CatalogEntry entry : newCatalogEntries) {
			newItems.add(newItem(entry, taxesByCode));
		}

		// Apply Pandora nuances (ie: no -can item info)
		applyItemExemptions(newItems);

		// Apply new Items to cache
		for (Item newItem : newItems) {
			ItemVariation newItemVariation = newItem.getVariations()[0];
			String newItemSku = newItemVariation.getSku();

			// Check for an existing matching item
			Item currentItem = catalogItemCache.get(newItemSku);
			if (currentItem != null) {
				ItemVariation currentItemVariation = currentItem.getVariations()[0];
				newItem.setId(currentItem.getId());
				newItemVariation.setItemId(currentItem.getId());
				newItemVariation.setId(currentItemVariation.getId());
			}

			catalogItemCache.put(newItemSku, newItem);
		}

		// Remove old items from cache
		Iterator<Map.Entry<String, Item>> iter = catalogItemCache.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Item> entry = iter.next();

			Item currentItem =  entry.getValue();
			String currentSku = currentItem.getVariations()[0].getSku();

			Boolean valid = false;
			for (Item newItem : newItems) {
				String newSku = newItem.getVariations()[0].getSku();
				if (newSku.equals(currentSku)) {
					valid = true;
					break;
				}
			}
			if (!valid) {
				iter.remove();
			}
		}

		// Clear catalog items
		newCatalog.setItems(new HashMap<String, Item>());

		// Add new items to catalog
		for (Item i : catalogItemCache.values()) {
			newCatalog.addItem(i);
		}
	}
	
	private void addNewCatalogDiscounts(Catalog newCatalog) {
		HashSet<Object> ignoreDiscountFields = new HashSet<Object>();
		ignoreDiscountFields.add(Discount.Field.ID);
		ignoreDiscountFields.add(Discount.Field.COLOR);
		
		HashMap<String, Discount> catalogDiscountCache = new HashMap<String, Discount>();

		// Get unique discounts by name
		for (Discount newD : newCatalog.getDiscounts().values()) {
			catalogDiscountCache.put(newD.getName(), newD);
		}

		// Remove invalid discounts from cache
		Iterator<Map.Entry<String, Discount>> iter = catalogDiscountCache.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Discount> entry = iter.next();

			Boolean valid = false;
			for (Discount defaultD : getPandoraDiscounts()) {
				if (entry.getValue().equals(defaultD, ignoreDiscountFields)) {
					valid = true;
					break;
				}
			}
			if (!valid) {
				iter.remove();
			}
		}

		// Apply missing valid discounts to cache
		for (Discount defaultD : getPandoraDiscounts()) {
			if (!catalogDiscountCache.containsKey(defaultD.getName())) {
				catalogDiscountCache.put(defaultD.getName(), defaultD);
			}
		}

		// Clear catalog discounts
		newCatalog.setDiscounts(new HashMap<String, Discount>());
		
		// Add new discounts to catalog
		for (Discount d : catalogDiscountCache.values()) {
			newCatalog.addDiscount(d);
		}
	}
	
	private HashMap<String, Fee> getActiveTableTaxesByCode() {
		HashMap<String, Fee> taxTableCodeToFee = new HashMap<String, Fee>();

		// Get tax table of active fees
		for (Tax tax : taxTable.getTaxes().values()) {			
			if (!tax.getRate().equals("0")) {
				Fee f = newTax(tax.getId(), tax.getRate());
				taxTableCodeToFee.put(tax.getCode(), f);
			}
		}
		
		return taxTableCodeToFee;
	}
	
	private void addNewCatalogFees(Catalog newCatalog) {	
		HashMap<String, Fee> taxTableFees = getActiveTableTaxesByCode();

		HashSet<Object> ignoreFeeFields = new HashSet<Object>();
		ignoreFeeFields.add(Fee.Field.ID);
		
		HashMap<String, Fee> catalogFeeCache = new HashMap<String, Fee>();

		// Get unique catalog taxes by name
		for (Fee newF : newCatalog.getFees().values()) {
			catalogFeeCache.put(newF.getName(), newF);
		}

		// Remove invalid taxes from catalog cache
		Iterator<Map.Entry<String, Fee>> iter = catalogFeeCache.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Fee> entry = iter.next();

			Boolean valid = false;
			for (Fee defaultF : taxTableFees.values()) {
				if (entry.getValue().equals(defaultF, ignoreFeeFields)) {
					valid = true;
					break;
				}
			}
			if (!valid) {
				iter.remove();
			}
		}

		// Apply missing valid taxes to cache
		for (Fee defaultF : taxTableFees.values()) {
			if (!catalogFeeCache.containsKey(defaultF.getName())) {
				catalogFeeCache.put(defaultF.getName(), defaultF);
			}
		}

		// Clear catalog taxes
		newCatalog.setFees(new HashMap<String, Fee>());

		// Add new taxes to catalog
		for (Fee f : catalogFeeCache.values()) {
			newCatalog.addFee(f);
		}
	}

	private Fee newTax(int id, String rate) {
		Fee t = new Fee();

		t.setName("Tax [" + id + "]");
		t.setRate(rate);
		t.setCalculationPhase("FEE_SUBTOTAL_PHASE");
		t.setAdjustmentType("TAX");
		t.setAppliesToCustomAmounts(true);
		t.setEnabled(true);
		t.setInclusionType("ADDITIVE");
		
		return t;
	}
	
	private Discount[] getPandoraDiscounts() {
		Discount[] discounts = new Discount[] {
			newDiscount("Item % Discount", "VARIABLE_PERCENTAGE", "0", 0),
			newDiscount("Item $ Discount", "VARIABLE_AMOUNT", null, 0),
			newDiscount("Associate Gift Cert", "FIXED", null, 2500),
			newDiscount("Airport 10%", "FIXED", "0.1", 0),
			newDiscount("Paradies 25%","FIXED", "0.25", 0),
			newDiscount("% Trans Off", "VARIABLE_PERCENTAGE", "0", 0),
			newDiscount("$ Trans Off", "VARIABLE_AMOUNT", null, 0)
		};
		
		return discounts;
	}
	
	private Discount newDiscount(String name, String type, String rate, int amount) {
		Discount d = new Discount();

		d.setName(name);
		d.setDiscountType(type);
		
		// Do not include this field for amount-based discounts.
		if (rate != null) {
			d.setRate(rate);
		} else {
			// Do not include this field for rate-based discounts.
			Money money = new Money(amount, this.currencyCode);
			d.setAmountMoney(money);
		}		

		return d;
	}
	
	private Item newItem(CatalogEntry entry, Map<String, Fee> taxTable) {
		Item item = new Item();

		item.setName(entry.getName() + " (" + entry.getUpc() + ")");

		ItemVariation variation = new ItemVariation(entry.getSku() + entry.getDeptCode());
		variation.setSku(entry.getUpc());
		variation.setPriceMoney(new Money(Util.getMoneyAmountFromDecimalString(entry.getPrice())));

		item.setVariations(new ItemVariation[]{variation});
		item.setFees(new Fee[]{taxTable.get(entry.getTaxCode())});

		return item;
	}
}
