package vfcorp.tlog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.squareup.connect.CashDrawerShift;
import com.squareup.connect.Employee;

import vfcorp.Record;
import vfcorp.RecordDetails;

public class AuthorizationCode extends Record {

	private static Map<String,RecordDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,RecordDetails>();
		length = 24;
		id = "023";
		
		fields.put("Identifier", new RecordDetails(3, 1, ""));
		fields.put("Authorization Code", new RecordDetails(8, 4, ""));
		fields.put("Function Indicator", new RecordDetails(2, 12, ""));
		fields.put("Employee Number", new RecordDetails(11, 14, "right justified, zero filled"));
	}
	
	public AuthorizationCode() {
		super();
	}

	public AuthorizationCode(String record) {
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
	
	public AuthorizationCode parse(List<Employee> squareEmployees, CashDrawerShift cashDrawerShift, String functionIndicator) {
		String employeeNumber = "";
		
		if (cashDrawerShift.getOpeningEmployeeId() != null) {
			for (Employee squareEmployee : squareEmployees) {
				if (squareEmployee.getId().equals(cashDrawerShift.getOpeningEmployeeId())) {
					employeeNumber = squareEmployee.getExternalId();
				}
			}
		}
		
		
		putValue("Authorization Code", ""); // not supported
		putValue("Function Indicator", functionIndicator); // several different supported types
		putValue("Employee Number", employeeNumber);
		
		return this;
	}
}
