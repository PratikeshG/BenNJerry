package vfcorp.smartwool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.OrderLineItem;
import com.squareup.connect.v2.Payment;
import com.squareup.connect.v2.SquareClientV2;
import com.squareup.connect.v2.Transaction;

import util.ConnectV2MigrationHelper;
import util.Constants;
import util.LocationContext;
import util.SquarePayload;
import util.reports.CSVGenerator;
import util.reports.DashboardCsvRowFactory;

public class TransformItemsToCsvCallable implements Callable {

    public String[] HEADERS = new String[] { "Date", "Time", "Time Zone", "Category", "Item", "Qty", "Price Point Name",
            "SKU,Modifiers Applied", "Gross Sales", "Discounts", "Net Sales", "Tax", "Transaction ID", "Payment ID",
            "Device Name", "Notes", "Details", "Event Type", "Location", "Dining Option", "Customer ID",
            "Customer Name", "Customer Reference ID" };

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
        Map<String, List<Order>> locationsOrders = (HashMap<String, List<Order>>) message
                .getProperty(Constants.PAYMENTS, PropertyScope.INVOCATION);

        String apiUrl = message.getProperty(Constants.API_URL, PropertyScope.SESSION);
        SquarePayload sqPayload = message.getProperty(Constants.SQUARE_PAYLOAD, PropertyScope.SESSION);

        CSVGenerator csvGenerator = new CSVGenerator(this.HEADERS);

        DashboardCsvRowFactory csvRowFactory = new DashboardCsvRowFactory();

        // loop through locations and process the file for each

        for (String locationId : locationsOrders.keySet()) {
            List<Order> orders = locationsOrders.get(locationId);
            LocationContext locationCtx = locationContexts.get(locationId);
            SquareClientV2 clientv2 = new SquareClientV2(apiUrl, sqPayload.getAccessToken(this.ENCRYPTION_KEY));
            clientv2.setLogInfo(sqPayload.getMerchantId() + " - " + locationId);
            Map<String, CatalogObject> catalogMap = ConnectV2MigrationHelper.getCatalogObjectsForOrder(clientv2, orders.toArray(new Order[0]));
            Map<String, String> params = locationCtx.generateQueryParamMap();
            params.put("location_id", locationId);
            Payment[] payments = clientv2.payments().list(params);
            Map<String, Payment> tenderToPayment = ConnectV2MigrationHelper.getTenderToPayment(orders.toArray(new Order[0]), payments, clientv2, locationCtx.generateQueryParamMap());
            // loop through payments and generate csv row entries for each
            // itemization
            for (Order order : orders) {
            	if(order.getTenders() != null && order.getTenders().length > 0) {
                    Customer customer = ConnectV2MigrationHelper.getCustomer(order, clientv2);
                    if(order.getLineItems() != null) {
                    	for (OrderLineItem lineItem : order.getLineItems()) {
                            csvGenerator.addRecord(csvRowFactory.generateItemCsvRow(order, lineItem, catalogMap, tenderToPayment,
                                    customer, locationCtx, this.DOMAIN_URL));
                        }
                    }
            	}
            }
        }

        return csvGenerator.build();
    }
}
