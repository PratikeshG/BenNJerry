package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import com.squareup.connect.CashDrawerShift;

import vfcorp.Record;
import vfcorp.RecordDetails;

public class EndingBank extends Record {

	private static Map<String,RecordDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,RecordDetails>();
		length = 20;
		id = "019";
		
		fields.put("Identifier", new RecordDetails(3, 1, ""));
		fields.put("Identification Number", new RecordDetails(6, 4, "zero filled"));
		fields.put("Identification Type", new RecordDetails(1, 10, "="));
		fields.put("Amount", new RecordDetails(10, 11, "zero filled"));
	}
	
	public EndingBank() {
		super();
	}

	public EndingBank(String record) {
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
	
	public EndingBank parse(CashDrawerShift cashDrawerShift, String amount) {
		String registerNumber = "";
		
		if (cashDrawerShift.getDevice().getName() != null) {
			registerNumber = cashDrawerShift.getDevice().getName();
		}
		
		putValue("Identification Number", registerNumber);
		putValue("Identification Type", "0"); // doesn't support cashiers
		putValue("Amount", amount);
		
		return this;
	}
}
