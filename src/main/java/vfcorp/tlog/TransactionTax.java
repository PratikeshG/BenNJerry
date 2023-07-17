package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.FieldDetails;
import vfcorp.Record;

import com.squareup.connect.v2.Order;

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

	public TransactionTax parse(Order order) throws Exception {
		int tax = order.getTotalTaxMoney() != null ? order.getTotalTaxMoney().getAmount() : 0;
		putValue("Amount", "" + tax);
		// TODO(): needs to be refactored for refunds
		putValue("Sign Indicator", "0"); // always positive

		return this;
	}
}
