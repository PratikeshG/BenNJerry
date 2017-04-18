package vfcorp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;

public class TlogUploadToSftpCallable implements Callable {
    private static final String TLOG_PREFIX = "SA";
    private static final String TLOG_SUFFIX = ".NEW";

    private String sftpHost;
    private int sftpPort;
    private String sftpUser;
    private String sftpPassword;

    public void setSftpHost(String sftpHost) {
        this.sftpHost = sftpHost;
    }

    public void setSftpPort(int sftpPort) {
        this.sftpPort = sftpPort;
    }

    public void setSftpUser(String sftpUser) {
        this.sftpUser = sftpUser;
    }

    public void setSftpPassword(String sftpPassword) {
        this.sftpPassword = sftpPassword;
    }

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        String tlog = message.getProperty("tlog", PropertyScope.INVOCATION);
        String vfcorpStoreNumber = message.getProperty("vfcorpStoreNumber", PropertyScope.INVOCATION);
        VfcDeployment deployment = message.getProperty("tlogVfcDeployment", PropertyScope.INVOCATION);

        String uploadPattern = TLOG_PREFIX + vfcorpStoreNumber + TLOG_SUFFIX;
        InputStream uploadStream = new ByteArrayInputStream(tlog.getBytes("UTF-8"));

        Session session = Util.createSSHSession(sftpHost, sftpUser, sftpPassword, sftpPort);
        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();

        sftpChannel.cd(deployment.getTlogPath());
        sftpChannel.put(uploadStream, uploadPattern, ChannelSftp.OVERWRITE);

        sftpChannel.disconnect();
        session.disconnect();

        return tlog;
    }
}
