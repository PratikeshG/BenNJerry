package vfcorp.rpc;

import java.util.HashMap;
import java.util.Map;

import vfcorp.Record;
import vfcorp.FieldDetails;

public class AlternateRecord extends Record {

	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,FieldDetails>();
		length = 199;
		id = "02";
		
		fields.put("Record Type", new FieldDetails(2, 1, "zero filled, Always 02"));
		fields.put("Action Type", new FieldDetails(1, 3, "zero filled"));
		fields.put("Alternate Number", new FieldDetails(24, 4, "space filled"));
		fields.put("Number Type", new FieldDetails(8, 28, "space filled"));
		fields.put("Item Number", new FieldDetails(24, 36, "space filled"));
		fields.put("Additional Data 1", new FieldDetails(30, 60, "space filled, Left justified"));
		fields.put("Additional Data 2", new FieldDetails(30, 90, "space filled, Left justified"));
		fields.put("Additional Data 3", new FieldDetails(30, 120, "space filled, Left justified"));
		fields.put("Additional Data 4", new FieldDetails(30, 150, "space filled, Left justified"));
		fields.put("Not in Use", new FieldDetails(20, 180, "space filled"));
	}
	
	public AlternateRecord() {
		super();
	}

	public AlternateRecord(String record) {
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
