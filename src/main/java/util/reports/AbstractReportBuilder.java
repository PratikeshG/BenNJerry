package util.reports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.mule.api.MuleMessage;

import com.google.common.base.Preconditions;
import com.squareup.connect.SquareClient;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.SquareClientV2;

import util.TimeManager;

/**
 *
 * @author Jordan Finci
 *
 * @param <T>
 *            ConnectAdapter result
 */
public abstract class AbstractReportBuilder<T> {
	private final String apiUrl;
	private final String accessToken;
	private final SquareClientV2 client;
	private final HashSet<String> locationIds = new HashSet<String>();
	private final ArrayList<Location> locations = new ArrayList<Location>();
	private final String merchantId;
	protected final MuleMessage message;

	private boolean dateRangeFiltersSet = false;
	private int range = 0;
	private int offset = 0;

	public SquareClientV2 getClient() {
		return client;
	}

	public Map<String, String> getDateRangeFilters(String timezone) {
		return TimeManager.getPastDayInterval(this.range, this.offset, timezone);
	}

	public List<Location> getLocations() {
		return this.locations;
	}

	public boolean isDateRangeFiltersSet() {
		return this.dateRangeFiltersSet;
	}

	/**
	 * Generic report of location to connect object result.
	 *
	 * @param apiUrl
	 * @param accessToken
	 * @param merchantId
	 */
	public AbstractReportBuilder(String apiUrl, String accessToken, String merchantId) {
		this.apiUrl = Preconditions.checkNotNull(apiUrl);
		this.accessToken = Preconditions.checkNotNull(accessToken);
		this.merchantId = Preconditions.checkNotNull(merchantId);
		this.client = new SquareClientV2(this.apiUrl, this.accessToken, "2023-05-17");
		this.message = null;
	}

	/**
	 * Generic report of location to connect object result.
	 *
	 * @param apiUrl
	 * @param accessToken
	 * @param merchantId
	 */
	public AbstractReportBuilder(String apiUrl, String accessToken, String merchantId, MuleMessage message) {
		this.apiUrl = Preconditions.checkNotNull(apiUrl);
		this.accessToken = Preconditions.checkNotNull(accessToken);
		this.merchantId = Preconditions.checkNotNull(merchantId);
		this.client = new SquareClientV2(this.apiUrl, this.accessToken, "2023-05-17");
		this.message = message;
	}

	/**
	 * {@code client} provided for test.
	 *
	 * @param apiUrl
	 * @param accessToken
	 * @param merchantId
	 * @param client
	 */
	public AbstractReportBuilder(String apiUrl, String accessToken, String merchantId, SquareClientV2 client) {
		if (merchantId == null || accessToken == null || apiUrl == null) {
			throw new IllegalArgumentException("merchantId, accessToken, and apiUrl must not be null.");
		}
		this.apiUrl = apiUrl;
		this.accessToken = accessToken;
		this.merchantId = merchantId;
		this.client = client;
		this.message = null;
	}

	/**
	 * Must implement.
	 *
	 * @return Generic report map of location Id to connect result.
	 * @throws Exception
	 */
	public abstract Map<String, List<T>> build() throws Exception;

	/**
	 * Locations included in report.
	 *
	 * @param locations
	 * @return
	 */
	public AbstractReportBuilder<T> forLocations(List<Location> locations) {
		for (Location location : locations) {
			if (!this.locationIds.contains(location.getId())) {
				this.locationIds.add(location.getId());
				this.locations.add(location);
			}
		}
		return this;
	}

	/**
	 * Filter report over a past day interval.
	 *
	 * @param range
	 * @param offset
	 * @return
	 */
	public AbstractReportBuilder<T> forPastDayInterval(int range, int offset) {
		this.offset = offset;
		this.range = range;
		this.dateRangeFiltersSet = true;
		return this;
	}
}
