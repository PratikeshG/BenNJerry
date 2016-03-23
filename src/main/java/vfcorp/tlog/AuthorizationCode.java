package vfcorp.tlog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.squareup.connect.CashDrawerShift;
import com.squareup.connect.Employee;

import vfcorp.Record;
import vfcorp.FieldDetails;

public class AuthorizationCode extends Record {

	public static enum FunctionIndicator {
		CASH_A_CHECK,
		NO_SALE,
		PAYIN,
		PAYOUT,
		PICKUP,
		RETURN,
		CLEAR_REGISTER,
		CLEAR_CASHIER,
		ENTER_END_OF_DAY_MODE,
		STOP_POS_DEVICE,
		ENTER_TRAINING_MODE,
		EXIT_TRAINING_MODE,
		LINE_ITEM_DISCOUNT_BY_PERCENT,
		LINE_ITEM_DISCOUNT_BY_AMOUNT,
		GROUP_DISCOUNT_BY_PERCENT,
		GROUP_DISCOUNT_BY_AMOUNT,
		TRANSACTION_DISCOUNT_BY_PERCENT,
		TRANSACTION_DISCOUNT_BY_AMOUNT,
		VOID_AN_ITEM,
		PRICE_CORRECT,
		CASH_TENDER_MIN_MAX_VIOLATIONS,
		NONCASH_TENDER_MIN_MAX_VIOLATIONS,
		POST_VOID,
		EMPLOYEE_TRANSACTION,
		LOGOFF_CASHIER,
		LAYAWAY_DEPOSIT_OVERRIDE,
		SPECIAL_ORDER_DEPOSIT_OVERRIDE,
		CANCEL_TRANSACTION,
		READ_REPORTS,
		GIFT_CERTIFICATE_SALE,
		NEGATIVE_CHECK_OVERRIDE,
		CHANGE_DATE_TIME,
		ENTER_EXIT_REENTRY_MODE,
		FEE_OVERRIDE,
		OPEN_REGISTER,
		EXCEED_TRANSACTION_TOTAL_BY_NONEXCEED_TENDERS,
		TENDER_RETURN,
		DISCOUNT_MIN_MAX,
		DISCOUNT_OVERRIDE,
		DEPOSIT,
		STARTING_BANK,
		HOT_KEY_TO_BACK_OFFICE,
		REPRINT_LAST_RECEIPT,
		GIFT_CARD_ISSUE,
		GIFT_CARD_REISSUE,
		GIFT_CARD_BALANCE_INQUIRY,
		GIFT_CARD_RETURN,
		GIFT_CARD_MIN_MAX,
		RETURN_WITHOUT_VALIDATION,
		UNSCHEDULED_CLOCK_IN,
		RECOVER_FROM_SECURITY_TIMEOUT_WITH_NEW_CASHIER,
		TAX_OVERRIDE,
		REPRINT_GIFT_RECEIPT,
		SEARCH_FOR_TRANSACTION
	}
	
	private static Map<String,FieldDetails> fields;
	private static Map<FunctionIndicator,String> functionIndicators;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,FieldDetails>();
		functionIndicators = new HashMap<FunctionIndicator,String>();
		length = 24;
		id = "023";
		
		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Authorization Code", new FieldDetails(8, 4, ""));
		fields.put("Function Indicator", new FieldDetails(2, 12, ""));
		fields.put("Employee Number", new FieldDetails(11, 14, "right justified, zero filled"));
		
		functionIndicators.put(FunctionIndicator.CASH_A_CHECK, "01");
		functionIndicators.put(FunctionIndicator.NO_SALE, "02");
		functionIndicators.put(FunctionIndicator.PAYIN, "03");
		functionIndicators.put(FunctionIndicator.PAYOUT, "04");
		functionIndicators.put(FunctionIndicator.PICKUP, "05");
		functionIndicators.put(FunctionIndicator.RETURN, "06");
		functionIndicators.put(FunctionIndicator.CLEAR_REGISTER, "07");
		functionIndicators.put(FunctionIndicator.CLEAR_CASHIER, "08");
		functionIndicators.put(FunctionIndicator.ENTER_END_OF_DAY_MODE, "09");
		functionIndicators.put(FunctionIndicator.STOP_POS_DEVICE, "10");
		functionIndicators.put(FunctionIndicator.ENTER_TRAINING_MODE, "11");
		functionIndicators.put(FunctionIndicator.EXIT_TRAINING_MODE, "12");
		functionIndicators.put(FunctionIndicator.LINE_ITEM_DISCOUNT_BY_PERCENT, "13");
		functionIndicators.put(FunctionIndicator.LINE_ITEM_DISCOUNT_BY_AMOUNT, "14");
		functionIndicators.put(FunctionIndicator.GROUP_DISCOUNT_BY_PERCENT, "15");
		functionIndicators.put(FunctionIndicator.GROUP_DISCOUNT_BY_AMOUNT, "16");
		functionIndicators.put(FunctionIndicator.TRANSACTION_DISCOUNT_BY_PERCENT, "17");
		functionIndicators.put(FunctionIndicator.TRANSACTION_DISCOUNT_BY_AMOUNT, "18");
		functionIndicators.put(FunctionIndicator.VOID_AN_ITEM, "19");
		functionIndicators.put(FunctionIndicator.PRICE_CORRECT, "20");
		functionIndicators.put(FunctionIndicator.CASH_TENDER_MIN_MAX_VIOLATIONS, "21");
		functionIndicators.put(FunctionIndicator.NONCASH_TENDER_MIN_MAX_VIOLATIONS, "22");
		functionIndicators.put(FunctionIndicator.POST_VOID, "23");
		functionIndicators.put(FunctionIndicator.EMPLOYEE_TRANSACTION, "24");
		functionIndicators.put(FunctionIndicator.LOGOFF_CASHIER, "25");
		functionIndicators.put(FunctionIndicator.LAYAWAY_DEPOSIT_OVERRIDE, "26");
		functionIndicators.put(FunctionIndicator.SPECIAL_ORDER_DEPOSIT_OVERRIDE, "27");
		functionIndicators.put(FunctionIndicator.CANCEL_TRANSACTION, "28");
		functionIndicators.put(FunctionIndicator.READ_REPORTS, "29");
		functionIndicators.put(FunctionIndicator.GIFT_CERTIFICATE_SALE, "30");
		functionIndicators.put(FunctionIndicator.NEGATIVE_CHECK_OVERRIDE, "31");
		functionIndicators.put(FunctionIndicator.CHANGE_DATE_TIME, "32");
		functionIndicators.put(FunctionIndicator.ENTER_EXIT_REENTRY_MODE, "33");
		functionIndicators.put(FunctionIndicator.FEE_OVERRIDE, "34");
		functionIndicators.put(FunctionIndicator.OPEN_REGISTER, "35");
		functionIndicators.put(FunctionIndicator.EXCEED_TRANSACTION_TOTAL_BY_NONEXCEED_TENDERS, "36");
		functionIndicators.put(FunctionIndicator.TENDER_RETURN, "37");
		functionIndicators.put(FunctionIndicator.DISCOUNT_MIN_MAX, "38");
		functionIndicators.put(FunctionIndicator.DISCOUNT_OVERRIDE, "39");
		functionIndicators.put(FunctionIndicator.DEPOSIT, "40");
		functionIndicators.put(FunctionIndicator.STARTING_BANK, "41");
		functionIndicators.put(FunctionIndicator.HOT_KEY_TO_BACK_OFFICE, "42");
		functionIndicators.put(FunctionIndicator.REPRINT_LAST_RECEIPT, "43");
		functionIndicators.put(FunctionIndicator.GIFT_CARD_ISSUE, "44");
		functionIndicators.put(FunctionIndicator.GIFT_CARD_REISSUE, "45");
		functionIndicators.put(FunctionIndicator.GIFT_CARD_BALANCE_INQUIRY, "46");
		functionIndicators.put(FunctionIndicator.GIFT_CARD_RETURN, "47");
		functionIndicators.put(FunctionIndicator.GIFT_CARD_MIN_MAX, "48");
		functionIndicators.put(FunctionIndicator.RETURN_WITHOUT_VALIDATION, "49");
		functionIndicators.put(FunctionIndicator.UNSCHEDULED_CLOCK_IN, "50");
		functionIndicators.put(FunctionIndicator.RECOVER_FROM_SECURITY_TIMEOUT_WITH_NEW_CASHIER, "51");
		functionIndicators.put(FunctionIndicator.TAX_OVERRIDE, "52");
		functionIndicators.put(FunctionIndicator.REPRINT_GIFT_RECEIPT, "53");
		functionIndicators.put(FunctionIndicator.SEARCH_FOR_TRANSACTION, "54");
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
	
	public AuthorizationCode parse(List<Employee> squareEmployees, CashDrawerShift cashDrawerShift, FunctionIndicator functionIndicator) {
		String employeeNumber = "";
		
		if (cashDrawerShift.getOpeningEmployeeId() != null) {
			for (Employee squareEmployee : squareEmployees) {
				if (squareEmployee.getId().equals(cashDrawerShift.getOpeningEmployeeId())) {
					employeeNumber = squareEmployee.getExternalId();
				}
			}
		}
		
		
		putValue("Authorization Code", ""); // not supported
		putValue("Function Indicator", functionIndicators.get(functionIndicator)); // several different supported types
		putValue("Employee Number", employeeNumber);
		
		return this;
	}
}
