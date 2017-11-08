package util.reports;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.squareup.connect.Payment;
import com.squareup.connect.SquareClient;
import com.squareup.connect.v2.Location;

/**
 *
 * @author finci
 *
 */
public class PaymentsReportBuilder extends AbstractReportBuilder<Payment> {
	/**
	 * Creates a report map of location Id to Payments filtered by date range
	 * set by calling {@code forPastDayInterval(int range, int offset)}.
	 * @param apiUrl
	 * @param accessToken
	 * @param merchantId
	 */
	public PaymentsReportBuilder(String apiUrl, String accessToken, String merchantId) {
		super(apiUrl, accessToken, merchantId);
	}
	/**
	 * {@code clientOverride} provided for test.
	 * @param apiUrl
	 * @param accessToken
	 * @param merchantId
	 * @param clientOverride
	 */
	public PaymentsReportBuilder(String apiUrl, String accessToken, String merchantId, SquareClient clientOverride) {
		super(apiUrl, accessToken, merchantId, clientOverride);
	}
	/**
	 * Build map.
	 */
	public HashMap<String, List<Payment>> build() throws Exception {
		HashMap<String, List<Payment>> locationsPayments = new HashMap<String, List<Payment>>();
		for (Location location : this.getLocations()) {
			this.processLocation(location, locationsPayments);
		}
		return locationsPayments;
	}
	private void processLocation(Location location, HashMap<String, List<Payment>> locationsPayments) throws Exception {
		String locationId = location.getId();
		String timezone = location.getTimezone();

		this.getClient().setLocation(locationId);
		if (this.isDateRangeFiltersSet()) {
			locationsPayments.put(locationId, Arrays.asList(this.getClient().payments().list(this.getDateRangeFilters(timezone))));
		} else {
			locationsPayments.put(locationId, Arrays.asList(this.getClient().payments().list()));
		}
	}
}