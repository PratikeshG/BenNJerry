package vfcorp;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.mysql.jdbc.ResultSet;

public class Util {

    public static String getValueBetweenChars(String input, char c, char d) {
        String value = "";

        if (input != null) {
            int firstIndex = input.indexOf(c);
            int lastIndex = input.indexOf(d);
            if (firstIndex > -1 && lastIndex > -1 && lastIndex > firstIndex) {
                value = input.substring(firstIndex + 1, lastIndex);
            }
        }

        return value;
    }

    public static String getValueInBrackets(String input) {
        return getValueBetweenChars(input, '[', ']');
    }

    public static String getValueInParenthesis(String input) {
        return getValueBetweenChars(input, '(', ')');
    }

    public static String getRegisterNumber(String deviceName) {
        String registerNumber = "99"; // default

        if (deviceName != null) {
            String n = getValueInParenthesis(deviceName).replaceAll("[^\\d]", "");
            if (n.length() > 0) {
                registerNumber = n;
            }
        }

        // Pad to three characters with left zeros
        return String.format("%03d", Integer.parseInt(registerNumber));
    }

    public static String getStoreNumber(String input) {
        String n = getValueInParenthesis(input).replaceAll("[^\\d]", "");
        String storeNumber = n.length() > 0 ? n : "0";

        // Pad to five characters with left zeros
        return String.format("%05d", Integer.parseInt(storeNumber));
    }

    public static int[] divideIntegerEvenly(int amount, int totalPieces) {
        int quotient = amount / totalPieces;
        int remainder = amount % totalPieces;

        int[] results = new int[totalPieces];
        for (int i = 0; i < totalPieces; i++) {
            results[i] = i < remainder ? quotient + 1 : quotient;
        }

        // Reverse - provide smallest discounts first
        for (int i = 0; i < results.length / 2; i++) {
            int temp = results[i]; // swap numbers
            results[i] = results[results.length - 1 - i];
            results[results.length - 1 - i] = temp;
        }

        return results;
    }

    public static Session createSSHSession(String host, String username, String password, int port)
            throws JSchException, IOException {
        JSch jsch = new JSch();

        Session session = jsch.getSession(username, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");

        System.out.println("Establishing Connection...");
        session.connect();
        System.out.println("Connection established.");

        return session;
    }

    public static List<VFCDeployment> getVFCDeployments(String host, String user, String password, String whereFilter)
            throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(host, user, password);
        Statement stmt = conn.createStatement();

        String query = "SELECT vfcorp_deployments.deployment as deployment, storeId, timeZone, "
                + "pluPath, pluFiltered, tlogPath, tlogRange, tlogOffset, token, merchantId, locationId FROM vfcorp_deployments "
                + "LEFT JOIN token ON vfcorp_deployments.deployment = token.deployment WHERE " + whereFilter + ";";

        ResultSet result = (ResultSet) stmt.executeQuery(query);

        ArrayList<VFCDeployment> deployments = new ArrayList<VFCDeployment>();
        while (result.next()) {
            VFCDeployment deployment = new VFCDeployment();
            deployment.setDeployment(result.getString("deployment"));
            deployment.setStoreId(result.getString("storeId"));
            deployment.setTimeZone(result.getString("timeZone"));
            deployment.setPluPath(result.getString("pluPath"));
            deployment.setPluFiltered(result.getBoolean("pluFiltered"));
            deployment.setTlogPath(result.getString("tlogPath"));
            deployment.setTlogRange(result.getInt("tlogRange"));
            deployment.setTlogOffset(result.getInt("tlogOffset"));
            deployment.setAccessToken(result.getString("token"));
            deployment.setMerchantId(result.getString("merchantId"));
            deployment.setLocationId(result.getString("locationId"));

            deployments.add(deployment);
        }

        return deployments;
    }
}
