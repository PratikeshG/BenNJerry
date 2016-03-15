package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.Record;
import vfcorp.RecordDetails;
import vfcorp.TLOG;
import vfcorp.TLOG.TENDER_CODE;

public class Tender extends Record {
	
	private static Map<String,RecordDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,RecordDetails>();
		length = 40;
		id = "061";
		
		fields.put("Identifier", new RecordDetails(3, 1, ""));
		fields.put("Tender Code", new RecordDetails(8, 4, "left justified"));
		fields.put("Tender Amount", new RecordDetails(10, 12, "zero filled"));
		fields.put("Tender Count", new RecordDetails(3, 22, "zero filled"));
		fields.put("Sign Indicator", new RecordDetails(1, 25, "zero filled"));
		fields.put("Currency Indicator", new RecordDetails(1, 26, "zero filled"));
		fields.put("Currency Exchange Rate", new RecordDetails(14, 27, "zero filled"));
	}

	public Tender() {
		super();
	}

	public Tender(String record) {
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
	
	public Tender parse(com.squareup.connect.Tender tender) {
		Map<TENDER_CODE,String> tenderCodes = TLOG.getTenderCodes();
		
		String tenderCode = "";
		if (tender.getType().equals("CASH")) {
			tenderCode = tenderCodes.get(TENDER_CODE.CASH);
		} else if (tender.getType().equals("CREDIT_CARD")) {
			if (tender.getCardBrand().equals("VISA")) {
				tenderCode = tenderCodes.get(TENDER_CODE.VISA);
			} else if (tender.getCardBrand().equals("MASTER_CARD")) {
				tenderCode = tenderCodes.get(TENDER_CODE.MASTERCARD);
			} else if (tender.getCardBrand().equals("AMERICAN_EXPRESS")) {
				tenderCode = tenderCodes.get(TENDER_CODE.AMEX);
			} else if (tender.getCardBrand().equals("DISCOVER")) {
				tenderCode = tenderCodes.get(TENDER_CODE.DISCOVER);
			} else if (tender.getCardBrand().equals("DISCOVER_DINERS")) {
				tenderCode = tenderCodes.get(TENDER_CODE.DISCOVERDINERS);
			} else if (tender.getCardBrand().equals("JCB")) {
				tenderCode = tenderCodes.get(TENDER_CODE.JCB);
			} else {
				tenderCode = tenderCodes.get(TENDER_CODE.UNKNOWN);
			}
		} else {
			tenderCode = tenderCodes.get(TENDER_CODE.UNKNOWN);
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
