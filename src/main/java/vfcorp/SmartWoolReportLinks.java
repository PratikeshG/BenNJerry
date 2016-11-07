package vfcorp;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import com.squareup.connect.v2.SquareClientV2;
import com.squareup.connect.v2.Location;

import util.SquarePayload;
import util.TimeManager;

import java.util.HashMap;
import java.util.Map;

public class SmartWoolReportLinks implements Callable {
    
    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();
      
        // session var for API requests and urls
        String apiUrl = message.getProperty("apiUrl", PropertyScope.SESSION);
        String domainUrl = message.getProperty("domainUrl", PropertyScope.SESSION);
        
        // set SquareClientV2 for API calls
        SquarePayload payload = (SquarePayload) message.getPayload();
        SquareClientV2 client = new SquareClientV2(apiUrl, payload.getAccessToken());
        
        // final payload as emailBody in html
        StringBuilder emailBody = new StringBuilder();
        
        // connect.V2 location objects
        Location[] locations = null;
        
        // get list of SmartWool locations using SquareClientV2
        locations = client.locations().list();
        
        // set email html tags
        emailBody.append("<!DOCTYPE html>");
        emailBody.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        emailBody.append("<body>");
        emailBody.append("Please see the links below to download the prior day's transaction, item, and deposit reports.<br/>");
        
        // list transaction, item, and deposit report links on a per location basis
        for (int i = 0; i < locations.length; i++) {
        	ReportLinks reportsTemp = new ReportLinks(domainUrl, locations[i]);
        	
        	emailBody.append("<h4>" + reportsTemp.getName()  + "</h4>");
        	emailBody.append("<ul>");
        	emailBody.append("<li><a href='" + reportsTemp.getTransactionUrl() + "'>Transaction Report</a></li>");
        	emailBody.append("<li><a href='" + reportsTemp.getItemUrl() + "'>Item Detail Report</a></li>");
        	emailBody.append("<li><a href='" + reportsTemp.getDepositUrl() + "'>Deposit Report</a></li>");
        	emailBody.append("</ul>");
        	
        }
        emailBody.append("</body></html>");
        
        return emailBody.toString();
        
    }

    /* Wrapper for transaction, item, deposit report URLs
     *     Example URLs
     *         1) transaction:https://squareup.com/v2/reports/transactions.csv?begin_time=2016-11-02T00:00:00-07:00&end_time=2016-11-03T00:00:00-07:00&Time-Zone=America/Los_Angeles&event_based=true&locale=en&subunit_merchant_token[]=FXQFKY6YHS7FM
     *	       2) itemDetail: https://squareup.com/v2/reports/items.csv?begin_time=2016-11-02T00:00:00-07:00&end_time=2016-11-03T00:00:00-07:00&Time-Zone=America/Los_Angeles&event_based=true&locale=en&subunit_merchant_token[]=FXQFKY6YHS7FM
     *         3) deposit: https://squareup.com/v2/reports/deposit-details.csv?begin_time=2016-11-02T00:00:00-07:00&end_time=2016-11-03T00:00:00-07:00&Time-Zone=America/Los_Angeles&event_based=true&locale=en&subunit_merchant_token[]=3MH6E2GMAP5A5
     *
     *     NOTE: Assume this runs at 7:00 am EST
     */
    class ReportLinks {
		private final String transactionPath = "/v2/reports/transactions.csv?";
		private final String itemDetailPath = "/v2/reports/items.csv?";
		private final String depositPath = "/v2/reports/deposit-details.csv?";
    	private Map<String, String> reportRange;
    	private Map<String, String> urlParams;
    	private String domainUrl;
    	private String locationName;
    	
    	public ReportLinks(String domainUrl, Location location) {   	
    		// initialize all component values
    		this.domainUrl = domainUrl;
    		
    		// use TimeManager.java to get correctly formatted begin/end times for the day before (offset 1)
    		reportRange = TimeManager.getPastDayInterval(0, 1, "GMT-07:00");
    		
    		// add parameters into key/value pairs
    		urlParams = new HashMap<String, String>();
    		urlParams.put("begin_time", reportRange.get("begin_time"));
    		urlParams.put("end_time", reportRange.get("end_time"));
    	    urlParams.put("event_based", "true");
    		urlParams.put("locale", "en");
    		urlParams.put("subunit_merchant_token[]", location.getId());
    		urlParams.put("Time-Zone", location.getTimezone());

    		locationName = location.getName();		 		
    	}
    	
    	public String getTransactionUrl() {
    		return domainUrl + transactionPath + buildParams();
    	}
    	
    	public String getItemUrl() {   		
    		return domainUrl + itemDetailPath + buildParams();
    	}
    	
    	public String getDepositUrl() {   		
    		return domainUrl + depositPath + buildParams();
    	}
    	
    	public String getName() {
    		return locationName;
    	}
    	
    	// construct parameters for URL
    	//     begin_time=2016-11-02T00:00:00-07:00&end_time=2016-11-03T00:00:00-07:00&Time-Zone=America/Los_Angeles&event_based=true&locale=en&subunit_merchant_token[]=FXQFKY6YHS7FM
    	private String buildParams() {    		
    		String params = "";
    		String delim = "&";
    		
    		for (String key: urlParams.keySet()) {
    			params += delim;
    			params += key;
    			params += "=";
    			params += urlParams.get(key);
    		}
    		params = params.replaceFirst(delim, "");
    		
    		return params;
    	}
    	
    } // end inner class LocationUrls
    
}
