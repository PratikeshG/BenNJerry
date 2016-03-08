package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import com.squareup.connect.Payment;

import vfcorp.Record;
import vfcorp.RecordDetails;

public class EventGiveback extends Record {
	
	private static Map<String,RecordDetails> fields;
	private static int length;
	private static String id;

	static {
		fields = new HashMap<String,RecordDetails>();
		length = 73;
		id = "071";
		
		fields.put("Identifier", new RecordDetails(3, 1, ""));
		fields.put("Item Number", new RecordDetails(24, 4, "Left justified, space filled"));
		fields.put("Amount", new RecordDetails(10, 28, ""));
		fields.put("Event Number", new RecordDetails(5, 38, ""));
		fields.put("Deal Number", new RecordDetails(5, 43, ""));
		fields.put("Coupon Number", new RecordDetails(24, 48, ""));
		fields.put("Transaction Discount", new RecordDetails(1, 72, "0 = Item, 1 = Transaction"));
		fields.put("Component Type", new RecordDetails(1, 73, ""));
	}
	
	public EventGiveback() {
		super();
	}

	public EventGiveback(String record) {
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
	
	public EventGiveback parse(Payment squarePayment) {
		return this;
	}
}
