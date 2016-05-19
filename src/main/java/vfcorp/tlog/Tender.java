package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.FieldDetails;
import vfcorp.Record;

public class Tender extends Record {
	
	public static final String TENDER_CODE_CASH = "1";
	public static final String TENDER_CODE_CHEQUE = "2";
	public static final String TENDER_CODE_STORE_CREDIT = "4";
	public static final String TENDER_CODE_TRAVELERS_CHEQUE = "5";
	public static final String TENDER_CODE_GIFT_CERTIFICATE = "6";
	public static final String TENDER_CODE_VISA = "7";
	public static final String TENDER_CODE_MASTERCARD = "9";
	public static final String TENDER_CODE_AMEX = "11";
	public static final String TENDER_CODE_MALL_GC = "12";
	public static final String TENDER_CODE_DISCOVER = "13";
	public static final String TENDER_CODE_JCB = "14";
	public static final String TENDER_CODE_DISCOVERDINERS = "17";
	public static final String TENDER_CODE_DEBIT = "19";
	public static final String TENDER_CODE_MAIL_CHEQUE = "20";
	public static final String TENDER_CODE_EGC = "30";
	public static final String TENDER_CODE_98 = "98";
	public static final String TENDER_CODE_ECHECK = "99";
	
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
	
	public Tender parse(com.squareup.connect.Tender tender) throws Exception {
		String tenderCode = "";
		if (tender.getType().equals("CASH")) {
			tenderCode = TENDER_CODE_CASH;
		} else if (tender.getType().equals("CREDIT_CARD")) {
			if (tender.getCardBrand().equals("VISA")) {
				tenderCode = TENDER_CODE_VISA;
			} else if (tender.getCardBrand().equals("MASTER_CARD")) {
				tenderCode = TENDER_CODE_MASTERCARD;
			} else if (tender.getCardBrand().equals("AMERICAN_EXPRESS")) {
				tenderCode = TENDER_CODE_AMEX;
			} else if (tender.getCardBrand().equals("DISCOVER")) {
				tenderCode = TENDER_CODE_DISCOVER;
			} else if (tender.getCardBrand().equals("DISCOVER_DINERS")) {
				tenderCode = TENDER_CODE_DISCOVERDINERS;
			} else if (tender.getCardBrand().equals("JCB")) {
				tenderCode = TENDER_CODE_JCB;
			} else {
				tenderCode = TENDER_CODE_ECHECK;
			}
		} else {
			tenderCode = TENDER_CODE_ECHECK;
		}
		
		String tenderAmount = "";
		if (tender.getTenderedMoney() == null) {
			tenderAmount = "" + tender.getTotalMoney().getAmount();
		} else {
			tenderAmount = "" + tender.getTenderedMoney().getAmount();
		}
		
		putValue("Tender Code", tenderCode);
		putValue("Tender Amount", tenderAmount);
		putValue("Tender Count", "");
		// TODO(): needs to be refactored for refunds
		putValue("Sign Indicator", "0"); // always positive
		putValue("Currency Indicator", "0");
		putValue("Currency Exchange Rate", ""); // not supported
		
		return this;
	}
}
