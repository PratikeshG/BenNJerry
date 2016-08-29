package paradies;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import util.TimeManager;

import com.squareup.connect.Employee;
import com.squareup.connect.EmployeeRole;
import com.squareup.connect.Merchant;
import com.squareup.connect.SquareClient;
import com.squareup.connect.diff.Catalog;
import com.squareup.connect.diff.EmployeeChangeRequest;

public class RunEmployees {

	public static void main( String[] args ) throws Exception {
		/*
		 * Employee testing
		 */

		final String PATH = "/Users/bhartard/desktop/Paradies/Spec/square_empxtxnSMALL.asc";
		final String PANDORA_TOKEN = "sq0ats-nNcEYG_Sm37_EG5dtZbcGg";
		final String PANDORA_MERCHANT_ID = "8ZK3ZBFR828N0";
		
		System.out.println("Running...");

		SquareClient client = new SquareClient(PANDORA_TOKEN, "https://connect.squareupstaging.com", "v1", PANDORA_MERCHANT_ID);

		System.out.println("Getting employees...");
		ArrayList<Employee> currentEmployees = new ArrayList<Employee>(Arrays.asList(client.employees().list()));

		System.out.println("Getting roles...");
		EmployeeRole[] currentRoles = client.roles().list();
		ArrayList<EmployeeRole> ownerRoles = (ArrayList<EmployeeRole>) EmployeeGenerator.getOwnerRoles(currentRoles);

		// Parse input
		File file = new File(PATH);
		byte[] fileInBytes = new byte[(int) file.length()];
		FileInputStream inputStream = null;
	    try {
	        inputStream = new FileInputStream(file);
	        inputStream.read(fileInBytes);
	    } finally {
	        inputStream.close();
	    }

	    // Create updated employees
        EmployeeGenerator employeeGenerator = new EmployeeGenerator();
		ArrayList<Employee> updatedEmployees = (ArrayList<Employee>) employeeGenerator.parsePayload(fileInBytes, currentRoles);
		
		// Perform diff
		HashSet<Object> ignoreEmployeeFields = new HashSet<Object>();
		ignoreEmployeeFields.add(Employee.Field.EXTERNAL_ID);
		ignoreEmployeeFields.add(Employee.Field.STATUS);
		ignoreEmployeeFields.add(Employee.Field.CREATED_AT);
		ignoreEmployeeFields.add(Employee.Field.UPDATED_AT);
		ignoreEmployeeFields.add(Employee.Field.EMAIL);

		EmployeeChangeRequest ecr = EmployeeChangeRequest.diff(currentEmployees, updatedEmployees, ownerRoles, ignoreEmployeeFields);
		ecr.setSquareClient(client);
		ecr.call();
		
        System.out.println("Done.");
    }
}
