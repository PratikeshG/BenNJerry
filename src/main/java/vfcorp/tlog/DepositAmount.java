package vfcorp.tlog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vfcorp.Record;
import vfcorp.RecordDetails;

import com.squareup.connect.Merchant;
import com.squareup.connect.Payment;

public class DepositAmount extends Record {

	private static Map<String,RecordDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,RecordDetails>();
		length = 21;
		id = "038";
		
		fields.put("Identifier", new RecordDetails(3, 1, ""));
		fields.put("Identification Number", new RecordDetails(6, 4, "zero filled"));
		fields.put("Identification Type", new RecordDetails(1, 10, "zero filled"));
		fields.put("Amount", new RecordDetails(10, 11, "zero filled"));
		fields.put("Currency Indicator", new RecordDetails(1, 21, ""));
	}
	
	public DepositAmount() {
		super();
	}

	public DepositAmount(String record) {
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
	
	public DepositAmount parse(List<Payment> squarePayments, Merchant location) {
		int total = 0;
		for (Payment payment : squarePayments) {
			// TODO(colinlam): this doesn't account for refunds
			total += payment.getNetTotalMoney().getAmount();
		}
		
		String storeNumber = location.getLocationDetails().getNickname() != null ? location.getLocationDetails().getNickname() : "";
		
		values.put("Identification Number", storeNumber);
		values.put("Identification Type", "2"); // default of "store"
		values.put("Amount", "" + total);
		values.put("Currency Indicator", "0");
		
		return this;
	}
}
