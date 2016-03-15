package vfcorp.tlog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vfcorp.Record;
import vfcorp.RecordDetails;

import com.squareup.connect.Employee;

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
	
	public Associate parse(String employeeId, List<Employee> squareEmployees) {
		for (Employee employee : squareEmployees) {
			if (employee.getId().equals(employeeId)) {
				putValue("Associate Number", employee.getExternalId());
				putValue("Employee Number", employee.getExternalId());
			}
		}
		
		if (values.get("Associate Number") == null) {
			putValue("Associate Number", "");
			putValue("Employee Number", "");
		}
		
		putValue("Team Associate Ind", "0");
		putValue("Team Number", "000");
		
		return this;
	}
}
