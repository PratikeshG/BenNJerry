package vfcorp;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import com.squareup.connect.v2.SquareClientV2;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.LocationsAdapter;

import util.SquarePayload;
import util.TimeManager;

public class SmartWoolReportLinks implements Callable {
    
    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();
      
        // session var for API requests
        String apiUrl = message.getProperty("apiUrl", PropertyScope.SESSION);
        // set SquareClientV2 for API calls
        SquarePayload payload = (SquarePayload) message.getPayload();
        SquareClientV2 client = new SquareClientV2(apiUrl,payload.getAccessToken());
        
        // final payload as emailBody in html
        StringBuilder emailBody = new StringBuilder();
        
        // connect.V2 location objects
        LocationsAdapter locationAdapter = null;
        Location[] locations = null;
        
        // get list of SmartWool locations using SquareClientV2
        locationAdapter = client.locations();
        locations = locationAdapter.list();
        
        // set email html tags
        emailBody.append("<!DOCTYPE html>");
        emailBody.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        emailBody.append("<body>");
        emailBody.append("Please see the links below to download the prior day's transaction, item, and deposit reports.<br/>");
        
        // sort ReportLinks according to timezone
        for (int i = 0; i < locations.length; i++) {
        	ReportLinks reportsTemp = new ReportLinks(locations[i]);
        	
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
     * Examples for TimeManager.java:
     *     Today: 11/03, Time: ~2:50pm
     *
     *     TimeManager.getPastDayInterval(0, 0, "GMT-00:00").get("begin_time") = 2016-11-03T00:00:00Z 
     *     TimeManager.getPastDayInterval(0, 0, "GMT-00:00").get("end_time") = 2016-11-03T21:45:00Z
     *
     *     TimeManager.getPastDayInterval(0, 0, "GMT-07:00").get("begin_time") = 2016-11-03T00:00:00-07:00
     *     TimeManager.getPastDayInterval(0, 0, "GMT-07:00").get("end_time") = 2016-11-03T14:53:12-07:00
     *
     *     TimeManager.getPastDayInterval(1, 0, "GMT-07:00").get("begin_time") = 2016-11-03T00:00:00-07:00
     *     TimeManager.getPastDayInterval(1, 0, "GMT-07:00").get("end_time") = 2016-11-03T14:56:28-07:00
     *
     *     TimeManager.getPastDayInterval(0, -1, "GMT-07:00").get("begin_time") = 2016-11-04T00:00:00-07:00
     *     TimeManager.getPastDayInterval(0, -1, "GMT-07:00").get("end_time") = 2016-11-04T14:58:41-07:00 
     *
     *     TimeManager.getPastDayInterval(0, 1, "GMT-07:00").get("begin_time") = 2016-11-02T00:00:00-07:00
     *     TimeManager.getPastDayInterval(0, 1, "GMT-07:00").get("end_time") = 2016-11-02T23:59:59-07:00
     *
     *     NOTE: this produces the same result as above with different parameters!!
     *     TimeManager.getPastDayInterval(1, 1, "GMT-07:00").get("begin_time") = 2016-11-02T00:00:00-07:00
     *     TimeManager.getPastDayInterval(1, 1, "GMT-07:00").get("end_time") = 2016-11-02T23:59:59-07:00
     *
     *	   IN-USE: this is a hack to get the date range for the report links
     *     TimeManager.getPastDayInterval(0, 1, "GMT-07:00").get("begin_time") = 2016-11-02T00:00:00-07:00
     *     TimeManager.getPastDayInterval(0, 0, "GMT-07:00").get("begin_time") = 2016-11-03T00:00:00-07:00
     */
    
    // Assume this runs at 8:00 am (cron time, begin/end times of report, day of report)
    class ReportLinks {    	
    	private String transactionUrl;
    	private String itemDetailUrl;
    	private String depositUrl;
    	private String beginTime;
    	private String endTime;
    	private String timezone;
    	private String eventBased;
    	private String locale;
    	private String locationToken;
    	private String locationName;
    	
    	public ReportLinks(Location location) {
    		// initialize all component values
    		transactionUrl = "https://squareupstaging.com/v2/reports/transactions.csv?";
    		itemDetailUrl = "https://squareupstaging.com/v2/reports/items.csv?";
    		depositUrl = "https://squareupstaging.com/v2/reports/deposit-details.csv?";
    		
    		// TODO(wtsang): fix this hack to get the correct timestamps for the report URL
    		beginTime = "begin_time=" + TimeManager.getPastDayInterval(0, 1, "GMT-07:00").get("begin_time");
    		endTime = "end_time=" + TimeManager.getPastDayInterval(0, 0, "GMT-07:00").get("begin_time");
    		
    		// hard-code values for now; update code in the future if necessary
    		eventBased = "event_based=true"; 
    		locale = "locale=en";
    		
    		// extract relevant info from location object
    		locationToken = "subunit_merchant_token[]=" + location.getId();
    		timezone = "Time-Zone=" + location.getTimezone();
    		locationName = location.getName();    		 		
    	}
    	
    	public String getTransactionUrl() {
    		return transactionUrl + buildParams();
    	}
    	
    	public String getItemUrl() {   		
    		return itemDetailUrl + buildParams();
    	}
    	
    	public String getDepositUrl() {   		
    		return depositUrl + buildParams();
    	}
    	
    	public String getName() {
    		return locationName;    		
    	}
    	
    	// construct parameters for URL
    	//     begin_time=2016-11-02T00:00:00-07:00&end_time=2016-11-03T00:00:00-07:00&Time-Zone=America/Los_Angeles&event_based=true&locale=en&subunit_merchant_token[]=FXQFKY6YHS7FM
    	private String buildParams() {
    		String params = "";
    		String delim = "&";
    		
    		params += beginTime + delim + endTime + delim + timezone + delim;
    		params += eventBased + delim + locale + delim + locationToken;
    				    		
    		return params; 		
    	}	
    	   	
    } // end inner class LocationUrls        
    
}
