package util.reports;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mule.api.MuleMessage;
import org.mule.api.transport.PropertyScope;

import com.squareup.connect.Refund;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.PaymentRefund;
import com.squareup.connect.v2.SquareClientV2;

import util.ConnectV2MigrationHelper;
import util.Constants;

/**
 *
 * @author Jordan Finci
 *
 */
public class RefundsReportBuilder extends AbstractReportBuilder<PaymentRefund> {
	/**
	 * Creates a report map of location Id to Refunds filtered by date range set
	 * by calling {@code forPastDayInterval(int range, int offset)}.
	 *
	 * @param apiUrl
	 * @param accessToken
	 * @param merchantId
	 */
	public RefundsReportBuilder(String apiUrl, String accessToken, String merchantId, MuleMessage message) {
		super(apiUrl, accessToken, merchantId, message);
	}

	/**
	 * {@code clientOverride} param provided for test.
	 *
	 * @param apiUrl
	 * @param accessToken
	 * @param merchantId
	 * @param clientOverride
	 */
	public RefundsReportBuilder(String apiUrl, String accessToken, String merchantId, SquareClientV2 clientOverride) {
		super(apiUrl, accessToken, merchantId, clientOverride);
	}

	/**
	 * Build map.
	 */
	public Map<String, List<PaymentRefund>> build() throws Exception {
		Map<String, List<PaymentRefund>> locationsRefunds = new HashMap<String, List<PaymentRefund>>();
        Map<String, List<Refund>> locationsV1Refunds = new HashMap<>();
		for (Location location : this.getLocations()) {
			this.processLocation(location, locationsRefunds, locationsV1Refunds);
		}
        this.message.setProperty(Constants.V1REFUNDS, locationsV1Refunds, PropertyScope.INVOCATION);
		return locationsRefunds;
	}

	private void processLocation(Location location, Map<String, List<PaymentRefund>> locationsRefunds, Map<String, List<Refund>> locationsV1Refunds) throws Exception {
		String locationId = location.getId();
		String timezone = location.getTimezone();
		PaymentRefund[] refunds;

		if (this.isDateRangeFiltersSet()) {
			refunds = this.getClient().refunds().listPaymentRefunds(this.getDateRangeFilters(timezone));
		} else {
			refunds = this.getClient().refunds().listPaymentRefunds(new HashMap<>());
		}
        List<Refund> v1Refunds = ConnectV2MigrationHelper.toV1Refunds(refunds, this.getClient(), locationId, this.getDateRangeFilters(timezone));
        locationsV1Refunds.put(locationId, v1Refunds);
		locationsRefunds.put(locationId, Arrays.asList(refunds));
	}
}
