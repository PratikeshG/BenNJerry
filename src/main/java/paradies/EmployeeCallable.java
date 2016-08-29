package paradies;

import util.SquarePayload;

import org.mule.api.MuleMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

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

		SquarePayload sqPayload = (SquarePayload) message.getPayload();
		String apiUrl = message.getProperty("apiUrl", PropertyScope.SESSION);
		String apiVersion = message.getProperty("apiVersion", PropertyScope.SESSION);
		SquareClient client = new SquareClient(sqPayload.getAccessToken(), apiUrl, apiVersion, sqPayload.getMerchantId(), sqPayload.getLocationId());
		
		byte[] ftpPayload = eventContext.getMessage().getProperty("ftpPayload", PropertyScope.SESSION);
		EmployeeRole[] currentRoles = client.roles().list();
		
		EmployeeGenerator employeeGenerator = new EmployeeGenerator();
		ArrayList<Employee> updatedEmployees = (ArrayList<Employee>) employeeGenerator.parsePayload(ftpPayload, currentRoles);

		// Perform diff
		HashSet<Object> ignoreEmployeeFields = new HashSet<Object>();
		ignoreEmployeeFields.add(Employee.Field.ID);
		ignoreEmployeeFields.add(Employee.Field.STATUS);
		ignoreEmployeeFields.add(Employee.Field.CREATED_AT);
		ignoreEmployeeFields.add(Employee.Field.UPDATED_AT);
		ignoreEmployeeFields.add(Employee.Field.EMAIL);
		
		ArrayList<Employee> currentEmployees = new ArrayList<Employee>(Arrays.asList(client.employees().list()));
		ArrayList<EmployeeRole> ownerRoles = (ArrayList<EmployeeRole>) EmployeeGenerator.getOwnerRoles(currentRoles);
		
		EmployeeChangeRequest ecr = EmployeeChangeRequest.diff(currentEmployees, updatedEmployees, ownerRoles, ignoreEmployeeFields);
		ecr.setSquareClient(client);
		ecr.call();
		
		return updatedEmployees;
	}
}
