package util;

import java.util.Map;

public class SquarePayload {

	private String merchantId;
	private String locationId;
	private String accessToken;
	private Map<String,String> params;
	
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
	public Map<String,String> getParams() {
		return params;
	}
	public void setParams(Map<String,String> params) {
		this.params = params;
	}
}
