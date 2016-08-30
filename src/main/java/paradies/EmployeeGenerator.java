package paradies;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.squareup.connect.Employee;
import com.squareup.connect.EmployeeRole;

public class EmployeeGenerator {

	public static final String COMMAND_ADD_EMPLOYEE = "1";
	public static final String COMMAND_UPDATE_EMPLOYEE = "2";
	public static final String COMMAND_DELETE_EMPLOYEE = "3";

	public static final String ROLE_MANAGER = "Store Manager";
	public static final String ROLE_CASHIER = "Cashier";
	
	public EmployeeGenerator() throws Exception {
	}

	public List<Employee> parsePayload(byte[] payload, EmployeeRole[] currentRoles) throws IOException {

		// Find the roles
		String managerRole = getRole(currentRoles, ROLE_MANAGER) != null ? getRole(currentRoles, ROLE_MANAGER).getId() : "";
		String cashierRole = getRole(currentRoles, ROLE_CASHIER) != null ? getRole(currentRoles, ROLE_CASHIER).getId() : "";

		ArrayList<Employee> updatedEmployees = new ArrayList<Employee>();

		// Process file and create matching employee objects
		InputStream stream = new ByteArrayInputStream(payload);
        BufferedReader bfReader = new BufferedReader(new InputStreamReader(stream));

        String line = "";
        while((line = bfReader.readLine()) != null) {
        	if (line.length() < 20) {
        		continue;
        	}
        	
        	String[] fields = line.split(",");

        	// Delete employee command
        	if (fields[0].equals(COMMAND_DELETE_EMPLOYEE)) {
        		continue;
        	}

        	String role = fields[20].equals("5") ? managerRole : cashierRole;

        	Employee newEmployee = new Employee();
        	newEmployee.setExternalId(fields[2]);
        	newEmployee.setFirstName(fields[4]);
        	newEmployee.setLastName(fields[23]);
        	newEmployee.setRoleIds(new String[]{role});

        	updatedEmployees.add(newEmployee);
        }

        if(stream != null) {
        	stream.close();
        }

		return updatedEmployees;
	}

	public static List<EmployeeRole> getOwnerRoles(EmployeeRole[] roles) {
		ArrayList<EmployeeRole> ownerRoles = new ArrayList<EmployeeRole>();
		for (EmployeeRole role : roles) {
			if (role.isOwner()) {
				ownerRoles.add(role);
			}
		}
		return ownerRoles;
	}
	
	public static EmployeeRole getRole(EmployeeRole[] roles, String name) {
		for (EmployeeRole role : roles) {
			if (role.getName().equals(name)) {
				return role;
			}
		}
		return null;
	}
}
