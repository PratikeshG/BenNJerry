package vfcorp.smartwool;

import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.v2.Refund;
import com.squareup.connect.v2.SquareClientV2;

import util.LocationContext;
import util.SquarePayload;
import util.reports.CSVGenerator;

public class TransformRefundsToCsvCallable implements Callable {
	@Value("${vfcorp.smartwool.csv.refunds.headers}")
	public String[] HEADERS;
	@Value("${vfcorp.smartwool.range}")
	private String RANGE;
	@Value("${vfcorp.smartwool.offset}")
	private String OFFSET;
	@Value("${encryption.key.tokens}")
    private String ENCRYPTION_KEY;
	@Value("${vfcorp.smartwool.csv.zoneId}")
	private String TIME_ZONE_ID;

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		MuleMessage message = eventContext.getMessage();
		Map<String, LocationContext> locationContexts = message.getProperty(Constants.LOCATION_CONTEXT_MAP, PropertyScope.SESSION);

		String apiUrl = message.getProperty(Constants.API_URL, PropertyScope.SESSION);
		SquarePayload sqPayload = message.getProperty(Constants.SQUARE_PAYLOAD, PropertyScope.SESSION);

		CSVGenerator csvGenerator = new CSVGenerator(this.HEADERS);

		DashboardCsvRowFactory csvRowFactorty = new DashboardCsvRowFactory();

		for (String locationId : locationContexts.keySet()) {
			LocationContext locationCtx = locationContexts.get(locationId);
			SquareClientV2 clientv2 = new SquareClientV2(apiUrl, sqPayload.getAccessToken(this.ENCRYPTION_KEY), sqPayload.getMerchantId(), locationId);

			Refund[] refunds = clientv2.refunds().list(locationCtx.generateQueryParamMap());
			for (Refund refund : refunds) {
				csvGenerator.addRecord(csvRowFactorty.generateRefundCsvRow(refund, locationCtx, this.TIME_ZONE_ID));
			}
		}
		return csvGenerator.toString();
	}

}
