package vfcorp.rpc;

import java.util.HashMap;
import java.util.Map;

import vfcorp.Record;
import vfcorp.FieldDetails;

public class ItemAdditionalDataRecord extends Record {

	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,FieldDetails>();
		length = 163;
		id = "36";
		
		fields.put("Record Type", new FieldDetails(2, 1, ""));
		fields.put("Action Type", new FieldDetails(1, 3, ""));
		fields.put("Item Number", new FieldDetails(24, 4, ""));
		fields.put("Additional Data Code", new FieldDetails(5, 28, ""));
		fields.put("Additional Data Type", new FieldDetails(1, 33, ""));
		fields.put("Additional Data", new FieldDetails(30, 34, ""));
		fields.put("Reserved for Future Use", new FieldDetails(100, 64, ""));
	}
	
	public ItemAdditionalDataRecord() {
		super();
	}

	public ItemAdditionalDataRecord(String record) {
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
