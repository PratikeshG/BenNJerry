package vfcorp.smartwool;

import java.util.Calendar;
import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.v2.Location;

import util.SquarePayload;
import util.TimeManager;
import util.reports.PaymentsReportBuilder;

public class AggregateLocationsPreviousDayPaymentsCallable implements Callable {
	@Value("${vfcorp.smartwool.range}")
	private String VAR_RANGE;
	@Value("${vfcorp.smartwool.offset}")
	private String VAR_OFFSET;
	@Value("${encryption.key.tokens}")
    private String encryptionKey;

	private final String VAR_API_URL = "apiUrl";
	private final String VAR_CREATED_AT = "createdAt";
	private final String VAR_MERCHANT_DETAILS = "merchantDetails";

	private final String UTC = "UTC";
	/**
	 * Maps location {@code Id} to {@code Payment}s for a merchants location over a previous day interval
	 * defined via session vars {@code range} and {@code offset}. Sets var {@code createdAt} on session.
	 */
	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		MuleMessage message = eventContext.getMessage();

		@SuppressWarnings("unchecked")
		List<Location> locations = (List<Location>) message.getPayload();
		SquarePayload merchantDetails = (SquarePayload) message.getProperty(VAR_MERCHANT_DETAILS, PropertyScope.SESSION);

		String apiUrl = message.getProperty(VAR_API_URL, PropertyScope.SESSION);
		String merchantId = merchantDetails.getMerchantId();
		String accessToken = merchantDetails.getAccessToken(this.encryptionKey);

		int range = Integer.parseInt(VAR_RANGE);
		int offset = Integer.parseInt(VAR_OFFSET);

		message.setProperty(VAR_CREATED_AT, TimeManager.toIso8601(Calendar.getInstance(), UTC), PropertyScope.SESSION);

		return new PaymentsReportBuilder(apiUrl, accessToken, merchantId)
				.forLocations(locations)
				.forPastDayInterval(range, offset)
				.build();
	}
}
