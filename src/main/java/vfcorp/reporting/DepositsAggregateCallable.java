package vfcorp.reporting;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.activation.DataHandler;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import com.squareup.connect.Settlement;
import com.squareup.connect.SquareClient;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.SquareClientV2;

import util.SquarePayload;
import util.TimeManager;

public class DepositsAggregateCallable implements Callable {

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        @SuppressWarnings("unchecked")
        List<SquarePayload> deploymentPayloads = (ArrayList<SquarePayload>) message.getPayload();

        String apiUrl = message.getProperty("apiUrl", PropertyScope.SESSION);
        String apiVersion = message.getProperty("apiVersion", PropertyScope.SESSION);

        int offset = Integer.parseInt(message.getProperty("offset", PropertyScope.SESSION));
        int range = Integer.parseInt(message.getProperty("range", PropertyScope.SESSION));

        StringBuilder builder = new StringBuilder();
        builder.append("\"Date\",\"Deposit ID\",\"Store Number\",\"Amount\"\n");

        for (SquarePayload deployment : deploymentPayloads) {
            SquareClient squareV1Client = new SquareClient(deployment.getAccessToken(), apiUrl, apiVersion,
                    deployment.getMerchantId(), deployment.getLocationId());
            SquareClientV2 squareV2Client = new SquareClientV2(apiUrl, deployment.getAccessToken(),
                    deployment.getLocationId());

            Location location = squareV2Client.locations().retrieve(deployment.getLocationId());

            // No location found
            if (location == null) {
                throw new Exception(
                        String.format("No matching location ID (%s) found in V2 API!", deployment.getLocationId()));
            }

            Map<String, String> params = TimeManager.getPastDayInterval(range, offset, location.getTimezone());

            Settlement[] settlements = squareV1Client.settlements().list(params);

            for (Settlement settlement : settlements) {
                builder.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\"\n", settlement.getInitiatedAt(),
                        settlement.getId(), location.getName(), formatTotal(settlement.getTotalMoney().getAmount())));
            }
        }

        // Calculate day of report generated
        String timezone = "America/Los_Angeles";
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timezone));
        String currentDate = TimeManager.toSimpleDateTimeInTimeZone(TimeManager.toIso8601(cal, timezone), timezone,
                "yyyy-MM-dd");
        DataHandler dataHandler = new DataHandler(builder.toString(), "text/plain; charset=UTF-8");
        eventContext.getMessage().addOutboundAttachment(currentDate + "-deposit-summary.csv", dataHandler);

        // empty return
        return "See attachment";
    }

    private String formatTotal(int gpv) {
        NumberFormat n = NumberFormat.getCurrencyInstance(Locale.US);
        return n.format(gpv / 100.0);
    }
}
