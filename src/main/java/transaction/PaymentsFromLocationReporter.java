package transaction;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;

import com.squareup.connect.Payment;
import com.squareup.connect.SquareClient;

public class PaymentsFromLocationReporter implements Callable {

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		SquareAccount sa = (SquareAccount) eventContext.getMessage().getPayload();
		
		SquareClient client = new SquareClient(sa.getAccessToken(), "https://connect.squareupstaging.com/v1", sa.getSquareId());
        
        Payment[] payments = client.payments().get();
        
        if (payments != null) {
        	return payments;
        } else {
        	return null;
        }
	}
}
