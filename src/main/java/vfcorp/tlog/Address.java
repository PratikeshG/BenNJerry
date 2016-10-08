package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.FieldDetails;
import vfcorp.Record;

public class Address extends Record {

	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;

	static {
		fields = new HashMap<String,FieldDetails>();
		length = 281;
		id = "030";

		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Address 1", new FieldDetails(30, 4, "Left justified, space filled"));
		fields.put("Address 2", new FieldDetails(30, 34, "Left justified, space filled"));
		fields.put("City", new FieldDetails(32, 64, "Left justified, space filled"));
		fields.put("State/County", new FieldDetails(20, 96, "Left justified, space filled"));
		fields.put("Zip/Postal Code", new FieldDetails(12, 116, "Left justified, space filled"));
		fields.put("County", new FieldDetails(20, 128, ""));
		fields.put("Country", new FieldDetails(32, 148, ""));
		fields.put("Location Code", new FieldDetails(2, 180, "zero filled"));
		fields.put("Email Address", new FieldDetails(100, 182, "Left justified, space filled"));
	}

	public Address() {
		super();
	}

	public Address(String record) {
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

	public Address parse(String addressLine1, String addressLine2, String city, String state, String zip, String email) throws Exception {		
		putValue("Address 1", addressLine1);
		putValue("Address 2", addressLine2);
		putValue("City", city);
		putValue("State/County", state);
		putValue("Zip/Postal Code", zip);
		putValue("County", ""); // spaces if not send sale
		putValue("Country", ""); // spaces if not send sale
		putValue("Location Code", "00"); // zeros if not send sale
		putValue("Email Address", email);

		return this;
	}
}
