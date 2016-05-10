package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import com.squareup.connect.Payment;
import com.squareup.connect.Refund;

import vfcorp.Record;
import vfcorp.FieldDetails;

public class TransactionSubTotal extends Record {
	
	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,FieldDetails>();
		length = 14;
		id = "051";
		
		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Amount", new FieldDetails(10, 4, "zero filled"));
		fields.put("Sign Indicator", new FieldDetails(1, 14, ""));
	}

	public TransactionSubTotal() {
		super();
	}

	public TransactionSubTotal(String record) {
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
	
	public TransactionSubTotal parse(Payment payment, boolean refund) {
		int subtotal = payment.getTotalCollectedMoney().getAmount() - payment.getTaxMoney().getAmount();
		
		putValue("Amount", "" + Math.abs(subtotal));
		putValue("Sign Indicator", (refund ? "1" : "0"));
		
		return this;
	}
	
	public TransactionSubTotal parse(Refund refund) {
		putValue("Amount", "" + Math.abs(refund.getRefundedMoney().getAmount()));
		putValue("Sign Indicator", "1"); // subtotals are always negative
		
		return this;
	}
}
