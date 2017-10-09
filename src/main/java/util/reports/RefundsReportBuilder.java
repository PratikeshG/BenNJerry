package util.reports;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.squareup.connect.Refund;
import com.squareup.connect.SquareClient;
import com.squareup.connect.v2.Location;

/**
 *
 * @author Jordan Finci
 *
 */
public class RefundsReportBuilder extends AbstractReportBuilder<Refund> {
	/**
	 * Creates a report map of location Id to Refunds filtered by date range
	 * set by calling {@code forPastDayInterval(int range, int offset)}.
	 * @param apiUrl
	 * @param accessToken
	 * @param merchantId
	 */
	public RefundsReportBuilder(String apiUrl, String accessToken, String merchantId) {
		super(apiUrl, accessToken, merchantId);
	}
	/**
	 * {@code clientOverride} param provided for test.
	 * @param apiUrl
	 * @param accessToken
	 * @param merchantId
	 * @param clientOverride
	 */
	public RefundsReportBuilder(String apiUrl, String accessToken, String merchantId, SquareClient clientOverride) {
		super(apiUrl, accessToken, merchantId, clientOverride);
	}
	/**
	 * Build map.
	 */
	public HashMap<String, List<Refund>> build() throws Exception {
		HashMap<String, List<Refund>> locationsPayments = new HashMap<String, List<Refund>>();
		for (Location location : this.getLocations()) {
			this.processLocation(location, locationsPayments);
		}
		return locationsPayments;
	}
	private void processLocation(Location location, HashMap<String, List<Refund>> locationsPayments) throws Exception {
		String locationId = location.getId();
		String timezone = location.getTimezone();
		this.getClient().setLocation(location.getId());
		if (this.isDateRangeFiltersSet()) {
			locationsPayments.put(locationId, Arrays.asList(this.getClient().refunds().list(this.getDateRangeFilters(timezone))));
		} else {
			locationsPayments.put(locationId, Arrays.asList(this.getClient().refunds().list()));
		}
	}
}


