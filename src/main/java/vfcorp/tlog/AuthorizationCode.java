package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.Record;
import vfcorp.RecordDetails;

public class AuthorizationCode extends Record {

	private static Map<String,RecordDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,RecordDetails>();
		length = 24;
		id = "023";
		
		fields.put("Identifier", new RecordDetails(3, 1, ""));
		fields.put("Authorization Code", new RecordDetails(8, 4, ""));
		fields.put("Function Indicator", new RecordDetails(2, 12, ""));
		fields.put("Employee Number", new RecordDetails(11, 14, "right justified, zero filled"));
	}
	
	public AuthorizationCode() {
		super();
	}

	public AuthorizationCode(String record) {
		super(record);
	}

	@Override
	public Map<String, RecordDetails> getFields() {
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
	
	public AuthorizationCode parse(String functionIndicator, String employeeNumber) {
		values.put("Authorization Code", ""); // not supported
		values.put("Function Indicator", functionIndicator); // several different supported types
		values.put("Employee Number", employeeNumber);
		
		return this;
	}
}
