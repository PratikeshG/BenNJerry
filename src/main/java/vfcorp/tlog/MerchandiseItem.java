package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.Record;
import vfcorp.RecordDetails;

import com.squareup.connect.PaymentItemization;

public class MerchandiseItem extends Record {
	
	private static Map<String,RecordDetails> fields;
	private static int length;
	private static String id;

	static {
		fields = new HashMap<String,RecordDetails>();
		length = 122;
		id = "001";
		
		fields.put("Identifier", new RecordDetails(3, 1, ""));
		fields.put("Void Indicator", new RecordDetails(1, 4, "1 = Void"));
		fields.put("Exchange Indicator", new RecordDetails(1, 5, "1 = Exchanged"));
		fields.put("Item Number", new RecordDetails(24, 6, "Left justified, space filled"));
		fields.put("Sku/Class Indicator", new RecordDetails(1, 30, "0 = SKU, 1 = Class"));
		fields.put("Department Number", new RecordDetails(4, 31, "Left justified, space filled"));
		fields.put("Class Number", new RecordDetails(4, 35, ""));
		fields.put("Quantity", new RecordDetails(9, 39, "9(6)v999, right justified, zero filled"));
		fields.put("Price Original", new RecordDetails(10, 48, "zero filled"));
		fields.put("Price On Lookup", new RecordDetails(10, 58, "zero filled"));
		fields.put("Price Before Discount", new RecordDetails(10, 68, "zero filled"));
		fields.put("Reserved for Future Use", new RecordDetails(10, 78, "zero filled"));
		fields.put("Price Indicator", new RecordDetails(2, 88, ""));
		fields.put("Extended Total", new RecordDetails(10, 90, "zero filled"));
		fields.put("Not On File Indicator", new RecordDetails(1, 100, "1 = Not on file"));
		fields.put("Entry Indicator", new RecordDetails(1, 101, "1 = Scanned"));
		fields.put("Taxable Indicator", new RecordDetails(1, 102, "1 = Not taxable"));
		fields.put("Item Status", new RecordDetails(2, 103, "see below"));
		fields.put("Raincheck", new RecordDetails(1, 105, "1 = Rainchecked"));
		fields.put("Gift Item", new RecordDetails(1, 106, "1 = Gift receipt item"));
		fields.put("Package", new RecordDetails(1, 107, "1 = Item in package"));
		fields.put("Send Item Indicator", new RecordDetails(1, 108, "see below"));
		fields.put("Send to Location #", new RecordDetails(2, 109, "Zero filled"));
		fields.put("Team Associate Item Ind", new RecordDetails(1, 111, "1 = Item in team"));
		fields.put("Team Number", new RecordDetails(3, 112, "# is 0 if ind = 0"));
		fields.put("Return Indicator", new RecordDetails(1, 115, "1 = Can be Returned, 0 = Cannot be Returned"));
		fields.put("Item Sequence", new RecordDetails(7, 116, ""));
	}
	
	public MerchandiseItem() {
		super();
	}
	
	public MerchandiseItem(String record) {
		super(record);
	}

	@Override
	public Map<String,RecordDetails> getFields() {
		return fields;
	}

	@Override
	public int getLength() {
		return length;
	}

	@Override
	public String getId() {
		return id;
	}
	
	public MerchandiseItem parse(PaymentItemization itemization, int itemNumberLookupLength) {
		String sku = itemization.getItemDetail().getSku(); // requires special formating - check docs
		if (sku.matches("[0-9]+")) {
			sku = String.format("%0" + Integer.toString(itemNumberLookupLength) + "d", Integer.parseInt(sku));
		}
		String quantity = String.format( "%.3f", itemization.getQuantity()).replace(".", ""); // requires special formating - check docs
		
		values.put("Void Indicator", "0"); // no such thing as voided transactions
		values.put("Exchange Indicator", "0"); // no such thing as exchanged transactions
		values.put("Item Number", sku); // requires special formating, according to documentation
		values.put("Sku/Class Indicator", "0"); // this should always be 0...right?
		values.put("Department Number", ""); // no such thing as a "department number"
		values.put("Class Number", ""); // no such thing as a "class number"
		values.put("Quantity", quantity); // three decimal places are implied
		values.put("Price Original", Integer.toString(itemization.getSingleQuantityMoney().getAmount()));
		values.put("Price On Lookup", Integer.toString(itemization.getSingleQuantityMoney().getAmount()));
		values.put("Price Before Discount", Integer.toString(itemization.getSingleQuantityMoney().getAmount())); // not supported
		values.put("Reserved for Future Use", Integer.toString(itemization.getSingleQuantityMoney().getAmount())); // this follows the pattern of what VFC gave us
		values.put("Price Indicator", "01"); // not supported
		values.put("Extended Total", Integer.toString(itemization.getGrossSalesMoney().getAmount()));
		values.put("Not On File Indicator", "0"); // not supported
		values.put("Entry Indicator", "1"); // not supported
		values.put("Taxable Indicator", itemization.getTaxes() == null ? "1" : "0");
		values.put("Item Status", "00"); // not supported
		values.put("Raincheck", "0"); // not supported
		values.put("Gift Item", "0"); // not supported
		values.put("Package", "0"); // not supported
		values.put("Send Item Indicator", "0"); // not supported
		values.put("Send to Location #", "00"); // not supported
		values.put("Team Associate Item Ind", "0"); // not supported
		values.put("Team Number", "000"); // not supported
		values.put("Return Indicator", "0"); // not supported
		values.put("Item Sequence", "0000000"); // not supported
		
		return this;
	}
}
