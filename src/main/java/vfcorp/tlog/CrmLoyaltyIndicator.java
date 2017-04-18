package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.FieldDetails;
import vfcorp.Record;

public class CrmLoyaltyIndicator extends Record {

	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;

	static {
		fields = new HashMap<String,FieldDetails>();
		length = 27;
		id = "099";

		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("User ID", new FieldDetails(3, 4, ""));
		fields.put("Preferred Customer Number", new FieldDetails(20, 7, "Left justified, space filled"));
		fields.put("Loyalty Indicator", new FieldDetails(1, 27, ""));
	}

	public CrmLoyaltyIndicator() {
		super();
	}

	public CrmLoyaltyIndicator(String record) {
		super(record);
	}

	@Override
	public Map<String, FieldDetails> getFields() {
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

	public CrmLoyaltyIndicator parse(String customerId, boolean isLoyaltyCustomer) throws Exception {		
		putValue("User ID", "010"); // Always 010
		putValue("Preferred Customer Number", customerId);
		putValue("Loyalty Indicator", isLoyaltyCustomer ? "1" : "0");

		return this;
	}
}
