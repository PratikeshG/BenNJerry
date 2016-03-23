package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import com.squareup.connect.CashDrawerShift;

import vfcorp.Record;
import vfcorp.FieldDetails;

public class CashierRegisterIdentification extends Record {

	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,FieldDetails>();
		length = 11;
		id = "036";
		
		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Cashier/Register Number", new FieldDetails(6, 4, "zero filled"));
		fields.put("Number Type", new FieldDetails(1, 10, "zero filled"));
		fields.put("Currency Indicator", new FieldDetails(1, 11, "zero filled"));
	}
	
	public CashierRegisterIdentification() {
		super();
	}

	public CashierRegisterIdentification(String record) {
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
	
	public CashierRegisterIdentification parse(CashDrawerShift cashDrawerShift) {
		String registerNumber = "";
		
		if (cashDrawerShift.getDevice().getName() != null) {
			registerNumber = cashDrawerShift.getDevice().getName();
		}
		
		putValue("Cashier/Register Number", registerNumber);
		putValue("Number Type", "0"); // 0 is "register"
		putValue("Currency Indicator", "0"); // 0 is "primary"; other currencies not supported
		
		return this;
	}
}
