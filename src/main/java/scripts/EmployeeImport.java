package scripts;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.Employee;
import com.squareup.connect.SquareClient;
import com.squareup.connect.v2.JobAssignment;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.SquareClientV2;
import com.squareup.connect.v2.TeamMember;
import com.squareup.connect.v2.TeamMemberAssignedLocations;
import com.squareup.connect.v2.WageSetting;

import util.SquarePayload;

public class EmployeeImport {
    private final static String MASTER_ACCOUNT_TOKEN = System.getenv("SCRIPT_ENCRYPTED_ACCOUNT_TOKEN");
    private final static String MERCHANT_ID = System.getenv("SCRIPT_MERCHANT_ID");
    private final static String ENCRYPTION_KEY = System.getenv("SCRIPT_ENCRYPTION_KEY");
    private final static String INPUT_PATH = System.getenv("SCRIPT_INPUT_PATH");

    private static final String MANAGER = "Manager";
    private static final String CASHIER = "Cashier";

    private static final String MANAGER_ROLE_ID = System.getenv("SCRIPT_MANAGER_ROLE_ID");
    private static final String CASHIER_ROLE_ID = System.getenv("SCRIPT_CASHIER_ROLE_ID");

    private final static HashMap<String, String> ROLE_PERMISSIONS;
    static {
        ROLE_PERMISSIONS = new HashMap<String, String>();
        ROLE_PERMISSIONS.put(MANAGER, MANAGER_ROLE_ID);
        ROLE_PERMISSIONS.put(CASHIER, CASHIER_ROLE_ID);
    }

    private final static String API_URL = "https://connect.squareup.com";
    private static Logger logger = LoggerFactory.getLogger(EmployeeImport.class);

    public static void main(String[] args) throws Exception {
        logger.info("Running script to import employee records...");

        SquarePayload account = new SquarePayload();
        account.setEncryptedAccessToken(MASTER_ACCOUNT_TOKEN);

        HashMap<String, String> locationsCache = new HashMap<String, String>();
        SquareClientV2 clientV2 = new SquareClientV2(API_URL, account.getAccessToken(ENCRYPTION_KEY));
        Location[] locations = clientV2.locations().list();
        for (Location location : locations) {
           // String storeId = Util.getValueInParenthesis(location.getName()).replaceAll("[^\\d]", "");
            locationsCache.put("", location.getId());
        }

        TeamMember[] teamMembers = clientV2.team().search();
        HashSet<String> teamMemberIdCache = new HashSet<String>();

        for (TeamMember tm : teamMembers) {
            if (tm.getReferenceId() != null && tm.getReferenceId().length() > 0) {
                teamMemberIdCache.add(tm.getReferenceId());
            }
        }

        SquareClient clientV1 = new SquareClient(account.getAccessToken(ENCRYPTION_KEY), API_URL, "v1", MERCHANT_ID);

        // Read employees from Excel File
        FileInputStream inputStream = new FileInputStream(new File(INPUT_PATH));

        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet firstSheet = workbook.getSheetAt(0);

        int starRow = 1;
        int endRow = firstSheet.getLastRowNum();

        for (int i = starRow; i < endRow + 1; i++) {
            TeamMember tm = new TeamMember();
            tm.setStatus("ACTIVE");

            String employeeId = firstSheet.getRow(i).getCell(0).getStringCellValue();
            tm.setReferenceId(employeeId);

            String firstName = firstSheet.getRow(i).getCell(1).getStringCellValue();
            tm.setGivenName(firstName);

            String lastName = firstSheet.getRow(i).getCell(2).getStringCellValue();
            tm.setFamilyName(lastName);

            String rawLocationIds = firstSheet.getRow(i).getCell(4).getStringCellValue();
            String[] splitRawLocationIds = rawLocationIds.split("\n");

            ArrayList<String> locationIds = new ArrayList<String>();
            for (String l : splitRawLocationIds) {
                l = l.trim().replace(" ", "");

                String locationId = locationsCache.get(l);

                if (locationId != null) {
                    locationIds.add(locationId);
                }
            }

            if (locationIds.size() < 1) {
                System.out.println("Location(s) assignment invalid. Skipping employee: " + employeeId);
                continue;
            }

            if (teamMemberIdCache.contains(employeeId)) {
                System.out.println("Employee already exists. Skipping: " + employeeId);
                continue;
            }

            System.out.println(tm.getReferenceId() + " " + tm.getGivenName());

            TeamMemberAssignedLocations assignedLocations = new TeamMemberAssignedLocations();
            assignedLocations.setAssignmentType("EXPLICIT_LOCATIONS");
            assignedLocations.setLocationIds(locationIds.toArray(new String[locationIds.size()]));
            tm.setAssignedLocations(assignedLocations);

            /*
            if (firstSheet.getRow(i).getCell(7) != null) {
                String email = firstSheet.getRow(i).getCell(7).getStringCellValue();
                tm.setEmailAddress(email);
            }*/

            String jobTitle = firstSheet.getRow(i).getCell(3).getStringCellValue();
            String permissionGroup = firstSheet.getRow(i).getCell(3).getStringCellValue();

            TeamMember teamMember = clientV2.team().createTeamMember(tm);

            // Update wage
            WageSetting wageSetting = new WageSetting();
            wageSetting.setTeamMemberId(teamMember.getId());
            wageSetting.setOvertimeExcempt(false);

            JobAssignment job = new JobAssignment();
            job.setJobTitle(jobTitle);
            job.setPayType("NONE");
            wageSetting.setJobAssignments(new JobAssignment[] { job });
            clientV2.team().updateWageSetting(teamMember.getId(), wageSetting);

            // Update employee to assign roles with v1 API
            Employee e = new Employee();
            e.setId(teamMember.getId());
            String permissionId = permissionGroup.contains(MANAGER) ? ROLE_PERMISSIONS.get(MANAGER)
                    : ROLE_PERMISSIONS.get(CASHIER);
            e.setRoleIds(new String[] { permissionId });
            Employee employee = clientV1.employees().update(e.getId(), e);

            logger.info(employee.toString());
        }
        inputStream.close();

        logger.info("Done.");
    }
}
