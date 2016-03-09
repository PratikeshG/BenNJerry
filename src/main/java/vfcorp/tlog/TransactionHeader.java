package vfcorp.tlog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		fields.put("Transaction Date", new RecordDetails(8, 35, "MMDDYYYY"));
		fields.put("Transaction Time", new RecordDetails(4, 43, "HHMM – Military"));
		fields.put("Transaction Type", new RecordDetails(3, 47, ""));
		fields.put("Transaction Status", new RecordDetails(2, 50, ""));
		fields.put("Cancel Indicator", new RecordDetails(1, 52, "1 = Cancelled"));
		fields.put("Post Void Indicator", new RecordDetails(1, 53, "1 = Post Void"));
		fields.put("Tax Exempt Indicator", new RecordDetails(1, 54, "1 = Tax Exempt"));
		fields.put("Training Indicator", new RecordDetails(1, 55, "1 = Training"));
		fields.put("User Data", new RecordDetails(3, 56, ""));
		fields.put("Transaction Processor Attempts", new RecordDetails(2, 59, ""));
		fields.put("Transaction Error Code", new RecordDetails(4, 61, ""));
		fields.put("Number of Records", new RecordDetails(8, 65, "Detail records + header, zero filled, right justified"));
		fields.put("Business Date", new RecordDetails(8, 73, "MMDDYYYY"));
		fields.put("RetailStore Product Generation", new RecordDetails(1, 81, ""));
		fields.put("RetailStore Major Version", new RecordDetails(1, 82, ""));
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
	
	public TransactionHeader parse(Payment squarePayment, Merchant location, List<Employee> squareEmployees, int numberOfRecords) {
		String date = squarePayment.getCreatedAt().substring(5, 7) +
				squarePayment.getCreatedAt().substring(8, 10) + 
				squarePayment.getCreatedAt().substring(0, 4);
		String time = squarePayment.getCreatedAt().substring(11,13) + squarePayment.getCreatedAt().substring(14, 16);
		
		String employeeNumber = "";
		for (Tender tender : squarePayment.getTender()) {
			if (tender.getEmployeeId() != null) {
				for (Employee employee : squareEmployees) {
					if (employee.getId().equals(tender.getEmployeeId())) {
						employeeNumber = employee.getExternalId();
					}
				}
			}
		}
		
		String registerNumber = squarePayment.getDevice().getName() != null ? squarePayment.getDevice().getName() : "";
		String storeNumber = location.getLocationDetails().getNickname() != null ? location.getLocationDetails().getNickname() : "";
		
		values.put("Store Number", storeNumber);
		values.put("Register Number", registerNumber); // Only if device is named correctly
		values.put("Cashier Number", "000000"); // What is the difference between a cashier and a register?
		values.put("Employee Number", employeeNumber);
		values.put("Transaction Number", "123456"); // Square transaction ID doesn't fit...where to get this?
		values.put("Transaction Date", date);
		values.put("Transaction Time", time);
		values.put("Transaction Type", "000"); // There are many possible kinds of these things
		values.put("Transaction Status", "01"); // There are many possible kinds of these things
		values.put("Cancel Indicator", "0"); // Doesn't exist in Square
		values.put("Post Void Indicator", "0"); // Doesn't exist in Square
		values.put("Tax Exempt Indicator", "0"); // Doesn't exist in Square
		values.put("Training Indicator", "0"); // Doesn't exist in Square
		values.put("Transaction Processor Attempts", "01"); // Will always be only 1
		values.put("Transaction Error Code", "0000"); // Doesn't exist in Square
		values.put("Number of Records", "" + numberOfRecords); // A count that needs to be adjusted after the fact
		values.put("Business Date", date);
		values.put("RetailStore Product Generation", "0"); // Not using RetailStore
		values.put("RetailStore Major Version", "0"); // Not using RetailStore
		values.put("RetailStore Minor Version", "00"); // Not using RetailStore
		values.put("RetailStore Service Pack", "00"); // Not using RetailStore
		values.put("RetailStore Hot Fix", "000"); // Not using RetailStore
		values.put("(Customer) Code Release Number", "000"); // Not using customer software
		values.put("(Customer) Code Release EFix", "000"); // Not using customer software
		values.put("(Customer) Release Additional Data", ""); // Not using customer software
		values.put("Tax Calculator", "9"); // Neither RetailStore nor TaxConnect calculated taxes
		
		return this;
	}
}
