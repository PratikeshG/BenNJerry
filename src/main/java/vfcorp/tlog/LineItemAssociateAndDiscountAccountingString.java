package vfcorp.tlog;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import vfcorp.FieldDetails;
import vfcorp.Record;

import com.squareup.connect.Payment;
import com.squareup.connect.PaymentItemization;

public class LineItemAssociateAndDiscountAccountingString extends Record {
	
	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;

	static {
		fields = new HashMap<String,FieldDetails>();
		length = 213;
		id = "056";
		
		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Team Identifier", new FieldDetails(1, 4, "1 = In Team, 0 = Not in Team"));
		fields.put("Team Number", new FieldDetails(3, 5, "000 if Not in Team"));
		fields.put("Item Number", new FieldDetails(24, 8, "Left justified, space filled"));
		fields.put("Non Merchandise Number", new FieldDetails(24, 32, ""));
		fields.put("EGC/Gift Certificate Number", new FieldDetails(20, 56, ""));
		fields.put("Associate Number", new FieldDetails(11, 76, "zero filled"));
		fields.put("Value (per associate)", new FieldDetails(10, 87, "zero filled"));
		fields.put("Type Indicator", new FieldDetails(2, 97, ""));
		fields.put("Adjust Line Item Quantity", new FieldDetails(1, 99, ""));
		fields.put("Emp Discount Value", new FieldDetails(10, 100, "zero filled"));
		fields.put("PCM Discount Value", new FieldDetails(10, 110, "zero filled"));
		fields.put("Line Item Discount Value", new FieldDetails(10, 120, "zero filled"));
		fields.put("Line Item Promo Value", new FieldDetails(10, 130, "zero filled"));
		fields.put("Transaction Discount Value", new FieldDetails(10, 140, "zero filled"));
		fields.put("Transaction Promo Value", new FieldDetails(10, 150, "zero filled"));
		fields.put("Price Override Indicator", new FieldDetails(1, 160, ""));
		fields.put("Price Override Value", new FieldDetails(10, 161, "zero filled"));
		fields.put("Receipt Presentation Price", new FieldDetails(10, 171, "zero filled"));
		fields.put("Productivity Quantity", new FieldDetails(9, 181, "zero filled"));
		fields.put("Employee Number", new FieldDetails(11, 190, "zero filled"));
		fields.put("PLU Sale Price Discount Value", new FieldDetails(10, 201, "zero filled"));
		fields.put("Reserved for Future Use", new FieldDetails(3, 211, "Space filled"));
	}
	
	public LineItemAssociateAndDiscountAccountingString() {
		super();
	}

	public LineItemAssociateAndDiscountAccountingString(String record) {
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
	
	public LineItemAssociateAndDiscountAccountingString parse(Payment payment, PaymentItemization itemization, int itemNumberLookupLength, String employeeId, double quantity) throws Exception {
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
		
		putValue("Team Identifier", "0"); // not supported
		putValue("Team Number", "000"); // not supported
		putValue("Item Number", sku);
		putValue("Non Merchandise Number", ""); // no such thing as "non merchandise" in square
		putValue("EGC/Gift Certificate Number", ""); // TODO(colinlam): gift card sales?
		putValue("Associate Number", "");
		putValue("Value (per associate)", "" + itemization.getNetSalesMoney().getAmount());
		putValue("Type Indicator", "01"); // "merchandise sale"; no other values supported
		putValue("Adjust Line Item Quantity", "0"); // transactions can't be altered after completion
		putValue("Emp Discount Value", "");
		putValue("PCM Discount Value", "");
		putValue("Line Item Discount Value", "" + -itemization.getDiscountMoney().getAmount());
		putValue("Line Item Promo Value", ""); // not supported
		putValue("Transaction Discount Value", "" + -payment.getDiscountMoney().getAmount());
		putValue("Transaction Promo Value", ""); // not supported
		putValue("Price Override Indicator", "0"); // not supported
		putValue("Price Override Value", ""); // not supported
		putValue("Receipt Presentation Price", "" + itemization.getGrossSalesMoney().getAmount());
		putValue("Employee Number", employeeId);
		putValue("Productivity Quantity", productivityQuantity);
		putValue("PLU Sale Price Discount Value", "" + -itemization.getDiscountMoney().getAmount());
		
		return this;
	}
}
