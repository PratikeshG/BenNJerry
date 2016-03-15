package vfcorp.tlog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.squareup.connect.Payment;

import vfcorp.Record;
import vfcorp.RecordDetails;

public class ForInStoreReportingUseOnly extends Record {

	private static Map<String,RecordDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,RecordDetails>();
		length = 24;
		id = "037";
		
		fields.put("Identifier", new RecordDetails(3, 1, ""));
		fields.put("Transaction Identifier", new RecordDetails(3, 4, ""));
		fields.put("Count", new RecordDetails(6, 7, "zero filled"));
		fields.put("Amount", new RecordDetails(10, 13, "zero filled"));
		fields.put("Amount Sign", new RecordDetails(1, 23, "1 = Negative"));
		fields.put("Currency Indicator", new RecordDetails(1, 24, "1 = Alternate, 0 = Primary"));
	}
	
	public ForInStoreReportingUseOnly() {
		super();
	}

	public ForInStoreReportingUseOnly(String record) {
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
	
	public ForInStoreReportingUseOnly parse(String transactionIdentifier, List<Payment> squarePayments) {
		int count = 0;
		int amount = 0;
		
		if ("002".equals(transactionIdentifier)) {
			count = squarePayments.size();
			for (Payment payment : squarePayments) {
				amount += payment.getNetTotalMoney().getAmount();
			}
		} else if ("003".equals(transactionIdentifier)) {
			// TODO(colinlam): do this
		} else if ("009".equals(transactionIdentifier)) {
			for (Payment payment : squarePayments) {
				if (payment.getDiscountMoney().getAmount() > 0) {
					count += 1;
					amount += payment.getDiscountMoney().getAmount();
				}
			}
		} else if ("013".equals(transactionIdentifier)) {
			for (Payment payment : squarePayments) {
				if (payment.getTaxMoney().getAmount() > 0) {
					count += 1;
					amount += payment.getTaxMoney().getAmount();
				}
			}
		} else if ("014".equals(transactionIdentifier)) {
			// TODO(colinlam): what is the difference between net sales and merchandise sales?
		} else if ("015".equals(transactionIdentifier)) {
			// TODO(colinlam): what is the difference between returns and merchandise returns?
		} else if ("017".equals(transactionIdentifier)) {
			// TODO(colinlam): what is taxable sales?
		} else if ("018".equals(transactionIdentifier)) {
			// TODO(colinlam): what is non-taxable sales?
		} else if ("036".equals(transactionIdentifier)) {
			// TODO(colinlam): what is a transaction discount?
		}
		
		values.put("Transaction Identifier", transactionIdentifier);
		values.put("Count", "" + count);
		values.put("Amount", "" + amount);
		values.put("Amount Sign", amount >= 0 ? "0" : "1");
		values.put("Currency Indicator", "0"); // 0 is primary; other value not supported
		
		return this;
	}
}
