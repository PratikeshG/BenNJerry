package vfcorp.tlog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vfcorp.Record;
import vfcorp.FieldDetails;

import com.squareup.connect.Merchant;
import com.squareup.connect.Payment;

public class DepositAmount extends Record {

	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,FieldDetails>();
		length = 21;
		id = "038";
		
		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Identification Number", new FieldDetails(6, 4, "zero filled"));
		fields.put("Identification Type", new FieldDetails(1, 10, "zero filled"));
		fields.put("Amount", new FieldDetails(10, 11, "zero filled"));
		fields.put("Currency Indicator", new FieldDetails(1, 21, ""));
	}
	
	public DepositAmount() {
		super();
	}

	public DepositAmount(String record) {
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
	
	public DepositAmount parse(List<Payment> squarePayments, Merchant location) {
		int total = 0;
		for (Payment payment : squarePayments) {
			// TODO(colinlam): this doesn't account for refunds
			total += payment.getNetTotalMoney().getAmount();
		}
		
		String storeNumber = location.getLocationDetails().getNickname() != null ? location.getLocationDetails().getNickname() : "";
		
		putValue("Identification Number", storeNumber);
		putValue("Identification Type", "2"); // default of "store"
		putValue("Amount", "" + total);
		putValue("Currency Indicator", "0");
		
		return this;
	}
}
