package vfcorp.altra;

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

import util.Constants;
import util.LocationContext;
import util.SquarePayload;
import util.TimeManager;

public class GetLocationDetailsCallable implements Callable {
    @Value("${encryption.key.tokens}")
    private String encryptionKey;

    /**
     * Get merchants {@code Location}'s for inclusion in report. Sets
     * {@code merchantDetails} session var and {@code locationDetailsMap} of
     * location {@code Id} to {@code Location}.
     */
    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        String apiUrl, accessToken, merchantId;

        MuleMessage message = eventContext.getMessage();

        int range = Integer.parseInt(message.getProperty(Constants.RANGE, PropertyScope.SESSION));
        int offset = Integer.parseInt(message.getProperty(Constants.OFFSET, PropertyScope.SESSION));

        SquarePayload sqPayload = (SquarePayload) message.getProperty(Constants.SQUARE_PAYLOAD, PropertyScope.SESSION);
        apiUrl = message.getProperty(Constants.API_URL, PropertyScope.SESSION);

        accessToken = sqPayload.getAccessToken(this.encryptionKey);
        merchantId = sqPayload.getMerchantId();

        SquareClientV2 client = new SquareClientV2(apiUrl, accessToken);
        client.setLogInfo(merchantId);
        sqPayload.setMerchantAlias(this.retrieveBusinessName(accessToken, apiUrl, merchantId));

        List<Location> locations = Arrays.asList(client.locations().list());
        message.setProperty(Constants.LOCATIONS, locations, PropertyScope.INVOCATION);
        message.setProperty(Constants.LOCATION_CONTEXT_MAP, this.generateLocationContextMap(locations, range, offset),
                PropertyScope.INVOCATION);

        return message.getPayload();
    }

    private HashMap<String, LocationContext> generateLocationContextMap(List<Location> locations, int range,
            int offset) {
        HashMap<String, LocationContext> locationIdToLocationDetails = new HashMap<String, LocationContext>();
        for (Location location : locations) {
            Map<String, String> queryParams = TimeManager.getPastDayInterval(range, offset, location.getTimezone());
            LocationContext context = new LocationContext(location, queryParams);
            locationIdToLocationDetails.put(location.getId(), context);
        }
        return locationIdToLocationDetails;
    }

    private String retrieveBusinessName(String accessToken, String apiUrl, String merchantId) throws Exception {
        SquareClient v1Client = new SquareClient(accessToken, apiUrl, "v1", merchantId);
        Merchant merchantInfo = v1Client.businessLocations().retrieve();
        return merchantInfo.getName();
    }
}
