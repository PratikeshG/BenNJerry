package vfcorp;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class PluProcessingResetCallable implements Callable {
    private static Logger logger = LoggerFactory.getLogger(PluProcessingResetCallable.class);

    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}")
    private String databaseUrl;
    @Value("${mysql.user}")
    private String databaseUser;
    @Value("${mysql.password}")
    private String databasePassword;

    @Value("${vfcorp.sftp.host}")
    private String sftpHost;
    @Value("${vfcorp.sftp.port}")
    private int sftpPort;
    @Value("${vfcorp.sftp.username}")
    private String sftpUser;
    @Value("${vfcorp.sftp.password}")
    private String sftpPassword;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        String brand = message.getProperty("brand", PropertyScope.INVOCATION);
        String whereFilter = String.format("vfcorp_deployments.deployment LIKE 'vfcorp-%s-%%'", brand);

        ArrayList<VfcDeployment> deployments = (ArrayList<VfcDeployment>) Util.getVfcDeployments(databaseUrl,
                databaseUser, databasePassword, whereFilter);

        Session session = Util.createSSHSession(sftpHost, sftpUser, sftpPassword, sftpPort);
        logger.info("VFC: SFTP session created");

        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();
        logger.info("VFC: SFTP channel created");

        queueStaleSyncRequests(sftpChannel, deployments);

        sftpChannel.disconnect();
        logger.info("VFC: SFTP channel disconnected");

        session.disconnect();
        logger.info("VFC: SFTP session disconnected");

        return 1;
    }

    private void queueStaleSyncRequests(ChannelSftp channel, List<VfcDeployment> deployments)
            throws SftpException, ParseException, InterruptedException {

        ArrayList<PluSyncToDatabaseRequest> syncRequests = new ArrayList<PluSyncToDatabaseRequest>();
        for (VfcDeployment deployment : deployments) {
            channel.cd(deployment.getPluPath());

            Vector<LsEntry> processingFiles = channel
                    .ls(String.format("%s/*_plu.chg*", Constants.PROCESSING_DIRECTORY));
            for (ChannelSftp.LsEntry entry : processingFiles) {
                logger.info(String.format("Found stale PLU file for deployment %s: %s", entry.getFilename(),
                        deployment.getDeployment()));

                queueFileForReProcessing(channel, entry.getFilename(), deployment);
            }
        }
    }

    private void queueFileForReProcessing(ChannelSftp channel, String processingFile, VfcDeployment deployment)
            throws SftpException {
        logger.info(String.format("Re-queuing %s for deployment %s...", processingFile, deployment.getDeployment()));

        String processingPath = String.format("%s/%s/%s", deployment.getPluPath(), Constants.PROCESSING_DIRECTORY,
                processingFile);
        String requeuePath = String.format("%s/%s", deployment.getPluPath(), processingFile.split("_", 2)[1]);

        channel.rename(processingPath, requeuePath);
    }
}