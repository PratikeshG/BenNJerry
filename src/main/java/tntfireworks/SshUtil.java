package tntfireworks;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SshUtil {
    private static Logger logger;

    public static ChannelSftp createConnection(String sftpHost, int sftpPort, String sftpUser, String sftpPassword)
            throws JSchException, IOException {
        // initialize local variables
        ChannelSftp sftpChannel;
        Session session;
        JSch jsch = new JSch();
        logger = LoggerFactory.getLogger(SshUtil.class);

        // initialize session properties
        session = jsch.getSession(sftpUser, sftpHost, sftpPort);
        session.setPassword(sftpPassword);
        session.setConfig("StrictHostKeyChecking", "no");

        // connection to session
        session.connect();
        logger.info(String.format("TNT: SFTP Session connection established to host: %s", sftpHost));

        // open/connect channel
        sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();
        logger.info("TNT: SFTP channel created.");

        return sftpChannel;
    }

    public static void closeConnection(ChannelSftp sftpChannel) throws JSchException {
        logger = LoggerFactory.getLogger(SshUtil.class);

        if (sftpChannel != null && sftpChannel.isConnected()) {
            sftpChannel.disconnect();
            logger.info("TNT: SFTP channel closed.");

            Session session = sftpChannel.getSession();
            if (session != null && session.isConnected()) {
                session.disconnect();
                logger.info("TNT: SFTP session closed.");
            }
        }
    }
}
