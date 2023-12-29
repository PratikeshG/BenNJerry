package benjerrys;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.SquareClient;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.SquareClientV2;

import util.Constants;
import util.SquarePayload;

public class LocationsCallable {
    @Value("${encryption.key.tokens}")
    private String encryptionKey;

    
    //public Object onCall(MuleEventContext eventContext) throws Exception {
    public Object onCall() throws Exception {
        //MuleMessage message = eventContext.getMessage();

        SquarePayload squarePayload = null;//(SquarePayload) message.getPayload();

        String apiUrl = null;//message.getProperty(Constants.API_URL, PropertyScope.SESSION);

        String accessToken = squarePayload.getAccessToken(this.encryptionKey);
        String merchantId = squarePayload.getMerchantId();

        SquareClientV2 clientV2 = new SquareClientV2(apiUrl, accessToken);

        List<Location> locations = Arrays.asList(clientV2.locations().list());

        //message.setProperty(Constants.SQUARE_PAYLOAD, squarePayload, PropertyScope.SESSION);

        // Get shared employees list
        SquareClient clientV1 = new SquareClient(accessToken, apiUrl, "v1", merchantId);
        //message.setProperty(Constants.EMPLOYEES, clientV1.employees().list(), PropertyScope.SESSION);

        return locations;
    }
}
