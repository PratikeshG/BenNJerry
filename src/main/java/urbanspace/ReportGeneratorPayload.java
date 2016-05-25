package urbanspace;

import java.util.HashMap;
import java.util.Map;

import com.squareup.connect.Category;
import com.squareup.connect.Discount;
import com.squareup.connect.Payment;
import com.squareup.connect.Refund;

public class ReportGeneratorPayload {

	private String merchantId;
	private String locationId;
	private String accessToken;
	private String merchantAlias;
	private boolean legacy;
	private Payment[] payments;
	private Refund[] refunds;
	private Payment[] refundPayments;
	private Category[] categories;
	private Discount[] discounts;
	private Map<String,String> params = new HashMap<String,String>();
	
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
	public Payment[] getPayments() {
		return payments;
	}
	public void setPayments(Payment[] payments) {
		this.payments = payments;
	}
	public Refund[] getRefunds() {
		return refunds;
	}
	public void setRefunds(Refund[] refunds) {
		this.refunds = refunds;
	}
	public Payment[] getRefundPayments() {
		return refundPayments;
	}
	public void setRefundPayments(Payment[] refundPayments) {
		this.refundPayments = refundPayments;
	}
	public Category[] getCategories() {
		return categories;
	}
	public void setCategories(Category[] categories) {
		this.categories = categories;
	}
	public Discount[] getDiscounts() {
		return discounts;
	}
	public void setDiscounts(Discount[] discounts) {
		this.discounts = discounts;
	}
	public Map<String, String> getParams() {
		return params;
	}
	public void setParams(Map<String, String> params) {
		this.params = params;
	}
}
