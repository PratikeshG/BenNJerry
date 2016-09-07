package paradies.health;

import paradies.health.HealthCheckPayload;
import util.TimeManager;

import com.squareup.connect.Payment;
import com.squareup.connect.PaymentItemization;
import com.squareup.connect.Refund;
import com.squareup.connect.Tender;

import org.mule.api.MuleMessage;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

public class HealthCheckCallable implements Callable {

	private static final String ERROR_INVALID_CUSTOM_AMOUNT = "Invalid 'custom amount' item";
	private static final String ERROR_INVALID_TENDER = "Invalid payment tender";
	private static final String ERROR_PARTIAL_REFUND = "Partial refund";
	private static final String ERROR_REFUND_DATE = "Refund from previous sales date";

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
					break; // move on to the next payment
				}
			}
		}

		// Invalid use of custom amount
		for (Payment payment : healthCheckPayload.getPayments()) {
			for (PaymentItemization itemization : payment.getItemizations()) {
				if (itemization.getItemizationType().equals("CUSTOM_AMOUNT")) {
					errors.add(new HealthCheckErrorPayload(healthCheckPayload.getLocation(), payment, ERROR_INVALID_CUSTOM_AMOUNT));
					break; // move on to the next payment
				}
			}
		}

		// Partial and stale (not initiated on same day) refunds
		for (Refund refund : healthCheckPayload.getRefunds()) {
			if (!refund.getType().equals("FULL")) {
				Payment refundPayment = getRefundPayment(healthCheckPayload.getRefundPayments(), refund.getPaymentId());
				errors.add(new HealthCheckErrorPayload(healthCheckPayload.getLocation(), refundPayment, ERROR_PARTIAL_REFUND));
			}

			/* Note: This should probably calculate the current day based on
			 * timezone of the location, but for simplicity's sake, just going to
			 * check that day matches on PST time.
			 */
			if (dateBeforeToday(refund.getCreatedAt())) {
				Payment refundPayment = getRefundPayment(healthCheckPayload.getRefundPayments(), refund.getPaymentId());
				errors.add(new HealthCheckErrorPayload(healthCheckPayload.getLocation(), refundPayment, ERROR_REFUND_DATE));
			}
		}

		message.setProperty("storeName", healthCheckPayload.getLocation().getLocationDetails().getNickname(), PropertyScope.INVOCATION);

		return errors;
	}

	private static Payment getRefundPayment(List<Payment> refundPayments, String refundTenderId) {
		Payment refundPayment = null;
		for (Payment payment : refundPayments) {
			for (Tender tender : payment.getTender()) {
				if (tender.getId().equals(refundTenderId)) {
					return payment;
				}
			}
		}
		return refundPayment;
	}

	private static boolean dateBeforeToday(String iso8601string) throws ParseException {
		Calendar calToday = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
		calToday.set(Calendar.MILLISECOND, 0);
		calToday.set(Calendar.SECOND, 0);
		calToday.set(Calendar.MINUTE, 0);
		calToday.set(Calendar.HOUR_OF_DAY, 0);

		Calendar calRefund = TimeManager.toCalendar(iso8601string);
		return calRefund.before(calToday);
	}

	private static boolean validTender(Tender tender) {
		if (tender.getType().equals("CREDIT_CARD") ||
				tender.getType().equals("NO_SALE") ||
				(tender.getType().equals("OTHER") && tender.getName().equals("MERCHANT_GIFT_CARD")) ||
				(tender.getType().equals("OTHER") && tender.getName().equals("CUSTOM"))) {
			return true;
		}
		return false;
	}
}
