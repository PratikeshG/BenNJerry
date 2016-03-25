package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.Record;
import vfcorp.FieldDetails;
import vfcorp.TLOG;
import vfcorp.TLOG.TenderCode;

public class Tender extends Record {
	
	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,FieldDetails>();
		length = 40;
		id = "061";
		
		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Tender Code", new FieldDetails(8, 4, "left justified"));
		fields.put("Tender Amount", new FieldDetails(10, 12, "zero filled"));
		fields.put("Tender Count", new FieldDetails(3, 22, "zero filled"));
		fields.put("Sign Indicator", new FieldDetails(1, 25, "zero filled"));
		fields.put("Currency Indicator", new FieldDetails(1, 26, "zero filled"));
		fields.put("Currency Exchange Rate", new FieldDetails(14, 27, "zero filled"));
	}

	public Tender() {
		super();
	}

	public Tender(String record) {
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
	
	public Tender parse(com.squareup.connect.Tender tender) {
		Map<TenderCode,String> tenderCodes = TLOG.getTenderCodes();
		
		String tenderCode = "";
		if (tender.getType().equals("CASH")) {
			tenderCode = tenderCodes.get(TenderCode.CASH);
		} else if (tender.getType().equals("CREDIT_CARD")) {
			if (tender.getCardBrand().equals("VISA")) {
				tenderCode = tenderCodes.get(TenderCode.VISA);
			} else if (tender.getCardBrand().equals("MASTER_CARD")) {
				tenderCode = tenderCodes.get(TenderCode.MASTERCARD);
			} else if (tender.getCardBrand().equals("AMERICAN_EXPRESS")) {
				tenderCode = tenderCodes.get(TenderCode.AMEX);
			} else if (tender.getCardBrand().equals("DISCOVER")) {
				tenderCode = tenderCodes.get(TenderCode.DISCOVER);
			} else if (tender.getCardBrand().equals("DISCOVER_DINERS")) {
				tenderCode = tenderCodes.get(TenderCode.DISCOVERDINERS);
			} else if (tender.getCardBrand().equals("JCB")) {
				tenderCode = tenderCodes.get(TenderCode.JCB);
			} else {
				tenderCode = tenderCodes.get(TenderCode.UNKNOWN);
			}
		} else {
			tenderCode = tenderCodes.get(TenderCode.UNKNOWN);
		}
		
		String tenderAmount = "";
		if (tender.getTenderedMoney() == null)
			tenderAmount = "" + tender.getTotalMoney().getAmount();
		else
			tenderAmount = "" + tender.getTenderedMoney().getAmount();
		
		putValue("Tender Code", tenderCode);
		putValue("Tender Amount", tenderAmount);
		putValue("Tender Count", "");
		putValue("Sign Indicator", "0"); // sign is always positive
		putValue("Currency Indicator", "0");
		putValue("Currency Exchange Rate", ""); // not supported
		
		return this;
	}
}
