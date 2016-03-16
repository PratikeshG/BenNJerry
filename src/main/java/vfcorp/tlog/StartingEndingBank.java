package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.Record;
import vfcorp.FieldDetails;

import com.squareup.connect.CashDrawerShift;

public class StartingEndingBank extends Record {

	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,FieldDetails>();
		length = 21;
		id = "016";
		
		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Starting or Ending Bank", new FieldDetails(1, 4, ""));
		fields.put("Identification Number", new FieldDetails(6, 5, "zero filled"));
		fields.put("Identification Type", new FieldDetails(1, 11, "="));
		fields.put("Amount", new FieldDetails(10, 12, "zero filled"));
	}
	
	public StartingEndingBank() {
		super();
	}

	public StartingEndingBank(String record) {
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
	
	public StartingEndingBank parse(CashDrawerShift cashDrawerShift, String amount, boolean starting) {
		String registerNumber = "";
		
		if (cashDrawerShift.getDevice().getName() != null) {
			if (cashDrawerShift.getDevice().getName() != null) {
				int registerNumberFirstIndex = cashDrawerShift.getDevice().getName().indexOf('(');
				int registerNumberLastIndex = cashDrawerShift.getDevice().getName().indexOf(')');
				if (registerNumberFirstIndex > -1 && registerNumberLastIndex > -1)
					registerNumber = cashDrawerShift.getDevice().getName().substring(registerNumberFirstIndex + 1, registerNumberLastIndex);
			}
		}
		
		putValue("Starting or Ending Bank", starting == true ? "0" : "1");
		putValue("Identification Number", registerNumber);
		putValue("Identification Type", "0"); // doesn't support cashiers
		putValue("Amount", amount);
		
		return this;
	}
}
