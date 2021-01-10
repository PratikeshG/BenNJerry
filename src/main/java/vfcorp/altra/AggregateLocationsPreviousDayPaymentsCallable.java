package vfcorp.altra;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.Payment;
import com.squareup.connect.Refund;
import com.squareup.connect.v2.Location;

import util.Constants;
import util.LocationContext;
import util.SquarePayload;
import util.TimeManager;
import util.reports.PaymentsReportBuilder;

public class AggregateLocationsPreviousDayPaymentsCallable implements Callable {

	@Value("${encryption.key.tokens}")
	private String encryptionKey;

	private final String UTC = "UTC";

	/**
	 * Maps location {@code Id} to {@code Payment}s for a merchants location
	 * over a previous day interval defined via session vars {@code range} and
	 * {@code offset}. Sets var {@code createdAt} on session.
	 */
	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		MuleMessage message = eventContext.getMessage();

		int range = Integer.parseInt(message.getProperty(Constants.RANGE, PropertyScope.SESSION));
		int offset = Integer.parseInt(message.getProperty(Constants.OFFSET, PropertyScope.SESSION));

		@SuppressWarnings("unchecked")
		HashMap<String, LocationContext> locationContexts = (HashMap<String, LocationContext>) message
				.getProperty(Constants.LOCATION_CONTEXT_MAP, PropertyScope.INVOCATION);
		ArrayList<Location> locations = new ArrayList<Location>();
		locationContexts.forEach((locId, locCtx) -> locations.add(locCtx.getLocation()));

		SquarePayload sqPayload = (SquarePayload) message.getProperty(Constants.SQUARE_PAYLOAD, PropertyScope.SESSION);

		String apiUrl = message.getProperty(Constants.API_URL, PropertyScope.SESSION);
		String merchantId = sqPayload.getMerchantId();
		String accessToken = sqPayload.getAccessToken(this.encryptionKey);

		message.setProperty(Constants.CREATED_AT, TimeManager.toIso8601(Calendar.getInstance(), UTC),
				PropertyScope.SESSION);

		HashMap<String, List<Payment>> locationsPayments = new PaymentsReportBuilder(apiUrl, accessToken, merchantId)
				.forLocations(locations).forPastDayInterval(range, offset).build();

		setPaymentsDatesToLocalTime(locationsPayments, locationContexts);

		message.setProperty(Constants.PAYMENTS, locationsPayments, PropertyScope.INVOCATION);

		return message.getPayload();
	}

	private void setPaymentsDatesToLocalTime(HashMap<String, List<Payment>> locationsPayments,
			HashMap<String, LocationContext> locationContexts) throws ParseException {
		for (Map.Entry<String, List<Payment>> locationPaymenEntry : locationsPayments.entrySet()) {
			for (Payment payment : locationPaymenEntry.getValue()) {
				Location location = locationContexts.get(locationPaymenEntry.getKey()).getLocation();
				String timeZone = location.getTimezone();
				payment.setCreatedAt(TimeManager.convertToLocalTime(payment.getCreatedAt(), timeZone));
				for (Refund refund : payment.getRefunds()) {
					refund.setCreatedAt(TimeManager.convertToLocalTime(refund.getCreatedAt(), timeZone));
					refund.setProcessedAt(TimeManager.convertToLocalTime(refund.getProcessedAt(), timeZone));
				}
			}
		}
	}
}
