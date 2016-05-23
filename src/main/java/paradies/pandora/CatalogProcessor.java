package paradies.pandora;

import paradies.Tax;
import paradies.TaxTable;
import paradies.Util;
import paradies.pandora.CatalogEntry;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.mule.api.MuleMessage;
import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import util.square.models.LocationCatalog;

import com.squareup.connect.Item;
import com.squareup.connect.ItemVariation;
import com.squareup.connect.Discount;
import com.squareup.connect.Fee;
import com.squareup.connect.Money;
import com.squareup.connect.SquareClient;
import com.squareup.connect.diff.Catalog;
import com.squareup.connect.diff.CatalogChangeRequest;

public class CatalogProcessor implements Callable {
	
	private String currencyCode;
	private String storeId;
	private TaxTable taxTable;
	
	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		
		MuleMessage message = eventContext.getMessage();
		LocationCatalog locationCatalog = (LocationCatalog) message.getPayload();
		this.currencyCode = locationCatalog.getLocation().getCurrencyCode();
		this.storeId = Util.getStoreIdFromLocationNickname(locationCatalog.getLocation().getLocationDetails().getNickname());
		this.taxTable = new TaxTable(storeId);

		byte[] ftpPayload = eventContext.getMessage().getProperty("ftpPayload", PropertyScope.SESSION);
		Catalog currentCatalog = locationCatalog.getCatalog();
		Catalog newCatalog = parsePayload(ftpPayload, currentCatalog);
		
		CatalogChangeRequest ccr = CatalogChangeRequest.diff(currentCatalog, newCatalog);
		
		String accessToken = message.getProperty("token", PropertyScope.SESSION);
		String apiUrl = message.getProperty("apiUrl", PropertyScope.SESSION);
		String apiVersion = message.getProperty("apiVersion", PropertyScope.SESSION);
		String merchantId = message.getProperty("merchantId", PropertyScope.SESSION);
		SquareClient client = new SquareClient(accessToken, apiUrl, apiVersion, merchantId, locationCatalog.getLocation().getId());
		
		ccr.setSquareClient(client);
		ccr.call();

		locationCatalog.setCatalog(newCatalog);
		return locationCatalog;
	}
	
	public Catalog parsePayload(byte[] payload, Catalog currentCatalog) throws IOException {

		InputStream stream = new ByteArrayInputStream(payload);
        BufferedReader bfReader = new BufferedReader(new InputStreamReader(stream));

        ArrayList<CatalogEntry> payloadEntries = new ArrayList<CatalogEntry>();

        String line = "";
        while((line = bfReader.readLine()) != null) {
        	CatalogEntry entry = new CatalogEntry();

        	entry.setUpc(line.substring(0, 15).trim());
        	entry.setName(line.substring(15, 36).trim());
        	entry.setSku(line.substring(36, 52).trim());
        	entry.setDeptCode("");
        	entry.setCost(line.substring(53, 60).trim());
        	entry.setPrice(line.substring(60, 68).trim());
        	entry.setTaxCode(line.substring(69, 72).trim());

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

	private void addNewCatalogItems(Catalog newCatalog, ArrayList<CatalogEntry> newCatalogEntries, HashMap<String, Fee> taxesByCode) {
		/*
		Object[] omit = new Object[] {
				Item.Field.ID, Item.Field.COLOR, Item.Field.DESCRIPTION, Item.Field.TYPE, Item.Field.ABBREVIATION,
				Item.Field.VISIBILITY, Item.Field.AVAILABLEONLINE, Item.Field.MASTERIMAGE, Item.Field.MODIFIERSLISTS,
				Item.Field.FEES, Item.Field.MODIFIERSLISTS,
			
				ItemVariation.Field.ID, ItemVariation.Field.INVENTORYALERTTHRESHOLD, ItemVariation.Field.INVENTORYALERTTYPE,
				ItemVariation.Field.ITEMID, ItemVariation.Field.ORDINAL, ItemVariation.Field.TRACKINVENTORY
		};		
		HashSet<Object> ignoreFields = new HashSet<Object>(Arrays.asList(omit));
	*/
		
		// Get current unique items by SKU
		HashMap<String, Item> catalogItemCache = new HashMap<String, Item>();
		for (Item newI : newCatalog.getItems().values()) {
			if (newI.getVariations().length == 1) {
				catalogItemCache.put(newI.getVariations()[0].getSku(), newI);
			}
		}

		ArrayList<Item> newItems = new ArrayList<Item>();
		for (CatalogEntry entry : newCatalogEntries) {
			newItems.add(newItem(entry, taxesByCode));
		}

		// Apply new items to cache
		for (Item newItem : newItems) {
			ItemVariation newItemVariation = newItem.getVariations()[0];
			String newItemSku = newItemVariation.getSku();

			// Find an existing matching item
			for (Item currentItem : catalogItemCache.values()) {
				ItemVariation currentItemVariation = currentItem.getVariations()[0];
				String currentItemSku = currentItemVariation.getSku();

				// Item already exists in the account
				if (newItemSku.equals(currentItemSku)) {
					newItem.setId(currentItem.getId());
					newItemVariation.setItemId(currentItem.getId());
					newItemVariation.setId(currentItemVariation.getId());
					break;
				}
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
				Fee f = newTax(tax.getName(), tax.getRate());
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

		// Get unique catalog fees by name
		for (Fee newF : newCatalog.getFees().values()) {
			catalogFeeCache.put(newF.getName(), newF);
		}

		// Remove invalid fees from catalog cache
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

		// Apply missing valid discounts to cache
		for (Fee defaultF : taxTableFees.values()) {
			if (!catalogFeeCache.containsKey(defaultF.getName())) {
				catalogFeeCache.put(defaultF.getName(), defaultF);
			}
		}

		// Clear catalog fees
		newCatalog.setFees(new HashMap<String, Fee>());

		// Add new fees to catalog
		for (Fee f : catalogFeeCache.values()) {
			newCatalog.addFee(f);
		}
	}

	private Fee newTax(String name, String rate) {
		Fee t = new Fee();

		t.setName(name);
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
	
	private Item newItem(CatalogEntry entry, HashMap<String, Fee> taxTable) {
		Item item = new Item();

		item.setName(entry.getName());

		ItemVariation variation = new ItemVariation(entry.getSku());
		variation.setSku(entry.getUpc());
		variation.setPriceMoney(new Money(Util.getMoneyAmountFromDecimalString(entry.getPrice())));
		variation.setUserData(entry.getCost() + "|" + entry.getDeptCode());

		item.setVariations(new ItemVariation[]{variation});
		item.setFees(new Fee[]{taxTable.get(entry.getTaxCode())});

		return item;
	}
}
