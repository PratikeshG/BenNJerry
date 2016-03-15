package vfcorp.tlog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.squareup.connect.CashDrawerShift;
import com.squareup.connect.Employee;
import com.squareup.connect.Merchant;
import com.squareup.connect.Payment;
import com.squareup.connect.Tender;

import vfcorp.Record;
import vfcorp.RecordDetails;

public class TransactionHeader extends Record {
	
	public static enum TransactionType {
		STORE_OPEN,
		CASHIER_LOGOFF,
		STORE_CLOSE,
		MACHINE_STARTED_FOR_THE_DAY,
		ENTER_TRAINING_MODE,
		EXIT_TRAINING_MODE,
		START_REENTRY_MODE,
		EXIT_REENTRY_MODE,
		CLEAR_REGISTER_TOTALS,
		CLEAR_CASHIER_TOTALS,
		READ_REGISTER_TOTALS,
		READ_CASHIER_TOTALS,
		READ_ASSOCIATE_TOTALS,
		READ_HOURLY_PRODUCTIVITY_TOTALS,
		READ_DEPARTMENT_TOTALS,
		READ_DEPARTMENT_CLASS_TOTALS,
		SUSPEND_SALE,
		SUSPEND_LAYAWAY_SALE,
		SUSPEND_SPECIAL_ORDER,
		SUSPEND_SEND_SALE,
		SUSPEND_RETURN,
		RESUME_SALE,
		RESUME_LAYAWAY_SALE,
		RESUME_SPECIAL_ORDER,
		RESUME_SEND_SALE,
		RESUME_RETURN,
		POST_VOID_SALE,
		POST_VOID_LAYAWAY_SALE,
		POST_VOID_LAYAWAY_PAYMENT,
		POST_VOID_LAYAWAY_ADJUSTMENT,
		POST_VOID_LAYAWAY_PARTIAL_PICKUP,
		POST_VOID_LAYAWAY_FINAL,
		POST_VOID_LAYAWAY_CANCEL,
		POST_VOID_LAYAWAY_RESTOCK,
		POST_VOID_SPECIAL_ORDER,
		POST_VOID_SPECIAL_ORDER_REMOVE_ITEMS,
		POST_VOID_SPECIAL_ORDER_PARTIAL_PICKUP,
		POST_VOID_SPECIAL_ORDER_FINAL,
		POST_VOID_SPECIAL_ORDER_CANCEL,
		POST_VOID_SPECIAL_ORDER_RESTOCK,
		POST_VOID_RETURN,
		POST_VOID_SEND_SALE,
		POST_VOID_ON_ACCOUNT_PAYMENT,
		POST_VOID_PICKUP,
		POST_VOID_PAYOUT,
		POST_VOID_PAYIN,
		POST_VOID_BUY_BACK_STORE_CREDIT,
		POST_VOID_CASH_A_CHECK,
		POST_VOID_INTERIM_BANK_DEPOSIT,
		SALE,
		RETURN,
		LAYAWAY_SALE,
		LAYAWAY_PAYMENT,
		LAYAWAY_ADJUSTMENT,
		LAYAWAY_PARTIAL_PICKUP,
		LAYAWAY_FINAL,
		LAYAWAY_CANCEL,
		LAYAWAY_RESTOCK,
		SPECIAL_ORDER,
		SPECIAL_ORDER_ADJUST,
		SPECIAL_ORDER_PARTIAL_PICKUP,
		SPECIAL_ORDER_FINAL,
		SPECIAL_ORDER_CANCEL,
		SPECIAL_ORDER_RESTOCK,
		SEND_SALE,
		REPRINT_RECEIPT,
		EGC_BALANCE_INQUIRY,
		TENDER_COUNT_REGISTER,
		TENDER_COUNT_CASHIER,
		CASHOUT_DEPOSIT_AMOUNT,
		CHANGE_DATE_TIME,
		STARTING_BANK,
		INTERIM_BANK_DEPOSIT,
		ZIP_POSTAL_CODE,
		USER_STOP_POS,
		ABNORMAL_TERMINATION_OF_POS,
		HOTKEY_TO_BACKOFFICE,
		TRANSACTION_NUMBER_CONSUMED_WITHOUT_SA,
		OPEN_REGISTER,
		VOID_RENTAL,
		PICKUP,
		PAYOUT,
		BUY_BACK_STORE_CREDIT,
		CASH_A_CHECK,
		PAYIN,
		ON_ACCOUNT_PAYMENT,
		NO_SALE,
		MANUAL_CASH_DRAWER_OPEN,
		USER_TRANSACTION
	}
	
	private static Map<String,RecordDetails> fields;
	private static Map<TransactionType,String> transactionTypes;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,RecordDetails>();
		transactionTypes = new HashMap<TransactionType,String>();
		length = 162;
		id = "000";
		
		fields.put("Identifier", new RecordDetails(3, 1, ""));
		fields.put("Store Number", new RecordDetails(5, 4, "Zero filled, right justified"));
		fields.put("Register Number", new RecordDetails(3, 9, "zero filled"));
		fields.put("Cashier Number", new RecordDetails(6, 12, "Zero filled, right justified"));
		fields.put("Employee Number", new RecordDetails(11, 18, "zero filled"));
		fields.put("Transaction Number", new RecordDetails(6, 29, ""));
		fields.put("Transaction Date", new RecordDetails(8, 35, "MMDDYYYY, zero filled"));
		fields.put("Transaction Time", new RecordDetails(4, 43, "HHMM – Military, zero filled"));
		fields.put("Transaction Type", new RecordDetails(3, 47, ""));
		fields.put("Transaction Status", new RecordDetails(2, 50, ""));
		fields.put("Cancel Indicator", new RecordDetails(1, 52, "1 = Cancelled"));
		fields.put("Post Void Indicator", new RecordDetails(1, 53, "1 = Post Void"));
		fields.put("Tax Exempt Indicator", new RecordDetails(1, 54, "1 = Tax Exempt"));
		fields.put("Training Indicator", new RecordDetails(1, 55, "1 = Training"));
		fields.put("User Data", new RecordDetails(3, 56, ""));
		fields.put("Transaction Processor Attempts", new RecordDetails(2, 59, ""));
		fields.put("Transaction Error Code", new RecordDetails(4, 61, "zero filled"));
		fields.put("Number of Records", new RecordDetails(8, 65, "Detail records + header, zero filled, right justified"));
		fields.put("Business Date", new RecordDetails(8, 73, "MMDDYYYY, zero filled"));
		fields.put("RetailStore Product Generation", new RecordDetails(1, 81, "zero filled"));
		fields.put("RetailStore Major Version", new RecordDetails(1, 82, "zero filled"));
		fields.put("RetailStore Minor Version", new RecordDetails(2, 83, "Zero filled, right justified"));
		fields.put("RetailStore Service Pack", new RecordDetails(2, 85, "Zero filled, right justified"));
		fields.put("RetailStore Hot Fix", new RecordDetails(3, 87, "Zero filled, right justified"));
		fields.put("(Customer) Code Release Number", new RecordDetails(3, 90, "Zero filled, right justified"));
		fields.put("(Customer) Code Release EFix", new RecordDetails(3, 93, "Zero filled"));
		fields.put("(Customer) Release Additional Data", new RecordDetails(17, 96, "Left justified, space filled"));
		fields.put("Tax Calculator", new RecordDetails(1, 113, ""));
		fields.put("Reserved for Future Use", new RecordDetails(49, 114, "Space filled"));
		
		transactionTypes.put(TransactionType.STORE_OPEN, "010");
		transactionTypes.put(TransactionType.CASHIER_LOGOFF, "030");
		transactionTypes.put(TransactionType.STORE_CLOSE, "040");
		transactionTypes.put(TransactionType.MACHINE_STARTED_FOR_THE_DAY, "050");
		transactionTypes.put(TransactionType.ENTER_TRAINING_MODE, "060");
		transactionTypes.put(TransactionType.EXIT_TRAINING_MODE, "069");
		transactionTypes.put(TransactionType.START_REENTRY_MODE, "070");
		transactionTypes.put(TransactionType.EXIT_REENTRY_MODE, "079");
		transactionTypes.put(TransactionType.CLEAR_REGISTER_TOTALS, "080");
		transactionTypes.put(TransactionType.CLEAR_CASHIER_TOTALS, "081");
		transactionTypes.put(TransactionType.READ_REGISTER_TOTALS, "090");
		transactionTypes.put(TransactionType.READ_CASHIER_TOTALS, "091");
		transactionTypes.put(TransactionType.READ_ASSOCIATE_TOTALS, "092");
		transactionTypes.put(TransactionType.READ_HOURLY_PRODUCTIVITY_TOTALS, "093");
		transactionTypes.put(TransactionType.READ_DEPARTMENT_TOTALS, "094");
		transactionTypes.put(TransactionType.READ_DEPARTMENT_CLASS_TOTALS, "095");
		transactionTypes.put(TransactionType.SUSPEND_SALE, "100");
		transactionTypes.put(TransactionType.SUSPEND_LAYAWAY_SALE, "101");
		transactionTypes.put(TransactionType.SUSPEND_SPECIAL_ORDER, "102");
		transactionTypes.put(TransactionType.SUSPEND_SEND_SALE, "103");
		transactionTypes.put(TransactionType.SUSPEND_RETURN, "104");
		transactionTypes.put(TransactionType.RESUME_SALE, "110");
		transactionTypes.put(TransactionType.RESUME_LAYAWAY_SALE, "111");
		transactionTypes.put(TransactionType.RESUME_SPECIAL_ORDER, "112");
		transactionTypes.put(TransactionType.RESUME_SEND_SALE, "113");
		transactionTypes.put(TransactionType.RESUME_RETURN, "114");
		transactionTypes.put(TransactionType.POST_VOID_SALE, "120");
		transactionTypes.put(TransactionType.POST_VOID_LAYAWAY_SALE, "121");
		transactionTypes.put(TransactionType.POST_VOID_LAYAWAY_PAYMENT, "122");
		transactionTypes.put(TransactionType.POST_VOID_LAYAWAY_ADJUSTMENT, "123");
		transactionTypes.put(TransactionType.POST_VOID_LAYAWAY_PARTIAL_PICKUP, "124");
		transactionTypes.put(TransactionType.POST_VOID_LAYAWAY_FINAL, "125");
		transactionTypes.put(TransactionType.POST_VOID_LAYAWAY_CANCEL, "126");
		transactionTypes.put(TransactionType.POST_VOID_LAYAWAY_RESTOCK, "127");
		transactionTypes.put(TransactionType.POST_VOID_SPECIAL_ORDER, "128");
		transactionTypes.put(TransactionType.POST_VOID_SPECIAL_ORDER_REMOVE_ITEMS, "129");
		transactionTypes.put(TransactionType.POST_VOID_SPECIAL_ORDER_PARTIAL_PICKUP, "130");
		transactionTypes.put(TransactionType.POST_VOID_SPECIAL_ORDER_FINAL, "131");
		transactionTypes.put(TransactionType.POST_VOID_SPECIAL_ORDER_CANCEL, "132");
		transactionTypes.put(TransactionType.POST_VOID_SPECIAL_ORDER_RESTOCK, "133");
		transactionTypes.put(TransactionType.POST_VOID_RETURN, "134");
		transactionTypes.put(TransactionType.POST_VOID_SEND_SALE, "135");
		transactionTypes.put(TransactionType.POST_VOID_ON_ACCOUNT_PAYMENT, "136");
		transactionTypes.put(TransactionType.POST_VOID_PICKUP, "137");
		transactionTypes.put(TransactionType.POST_VOID_PAYOUT, "138");
		transactionTypes.put(TransactionType.POST_VOID_PAYIN, "139");
		transactionTypes.put(TransactionType.POST_VOID_BUY_BACK_STORE_CREDIT, "140");
		transactionTypes.put(TransactionType.POST_VOID_CASH_A_CHECK, "141");
		transactionTypes.put(TransactionType.POST_VOID_INTERIM_BANK_DEPOSIT, "142");
		transactionTypes.put(TransactionType.SALE, "200");
		transactionTypes.put(TransactionType.RETURN, "201");
		transactionTypes.put(TransactionType.LAYAWAY_SALE, "202");
		transactionTypes.put(TransactionType.LAYAWAY_PAYMENT, "203");
		transactionTypes.put(TransactionType.LAYAWAY_ADJUSTMENT, "204");
		transactionTypes.put(TransactionType.LAYAWAY_PARTIAL_PICKUP, "205");
		transactionTypes.put(TransactionType.LAYAWAY_FINAL, "206");
		transactionTypes.put(TransactionType.LAYAWAY_CANCEL, "207");
		transactionTypes.put(TransactionType.LAYAWAY_RESTOCK, "208");
		transactionTypes.put(TransactionType.SPECIAL_ORDER, "209");
		transactionTypes.put(TransactionType.SPECIAL_ORDER_ADJUST, "210");
		transactionTypes.put(TransactionType.SPECIAL_ORDER_PARTIAL_PICKUP, "211");
		transactionTypes.put(TransactionType.SPECIAL_ORDER_FINAL, "212");
		transactionTypes.put(TransactionType.SPECIAL_ORDER_CANCEL, "213");
		transactionTypes.put(TransactionType.SPECIAL_ORDER_RESTOCK, "214");
		transactionTypes.put(TransactionType.SEND_SALE, "215");
		transactionTypes.put(TransactionType.REPRINT_RECEIPT, "216");
		transactionTypes.put(TransactionType.EGC_BALANCE_INQUIRY, "217");
		transactionTypes.put(TransactionType.TENDER_COUNT_REGISTER, "400");
		transactionTypes.put(TransactionType.TENDER_COUNT_CASHIER, "401");
		transactionTypes.put(TransactionType.CASHOUT_DEPOSIT_AMOUNT, "403");
		transactionTypes.put(TransactionType.CHANGE_DATE_TIME, "501");
		transactionTypes.put(TransactionType.STARTING_BANK, "502");
		transactionTypes.put(TransactionType.INTERIM_BANK_DEPOSIT, "503");
		transactionTypes.put(TransactionType.ZIP_POSTAL_CODE, "598");
		transactionTypes.put(TransactionType.USER_STOP_POS, "599");
		transactionTypes.put(TransactionType.ABNORMAL_TERMINATION_OF_POS, "600");
		transactionTypes.put(TransactionType.HOTKEY_TO_BACKOFFICE, "601");
		transactionTypes.put(TransactionType.TRANSACTION_NUMBER_CONSUMED_WITHOUT_SA, "610");
		transactionTypes.put(TransactionType.OPEN_REGISTER, "699");
		transactionTypes.put(TransactionType.VOID_RENTAL, "710");
		transactionTypes.put(TransactionType.PICKUP, "800");
		transactionTypes.put(TransactionType.PAYOUT, "801");
		transactionTypes.put(TransactionType.BUY_BACK_STORE_CREDIT, "802");
		transactionTypes.put(TransactionType.CASH_A_CHECK, "803");
		transactionTypes.put(TransactionType.PAYIN, "850");
		transactionTypes.put(TransactionType.ON_ACCOUNT_PAYMENT, "851");
		transactionTypes.put(TransactionType.NO_SALE, "900");
		transactionTypes.put(TransactionType.MANUAL_CASH_DRAWER_OPEN, "901");
		transactionTypes.put(TransactionType.USER_TRANSACTION, "902");
	}
	
	public TransactionHeader() {
		super();
	}
	
	public TransactionHeader(String record) {
		super(record);
	}

	@Override
	public Map<String,RecordDetails> getFields() {
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
	
	public TransactionHeader parse(Merchant location, List<Employee> squareEmployees, CashDrawerShift cashDrawerShift, TransactionType transactionType, int numberOfRecords) {
		Map<String,String> params = new HashMap<String,String>();
		
		if (cashDrawerShift.getOpeningEmployeeId() != null) {
			for (Employee squareEmployee : squareEmployees) {
				if (squareEmployee.getId().equals(cashDrawerShift.getOpeningEmployeeId())) {
					params.put("Employee Number", squareEmployee.getExternalId());
				}
			}
		}
		
		if (cashDrawerShift.getDevice().getName() != null) {
			params.put("Register Number", cashDrawerShift.getDevice().getName());
		}
		
		String cashDrawerDate = cashDrawerShift.getOpenedAt().substring(5, 7) +
				cashDrawerShift.getOpenedAt().substring(8, 10) + 
				cashDrawerShift.getOpenedAt().substring(0, 4);
		params.put("Transaction Date", cashDrawerDate);
		String cashDrawerTime = cashDrawerShift.getOpenedAt().substring(11,13) + cashDrawerShift.getOpenedAt().substring(14, 16);
		params.put("Transaction Time", cashDrawerTime);
		
		params.put("Transaction Type", transactionTypes.get(transactionType));
		params.put("Store Number", location.getLocationDetails().getNickname() != null ? location.getLocationDetails().getNickname() : "");
		params.put("Number of Records", "" + numberOfRecords);
		
		return parse(params);
	}
	
	public TransactionHeader parse(Merchant location, List<CashDrawerShift> cashDrawerShifts, TransactionType transactionType, int numberOfRecords) {
		Map<String,String> params = new HashMap<String,String>();
		
		String startDate = "";
		if (transactionType == TransactionType.STORE_OPEN) {
			for (CashDrawerShift cashDrawerShift : cashDrawerShifts) {
				if (startDate.equals("") || startDate.compareTo(cashDrawerShift.getOpenedAt()) > 0) {
					startDate = cashDrawerShift.getOpenedAt();
				}
			}
		} else if (transactionType == TransactionType.STORE_CLOSE) {
			for (CashDrawerShift cashDrawerShift : cashDrawerShifts) {
				if (startDate.equals("") || startDate.compareTo(cashDrawerShift.getClosedAt()) < 0) {
					startDate = cashDrawerShift.getClosedAt();
				}
			}
		}
		if (startDate.equals("")) {
			startDate = "00000000000000000000";
		}
		
		String date = startDate.substring(5, 7) +
				startDate.substring(8, 10) + 
				startDate.substring(0, 4);
		params.put("Transaction Date", date);
		String time = startDate.substring(11,13) + startDate.substring(14, 16);
		params.put("Transaction Time", time);
		
		params.put("Store Number", location.getLocationDetails().getNickname() != null ? location.getLocationDetails().getNickname() : "");
		
		params.put("Transaction Type", transactionTypes.get(transactionType));
		
		params.put("Number of Records", "" + numberOfRecords);
		
		return parse(params);
	}
	
	public TransactionHeader parse(Merchant location, Payment squarePayment, List<Employee> squareEmployees, TransactionType transactionType, int numberOfRecords) {
		Map<String,String> params = new HashMap<String,String>();
		
		String date = squarePayment.getCreatedAt().substring(5, 7) +
				squarePayment.getCreatedAt().substring(8, 10) + 
				squarePayment.getCreatedAt().substring(0, 4);
		params.put("Transaction Date", date);
		String time = squarePayment.getCreatedAt().substring(11,13) + squarePayment.getCreatedAt().substring(14, 16);
		params.put("Transaction Time", time);
		
		for (Tender tender : squarePayment.getTender()) {
			if (tender.getEmployeeId() != null) {
				for (Employee employee : squareEmployees) {
					if (employee.getId().equals(tender.getEmployeeId())) {
						params.put("Employee Number", employee.getExternalId());
					}
				}
			}
		}
		
		params.put("Register Number", squarePayment.getDevice().getName() != null ? squarePayment.getDevice().getName() : "");
		params.put("Store Number", location.getLocationDetails().getNickname() != null ? location.getLocationDetails().getNickname() : "");
		params.put("Transaction Type", transactionTypes.get(transactionType));
		params.put("Number of Records", "" + numberOfRecords);
		params.put("Business Date", date);
		
		return parse(params);
	}
	
	public TransactionHeader parse(Map<String,String> params) {
		putValue("Store Number", params.getOrDefault("Store Number", ""));
		putValue("Register Number", params.getOrDefault("Register Number", "")); // Only if device is named correctly
		putValue("Cashier Number", ""); // What is the difference between a cashier and a register?
		putValue("Employee Number", params.getOrDefault("Employee Number", ""));
		putValue("Transaction Number", "123456"); // TODO(colinlam): Square transaction ID doesn't fit...where to get this?
		putValue("Transaction Date", params.getOrDefault("Transaction Date", "")); // not supported
		putValue("Transaction Time", params.getOrDefault("Transaction Time", "")); // not supported
		putValue("Transaction Type", params.getOrDefault("Transaction Type", "")); // There are many possible kinds of these things
		putValue("Transaction Status", "01"); // There are many possible kinds of these things
		putValue("Cancel Indicator", "0"); // Doesn't exist in Square
		putValue("Post Void Indicator", "0"); // Doesn't exist in Square
		putValue("Tax Exempt Indicator", "0"); // Doesn't exist in Square
		putValue("Training Indicator", "0"); // Doesn't exist in Square
		putValue("Transaction Processor Attempts", "01"); // Will always be only 1
		putValue("Transaction Error Code", ""); // Doesn't exist in Square
		putValue("Number of Records", params.getOrDefault("Number of Records", "")); // A count that needs to be adjusted after the fact
		putValue("Business Date", params.getOrDefault("Business Date", "")); // not supported
		putValue("RetailStore Product Generation", ""); // Not using RetailStore
		putValue("RetailStore Major Version", ""); // Not using RetailStore
		putValue("RetailStore Minor Version", ""); // Not using RetailStore
		putValue("RetailStore Service Pack", ""); // Not using RetailStore
		putValue("RetailStore Hot Fix", ""); // Not using RetailStore
		putValue("(Customer) Code Release Number", ""); // Not using customer software
		putValue("(Customer) Code Release EFix", ""); // Not using customer software
		putValue("(Customer) Release Additional Data", ""); // Not using customer software
		putValue("Tax Calculator", "9"); // Neither RetailStore nor TaxConnect calculated taxes
		
		return this;
	}
}
