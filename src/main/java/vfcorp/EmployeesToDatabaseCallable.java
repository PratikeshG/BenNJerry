package vfcorp;

import java.sql.Connection;
import java.sql.DriverManager;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.v2.SquareClientV2;
import com.squareup.connect.v2.TeamMember;

public class EmployeesToDatabaseCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(EmployeesToDatabaseCallable.class);

    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}")
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

        // Retrieve a single deployment for credentials for master account
        VfcDeployment masterAccount = Util.getMasterAccountDeployment(databaseUrl, databaseUser, databasePassword,
                brand);

        SquareClientV2 client = new SquareClientV2(apiUrl,
                masterAccount.getSquarePayload().getAccessToken(encryptionKey));
        client.setLogInfo(masterAccount.getSquarePayload().getMerchantId());

        TeamMember[] teamMembers = client.team().search();

        logger.info("" + teamMembers.length);
        if (teamMembers.length < minEmployees) {
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

        databaseApi.setEmployeesForBrand(deployment, teamMembers);

        return null;
    }
}
