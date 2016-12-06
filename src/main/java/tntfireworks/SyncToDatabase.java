package tntfireworks;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;

public class SyncToDatabase implements Callable {

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        return new String();
    }
}
