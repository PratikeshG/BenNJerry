package vfcorp.rpc;

import java.util.HashMap;
import java.util.Map;

import vfcorp.Record;
import vfcorp.FieldDetails;

public class ItemAlternateDescription extends Record {

	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,FieldDetails>();
		length = 69;
		id = "29";
		
		fields.put("Record Type", new FieldDetails(2, 1, "zero filled, Always 29"));
		fields.put("Action Type", new FieldDetails(1, 3, "zero filled"));
		fields.put("Item Number", new FieldDetails(24, 4, "space filled, Left-justified, space-filled"));
		fields.put("Alternate Description Type", new FieldDetails(2, 28, "zero filled, Always 01"));
		fields.put("Item Alternate Description", new FieldDetails(40, 30, "space filled"));
	}
	
	public ItemAlternateDescription() {
		super();
	}

	public ItemAlternateDescription(String record) {
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
}
