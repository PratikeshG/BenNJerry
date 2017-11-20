package vfcorp.smartwool;

import java.util.Calendar;
import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.v2.Location;

import util.Constants;
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
		List<Location> locations = (List<Location>) message.getProperty(Constants.LOCATIONS, PropertyScope.SESSION);
		SquarePayload sqPayload = (SquarePayload) message.getProperty(Constants.SQUARE_PAYLOAD, PropertyScope.SESSION);

		String apiUrl = message.getProperty(Constants.API_URL, PropertyScope.SESSION);
		String merchantId = sqPayload.getMerchantId();
		String accessToken = sqPayload.getAccessToken(this.encryptionKey);

		message.setProperty(Constants.CREATED_AT, TimeManager.toIso8601(Calendar.getInstance(), UTC),
				PropertyScope.SESSION);

		message.setProperty(Constants.PAYMENTS, new PaymentsReportBuilder(apiUrl, accessToken, merchantId)
				.forLocations(locations).forPastDayInterval(range, offset).build(), PropertyScope.SESSION);

		return message.getPayload();
	}
}
