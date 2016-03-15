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
	
	private static Map<String,RecordDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,RecordDetails>();
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
	
	public TransactionHeader parse(Merchant location, List<Employee> squareEmployees, CashDrawerShift cashDrawerShift, String transactionType, int numberOfRecords) {
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
		
		params.put("Transaction Type", transactionType);
		params.put("Store Number", location.getLocationDetails().getNickname() != null ? location.getLocationDetails().getNickname() : "");
		params.put("Number of Records", "" + numberOfRecords);
		
		return parse(params);
	}
	
	public TransactionHeader parse(Merchant location, List<CashDrawerShift> cashDrawerShifts, String transactionType, int numberOfRecords) {
		Map<String,String> params = new HashMap<String,String>();
		
		String startDate = "";
		if ("010".equals(transactionType)) {
			for (CashDrawerShift cashDrawerShift : cashDrawerShifts) {
				if (startDate.equals("") || startDate.compareTo(cashDrawerShift.getOpenedAt()) > 0) {
					startDate = cashDrawerShift.getOpenedAt();
				}
			}
		} else if ("040".equals(transactionType)) {
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
		
		params.put("Transaction Type", transactionType);
		
		params.put("Number of Records", "" + numberOfRecords);
		
		return parse(params);
	}
	
	public TransactionHeader parse(Merchant location, Payment squarePayment, List<Employee> squareEmployees, String transactionType, int numberOfRecords) {
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
		params.put("Transaction Type", transactionType);
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
