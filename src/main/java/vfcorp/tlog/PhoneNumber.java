package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.FieldDetails;
import vfcorp.Record;

public class PhoneNumber extends Record {

	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;

	static {
		fields = new HashMap<String,FieldDetails>();
		length = 22;
		id = "031";

		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Type", new FieldDetails(2, 4, "zero filled"));
		fields.put("Phone Number", new FieldDetails(15, 6, "Left justified, space filled"));
		fields.put("Send Location Code", new FieldDetails(2, 21, "zero filled"));
	}

	public PhoneNumber() {
		super();
	}

	public PhoneNumber(String record) {
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

	public PhoneNumber parse(String type, String number) throws Exception {		
		putValue("Type", type);
		putValue("Phone Number", number);
		putValue("Send Location Code", "00"); // 00 = not a Send Sale

		return this;
	}
}
