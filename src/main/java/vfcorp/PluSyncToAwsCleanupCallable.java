package vfcorp;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;

public class PluSyncToAwsCleanupCallable implements Callable {

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        ChannelSftp sftpChannel = (ChannelSftp) message.getProperty("sftpChannel", PropertyScope.INVOCATION);

        if (sftpChannel != null && sftpChannel.isConnected()) {
            sftpChannel.disconnect();
            System.out.println("SFTP AWS channel disconnected.");

            Session session = sftpChannel.getSession();
            if (session != null && session.isConnected()) {
                session.disconnect();
                System.out.println("SFTP AWS session disconnected.");
            }
        }

        return null;
    }
}
