package paradies;

import util.SquarePayload;

import org.mule.api.MuleMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import com.squareup.connect.SquareClient;
import com.squareup.connect.diff.EmployeeChangeRequest;
import com.squareup.connect.Employee;
import com.squareup.connect.EmployeeRole;

public class EmployeeCallable implements Callable {

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		MuleMessage message = eventContext.getMessage();

		// Cache employee info from database
		HashMap<String, String> employeeIdCache = new HashMap<String, String>();

		@SuppressWarnings("unchecked")
		List<Map<String,Object>> employeeMappings = (List<Map<String,Object>>) message.getProperty("employeeIdMappings", PropertyScope.SESSION);
		for (Map<String,Object> employeeMapping : employeeMappings) {
			String id = (String) employeeMapping.get("squareId");
			String externalId = (String) employeeMapping.get("externalId");
			employeeIdCache.put(externalId, id);
		}

		SquarePayload sqPayload = (SquarePayload) message.getPayload();
		String apiUrl = message.getProperty("apiUrl", PropertyScope.SESSION);
		String apiVersion = message.getProperty("apiVersion", PropertyScope.SESSION);
		SquareClient client = new SquareClient(sqPayload.getAccessToken(), apiUrl, apiVersion, sqPayload.getMerchantId(), sqPayload.getLocationId());
		
		byte[] ftpPayload = eventContext.getMessage().getProperty("ftpPayload", PropertyScope.SESSION);
		EmployeeRole[] currentRoles = client.roles().list();
		
		EmployeeGenerator employeeGenerator = new EmployeeGenerator();
		ArrayList<Employee> updatedEmployees = (ArrayList<Employee>) employeeGenerator.parsePayload(ftpPayload, currentRoles);
		for (Employee updatedEmployee : updatedEmployees) {
			String cachedEmployeeId = employeeIdCache.get(updatedEmployee.getExternalId());
        	if (cachedEmployeeId != null) {
        		updatedEmployee.setId(cachedEmployeeId);
        	} else {
        		// Employee doesn't exist yet, create now
        		System.out.println("TRYING TO CREATE");
        		Employee createdEmployee = client.employees().create(updatedEmployee);
        		updatedEmployee.setId(createdEmployee.getId());
        	}
		}

		// Perform diff
		ArrayList<Employee> currentEmployees = new ArrayList<Employee>(Arrays.asList(client.employees().list()));
		ArrayList<EmployeeRole> ownerRoles = (ArrayList<EmployeeRole>) EmployeeGenerator.getOwnerRoles(currentRoles);
		
		HashSet<Object> ignoreEmployeeFields = new HashSet<Object>();
		ignoreEmployeeFields.add(Employee.Field.EXTERNAL_ID);
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
