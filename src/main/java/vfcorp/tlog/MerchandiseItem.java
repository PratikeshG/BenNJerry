package vfcorp.tlog;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import vfcorp.Record;
import vfcorp.FieldDetails;

import com.squareup.connect.Item;
import com.squareup.connect.PaymentItemization;

public class MerchandiseItem extends Record {
	
	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;

	static {
		fields = new HashMap<String,FieldDetails>();
		length = 122;
		id = "001";
		
		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Void Indicator", new FieldDetails(1, 4, "1 = Void"));
		fields.put("Exchange Indicator", new FieldDetails(1, 5, "1 = Exchanged"));
		fields.put("Item Number", new FieldDetails(24, 6, "Left justified, space filled"));
		fields.put("Sku/Class Indicator", new FieldDetails(1, 30, "0 = SKU, 1 = Class"));
		fields.put("Department Number", new FieldDetails(4, 31, "Left justified, space filled"));
		fields.put("Class Number", new FieldDetails(4, 35, "left justified, space filled"));
		fields.put("Quantity", new FieldDetails(9, 39, "9(6)v999, right justified, zero filled"));
		fields.put("Price Original", new FieldDetails(10, 48, "zero filled"));
		fields.put("Price On Lookup", new FieldDetails(10, 58, "zero filled"));
		fields.put("Price Before Discount", new FieldDetails(10, 68, "zero filled"));
		fields.put("Reserved for Future Use", new FieldDetails(10, 78, "zero filled"));
		fields.put("Price Indicator", new FieldDetails(2, 88, ""));
		fields.put("Extended Total", new FieldDetails(10, 90, "zero filled"));
		fields.put("Not On File Indicator", new FieldDetails(1, 100, "1 = Not on file"));
		fields.put("Entry Indicator", new FieldDetails(1, 101, "1 = Scanned"));
		fields.put("Taxable Indicator", new FieldDetails(1, 102, "1 = Not taxable"));
		fields.put("Item Status", new FieldDetails(2, 103, "see below"));
		fields.put("Raincheck", new FieldDetails(1, 105, "1 = Rainchecked"));
		fields.put("Gift Item", new FieldDetails(1, 106, "1 = Gift receipt item"));
		fields.put("Package", new FieldDetails(1, 107, "1 = Item in package"));
		fields.put("Send Item Indicator", new FieldDetails(1, 108, "see below"));
		fields.put("Send to Location #", new FieldDetails(2, 109, "Zero filled"));
		fields.put("Team Associate Item Ind", new FieldDetails(1, 111, "1 = Item in team"));
		fields.put("Team Number", new FieldDetails(3, 112, "# is 0 if ind = 0"));
		fields.put("Return Indicator", new FieldDetails(1, 115, "1 = Can be Returned, 0 = Cannot be Returned"));
		fields.put("Item Sequence", new FieldDetails(7, 116, ""));
	}
	
	public MerchandiseItem() {
		super();
	}
	
	public MerchandiseItem(String record) {
		super(record);
	}

	@Override
	public Map<String,FieldDetails> getFields() {
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
	
	public MerchandiseItem parse(PaymentItemization itemization, List<Item> squareItemsList, int itemNumberLookupLength) throws Exception {
		String sku = itemization.getItemDetail().getSku(); // requires special formating - check docs
		if (sku.matches("[0-9]+")) {
			sku = String.format("%0" + Integer.toString(itemNumberLookupLength) + "d", new BigInteger(sku));
		}
		String quantity = String.format( "%.3f", itemization.getQuantity()).replace(".", ""); // requires special formating - check docs

		String departmentNumber = "";
		String classNumber = "";
		Matcher m = Pattern.compile("\\((.*?)\\)").matcher(itemization.getItemVariationName());
		while(m.find()) {
			String deptClass = m.group(1);
			departmentNumber = deptClass.substring(0, 4);
			classNumber = deptClass.substring(4, 8);
		    break;
		}

		putValue("Void Indicator", "0"); // no such thing as voided transactions
		putValue("Exchange Indicator", "0"); // no such thing as exchanged transactions
		putValue("Item Number", sku); // requires special formating, according to documentation
		putValue("Sku/Class Indicator", "0"); // this should always be 0...right?
		putValue("Department Number", departmentNumber);
		putValue("Class Number", classNumber);
		putValue("Quantity", quantity); // three decimal places are implied
		putValue("Price Original", Integer.toString(itemization.getSingleQuantityMoney().getAmount()));
		putValue("Price On Lookup", Integer.toString(itemization.getSingleQuantityMoney().getAmount()));
		putValue("Price Before Discount", Integer.toString(itemization.getSingleQuantityMoney().getAmount())); // not supported
		putValue("Reserved for Future Use", Integer.toString(itemization.getSingleQuantityMoney().getAmount())); // this follows the pattern of what VFC gave us
		putValue("Price Indicator", "01"); // not supported
		putValue("Extended Total", Integer.toString(itemization.getGrossSalesMoney().getAmount()));
		putValue("Not On File Indicator", "0"); // not supported
		putValue("Entry Indicator", "1"); // not supported
		putValue("Taxable Indicator", itemization.getTaxes().length == 0 ? "1" : "0");
		putValue("Item Status", "00"); // not supported
		putValue("Raincheck", "0"); // not supported
		putValue("Gift Item", "0"); // not supported
		putValue("Package", "0"); // not supported
		putValue("Send Item Indicator", "0"); // not supported
		putValue("Send to Location #", "00"); // not supported
		putValue("Team Associate Item Ind", "0"); // not supported
		putValue("Team Number", "000"); // not supported
		putValue("Return Indicator", "0"); // not supported
		putValue("Item Sequence", "0000000"); // not supported
		
		return this;
	}
}
