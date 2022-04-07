package vfcorp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.v2.SquareClientV2;
import com.squareup.connect.v2.TeamMember;

import util.SquarePayload;

public class EmployeesToDatabaseCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(EmployeesToDatabaseCallable.class);

    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}?autoReconnect=true")
    private String databaseUrl;
    @Value("${mysql.user}")
    private String databaseUser;
    @Value("${mysql.password}")
    private String databasePassword;
    @Value("${api.url}")
    private String apiUrl;
    @Value("${encryption.key.tokens}")
    private String encryptionKey;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        String brand = (String) message.getProperty("brand", PropertyScope.SESSION);
        int minEmployees = Integer.parseInt(message.getProperty("minEmployees", PropertyScope.INVOCATION));

        // Get employees from all master accounts for this deployment
        ArrayList<SquarePayload> masterAccounts = (ArrayList<SquarePayload>) Util.getMasterAccountsForBrand(databaseUrl,
                databaseUser, databasePassword, brand);

        ArrayList<TeamMember> teamMembers = new ArrayList<TeamMember>();

        for (SquarePayload account : masterAccounts) {
            SquareClientV2 client = new SquareClientV2(apiUrl, account.getAccessToken(encryptionKey));
            client.setLogInfo(account.getMerchantId());
            TeamMember[] team = client.team().search();

            teamMembers.addAll(Arrays.asList(team));
        }

        logger.info("" + teamMembers.size());
        if (teamMembers.size() < minEmployees) {
            logger.info("DO NOTHING - EMPLOYEES NUMBER SEEMS INVALID");
            return null;
        }

        for (TeamMember tm : teamMembers) {
            if (tm.getReferenceId() == null) {
                tm.setReferenceId("");
            }
        }

        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
        VfcDatabaseApi databaseApi = new VfcDatabaseApi(conn);

        String deployment = "vfcorp-" + brand;
        databaseApi.deleteEmployeesForBrand(deployment);
        databaseApi.setEmployeesForBrand(deployment, teamMembers.toArray(new TeamMember[teamMembers.size()]));

        return null;
    }
}
