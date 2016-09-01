package paradies.health;

import paradies.health.HealthCheckPayload;

import com.squareup.connect.Payment;
import com.squareup.connect.Refund;
import com.squareup.connect.Tender;

import org.mule.api.MuleMessage;

import java.util.ArrayList;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

public class HealthCheckCallable implements Callable {

	private static final String ERROR_INVALID_TENDER =  "Invalid payment tender";
	private static final String ERROR_PARTIAL_REFUND =  "Partial refund";
	
	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		MuleMessage message = eventContext.getMessage();
		HealthCheckPayload healthCheckPayload = (HealthCheckPayload) message.getPayload();

		ArrayList<HealthCheckErrorPayload> errors = new ArrayList<HealthCheckErrorPayload>();

		// Invalid tenders
		for (Payment payment : healthCheckPayload.getPayments()) {
			for (Tender tender : payment.getTender()) {
				if (!validTender(tender)) {
					errors.add(new HealthCheckErrorPayload(healthCheckPayload.getLocation(), payment, ERROR_INVALID_TENDER));
				}
			}
		}

		// Partial refunds
		for (Refund refund : healthCheckPayload.getRefunds()) {
			if (!refund.getType().equals("FULL")) {
				// Find the matching payment (need to match tender IDs)
				Payment refundPayment = null;
				for (Payment payment : healthCheckPayload.getRefundPayments()) {
					for (Tender tender : payment.getTender()) {
						if (tender.getId().equals(refund.getPaymentId())) {
							refundPayment = payment;
							break;
						}
					}
				}
				errors.add(new HealthCheckErrorPayload(healthCheckPayload.getLocation(), refundPayment, ERROR_PARTIAL_REFUND));
			}
		}

		message.setProperty("storeName", healthCheckPayload.getLocation().getLocationDetails().getNickname(), PropertyScope.INVOCATION);

		return errors;
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
