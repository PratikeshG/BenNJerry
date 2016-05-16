package vfcorp.tlog;

import java.util.HashMap;
import java.util.Map;

import vfcorp.FieldDetails;
import vfcorp.Record;

public class AuthorizationCode extends Record {

	public static final String FUNCTION_INDICATOR_CASH_A_CHECK = "01";
	public static final String FUNCTION_INDICATOR_NO_SALE = "02";
	public static final String FUNCTION_INDICATOR_PAYIN = "03";
	public static final String FUNCTION_INDICATOR_PAYOUT = "04";
	public static final String FUNCTION_INDICATOR_PICKUP = "05";
	public static final String FUNCTION_INDICATOR_RETURN = "06";
	public static final String FUNCTION_INDICATOR_CLEAR_REGISTER = "07";
	public static final String FUNCTION_INDICATOR_CLEAR_CASHIER = "08";
	public static final String FUNCTION_INDICATOR_ENTER_END_OF_DAY_MODE = "09";
	public static final String FUNCTION_INDICATOR_STOP_POS_DEVICE = "10";
	public static final String FUNCTION_INDICATOR_ENTER_TRAINING_MODE = "11";
	public static final String FUNCTION_INDICATOR_EXIT_TRAINING_MODE = "12";
	public static final String FUNCTION_INDICATOR_LINE_ITEM_DISCOUNT_BY_PERCENT = "13";
	public static final String FUNCTION_INDICATOR_LINE_ITEM_DISCOUNT_BY_AMOUNT = "14";
	public static final String FUNCTION_INDICATOR_GROUP_DISCOUNT_BY_PERCENT = "15";
	public static final String FUNCTION_INDICATOR_GROUP_DISCOUNT_BY_AMOUNT = "16";
	public static final String FUNCTION_INDICATOR_TRANSACTION_DISCOUNT_BY_PERCENT = "17";
	public static final String FUNCTION_INDICATOR_TRANSACTION_DISCOUNT_BY_AMOUNT = "18";
	public static final String FUNCTION_INDICATOR_VOID_AN_ITEM = "19";
	public static final String FUNCTION_INDICATOR_PRICE_CORRECT = "20";
	public static final String FUNCTION_INDICATOR_CASH_TENDER_MIN_MAX_VIOLATIONS = "21";
	public static final String FUNCTION_INDICATOR_NONCASH_TENDER_MIN_MAX_VIOLATIONS = "22";
	public static final String FUNCTION_INDICATOR_POST_VOID = "23";
	public static final String FUNCTION_INDICATOR_EMPLOYEE_TRANSACTION = "24";
	public static final String FUNCTION_INDICATOR_LOGOFF_CASHIER = "25";
	public static final String FUNCTION_INDICATOR_LAYAWAY_DEPOSIT_OVERRIDE = "26";
	public static final String FUNCTION_INDICATOR_SPECIAL_ORDER_DEPOSIT_OVERRIDE = "27";
	public static final String FUNCTION_INDICATOR_CANCEL_TRANSACTION = "28";
	public static final String FUNCTION_INDICATOR_READ_REPORTS = "29";
	public static final String FUNCTION_INDICATOR_GIFT_CERTIFICATE_SALE = "30";
	public static final String FUNCTION_INDICATOR_NEGATIVE_CHECK_OVERRIDE = "31";
	public static final String FUNCTION_INDICATOR_CHANGE_DATE_TIME = "32";
	public static final String FUNCTION_INDICATOR_ENTER_EXIT_REENTRY_MODE = "33";
	public static final String FUNCTION_INDICATOR_FEE_OVERRIDE = "34";
	public static final String FUNCTION_INDICATOR_OPEN_REGISTER = "35";
	public static final String FUNCTION_INDICATOR_EXCEED_TRANSACTION_TOTAL_BY_NONEXCEED_TENDERS = "36";
	public static final String FUNCTION_INDICATOR_TENDER_RETURN = "37";
	public static final String FUNCTION_INDICATOR_DISCOUNT_MIN_MAX = "38";
	public static final String FUNCTION_INDICATOR_DISCOUNT_OVERRIDE = "39";
	public static final String FUNCTION_INDICATOR_DEPOSIT = "40";
	public static final String FUNCTION_INDICATOR_STARTING_BANK = "41";
	public static final String FUNCTION_INDICATOR_HOT_KEY_TO_BACK_OFFICE = "42";
	public static final String FUNCTION_INDICATOR_REPRINT_LAST_RECEIPT = "43";
	public static final String FUNCTION_INDICATOR_GIFT_CARD_ISSUE = "44";
	public static final String FUNCTION_INDICATOR_GIFT_CARD_REISSUE = "45";
	public static final String FUNCTION_INDICATOR_GIFT_CARD_BALANCE_INQUIRY = "46";
	public static final String FUNCTION_INDICATOR_GIFT_CARD_RETURN = "47";
	public static final String FUNCTION_INDICATOR_GIFT_CARD_MIN_MAX = "48";
	public static final String FUNCTION_INDICATOR_RETURN_WITHOUT_VALIDATION = "49";
	public static final String FUNCTION_INDICATOR_UNSCHEDULED_CLOCK_IN = "50";
	public static final String FUNCTION_INDICATOR_RECOVER_FROM_SECURITY_TIMEOUT_WITH_NEW_CASHIER = "51";
	public static final String FUNCTION_INDICATOR_TAX_OVERRIDE = "52";
	public static final String FUNCTION_INDICATOR_REPRINT_GIFT_RECEIPT = "53";
	public static final String FUNCTION_INDICATOR_SEARCH_FOR_TRANSACTION = "54";
	
	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,FieldDetails>();
		length = 24;
		id = "023";
		
		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Authorization Code", new FieldDetails(8, 4, ""));
		fields.put("Function Indicator", new FieldDetails(2, 12, ""));
		fields.put("Employee Number", new FieldDetails(11, 14, "right justified, zero filled"));
	}
	
	public AuthorizationCode() {
		super();
	}

	public AuthorizationCode(String record) {
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
	
	public AuthorizationCode parse(String employeeNumber) throws Exception {
		// TODO(colinlam): need to decipher the function indicator here.
		// Is this a line item or transaction discount?
		// Is it by percentage or amoount?
		String functionIndicator = FUNCTION_INDICATOR_TRANSACTION_DISCOUNT_BY_AMOUNT;
		
		putValue("Authorization Code", ""); // not supported
		putValue("Function Indicator", functionIndicator); // several different supported types
		putValue("Employee Number", employeeNumber);
		
		return this;
	}
}
