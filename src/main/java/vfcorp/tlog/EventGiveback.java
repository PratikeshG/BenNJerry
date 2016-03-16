package vfcorp.tlog;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import vfcorp.Record;
import vfcorp.FieldDetails;

import com.squareup.connect.PaymentItemization;

public class EventGiveback extends Record {
	
	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;

	static {
		fields = new HashMap<String,FieldDetails>();
		length = 73;
		id = "071";
		
		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Item Number", new FieldDetails(24, 4, "Left justified, space filled"));
		fields.put("Amount", new FieldDetails(10, 28, "zero filled"));
		fields.put("Event Number", new FieldDetails(5, 38, "zero filled"));
		fields.put("Deal Number", new FieldDetails(5, 43, "zero filled"));
		fields.put("Coupon Number", new FieldDetails(24, 48, ""));
		fields.put("Transaction Discount", new FieldDetails(1, 72, "0 = Item, 1 = Transaction"));
		fields.put("Component Type", new FieldDetails(1, 73, ""));
	}
	
	public EventGiveback() {
		super();
	}

	public EventGiveback(String record) {
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
	
	public EventGiveback parse(PaymentItemization itemization, int itemNumberLookupLength) {
		String sku = itemization.getItemDetail().getSku(); // requires special formating - check docs
		if (sku.matches("[0-9]+")) {
			sku = String.format("%0" + Integer.toString(itemNumberLookupLength) + "d", new BigInteger(sku));
		}
		
		putValue("Item Number", sku);
		putValue("Amount", ""); // promo events not supported
		putValue("Event Number", ""); // not supported
		putValue("Deal Number", ""); // not supported
		putValue("Coupon Number", ""); // not supported
		putValue("Transaction Discount", ""); // not supported
		putValue("Component Type", ""); // not supported
		
		return this;
	}
}
