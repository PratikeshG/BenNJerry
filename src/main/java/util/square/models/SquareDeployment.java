package util.square.models;

public class SquareDeployment  {

	private String merchantId;
	private String locationId;
	private String accessToken;
	private String merchantAlias;
	private boolean legacy;
	
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
}