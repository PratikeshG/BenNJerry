package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.Record;
import vfcorp.RecordDetails;

import com.squareup.connect.PaymentItemization;

public class LineItemAssociateAndDiscountAccountingString extends Record {
	
	private static Map<String,RecordDetails> fields;
	private static int length;
	private static String id;

	static {
		fields = new HashMap<String,RecordDetails>();
		length = 213;
		id = "056";
		
		fields.put("Identifier", new RecordDetails(3, 1, ""));
		fields.put("Team Identifier", new RecordDetails(1, 4, "1 = In Team, 0 = Not in Team"));
		fields.put("Team Number", new RecordDetails(3, 5, "000 if Not in Team"));
		fields.put("Item Number", new RecordDetails(24, 8, ""));
		fields.put("Non Merchandise Number", new RecordDetails(24, 32, ""));
		fields.put("EGC/Gift Certificate Number", new RecordDetails(20, 56, ""));
		fields.put("Associate Number", new RecordDetails(11, 76, ""));
		fields.put("Value (per associate)", new RecordDetails(10, 87, ""));
		fields.put("Type Indicator", new RecordDetails(2, 97, ""));
		fields.put("Adjust Line Item Quantity", new RecordDetails(1, 99, ""));
		fields.put("Emp Discount Value", new RecordDetails(10, 100, ""));
		fields.put("PCM Discount Value", new RecordDetails(10, 110, ""));
		fields.put("Line Item Discount Value", new RecordDetails(10, 120, ""));
		fields.put("Line Item Promo Value", new RecordDetails(10, 130, ""));
		fields.put("Transaction Discount Value", new RecordDetails(10, 140, ""));
		fields.put("Transaction Promo Value", new RecordDetails(10, 150, ""));
		fields.put("Price Override Indicator", new RecordDetails(1, 160, ""));
		fields.put("Price Override Value", new RecordDetails(10, 161, ""));
		fields.put("Receipt Presentation Price", new RecordDetails(10, 171, ""));
		fields.put("Productivity Quantity", new RecordDetails(9, 181, ""));
		fields.put("Employee Number", new RecordDetails(11, 190, ""));
		fields.put("PLU Sale Price Discount Value", new RecordDetails(10, 201, ""));
		fields.put("Reserved for Future Use", new RecordDetails(3, 211, "Space filled"));
	}
	
	public LineItemAssociateAndDiscountAccountingString() {
		super();
	}

	public LineItemAssociateAndDiscountAccountingString(String record) {
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
	
	public LineItemAssociateAndDiscountAccountingString parse(PaymentItemization itemization) {
		return this;
	}
}
