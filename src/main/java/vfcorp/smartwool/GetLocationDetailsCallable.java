package vfcorp.smartwool;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.Merchant;
import com.squareup.connect.SquareClient;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.SquareClientV2;

import util.SquarePayload;
import util.TimeManager;

public class GetLocationDetailsCallable implements Callable {
	@Value("${vfcorp.smartwool.range}")
	private String VAR_RANGE;
	@Value("${vfcorp.smartwool.offset}")
	private String VAR_OFFSET;

	private final String VAR_APIURL = "apiUrl";
	private final String VAR_MERCHANT_DETAILS = "merchantDetails";
	private final String VAR_LOCATION_DETAILS_MAP = "locationDetailsMap";
	/**
	 * Get merchants {@code Location}'s for inclusion in report. Sets {@code merchantDetails}
	 * session var and {@code locationDetailsMap} of location {@code Id} to {@code Location}.
	 */
	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		MuleMessage message = eventContext.getMessage();
		SquarePayload merchantDetails = (SquarePayload) message.getProperty("merchantDetails", PropertyScope.SESSION);

		int range = Integer.parseInt(VAR_RANGE);
		int offset = Integer.parseInt(VAR_OFFSET);

		String apiUrl = message.getProperty(VAR_APIURL, PropertyScope.SESSION);
		String merchantId = merchantDetails.getMerchantId();
		String accessToken = merchantDetails.getAccessToken();

		SquareClientV2 client = new SquareClientV2(apiUrl, accessToken, merchantId);
		merchantDetails.setMerchantAlias(this.retrieveBusinessName(accessToken, apiUrl, merchantId));
		message.setProperty(VAR_MERCHANT_DETAILS, merchantDetails, PropertyScope.SESSION);

		List<Location> locations = Arrays.asList(client.locations().list());
		message.setProperty(VAR_LOCATION_DETAILS_MAP, this.toLocationsMetadataMap(locations, range, offset), PropertyScope.SESSION);

		return locations;
	}
	private HashMap<String, Map<String, String>> toLocationsMetadataMap(List<Location> locations, int range, int offset) {
		HashMap<String, Map<String, String>> locationIdToLocationDetails = new HashMap<String, Map<String, String>>();
		for (Location location : locations) {
			Map<String, String> locationDetails = TimeManager.getPastDayInterval(range, offset, location.getTimezone());
			locationDetails.put("name", location.getName());
			locationIdToLocationDetails.put(location.getId(), locationDetails);
		}
		return locationIdToLocationDetails;
	}
	private String retrieveBusinessName(String accessToken, String apiUrl, String merchantId) throws Exception {
		SquareClient v1Client = new SquareClient(accessToken, apiUrl, "v1", merchantId);
		Merchant merchantInfo = v1Client.businessLocations().retrieve();
		return merchantInfo.getName();
	}
}
