package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import com.squareup.connect.Payment;
import com.squareup.connect.PaymentTax;

import vfcorp.Record;
import vfcorp.RecordDetails;

public class TransactionTaxExtended extends Record {

	private static Map<String,RecordDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,RecordDetails>();
		length = 60;
		id = "054";
		
		fields.put("Identifier", new RecordDetails(3, 1, ""));
		fields.put("Tax Type", new RecordDetails(2, 4, ""));
		fields.put("Tax Method", new RecordDetails(2, 6, ""));
		fields.put("Tax Code", new RecordDetails(25, 8, "Left justified, space filled"));
		fields.put("Tax Rate", new RecordDetails(7, 33, "zero filled"));
		fields.put("Taxable Amount", new RecordDetails(10, 40, "zero filled"));
		fields.put("Tax", new RecordDetails(10, 50, "zero filled"));
		fields.put("Sign Indicator", new RecordDetails(1, 60, ""));
	}
	
	public TransactionTaxExtended() {
		super();
	}

	public TransactionTaxExtended(String record) {
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
	
	public TransactionTaxExtended parse(Payment payment, PaymentTax tax) {
		String taxType = "01";
		String taxCode = "";
		switch (tax.getName()) {
			case "Sales Tax":
				taxType = "01";
				break;
			case "VAT":
				taxType = "02";
				break;
			case "GST":
				taxType = "03";
				break;
			case "GST/PST":
				taxType = "04";
				taxCode = "PST";
				break;
			case "PST ON GST":
				taxType = "05";
				break;
			case "No tax":
				taxType = "07";
				break;
			case "HST":
				taxType = "08";
				break;
		}
		
		long taxRate = Math.round(Double.parseDouble(tax.getRate()) * 10000000);
		
		int taxableAmount = payment.getTotalCollectedMoney().getAmount() - payment.getTaxMoney().getAmount();
		
		putValue("Tax Type", taxType);
		putValue("Tax Method", "01"); // not supported
		putValue("Tax Rate", "" + taxRate);
		putValue("Tax Code", taxCode);
		putValue("Taxable Amount", "" + taxableAmount); // TODO(colinlam): this doesn't take into account what kind of tax it is
		putValue("Tax", "" + tax.getAppliedMoney().getAmount());
		putValue("Sign Indicator", "0"); // always adds a positive amount
		
		return this;
	}
}
