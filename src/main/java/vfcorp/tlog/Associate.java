package vfcorp.tlog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vfcorp.Record;
import vfcorp.RecordDetails;

import com.squareup.connect.Employee;
import com.squareup.connect.Payment;
import com.squareup.connect.Tender;

public class Associate extends Record {
	
	private static Map<String,RecordDetails> fields;
	private static int length;
	private static String id;
	
	static {
		fields = new HashMap<String,RecordDetails>();
		length = 29;
		id = "026";
		
		fields.put("Identifier", new RecordDetails(3, 1, ""));
		fields.put("Associate Number", new RecordDetails(11, 4, "zero filled"));
		fields.put("Employee Number", new RecordDetails(11, 15, "zero filled"));
		fields.put("Team Associate Ind", new RecordDetails(1, 26, "1 = Member of team"));
		fields.put("Team Number", new RecordDetails(3, 27, "Team = 0 if Ind = 0"));
	}
	
	public Associate() {
		super();
	}
	
	public Associate(String record) {
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
	
	public Associate parse(Payment payment, List<Employee> squareEmployees) {
		// TODO(colinlam): odd corner case arises when different employees take different tenders
		// for the same transaction. Only one associate gets credited!
		for (Tender tender : payment.getTender()) {
			String employeeId = tender.getEmployeeId();
			if (employeeId != null) {
				for (Employee employee : squareEmployees) {
					if (employee.getId().equals(employeeId)) {
						values.put("Associate Number", employee.getExternalId());
						values.put("Employee Number", employee.getExternalId());
					}
				}
			}
		}
		
		if (values.get("Associate Number") == null) {
			values.put("Associate Number", "");
			values.put("Employee Number", "");
		}
		
		values.put("Team Associate Ind", "0");
		values.put("Team Number", "000");
		
		return this;
	}
}
