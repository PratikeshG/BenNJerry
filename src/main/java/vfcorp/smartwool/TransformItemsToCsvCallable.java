package vfcorp.smartwool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.Payment;
import com.squareup.connect.PaymentItemization;
import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.SquareClientV2;
import com.squareup.connect.v2.Transaction;

import util.LocationContext;
import util.SquarePayload;
import util.reports.CSVGenerator;

public class TransformItemsToCsvCallable implements Callable {
	@Value("${vfcorp.smartwool.csv.items.headers}")
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

		@SuppressWarnings("unchecked")
		HashMap<String, List<Payment>> locationsPayments = (HashMap<String, List<Payment>>) message.getPayload();

		String apiUrl = message.getProperty(Constants.API_URL, PropertyScope.SESSION);
		SquarePayload sqPayload = message.getProperty(Constants.SQUARE_PAYLOAD, PropertyScope.SESSION);

		CSVGenerator csvGenerator = new CSVGenerator(this.HEADERS);
		HashMap<String, Transaction> tenderTransactionMap = new HashMap<>();

		DashboardCsvRowFactory csvRowFactorty = new DashboardCsvRowFactory();

		// loop through locations and process the file for each
		for (String locationId : locationsPayments.keySet()) {
			List<Payment> payments = locationsPayments.get(locationId);
			LocationContext locationCtx = locationContexts.get(locationId);
			SquareClientV2 clientv2 = new SquareClientV2(apiUrl, sqPayload.getAccessToken(this.ENCRYPTION_KEY), sqPayload.getMerchantId(), locationId);

			Transaction[] transactions = clientv2.transactions().list(locationCtx.generateQueryParamMap());
			for(Transaction transaction : transactions) {
				tenderTransactionMap.put(transaction.getTenders()[0].getId(), transaction);
			}

			// loop through payments and generate csv row entries for each itemization
			for(Payment payment : payments) {
				String tenderId = payment.getTender()[0].getId();
				Transaction transaction = tenderTransactionMap.get(tenderId);
				Customer customer = getCustomer(transaction, clientv2); // TODO: refactor when bulk customers endpoint available
				for (PaymentItemization itemization : payment.getItemizations()) {
					csvGenerator.addRecord(csvRowFactorty.generateItemCsvRow(payment, itemization, transaction, customer, locationCtx.getName(), this.TIME_ZONE_ID));
				}
			}
		}
		return csvGenerator.build();
	}
	private Customer getCustomer(Transaction transaction, SquareClientV2 clientv2) throws Exception {
		if (transaction.getTenders()[0].getCustomerId() != null) {
			return clientv2.customers().retrieve(transaction.getTenders()[0].getCustomerId());
		}
		return null;
	}
}
