package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.Record;
import vfcorp.FieldDetails;

public class StoreClose extends Record {

	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,FieldDetails>();
		length = 33;
		id = "017";
		
		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Weather Code", new FieldDetails(2, 4, "zero filled"));
		fields.put("Holiday Code", new FieldDetails(2, 6, "zero filled"));
		fields.put("Special Event Code", new FieldDetails(2, 8, "zero filled"));
		fields.put("Special Event Description", new FieldDetails(24, 10, ""));
	}
	
	public StoreClose() {
		super();
	}

	public StoreClose(String record) {
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
	
	public StoreClose parse() {
		putValue("Weather Code", ""); // not supported
		putValue("Holiday Code", ""); // not supported
		putValue("Special Event Code", ""); // not supported
		putValue("Special Event Description", ""); // not supported
		
		return this;
	}
}
