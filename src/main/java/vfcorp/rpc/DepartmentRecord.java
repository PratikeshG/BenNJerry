package vfcorp.rpc;

import java.util.HashMap;
import java.util.Map;

import vfcorp.Record;
import vfcorp.FieldDetails;

public class DepartmentRecord extends Record {

	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,FieldDetails>();
		length = 31;
		id = "03";
		
		fields.put("Record Type", new FieldDetails(2, 1, "zero filled"));
		fields.put("Action Type", new FieldDetails(1, 3, "zero filled"));
		fields.put("Department Number", new FieldDetails(4, 4, "zero filled"));
		fields.put("Description", new FieldDetails(24, 8, "space filled"));
	}
	
	public DepartmentRecord() {
		super();
	}

	public DepartmentRecord(String record) {
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
