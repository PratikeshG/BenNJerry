package vfcorp.rpc;

import java.util.HashMap;
import java.util.Map;

import vfcorp.Record;
import vfcorp.FieldDetails;

public class DepartmentClassRecord extends Record {

	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,FieldDetails>();
		length = 37;
		id = "04";
		
		fields.put("Record Type", new FieldDetails(2, 1, "zero filled"));
		fields.put("Action Type", new FieldDetails(1, 3, "zero filled"));
		fields.put("Class Number", new FieldDetails(4, 4, "zero filled"));
		fields.put("Department Number", new FieldDetails(4, 8, "zero filled"));
		fields.put("Class Description", new FieldDetails(24, 12, "space filled"));
		fields.put("Tax Flag (VAT)", new FieldDetails(2, 36, "space filled"));
	}
	
	public DepartmentClassRecord() {
		super();
	}

	public DepartmentClassRecord(String record) {
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
