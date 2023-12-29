package util.reports;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.mule.api.MuleMessage;

import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.SquareClientV2;

import util.ConnectV2MigrationHelper;

/**
 *
 * @author finci
 *
 */
public class PaymentsReportBuilder extends AbstractReportBuilder<Order> {
	/**
	 * Creates a report map of location Id to Payments filtered by date range
	 * set by calling {@code forPastDayInterval(int range, int offset)}.
	 *
	 * @param apiUrl
	 * @param accessToken
	 * @param merchantId
	 */
	public PaymentsReportBuilder(String apiUrl, String accessToken, String merchantId) {
		super(apiUrl, accessToken, merchantId);
	}

//	public PaymentsReportBuilder(String apiUrl, String accessToken, String merchantId, MuleMessage message) {
//		super(apiUrl, accessToken, merchantId, message);
//	}

	/**
	 * {@code clientOverride} provided for test.
	 *
	 * @param apiUrl
	 * @param accessToken
	 * @param merchantId
	 * @param clientOverride
	 */
	public PaymentsReportBuilder(String apiUrl, String accessToken, String merchantId, SquareClientV2 clientOverride) {
		super(apiUrl, accessToken, merchantId, clientOverride);
	}

	/**
	 * Build map.
	 */

	public Map<String, List<Order>> build() throws Exception {
		HashMap<String, List<Order>> locationsOrders = new HashMap<String, List<Order>>();
		for (Location location : this.getLocations()) {
			this.processLocation(location, locationsOrders);
		}
		return locationsOrders;
	}

	private void processLocation(Location location, Map<String, List<Order>> locationsOrders) throws Exception {
		String locationId = location.getId();
		String timezone = location.getTimezone();
		Order[] orders;

		if (this.isDateRangeFiltersSet()) {
			orders = ConnectV2MigrationHelper.getOrdersWithExchanges(this.getClient(), locationId, this.getDateRangeFilters(timezone), true);
		} else {
			orders = ConnectV2MigrationHelper.getOrdersWithExchanges(this.getClient(), locationId, new HashMap<>(), true);
		}

		locationsOrders.put(locationId, Arrays.asList(orders));
	}
}
