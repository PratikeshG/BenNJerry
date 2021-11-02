package jockey;

import java.util.Arrays;
import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.SquareClient;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.SquareClientV2;

import util.Constants;
import util.SquarePayload;

public class MerchantDataCallable implements Callable {
    @Value("${encryption.key.tokens}")
    private String encryptionKey;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        SquarePayload squarePayload = (SquarePayload) message.getPayload();

        String apiUrl = message.getProperty(Constants.API_URL, PropertyScope.SESSION);
        String accessToken = squarePayload.getAccessToken(this.encryptionKey);
        String merchantId = squarePayload.getMerchantId();
        apiUrl = "https://connect.squareup.com";

        SquareClientV2 clientV2 = new SquareClientV2(apiUrl, accessToken);
        List<Location> locations = Arrays.asList(clientV2.locations().list());

        message.setProperty(Constants.SQUARE_PAYLOAD, squarePayload, PropertyScope.SESSION);

        // Get shared employees list
        SquareClient clientV1 = new SquareClient(accessToken, apiUrl, "v1", merchantId);

        message.setProperty(Constants.EMPLOYEES, clientV1.employees().list(), PropertyScope.SESSION);

        return locations;
    }
}
