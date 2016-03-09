package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import com.squareup.connect.Payment;

import vfcorp.Record;
import vfcorp.RecordDetails;

public class TransactionTotal extends Record {
	
	private static Map<String,RecordDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,RecordDetails>();
		length = 14;
		id = "053";
		
		fields.put("Identifier", new RecordDetails(3, 1, ""));
		fields.put("Amount", new RecordDetails(10, 4, "zero filled"));
		fields.put("Sign Indicator", new RecordDetails(1, 14, ""));
	}

	public TransactionTotal() {
		super();
	}

	public TransactionTotal(String record) {
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
	
	public TransactionTotal parse(Payment payment) {
		values.put("Amount", "" + payment.getTotalCollectedMoney().getAmount());
		values.put("Sign Indicator", "0"); // subtotals are always positive
		
		return this;
	}
}
