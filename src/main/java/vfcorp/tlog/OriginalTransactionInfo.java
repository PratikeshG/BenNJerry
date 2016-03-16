package vfcorp.tlog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.squareup.connect.Employee;
import com.squareup.connect.Merchant;
import com.squareup.connect.Payment;
import com.squareup.connect.Tender;

import vfcorp.FieldDetails;
import vfcorp.Record;

public class OriginalTransactionInfo extends Record {

	private static Map<String,FieldDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,FieldDetails>();
		length = 41;
		id = "040";
		
		fields.put("Identifier", new FieldDetails(3, 1, ""));
		fields.put("Type", new FieldDetails(2, 4, ""));
		fields.put("Store Number", new FieldDetails(5, 6, ""));
		fields.put("Register Number", new FieldDetails(3, 11, ""));
		fields.put("Transaction Number", new FieldDetails(6, 14, ""));
		fields.put("Transaction Date", new FieldDetails(8, 20, "MMDDYYYY"));
		fields.put("Cashier/Associate Number", new FieldDetails(11, 28, ""));
		fields.put("Indicator", new FieldDetails(1, 39, ""));
		fields.put("Host Lookup", new FieldDetails(2, 40, ""));
	}
	
	public OriginalTransactionInfo() {
		super();
	}

	public OriginalTransactionInfo(String record) {
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
	
	public OriginalTransactionInfo parse(Merchant location, Payment payment, List<Employee> squareEmployees) {
		String date = payment.getCreatedAt().substring(5, 7) +
				payment.getCreatedAt().substring(8, 10) + 
				payment.getCreatedAt().substring(0, 4);
		
		String registerNumber = "";
		if (payment.getDevice().getName() != null) {
			int registerNumberFirstIndex = payment.getDevice().getName().indexOf('(');
			int registerNumberLastIndex = payment.getDevice().getName().indexOf(')');
			if (registerNumberFirstIndex > -1 && registerNumberLastIndex > -1)
				registerNumber = payment.getDevice().getName().substring(registerNumberFirstIndex + 1, registerNumberLastIndex);
		}
		
		String storeNumber = "";
		if (location.getLocationDetails().getNickname() != null) {
			int storeNumberFirstIndex = location.getLocationDetails().getNickname().indexOf('(');
			int storeNumberLastIndex = location.getLocationDetails().getNickname().indexOf(')');
			if (storeNumberFirstIndex > -1 && storeNumberLastIndex > -1) {
				storeNumber = location.getLocationDetails().getNickname().substring(storeNumberFirstIndex + 1, storeNumberLastIndex);
				storeNumber = storeNumber.replaceAll("[^\\d]", "");
			}
		}
		
		putValue("Type", "01"); // this is "by item"
		putValue("Store Number", storeNumber);
		putValue("Register Number", registerNumber);
		putValue("Transaction Number", "123456"); // TODO(colinlam): Square transaction ID doesn't fit...where to get this?
		putValue("Transaction Date", date);
		putValue("Indicator", "0"); // this is "associate number"
		putValue("Host Lookup", "00"); // this is "no lookup attempted"
		
		for (Tender tender : payment.getTender()) {
			if (tender.getEmployeeId() != null) {
				for (Employee employee : squareEmployees) {
					if (employee.getId().equals(tender.getEmployeeId())) {
						putValue("Cashier/Associate Number", employee.getExternalId());
					}
				}
			}
		}
		
		return this;
	}
}
