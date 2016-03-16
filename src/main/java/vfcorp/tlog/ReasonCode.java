package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.Record;
import vfcorp.FieldDetails;

public class ReasonCode extends Record {

	public static final String FUNCTION_INDICATOR_PRICE_CORRECT = "01";
	public static final String FUNCTION_INDICATOR_RETURN = "02";
	public static final String FUNCTION_INDICATOR_PAYOUT = "03";
	public static final String FUNCTION_INDICATOR_PAYIN = "04";
	public static final String FUNCTION_INDICATOR_NO_SALE = "05";
	public static final String FUNCTION_INDICATOR_POST_VOID = "06";
	public static final String FUNCTION_INDICATOR_TAX_EXEMPT = "07";
	public static final String FUNCTION_INDICATOR_CASH_A_CHECK = "08";
	public static final String FUNCTION_INDICATOR_MANUAL_CASH_DRAWER_OPEN = "09";
	
	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,FieldDetails>();
		length = 14;
		id = "022";
		
		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Reason Code", new FieldDetails(8, 4, "left justified"));
		fields.put("Function Indicator", new FieldDetails(2, 12, ""));
		fields.put("Special Indicator", new FieldDetails(1, 14, ""));
	}
	
	public ReasonCode() {
		super();
	}

	public ReasonCode(String record) {
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
	
	public ReasonCode parse(String functionIndicator) {
		putValue("Reason Code", ""); // TODO(colinlam): where do these come from?
		putValue("Function Indicator", functionIndicator);
		putValue("Special Indicator", "0"); // not supported
		
		return this;
	}
}
