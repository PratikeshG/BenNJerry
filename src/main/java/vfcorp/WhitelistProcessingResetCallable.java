package vfcorp;

import java.util.Vector;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.Session;

public class WhitelistProcessingResetCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(WhitelistProcessingResetCallable.class);

    @Value("${vfcorp.sftp.host}")
    private String sftpHost;
    @Value("${vfcorp.sftp.port}")
    private int sftpPort;
    @Value("${vfcorp.sftp.username}")
    private String sftpUser;
    @Value("${vfcorp.sftp.password}")
    private String sftpPassword;

    @Value("${vfcorp.vans.sftp.path.whitelist}")
    private String sftpPath;

    private static final String PROCESSING_PREFIX = "processing_";

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        Session session = Util.createSSHSession(sftpHost, sftpUser, sftpPassword, sftpPort);
        logger.info("VFC: SFTP session created");

        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();
        logger.info("VFC: SFTP channel created");

        sftpChannel.cd(sftpPath);

        @SuppressWarnings("unchecked")
        Vector<LsEntry> processingWhitelistFiles = sftpChannel.ls(PROCESSING_PREFIX + "*");
        for (ChannelSftp.LsEntry entry : processingWhitelistFiles) {
            String processingFile = entry.getFilename();
            logger.info(String.format("Found and re-queuing stale whitelist %s...", processingFile));
            sftpChannel.rename(processingFile, processingFile.split(PROCESSING_PREFIX)[1]);
        }

        sftpChannel.disconnect();
        logger.info("VFC: SFTP channel disconnected");

        session.disconnect();
        logger.info("VFC: SFTP session disconnected");

        return 1;
    }
}
