package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import com.squareup.connect.Payment;

import vfcorp.Record;
import vfcorp.RecordDetails;

public class Associate extends Record {
	
	private static Map<String,RecordDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,RecordDetails>();
		length = 29;
		id = "026";
		
		fields.put("Identifier", new RecordDetails(3, 1, ""));
		fields.put("Associate Number", new RecordDetails(11, 4, ""));
		fields.put("Employee Number", new RecordDetails(11, 15, ""));
		fields.put("Team Associate Ind", new RecordDetails(1, 26, "1 = Member of team"));
		fields.put("Team Number", new RecordDetails(3, 27, "Team = 0 if Ind = 0"));
	}
	
	public Associate() {
		super();
	}
	
	public Associate(String record) {
		super(record);
	}

	@Override
	public Map<String,RecordDetails> getFields() {
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
	
	public Associate parse(Payment squarePayment) {
		return this;
	}
}
