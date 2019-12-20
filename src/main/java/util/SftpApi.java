package util;

import java.io.IOException;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SftpApi {
    public static Session createSSHSession(String host, String username, String password, int port)
            throws JSchException, IOException {
        JSch jsch = new JSch();

        Session session = jsch.getSession(username, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");

        System.out.println("Establishing SSH (SFTP) Connection...");
        session.connect();
        System.out.println("SSH (SFTP) connection established.");

        return session;
    }
}
