package jockey;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.SquareClientV2;
import com.squareup.connect.v2.TeamMember;

import util.Constants;
import util.SquarePayload;

public class MerchantDataCallable implements Callable {
    @Value("${encryption.key.tokens}")
    private String encryptionKey;

    @Value("${jockey.api.url}")
    private String apiUrl;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        SquarePayload squarePayload = (SquarePayload) message.getPayload();

        String accessToken = squarePayload.getAccessToken(this.encryptionKey);
        String merchantId = squarePayload.getMerchantId();

        SquareClientV2 clientV2 = new SquareClientV2(apiUrl, accessToken);
        clientV2.setLogInfo(merchantId);
        List<Location> locations = Arrays.asList(clientV2.locations().list());

        message.setProperty(Constants.SQUARE_PAYLOAD, squarePayload, PropertyScope.SESSION);

        // Get shared employees list
        Map<String, String> employeeCache = new HashMap<String, String>();
        TeamMember[] team = clientV2.team().search();
        for (TeamMember t : team) {
            employeeCache.put(t.getId(), t.getReferenceId());
        }

        message.setProperty(Constants.EMPLOYEES, employeeCache, PropertyScope.SESSION);

        return locations;
    }
}
