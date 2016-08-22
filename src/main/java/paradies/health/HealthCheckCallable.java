package paradies.health;

import paradies.health.HealthCheckPayload;

import com.squareup.connect.Payment;
import com.squareup.connect.Tender;

import org.mule.api.MuleMessage;

import java.util.ArrayList;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;

public class HealthCheckCallable implements Callable {

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		MuleMessage message = eventContext.getMessage();
		HealthCheckPayload healthCheckPayload = (HealthCheckPayload) message.getPayload();

		// Check for payments with invalid tender types
		ArrayList<Payment> invalidPayments = new ArrayList<Payment>();
		for (Payment payment : healthCheckPayload.getPayments()) {
			for (Tender tender : payment.getTender()) {
				if (!validTender(tender)) {
					invalidPayments.add(payment);
				}
			}
		}

		healthCheckPayload.setPayments(invalidPayments);
		return healthCheckPayload;
	}
	
	public static boolean validTender(Tender tender) {
		if (tender.getType().equals("CREDIT_CARD") ||
				tender.getType().equals("NO_SALE") ||
				(tender.getType().equals("OTHER") && tender.getName().equals("MERCHANT_GIFT_CARD")) ||
				(tender.getType().equals("OTHER") && tender.getName().equals("CUSTOM"))) {
			return true;
		}
		return false;
	}
}
