package tntfireworks;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;

public class AwsS3Util implements Callable {
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

        SyncToDatabaseRequest request = (SyncToDatabaseRequest) message.getPayload();
/*
        Session session = Util.createSSHSession(sftpHost, sftpUser, sftpPassword, sftpPort);
        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();
        System.out.println("SFTP AWS channel created.");

        System.out.println("Saving pluStreamReaderAWS stream...");
        sftpChannel.cd(request.getDeployment().getPluPath() + "/processing");

        InputStream is = sftpChannel.get(request.getProcessingFileName());

        message.setProperty("pluSyncToDatabaseRequest", request, PropertyScope.INVOCATION);
        message.setProperty("pluAwsKey",
                String.format("TNF/%s/PLU/%s", request.getDeployment().getStoreId(), request.getProcessingFileName()),
                PropertyScope.INVOCATION);
        message.setProperty("pluStreamReaderAws", is, PropertyScope.INVOCATION);
        message.setProperty("sftpChannel", sftpChannel, PropertyScope.INVOCATION);
*/
        return request;
    }

}
