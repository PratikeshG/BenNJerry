package urbanspace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

import util.SquarePayload;
import util.TimeManager;

public class ReportGeneratorPayloadCallable implements Callable {

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        List<Map<String, Object>> merchantDatabaseEntries = (List<Map<String, Object>>) message.getPayload();
        List<ReportGeneratorPayload> reportGeneratorPayloads = new ArrayList<ReportGeneratorPayload>();

        for (Map<String, Object> merchantDatabaseEntry : merchantDatabaseEntries) {
            ReportGeneratorPayload reportGeneratorPayload = new ReportGeneratorPayload();

            SquarePayload squarePayload = new SquarePayload();
            squarePayload.setEncryptedAccessToken((String) merchantDatabaseEntry.get("encryptedAccessToken"));
            squarePayload.setMerchantId((String) merchantDatabaseEntry.get("merchantId"));
            squarePayload.setLocationId((String) merchantDatabaseEntry.get("locationId"));
            squarePayload.setMerchantAlias((String) merchantDatabaseEntry.get("merchantAlias"));
            squarePayload.setLegacySingleLocationSquareAccount((Boolean) merchantDatabaseEntry.get("legacy"));
            reportGeneratorPayload.setSquarePayload(squarePayload);

            int offset = message.getProperty("offset", PropertyScope.SESSION);
            int range = message.getProperty("range", PropertyScope.SESSION);
            String timeZone = message.getProperty("timeZone", PropertyScope.SESSION);
            reportGeneratorPayload.setParams(TimeManager.getPastDayInterval(range, offset, timeZone));

            reportGeneratorPayloads.add(reportGeneratorPayload);
        }

        return reportGeneratorPayloads;
    }
}
