package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import com.squareup.connect.Payment;

import vfcorp.Record;
import vfcorp.RecordDetails;

public class LineItemAccountingString extends Record {
	
	private static Map<String,RecordDetails> fields;
	private static int length;
	private static String id;

	static {
		fields = new HashMap<String,RecordDetails>();
		length = 148;
		id = "055";
		
		fields.put("Identifier", new RecordDetails(3, 1, ""));
		fields.put("Item Number", new RecordDetails(24, 4, ""));
		fields.put("Non Merchandise Number", new RecordDetails(24, 28, ""));
		fields.put("EGC/Gift Certificate Number", new RecordDetails(20, 52, ""));
		fields.put("Item Value", new RecordDetails(10, 72, ""));
		fields.put("Type Indicator", new RecordDetails(2, 82, "See below"));
		fields.put("Adjust Line Item Quantity", new RecordDetails(1, 84, "0 = No, 1 = Yes"));
		fields.put("Sales Taxable Amount", new RecordDetails(10, 85, "Zero filled"));
		fields.put("Sales Not Taxable Amount", new RecordDetails(10, 95, "Zero filled"));
		fields.put("Sales Taxable Amount 2", new RecordDetails(10, 105, "Zero filled"));
		fields.put("Sales Not Taxable Amount 2", new RecordDetails(10, 115, "Zero filled"));
		fields.put("Return Quantity Indicator", new RecordDetails(2, 125, ""));
		fields.put("Productivity Quantity", new RecordDetails(9, 127, "9(6)V999"));
		fields.put("Item Index", new RecordDetails(7, 136, ""));
		fields.put("Reserved for Future Use", new RecordDetails(6, 143, "Space filled"));
	}
	
	public LineItemAccountingString() {
		super();
	}
	
	public LineItemAccountingString(String record) {
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
	
	public LineItemAccountingString parse(Payment squarePayment) {
		return this;
	}
}
