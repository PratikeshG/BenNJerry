package vfcorp.tlog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mule.api.store.ObjectStore;
import org.mule.api.store.ObjectStoreException;

import vfcorp.FieldDetails;
import vfcorp.Record;

import com.squareup.connect.Employee;
import com.squareup.connect.Merchant;
import com.squareup.connect.Payment;
import com.squareup.connect.Tender;

public class TransactionHeader extends Record {

	private static final int MAX_TRANSACTION_NUMBER = 999999;
	
	public static final String TRANSACTION_TYPE_STORE_OPEN = "010";
	public static final String TRANSACTION_TYPE_CASHIER_LOGOFF = "030";
	public static final String TRANSACTION_TYPE_STORE_CLOSE = "040";
	public static final String TRANSACTION_TYPE_MACHINE_STARTED_FOR_THE_DAY = "050";
	public static final String TRANSACTION_TYPE_ENTER_TRAINING_MODE = "060";
	public static final String TRANSACTION_TYPE_EXIT_TRAINING_MODE = "069";
	public static final String TRANSACTION_TYPE_START_REENTRY_MODE = "070";
	public static final String TRANSACTION_TYPE_EXIT_REENTRY_MODE = "079";
	public static final String TRANSACTION_TYPE_CLEAR_REGISTER_TOTALS = "080";
	public static final String TRANSACTION_TYPE_CLEAR_CASHIER_TOTALS = "081";
	public static final String TRANSACTION_TYPE_READ_REGISTER_TOTALS = "090";
	public static final String TRANSACTION_TYPE_READ_CASHIER_TOTALS = "091";
	public static final String TRANSACTION_TYPE_READ_ASSOCIATE_TOTALS = "092";
	public static final String TRANSACTION_TYPE_READ_HOURLY_PRODUCTIVITY_TOTALS = "093";
	public static final String TRANSACTION_TYPE_READ_DEPARTMENT_TOTALS = "094";
	public static final String TRANSACTION_TYPE_READ_DEPARTMENT_CLASS_TOTALS = "095";
	public static final String TRANSACTION_TYPE_SUSPEND_SALE = "100";
	public static final String TRANSACTION_TYPE_SUSPEND_LAYAWAY_SALE = "101";
	public static final String TRANSACTION_TYPE_SUSPEND_SPECIAL_ORDER = "102";
	public static final String TRANSACTION_TYPE_SUSPEND_SEND_SALE = "103";
	public static final String TRANSACTION_TYPE_SUSPEND_RETURN = "104";
	public static final String TRANSACTION_TYPE_RESUME_SALE = "110";
	public static final String TRANSACTION_TYPE_RESUME_LAYAWAY_SALE = "111";
	public static final String TRANSACTION_TYPE_RESUME_SPECIAL_ORDER = "112";
	public static final String TRANSACTION_TYPE_RESUME_SEND_SALE = "113";
	public static final String TRANSACTION_TYPE_RESUME_RETURN = "114";
	public static final String TRANSACTION_TYPE_POST_VOID_SALE = "120";
	public static final String TRANSACTION_TYPE_POST_VOID_LAYAWAY_SALE = "121";
	public static final String TRANSACTION_TYPE_POST_VOID_LAYAWAY_PAYMENT = "122";
	public static final String TRANSACTION_TYPE_POST_VOID_LAYAWAY_ADJUSTMENT = "123";
	public static final String TRANSACTION_TYPE_POST_VOID_LAYAWAY_PARTIAL_PICKUP = "124";
	public static final String TRANSACTION_TYPE_POST_VOID_LAYAWAY_FINAL = "125";
	public static final String TRANSACTION_TYPE_POST_VOID_LAYAWAY_CANCEL = "126";
	public static final String TRANSACTION_TYPE_POST_VOID_LAYAWAY_RESTOCK = "127";
	public static final String TRANSACTION_TYPE_POST_VOID_SPECIAL_ORDER = "128";
	public static final String TRANSACTION_TYPE_POST_VOID_SPECIAL_ORDER_REMOVE_ITEMS = "129";
	public static final String TRANSACTION_TYPE_POST_VOID_SPECIAL_ORDER_PARTIAL_PICKUP = "130";
	public static final String TRANSACTION_TYPE_POST_VOID_SPECIAL_ORDER_FINAL = "131";
	public static final String TRANSACTION_TYPE_POST_VOID_SPECIAL_ORDER_CANCEL = "132";
	public static final String TRANSACTION_TYPE_POST_VOID_SPECIAL_ORDER_RESTOCK = "133";
	public static final String TRANSACTION_TYPE_POST_VOID_RETURN = "134";
	public static final String TRANSACTION_TYPE_POST_VOID_SEND_SALE = "135";
	public static final String TRANSACTION_TYPE_POST_VOID_ON_ACCOUNT_PAYMENT = "136";
	public static final String TRANSACTION_TYPE_POST_VOID_PICKUP = "137";
	public static final String TRANSACTION_TYPE_POST_VOID_PAYOUT = "138";
	public static final String TRANSACTION_TYPE_POST_VOID_PAYIN = "139";
	public static final String TRANSACTION_TYPE_POST_VOID_BUY_BACK_STORE_CREDIT = "140";
	public static final String TRANSACTION_TYPE_POST_VOID_CASH_A_CHECK = "141";
	public static final String TRANSACTION_TYPE_POST_VOID_INTERIM_BANK_DEPOSIT = "142";
	public static final String TRANSACTION_TYPE_SALE = "200";
	public static final String TRANSACTION_TYPE_RETURN = "201";
	public static final String TRANSACTION_TYPE_LAYAWAY_SALE = "202";
	public static final String TRANSACTION_TYPE_LAYAWAY_PAYMENT = "203";
	public static final String TRANSACTION_TYPE_LAYAWAY_ADJUSTMENT = "204";
	public static final String TRANSACTION_TYPE_LAYAWAY_PARTIAL_PICKUP = "205";
	public static final String TRANSACTION_TYPE_LAYAWAY_FINAL = "206";
	public static final String TRANSACTION_TYPE_LAYAWAY_CANCEL = "207";
	public static final String TRANSACTION_TYPE_LAYAWAY_RESTOCK = "208";
	public static final String TRANSACTION_TYPE_SPECIAL_ORDER = "209";
	public static final String TRANSACTION_TYPE_SPECIAL_ORDER_ADJUST = "210";
	public static final String TRANSACTION_TYPE_SPECIAL_ORDER_PARTIAL_PICKUP = "211";
	public static final String TRANSACTION_TYPE_SPECIAL_ORDER_FINAL = "212";
	public static final String TRANSACTION_TYPE_SPECIAL_ORDER_CANCEL = "213";
	public static final String TRANSACTION_TYPE_SPECIAL_ORDER_RESTOCK = "214";
	public static final String TRANSACTION_TYPE_SEND_SALE = "215";
	public static final String TRANSACTION_TYPE_REPRINT_RECEIPT = "216";
	public static final String TRANSACTION_TYPE_EGC_BALANCE_INQUIRY = "217";
	public static final String TRANSACTION_TYPE_TENDER_COUNT_REGISTER = "400";
	public static final String TRANSACTION_TYPE_TENDER_COUNT_CASHIER = "401";
	public static final String TRANSACTION_TYPE_CASHOUT_DEPOSIT_AMOUNT = "403";
	public static final String TRANSACTION_TYPE_CHANGE_DATE_TIME = "501";
	public static final String TRANSACTION_TYPE_STARTING_BANK = "502";
	public static final String TRANSACTION_TYPE_INTERIM_BANK_DEPOSIT = "503";
	public static final String TRANSACTION_TYPE_ZIP_POSTAL_CODE = "598";
	public static final String TRANSACTION_TYPE_USER_STOP_POS = "599";
	public static final String TRANSACTION_TYPE_ABNORMAL_TERMINATION_OF_POS = "600";
	public static final String TRANSACTION_TYPE_HOTKEY_TO_BACKOFFICE = "601";
	public static final String TRANSACTION_TYPE_TRANSACTION_NUMBER_CONSUMED_WITHOUT_SA = "610";
	public static final String TRANSACTION_TYPE_OPEN_REGISTER = "699";
	public static final String TRANSACTION_TYPE_VOID_RENTAL = "710";
	public static final String TRANSACTION_TYPE_PICKUP = "800";
	public static final String TRANSACTION_TYPE_PAYOUT = "801";
	public static final String TRANSACTION_TYPE_BUY_BACK_STORE_CREDIT = "802";
	public static final String TRANSACTION_TYPE_CASH_A_CHECK = "803";
	public static final String TRANSACTION_TYPE_PAYIN = "850";
	public static final String TRANSACTION_TYPE_ON_ACCOUNT_PAYMENT = "851";
	public static final String TRANSACTION_TYPE_NO_SALE = "900";
	public static final String TRANSACTION_TYPE_MANUAL_CASH_DRAWER_OPEN = "901";
	public static final String TRANSACTION_TYPE_USER_TRANSACTION = "902";
	
	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,FieldDetails>();
		length = 162;
		id = "000";
		
		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Store Number", new FieldDetails(5, 4, "Zero filled, right justified"));
		fields.put("Register Number", new FieldDetails(3, 9, "zero filled"));
		fields.put("Cashier Number", new FieldDetails(6, 12, "Zero filled, right justified"));
		fields.put("Employee Number", new FieldDetails(11, 18, "zero filled"));
		fields.put("Transaction Number", new FieldDetails(6, 29, ""));
		fields.put("Transaction Date", new FieldDetails(8, 35, "MMDDYYYY, zero filled"));
		fields.put("Transaction Time", new FieldDetails(4, 43, "HHMM – Military, zero filled"));
		fields.put("Transaction Type", new FieldDetails(3, 47, ""));
		fields.put("Transaction Status", new FieldDetails(2, 50, ""));
		fields.put("Cancel Indicator", new FieldDetails(1, 52, "1 = Cancelled"));
		fields.put("Post Void Indicator", new FieldDetails(1, 53, "1 = Post Void"));
		fields.put("Tax Exempt Indicator", new FieldDetails(1, 54, "1 = Tax Exempt"));
		fields.put("Training Indicator", new FieldDetails(1, 55, "1 = Training"));
		fields.put("User Data", new FieldDetails(3, 56, ""));
		fields.put("Transaction Processor Attempts", new FieldDetails(2, 59, ""));
		fields.put("Transaction Error Code", new FieldDetails(4, 61, "zero filled"));
		fields.put("Number of Records", new FieldDetails(8, 65, "Detail records + header, zero filled, right justified"));
		fields.put("Business Date", new FieldDetails(8, 73, "MMDDYYYY, zero filled"));
		fields.put("RetailStore Product Generation", new FieldDetails(1, 81, "zero filled"));
		fields.put("RetailStore Major Version", new FieldDetails(1, 82, "zero filled"));
		fields.put("RetailStore Minor Version", new FieldDetails(2, 83, "Zero filled, right justified"));
		fields.put("RetailStore Service Pack", new FieldDetails(2, 85, "Zero filled, right justified"));
		fields.put("RetailStore Hot Fix", new FieldDetails(3, 87, "Zero filled, right justified"));
		fields.put("(Customer) Code Release Number", new FieldDetails(3, 90, "Zero filled, right justified"));
		fields.put("(Customer) Code Release EFix", new FieldDetails(3, 93, "Zero filled"));
		fields.put("(Customer) Release Additional Data", new FieldDetails(17, 96, "Left justified, space filled"));
		fields.put("Tax Calculator", new FieldDetails(1, 113, ""));
		fields.put("Reserved for Future Use", new FieldDetails(49, 114, "Space filled"));
	}
	
	public TransactionHeader() {
		super();
	}
	
	public TransactionHeader(String record) {
		super(record);
	}

	@Override
	public Map<String,FieldDetails> getFields() {
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
	
	public TransactionHeader parse(Merchant location, List<Payment> squarePaymentsList, String deviceName, String transactionType, int numberOfRecords, ObjectStore<String> objectStore, String deployment) {
		Map<String,String> params = new HashMap<String,String>();
		
		String lastDate = "";
		for (Payment squarePayment : squarePaymentsList) {
			if (squarePayment.getCreatedAt().compareTo(lastDate) > 0) {
				lastDate = squarePayment.getCreatedAt();
			}
		}
		if (lastDate != "") {
			params.put("Transaction Date", lastDate.substring(5, 7) + lastDate.substring(8, 10) + lastDate.substring(0, 4));
			params.put("Business Date", lastDate.substring(5, 7) + lastDate.substring(8, 10) + lastDate.substring(0, 4));
			params.put("Transaction Time", lastDate.substring(11,13) + lastDate.substring(14, 16));
		}
		
		String registerNumber = getRegisterNumber(deviceName);
		params.put("Register Number", registerNumber);
		
		return parse(location, transactionType, numberOfRecords, objectStore, deployment, registerNumber, params);
	}
	
	public TransactionHeader parse(Merchant location, Payment squarePayment, List<Employee> squareEmployees, String transactionType, int numberOfRecords, ObjectStore<String> objectStore, String deployment) {
		Map<String,String> params = new HashMap<String,String>();
		
		// TODO(colinlam): refactor to only include a single employee ID passed in
		for (Tender tender : squarePayment.getTender()) {
			if (tender.getEmployeeId() != null) {
				for (Employee employee : squareEmployees) {
					if (employee.getId().equals(tender.getEmployeeId())) {
						params.put("Employee Number", employee.getExternalId());
						break;
					}
				}
			}
		}
		
		params.put("Transaction Date", squarePayment.getCreatedAt().substring(5, 7) + squarePayment.getCreatedAt().substring(8, 10) + squarePayment.getCreatedAt().substring(0, 4));
		params.put("Business Date", squarePayment.getCreatedAt().substring(5, 7) + squarePayment.getCreatedAt().substring(8, 10) + squarePayment.getCreatedAt().substring(0, 4));
		params.put("Transaction Time", squarePayment.getCreatedAt().substring(11,13) + squarePayment.getCreatedAt().substring(14, 16));
		
		String registerNumber = getRegisterNumber(squarePayment.getDevice().getName());
		params.put("Register Number", registerNumber);
		
		return parse(location, transactionType, numberOfRecords, objectStore, deployment, registerNumber, params);
	}
	
	public TransactionHeader parse(Merchant location, String transactionType, int numberOfRecords, ObjectStore<String> objectStore, String deployment, String registerNumber, Map<String, String> params) {
		String storeNumber = getStoreNumber(location);
		params.put("Store Number", storeNumber);
		
		params.put("Transaction Number", String.format("%06d", getTransactionNumber(objectStore, storeNumber, registerNumber, deployment)));
		params.put("Number of Records", "" + numberOfRecords);
		params.put("Transaction Type", transactionType);
		
		putValue("Store Number", params.getOrDefault("Store Number", ""));
		putValue("Register Number", params.getOrDefault("Register Number", "")); // Only if device is named correctly
		putValue("Cashier Number", ""); // What is the difference between a cashier and a register?
		putValue("Employee Number", params.getOrDefault("Employee Number", ""));
		putValue("Transaction Number", params.getOrDefault("Transaction Number", "000000"));
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
		putValue("Tax Calculator", "0"); // ?? Neither RetailStore nor TaxConnect calculated taxes
		
		return this;
	}
	
	private int getTransactionNumber(ObjectStore<String> objectStore, String storeNumber, String registerNumber, String deployment) {
		if (storeNumber == null || storeNumber.equals("")) {
			storeNumber = "0";
		}
		if (registerNumber == null || registerNumber.equals("")) {
			registerNumber = "0";
		}
		
		String storeNumberFormatted = String.format("%05d", Integer.parseInt(storeNumber));
		String registerNumberFormatted = String.format("%03d", Integer.parseInt(registerNumber));
		
		try {
			String transactionNumberKey = deployment + "-transactionNumber-" + storeNumberFormatted + "-" + registerNumberFormatted;
			
			if (objectStore.contains(transactionNumberKey)) {
				int transactionNumber = Integer.parseInt(objectStore.retrieve(transactionNumberKey)) + 1;
				
				if (transactionNumber > MAX_TRANSACTION_NUMBER) {
					transactionNumber = 1;
				}
				
				objectStore.remove(transactionNumberKey);
				objectStore.store(transactionNumberKey, "" + transactionNumber);
				return transactionNumber;
			} else {
				objectStore.store(transactionNumberKey, "" + 1);
				return 1;
			}
		} catch (ObjectStoreException e) {
			return 1;
		}
	}

	private String getRegisterNumber(String deviceName) {
		String registerNumber = "";
		
		if (deviceName != null) {
			int registerNumberFirstIndex = deviceName.indexOf('(');
			int registerNumberLastIndex = deviceName.indexOf(')');
			if (registerNumberFirstIndex > -1 && registerNumberLastIndex > -1) {
				registerNumber = deviceName.substring(registerNumberFirstIndex + 1, registerNumberLastIndex);
			}
		}
		
		return registerNumber;
	}

	private String getStoreNumber(Merchant location) {
		String storeNumber = "";
		
		if (location.getLocationDetails().getNickname() != null) {
			int storeNumberFirstIndex = location.getLocationDetails().getNickname().indexOf('(');
			int storeNumberLastIndex = location.getLocationDetails().getNickname().indexOf(')');
			if (storeNumberFirstIndex > -1 && storeNumberLastIndex > -1) {
				storeNumber = location.getLocationDetails().getNickname().substring(storeNumberFirstIndex + 1, storeNumberLastIndex);
				storeNumber = storeNumber.replaceAll("[^\\d]", "");
			}
		}
		
		return storeNumber;
	}
}
