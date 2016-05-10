package util;

import java.util.HashMap;
import java.util.Map;

/**
 * A single payload representing the parameters and results associated with one
 * merchant location.
 * 
 * Because a single payload can go through multiple flows and gather multiple
 * results, all of the results and parameters necessary to execute those flows
 * are congregated into a single payload. This payload traverses nearly the
 * entire execution flow of an integration, from the moment the integration's
 * details are retrieved from the database holding the details, to the moment
 * the results are parsed into the outgoing format.
 */
public class SquarePayload {

	private String merchantId;
	private String locationId;
	private String accessToken;
	private String merchantAlias;
	private boolean legacy;
	private Map<String,String> params = new HashMap<String,String>();
	private Map<String,Object> results = new HashMap<String,Object>();
	
	public String getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public String getLocationId() {
		return locationId;
	}
	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getMerchantAlias() {
		return merchantAlias;
	}
	public void setMerchantAlias(String merchantAlias) {
		this.merchantAlias = merchantAlias;
	}
	public boolean isLegacy() {
		return legacy;
	}
	public void setLegacy(boolean legacy) {
		this.legacy = legacy;
	}
	public Map<String,String> getParams() {
		return params;
	}
	public void setParams(Map<String,String> params) {
		this.params = params;
	}
	public Map<String,Object> getResults() {
		return results;
	}
	public void setResults(Map<String,Object> results) {
		this.results = results;
	}
}
