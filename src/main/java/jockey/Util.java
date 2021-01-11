package jockey;

import java.io.IOException;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class Util {
    public static String getAmountAsXmlDecimal(int amount) {
        boolean isNegative = amount < 0 ? true : false;

        amount = Math.abs(amount);
        String amt = Integer.toString(amount);
        if (amt.length() == 1) {
            amt = "0.0" + amt + "00";
        } else if (amt.length() == 2) {
            amt = "0." + amt + "00";
        } else {
            amt = amt.substring(0, amt.length() - 2) + "." + amt.substring(amt.length() - 2) + "00";
        }

        if (isNegative) {
            amt = "-" + amt;
        }

        return amt;
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
}
