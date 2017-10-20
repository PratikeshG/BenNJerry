package scripts;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.jdbc.ResultSet;

import util.SquarePayload;

public class EncryptDatabaseAccessTokens {
    private final static String TOKEN_ENCRYPTION_KEY = System.getenv("SCRIPT_TOKEN_ENCRYPTION_KEY");
    private final static String DATABASE_HOST = System.getenv("SCRIPT_DATABASE_HOST");
    private final static String DATABASE_PORT = System.getenv("SCRIPT_DATABASE_PORT");
    private final static String DATABASE_DB = System.getenv("SCRIPT_DATABASE_DB");
    private final static String DATABASE_USERNAME = System.getenv("SCRIPT_DATABASE_USERNAME");
    private final static String DATABASE_PASSWORD = System.getenv("SCRIPT_DATABASE_PASSWORD");

    private final static String DATABASE_URL = String.format("jdbc:mysql://%s:%s/%s", DATABASE_HOST, DATABASE_PORT,
            DATABASE_DB);

    private final static String SELECT_QUERY = "SELECT id, token FROM token WHERE token IS NOT NULL;";
    private final static String UPDATE_QUERY_TEMPLATE = "UPDATE `token` SET encryptedAccessToken = '%s' WHERE id = '%s';";

    private static Logger logger = LoggerFactory.getLogger(EncryptDatabaseAccessTokens.class);

    public static void main(String[] args) throws Exception {
        logger.info("Running script to encrypt database tokensX...");

        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);
        Statement stmt = conn.createStatement();

        ResultSet result = (ResultSet) stmt.executeQuery(SELECT_QUERY);

        while (result.next()) {
            String id = result.getString("id");
            String token = result.getString("token");

            SquarePayload tokenEncrypt = new SquarePayload();
            tokenEncrypt.encryptAccessToken(token, TOKEN_ENCRYPTION_KEY);

            String updateString = String.format(UPDATE_QUERY_TEMPLATE, tokenEncrypt.getEncryptedAccessToken(), id);

            Statement updateStatement = conn.createStatement();
            updateStatement.executeUpdate(updateString);

            logger.info(String.format("Updating token ID %s", id));
        }

        logger.info("Done.");
    }
}
