package paradies;

import paradies.Util;
import paradies.pandora.CatalogEntry;
import util.SquarePayload;

import org.mule.api.MuleMessage;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import com.squareup.connect.SquareClient;
import com.squareup.connect.Employee;
import com.squareup.connect.EmployeeRole;
import com.squareup.connect.diff.EmployeeChangeRequest;

public class EmployeeCallable implements Callable {

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		MuleMessage message = eventContext.getMessage();

		SquarePayload sqPayload = (SquarePayload) message.getPayload();
		String apiUrl = message.getProperty("apiUrl", PropertyScope.SESSION);
		String apiVersion = message.getProperty("apiVersion", PropertyScope.SESSION);
		SquareClient client = new SquareClient(sqPayload.getAccessToken(), apiUrl, apiVersion, sqPayload.getMerchantId(), sqPayload.getLocationId());
		
		// Find the owner role
		ArrayList<EmployeeRole> ownerRoles = new ArrayList<EmployeeRole>();
		String managerRole = "";
		String cashierRole = "";
		EmployeeRole[] currentRoles = client.roles().list();
		for (EmployeeRole role : currentRoles) {
			if (role.isOwner()) {
				ownerRoles.add(role);
			} else if (role.getName().equals("Cashier")) {
				cashierRole = role.getId();
			} else if (role.getName().equals("Store Manager")) {
				managerRole = role.getId();
			}
		}
		ArrayList<Employee> currentEmployees = new ArrayList<Employee>(Arrays.asList(client.employees().list()));
		ArrayList<Employee> updatedEmployees = new ArrayList<Employee>();

		// Process file and create matching employee objects
		
		byte[] ftpPayload = eventContext.getMessage().getProperty("ftpPayload", PropertyScope.SESSION);
		InputStream stream = new ByteArrayInputStream(ftpPayload);
        BufferedReader bfReader = new BufferedReader(new InputStreamReader(stream));

        String line = "";
        while((line = bfReader.readLine()) != null) {
        	if (line.length() < 20) {
        		continue;
        	}
        	
        	String[] fields = line.split(",");

        	// Delete employee command
        	if (fields[0].equals("3")) {
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

		HashSet<Object> ignoreEmployeeFields = new HashSet<Object>();
		ignoreEmployeeFields.add(Employee.Field.ID);
		ignoreEmployeeFields.add(Employee.Field.STATUS);
		ignoreEmployeeFields.add(Employee.Field.CREATED_AT);
		ignoreEmployeeFields.add(Employee.Field.UPDATED_AT);
		ignoreEmployeeFields.add(Employee.Field.EMAIL);

		EmployeeChangeRequest ecr = EmployeeChangeRequest.diff(currentEmployees, updatedEmployees, ownerRoles, ignoreEmployeeFields);

		ecr.setSquareClient(client);
		ecr.call();

		return updatedEmployees;
	}
}
