package scripts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.Employee;
import com.squareup.connect.SquareClient;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.SquareClientV2;

import util.SquarePayload;

public class BJsEmployeeExport {
    private final static String MASTER_ACCOUNT_TOKEN = System.getenv("SCRIPT_MASTER_ACCOUNT_TOKEN");
    private final static String MERCHANT_ID = System.getenv("SCRIPT_MERCHANT_ID");
    private final static String LOCATION_OPERATOR_ROLE_ID = System.getenv("SCRIPT_LOCATION_OPERATOR_ROLE_ID");

    private final static String ENCRYPTION_KEY = System.getenv("SCRIPT_ENCRYPTION_KEY");
    private final static String API_URL = System.getenv("SCRIPT_API_URL"); // ex: https://connect.squareup.com

    private static Logger logger = LoggerFactory.getLogger(BJsEmployeeExport.class);

    public static void main(String[] args) throws Exception {
        logger.info("Running script to export BJs employee records...");

        SquarePayload account = new SquarePayload();
        account.setEncryptedAccessToken(MASTER_ACCOUNT_TOKEN);

        SquareClient clientV1 = new SquareClient(account.getAccessToken(ENCRYPTION_KEY), API_URL, "v1", MERCHANT_ID);
        SquareClientV2 clientV2 = new SquareClientV2(API_URL, account.getAccessToken(ENCRYPTION_KEY), MERCHANT_ID);

        List<Location> locations = Arrays.asList(clientV2.locations().list());
        Employee[] employees = clientV1.employees().list();

        for (Location location : locations) {
            ArrayList<Employee> operators = (ArrayList<Employee>) locationOperators(location, employees);

            for (Employee operator : operators) {
                String entry = String.format("%s, %s, %s, %s, %s, %s, %s", location.getName(), operator.getId(),
                        operator.getExternalId(), operator.getFirstName(), operator.getLastName(), operator.getStatus(),
                        operator.getEmail());
                logger.info(entry);
            }

            if (operators.size() < 1) {
                String entry = String.format("%s, %s", location.getName(), "NO OPERATORS ASSIGNED");
                logger.info(entry);
            }
        }

        logger.info("Done.");
    }

    private static List<Employee> locationOperators(Location location, Employee[] employees) {
        ArrayList<Employee> operators = new ArrayList<Employee>();

        for (Employee employee : employees) {
            if (employeeAtLocation(employee, location) && employeeIsLocationOperator(employee)) {
                operators.add(employee);
            }
        }

        return operators;
    }

    private static boolean employeeIsLocationOperator(Employee employee) {
        for (String roleId : employee.getRoleIds()) {
            if (roleId.equals(LOCATION_OPERATOR_ROLE_ID)) {
                return true;
            }
        }
        return false;
    }

    private static boolean employeeAtLocation(Employee employee, Location location) {
        for (String authorizedLocationId : employee.getAuthorizedLocationIds()) {
            if (authorizedLocationId.equals(location.getId())) {
                return true;
            }
        }
        return false;
    }
}
