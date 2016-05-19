package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.FieldDetails;
import vfcorp.Record;

import com.squareup.connect.Payment;

public class TransactionTotal extends Record {
	
	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,FieldDetails>();
		length = 14;
		id = "053";
		
		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Amount", new FieldDetails(10, 4, "zero filled"));
		fields.put("Sign Indicator", new FieldDetails(1, 14, ""));
	}

	public TransactionTotal() {
		super();
	}

	public TransactionTotal(String record) {
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
	
	public TransactionTotal parse(Payment payment) throws Exception {
		putValue("Amount", "" + payment.getTotalCollectedMoney().getAmount());
		// TODO(): needs to be refactored for refunds
		putValue("Sign Indicator", "0"); // sign is always positive
		
		return this;
	}
}
