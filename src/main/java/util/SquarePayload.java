package util;

import java.util.HashMap;
import java.util.Map;

public class SquarePayload {

	private String merchantId;
	private String accessToken;
	private Map<String,String> params = new HashMap<String,String>();
	private Map<String,Object> results = new HashMap<String,Object>();
	
	public String getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
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
	public Map<String,Object> getResults() {
		return results;
	}
	public void setResults(Map<String,Object> results) {
		this.results = results;
	}
}
