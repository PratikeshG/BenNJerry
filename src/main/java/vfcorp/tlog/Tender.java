package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.Record;
import vfcorp.RecordDetails;

public class Tender extends Record {
	
	public static enum TENDER_CODE {
		CASH, VISA, MASTERCARD, AMEX, DISCOVER, DISCOVERDINERS, JCB, DEBIT, CHECK, EGC, UNKNOWN
	}
	
	private static Map<String,RecordDetails> fields;
	private static Map<TENDER_CODE,String> tenderCodes;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,RecordDetails>();
		tenderCodes = new HashMap<TENDER_CODE,String>();
		length = 40;
		id = "061";
		
		fields.put("Identifier", new RecordDetails(3, 1, ""));
		fields.put("Tender Code", new RecordDetails(8, 4, ""));
		fields.put("Tender Amount", new RecordDetails(10, 12, "zero filled"));
		fields.put("Tender Count", new RecordDetails(3, 22, "zero filled"));
		fields.put("Sign Indicator", new RecordDetails(1, 25, "zero filled"));
		fields.put("Currency Indicator", new RecordDetails(1, 26, "zero filled"));
		fields.put("Currency Exchange Rate", new RecordDetails(14, 27, "zero filled"));
		
		// TODO(colinlam): these were found by examining the sample given to us. Seems like it can
		// be configured, though. Need to verify.
		tenderCodes.put(TENDER_CODE.CASH, "1");
		tenderCodes.put(TENDER_CODE.VISA, "7");
		tenderCodes.put(TENDER_CODE.MASTERCARD, "9");
		tenderCodes.put(TENDER_CODE.AMEX, "11");
		tenderCodes.put(TENDER_CODE.DISCOVER, "13");
		tenderCodes.put(TENDER_CODE.DEBIT, "19");
		tenderCodes.put(TENDER_CODE.EGC, "30");
		
		// TODO(colinlam): This is a guess. There doesn't seem to be one for JCB. Find this out.
		tenderCodes.put(TENDER_CODE.JCB, "15");
		tenderCodes.put(TENDER_CODE.DISCOVERDINERS, "17");
		tenderCodes.put(TENDER_CODE.UNKNOWN, "99");
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
	
	public Map<TENDER_CODE,String> getTenderCodes() {
		return tenderCodes;
	}
	
	public Tender parse(com.squareup.connect.Tender tender) {
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
		
		values.put("Tender Code", tenderCode);
		values.put("Tender Amount", tenderAmount);
		values.put("Tender Count", "");
		values.put("Sign Indicator", "0"); // sign is always positive
		values.put("Currency Indicator", "0"); // TODO(colinlam): this is supported; figure it out.
		values.put("Currency Exchange Rate", ""); // not supported
		
		return this;
	}
}
