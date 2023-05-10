package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.FieldDetails;
import vfcorp.Record;

import com.squareup.connect.Payment;
import com.squareup.connect.v2.Order;

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

	public TransactionSubTotal parse(Payment payment) throws Exception {
		int subtotal = payment.getNetSalesMoney().getAmount();

		putValue("Amount", "" + subtotal);
		// TODO(): needs to be refactored for refunds
		putValue("Sign Indicator", "0"); // always positive

		return this;
	}

	public TransactionSubTotal parse(Order order) throws Exception {
		int totalMoney = order.getTotalMoney() != null ? order.getTotalMoney().getAmount() : 0;
    	int totalTaxMoney = order.getTotalTaxMoney() != null ? order.getTotalTaxMoney().getAmount() : 0;
    	int totalTipMoney = order.getTotalTipMoney() != null ? order.getTotalTipMoney().getAmount() : 0;
        int subtotal = totalMoney - totalTaxMoney - totalTipMoney;

		putValue("Amount", "" + subtotal);
		// TODO(): needs to be refactored for refunds
		putValue("Sign Indicator", "0"); // always positive

		return this;
	}
}
