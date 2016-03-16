package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import com.squareup.connect.Payment;
import com.squareup.connect.Refund;

import vfcorp.Record;
import vfcorp.FieldDetails;

public class TransactionTax extends Record {

	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,FieldDetails>();
		length = 14;
		id = "052";
		
		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Amount", new FieldDetails(10, 4, "zero filled"));
		fields.put("Sign Indicator", new FieldDetails(1, 14, "zero filled"));
	}
	
	public TransactionTax() {
		super();
	}

	public TransactionTax(String record) {
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
	
	public TransactionTax parse(Payment payment, boolean refund) {
		putValue("Amount", "" + Math.abs(payment.getTaxMoney().getAmount()));
		putValue("Sign Indicator", (refund ? "1" : "0"));
		
		return this;
	}
	
	public TransactionTax parse(Refund refund) {
		putValue("Amount", "");
		putValue("Sign Indicator", "");
		
		return this;
	}
}
