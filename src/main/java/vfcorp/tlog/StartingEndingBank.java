package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.Record;
import vfcorp.RecordDetails;

public class StartingEndingBank extends Record {

	private static Map<String,RecordDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,RecordDetails>();
		length = 21;
		id = "016";
		
		fields.put("Identifier", new RecordDetails(3, 1, ""));
		fields.put("Starting or Ending Bank", new RecordDetails(1, 4, ""));
		fields.put("Identification Number", new RecordDetails(6, 5, "zero filled"));
		fields.put("Identification Type", new RecordDetails(1, 11, "="));
		fields.put("Amount", new RecordDetails(10, 12, "zero filled"));
	}
	
	public StartingEndingBank() {
		super();
	}

	public StartingEndingBank(String record) {
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
	
	public StartingEndingBank parse(boolean starting, String idNumber, String amount) {
		values.put("Starting or Ending Bank", starting == true ? "0" : "1");
		values.put("Identification Number", idNumber);
		values.put("Identification Type", "0"); // doesn't support cashiers
		values.put("Amount", amount);
		
		return this;
	}
}
