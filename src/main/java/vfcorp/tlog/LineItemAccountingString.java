package vfcorp.tlog;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import vfcorp.Record;
import vfcorp.FieldDetails;

import com.squareup.connect.PaymentItemization;

public class LineItemAccountingString extends Record {
	
	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;

	static {
		fields = new HashMap<String,FieldDetails>();
		length = 148;
		id = "055";
		
		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Item Number", new FieldDetails(24, 4, "Left justified, space filled"));
		fields.put("Non Merchandise Number", new FieldDetails(24, 28, ""));
		fields.put("EGC/Gift Certificate Number", new FieldDetails(20, 52, ""));
		fields.put("Item Value", new FieldDetails(10, 72, "zero filled"));
		fields.put("Type Indicator", new FieldDetails(2, 82, "See below"));
		fields.put("Adjust Line Item Quantity", new FieldDetails(1, 84, "0 = No, 1 = Yes"));
		fields.put("Sales Taxable Amount", new FieldDetails(10, 85, "Zero filled"));
		fields.put("Sales Not Taxable Amount", new FieldDetails(10, 95, "Zero filled"));
		fields.put("Sales Taxable Amount 2", new FieldDetails(10, 105, "Zero filled"));
		fields.put("Sales Not Taxable Amount 2", new FieldDetails(10, 115, "Zero filled"));
		fields.put("Return Quantity Indicator", new FieldDetails(2, 125, ""));
		fields.put("Productivity Quantity", new FieldDetails(9, 127, "9(6)V999, zero filled"));
		fields.put("Item Index", new FieldDetails(7, 136, "zero filled"));
		fields.put("Reserved for Future Use", new FieldDetails(6, 143, "Space filled"));
	}
	
	public LineItemAccountingString() {
		super();
	}
	
	public LineItemAccountingString(String record) {
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
	
	public LineItemAccountingString parse(PaymentItemization itemization, int itemNumberLookupLength, int index, double quantity) throws Exception {
		String sku = itemization.getItemDetail().getSku(); // requires special formating - check docs
		if (sku.matches("[0-9]+")) {
			sku = String.format("%0" + Integer.toString(itemNumberLookupLength) + "d", new BigInteger(sku));
		}
		String productivityQuantity = "";
		if (quantity > 1) {
			productivityQuantity = String.format( "%.3f", 1.0).replace(".", "");
		} else {
			productivityQuantity = String.format( "%.3f", quantity).replace(".", "");
		}
		
		putValue("Item Number", sku); // requires special formating, according to documentation
		putValue("Non Merchandise Number", ""); // no such thing as "non merchandise" in square
		putValue("EGC/Gift Certificate Number", ""); // TODO(colinlam): gift card sales?
		putValue("Item Value", "" + itemization.getTotalMoney().getAmount());
		putValue("Type Indicator", "01"); // "merchandise sale"
		putValue("Adjust Line Item Quantity", "0"); // transactions can't be altered after completion
		putValue("Sales Taxable Amount", "" + itemization.getTotalMoney().getAmount()); // not supported
		putValue("Sales Not Taxable Amount", ""); // not supported
		putValue("Sales Not Taxable Amount 2", ""); // not supported
		putValue("Sales Taxable Amount 2", ""); // not supported
		putValue("Return Quantity Indicator", "00"); // rentals are not supported
		putValue("Productivity Quantity", productivityQuantity); // this string gets repeated until quantity is below 1
		putValue("Item Index", "" + index);
		return this;
	}
}
