package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.Record;
import vfcorp.RecordDetails;

public class ReasonCode extends Record {

	public static enum FunctionIndicator {
		PRICE_CORRECT,
		RETURN,
		PAYOUT,
		PAYIN,
		NO_SALE,
		POST_VOID,
		TAX_EXEMPT,
		CASH_A_CHECK,
		MANUAL_CASH_DRAWER_OPEN
	}
	
	private static Map<String,RecordDetails> fields;
	private static Map<FunctionIndicator,String> functionIndicators;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,RecordDetails>();
		length = 14;
		id = "022";
		
		fields.put("Identifier", new RecordDetails(3, 1, ""));
		fields.put("Reason Code", new RecordDetails(8, 4, "left justified"));
		fields.put("Function Indicator", new RecordDetails(2, 12, ""));
		fields.put("Special Indicator", new RecordDetails(1, 14, ""));
		
		functionIndicators.put(FunctionIndicator.PRICE_CORRECT, "01");
		functionIndicators.put(FunctionIndicator.RETURN, "02");
		functionIndicators.put(FunctionIndicator.PAYOUT, "03");
		functionIndicators.put(FunctionIndicator.PAYIN, "04");
		functionIndicators.put(FunctionIndicator.NO_SALE, "05");
		functionIndicators.put(FunctionIndicator.POST_VOID, "06");
		functionIndicators.put(FunctionIndicator.TAX_EXEMPT, "07");
		functionIndicators.put(FunctionIndicator.CASH_A_CHECK, "08");
		functionIndicators.put(FunctionIndicator.MANUAL_CASH_DRAWER_OPEN, "09");
	}
	
	public ReasonCode() {
		super();
	}

	public ReasonCode(String record) {
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
	
	public ReasonCode parse(FunctionIndicator functionIndicator) {
		putValue("Reason Code", ""); // TODO(colinlam): where do these come from?
		putValue("Function Indicator", functionIndicators.get(functionIndicator));
		putValue("Special Indicator", "0"); // not supported
		
		return this;
	}
}
