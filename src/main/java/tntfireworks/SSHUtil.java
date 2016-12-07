package tntfireworks;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SSHUtil {
    private String sftpHost;
    private int sftpPort;
    private String sftpUser;
    private String sftpPassword;
    private static Logger logger;
    
    public static ChannelSftp createConnection(String sftpHost, int sftpPort, String sftpUser, String sftpPassword) 
            throws JSchException, IOException {
        // initialize local variables
        ChannelSftp sftpChannel;
        Session session;
        JSch jsch = new JSch();
        logger = LoggerFactory.getLogger(SSHUtil.class);  

        // initialize session properties
        session = jsch.getSession(sftpUser, sftpHost, sftpPort);
        session.setPassword(sftpPassword);
        session.setConfig("StrictHostKeyChecking", "no");

        // connection to session
        logger.info(String.format("Establishing SFTP connection to host: %s", sftpHost));
        session.connect();
        logger.info(String.format("Connection established to host: %s", sftpHost));
       
        // open/connect channel
        sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();
        logger.info("SFTP channel created.");

        return sftpChannel;
    }
    
    public static void closeConnection(ChannelSftp sftpChannel) throws JSchException {
        logger = LoggerFactory.getLogger(SSHUtil.class);

        if (sftpChannel != null && sftpChannel.isConnected()) {
            sftpChannel.disconnect();
            logger.info("SFTP-AWS channel disconnected.");

            Session session = sftpChannel.getSession();
            if (session != null && session.isConnected()) {
                session.disconnect();
                logger.info("SFTP-AWS session disconnected.");

            }
        }               
    }
}
