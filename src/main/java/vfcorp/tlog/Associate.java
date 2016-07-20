package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.FieldDetails;
import vfcorp.Record;

public class Associate extends Record {

	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,FieldDetails>();
		length = 29;
		id = "026";
		
		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Associate Number", new FieldDetails(11, 4, "zero filled"));
		fields.put("Employee Number", new FieldDetails(11, 15, "zero filled"));
		fields.put("Team Associate Ind", new FieldDetails(1, 26, ""));
		fields.put("Team Number", new FieldDetails(3, 27, "zero filled"));
	}
	
	public Associate() {
		super();
	}

	public Associate(String record) {
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
	
	public Associate parse(String employeeNumber) throws Exception {		
		putValue("Associate Number", employeeNumber);
		putValue("Employee Number", employeeNumber);
		putValue("Team Associate Ind", "0"); // 0 is "Non team associate sale"
		putValue("Team Number", "000"); // 000 is none

		return this;
	}
}
