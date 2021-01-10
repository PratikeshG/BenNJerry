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

import com.squareup.connect.Refund;
import com.squareup.connect.v2.Location;

import util.Constants;
import util.LocationContext;
import util.SquarePayload;
import util.TimeManager;
import util.reports.RefundsReportBuilder;

public class AggregateLocationsPreviousDayRefundsCallable implements Callable {
	@Value("${encryption.key.tokens}")
	private String encryptionKey;

	private final String UTC = "UTC";

	/**
	 * Maps location {@code Id} to {@code Refund}s for a merchants location over
	 * a previous day interval defined via session vars {@code range} and
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

		HashMap<String, List<Refund>> locationsRefunds = new RefundsReportBuilder(apiUrl, accessToken, merchantId)
				.forLocations(locations).forPastDayInterval(range, offset).build();

		setRefundsDatesToLocalTime(locationsRefunds, locationContexts);

		return locationsRefunds;
	}

	private void setRefundsDatesToLocalTime(HashMap<String, List<Refund>> locationsPayments,
			HashMap<String, LocationContext> locationContexts) throws ParseException {
		for (Map.Entry<String, List<Refund>> locationPaymenEntry : locationsPayments.entrySet()) {
			for (Refund refund : locationPaymenEntry.getValue()) {
				Location location = locationContexts.get(locationPaymenEntry.getKey()).getLocation();
				String timeZone = location.getTimezone();
				refund.setCreatedAt(TimeManager.convertToLocalTime(refund.getCreatedAt(), timeZone));
			}
		}
	}
}
