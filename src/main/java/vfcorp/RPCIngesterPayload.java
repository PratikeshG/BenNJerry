package vfcorp;

import com.squareup.connect.Category;
import com.squareup.connect.Fee;
import com.squareup.connect.Item;

public class RPCIngesterPayload {

	private String merchantId;
	private String locationId;
	private String accessToken;
	private String merchantAlias;
	private boolean legacy;
	private Item[] items;
	private Category[] categories;
	private Fee[] fees;
	
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
	public Item[] getItems() {
		return items;
	}
	public void setItems(Item[] items) {
		this.items = items;
	}
	public Category[] getCategories() {
		return categories;
	}
	public void setCategories(Category[] categories) {
		this.categories = categories;
	}
	public Fee[] getFees() {
		return fees;
	}
	public void setFees(Fee[] fees) {
		this.fees = fees;
	}
}
