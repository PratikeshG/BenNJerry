package vfcorp.smartwool;

import java.util.List;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.v2.Payment;
import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.SquareClientV2;

import util.ConnectV2MigrationHelper;
import util.Constants;
import util.LocationContext;
import util.SquarePayload;
import util.reports.CSVGenerator;
import util.reports.DashboardCsvRowFactory;

public class TransformTransactionsToCsvCallable implements Callable {

    public final String[] HEADERS = new String[] { "Date", "Time", "Time Zone", "Gross Sales", "Discounts", "Net Sales",
            "Gift Card Sales", "Tax", "Tip", "Partial Refunds", "Total Collected", "Source", "Card",
            "Card Entry Methods", "Cash", "Square Gift Card", "Other Tender", "Other Tender Type", "Other Tender Note",
            "Fees", "Net Total", "Transaction ID", "Payment ID", "Card Brand", "PAN Suffix", "Device Name",
            "Staff Name", "Staff ID", "Details", "Description", "Event Type", "Location", "Dining Option",
            "Customer ID", "Customer Name", "Customer Reference ID", "Device Nickname" };

    @Value("${domain.url}")
    private String DOMAIN_URL;
    @Value("${encryption.key.tokens}")
    private String ENCRYPTION_KEY;

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();
        Map<String, LocationContext> locationContexts = message.getProperty(Constants.LOCATION_CONTEXT_MAP,
                PropertyScope.INVOCATION);

        @SuppressWarnings("unchecked")
        Map<String, List<Order>> locationsOrders = (Map<String, List<Order>>) message
                .getProperty(Constants.PAYMENTS, PropertyScope.INVOCATION);

        String apiUrl = message.getProperty(Constants.API_URL, PropertyScope.SESSION);
        SquarePayload sqPayload = message.getProperty(Constants.SQUARE_PAYLOAD, PropertyScope.SESSION);

        CSVGenerator csvGenerator = new CSVGenerator(this.HEADERS);
        DashboardCsvRowFactory csvRowFactory = new DashboardCsvRowFactory();

        // loop through locations and process the file for each
        for (String locationId : locationsOrders.keySet()) {
            List<Order> orders = locationsOrders.get(locationId);
            LocationContext locationCtx = locationContexts.get(locationId);
            SquareClientV2 clientv2 = new SquareClientV2(apiUrl, sqPayload.getAccessToken(this.ENCRYPTION_KEY), "2023-05-17");
            clientv2.setLogInfo(sqPayload.getMerchantId() + " - " + locationId);
            Map<String, String> params = locationCtx.generateQueryParamMap();
            params.put("location_id", locationId);
            Payment[] payments = clientv2.payments().list(params);
            Map<String, Payment> tenderToPayment = ConnectV2MigrationHelper.getTenderToPayment(orders.toArray(new Order[0]), payments, clientv2, locationCtx.generateQueryParamMap());

            // loop through payments and generate csv row entries for each
            // itemization
            for (Order order : orders) {
                Customer customer = ConnectV2MigrationHelper.getCustomer(order, clientv2);
                csvGenerator.addRecord(csvRowFactory.generateTransactionCsvRow(order, tenderToPayment, customer,
                        locationCtx, this.DOMAIN_URL));
            }
        }
        return csvGenerator.build();
    }
}
