package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.FieldDetails;
import vfcorp.Record;

public class Name extends Record {

	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;

	static {
		fields = new HashMap<String,FieldDetails>();
		length = 58;
		id = "029";

		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Type", new FieldDetails(2, 4, ""));
		fields.put("Last Name", new FieldDetails(30, 6, "Left justified, space filled"));
		fields.put("First Name", new FieldDetails(20, 36, "Left justified, space filled"));
		fields.put("Middle Initial", new FieldDetails(1, 56, "Left justified, space filled"));
		fields.put("Location Code", new FieldDetails(2, 57, "zero filled"));
	}

	public Name() {
		super();
	}

	public Name(String record) {
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

	public Name parse(String lastName, String firstName) throws Exception {		
		putValue("Type", "01"); // 01 = Preferred Customer
		putValue("Last Name", lastName);
		putValue("First Name", firstName);
		putValue("Middle Initial", "");
		putValue("Location Code", "00"); // 00 = not a Send Sale

		return this;
	}
}
