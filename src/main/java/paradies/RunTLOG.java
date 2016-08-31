package paradies;

import java.util.Map;

import util.TimeManager;

import com.squareup.connect.Merchant;
import com.squareup.connect.SquareClient;
import com.squareup.connect.diff.Catalog;

public class RunTLOG {

	public static void main( String[] args ) throws Exception {
		/*
		 * TLOG testing
		 */

		final String PANDORA_TOKEN = "sq0ats-nNcEYG_Sm37_EG5dtZbcGg";
		final String PANDORA_MERCHANT_ID = "8ZK3ZBFR828N0";
		final String DEFAULT_DEVICE_ID = "20";
		final String TIMEZONE = "America/New_York";
		
		System.out.println("Running...");

		SquareClient client = new SquareClient(PANDORA_TOKEN, "https://connect.squareupstaging.com", "v1", PANDORA_MERCHANT_ID);

		TLOGGeneratorPayload reportPayload = new TLOGGeneratorPayload();
		reportPayload.setDefaultDeviceId(DEFAULT_DEVICE_ID);
		reportPayload.setTimeZone(TIMEZONE);
		
		System.out.println("Getting locations...");
        Merchant[] locations = client.businessLocations().list();
        Merchant location = locations[2];
        reportPayload.setLocation(location);
        
        // Assign client to location
        client.setLocation(location.getId());
        
        // Get employees for location
        System.out.println("Getting employees...");
        reportPayload.setEmployees(client.employees().list());
        
        // Get discounts for location
        System.out.println("Getting discounts...");
        reportPayload.setDiscounts(client.discounts().list());

        // Get payments for location
        int offset = 0;
        int range = 1;

        System.out.println("Getting payments...");
        Map<String,String>  paymentParams = TimeManager.getPastDayInterval(range, offset, TIMEZONE);
        reportPayload.setPayments(client.payments().list(paymentParams));

        TLOGGenerator tlogGenerator = new TLOGGenerator(reportPayload);
        
        Map<String, TLOG> tlogs = tlogGenerator.getTlogs();

        for (String deviceName : tlogs.keySet()) {
        	System.out.println("");
        	System.out.println("Store: " + tlogs.get(deviceName).getStoreId());
        	System.out.println("Device: " + deviceName);
        	System.out.println(tlogs.get(deviceName));
        	System.out.println("");
        }

        System.out.println("Done.");
    }
}
