package util;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.OrderLineItem;
import com.squareup.connect.v2.Payment;
import com.squareup.connect.v2.PaymentRefund;
import com.squareup.connect.v2.SquareClientV2;
import com.squareup.connect.v2.Tender;


/**
 * Helper methods for use in Square Connect v1 -> v2 migration
 */
public class ConnectV2MigrationHelper {

	private ConnectV2MigrationHelper() {};

	/**
	 * Given a list of Orders, return a map of Order id to the refunds associated with the order.
	 * Since refunds of an order create a new order record thats not linked to the original order, refunds are
	 * fetched by first fetching associated payments with the order, then fetching refunds associated with those payments.

	 * @param order
	 * @return Map of Order Id to list of PaymentRefunds associated with Order Id
	 */
    public static Map<String, List<PaymentRefund>> getRefundsForOrders(SquareClientV2 client, Order[] orders, Payment[] payments) throws Exception {
    	Map<String, List<PaymentRefund>> result = new HashMap<>();

    	Map<String, Payment> paymentsMap = new HashMap<>();
    	Arrays.stream(payments).forEach(payment -> paymentsMap.put(payment.getId(), payment));
    	for (Order order : orders) {
    		if(order != null && order.getTenders() != null) {
    			for (Tender tender : order.getTenders()) {
        			Payment payment;
        			if(paymentsMap.get(tender.getId()) != null) {
        				payment = paymentsMap.get(tender.getId());
        			} else {
        				payment = client.payments().get(tender.getId());
        			}
        			if(payment != null && payment.getRefundIds() != null) {
        				for (String refundId : payment.getRefundIds()) {
            				PaymentRefund refund = client.refunds().retrieve(refundId);
            				if(refund != null) {
            					if (result.containsKey(order.getId())) {
                					result.get(order.getId()).add(refund);
                				} else {
                					result.put(order.getId(), new ArrayList<>(Arrays.asList(refund)));
                				}
            				}
            			}
        			}
        		}
    		}
    	}


    	return result;
    }

    /**
     * Fetches all catalog objects associated with the given orders and returns a map of catalog object id to catalog object
 	 *
     * @param squareClientV2
     * @param orders
     * @return Map of Catalog Object ID to catalog object
     */
    public static Map<String, CatalogObject> getCatalogObjectsForOrder(SquareClientV2 squareClientV2, Order[] orders) throws Exception {

    	String[] catalogObjectIds = Stream.of(orders)
    		.map(Order::getLineItems)
    		.filter(Objects::nonNull)
    		.flatMap(Stream::of)
    		.map(OrderLineItem::getCatalogObjectId)
    		.filter(Objects::nonNull)
    		.collect(Collectors.toSet())
    		.toArray(new String[0]);

    	CatalogObject[] catalogObjects = squareClientV2.catalog().batchRetrieve(catalogObjectIds, false);
    	Map<String, CatalogObject> catalogObjectsMap = new HashMap<>();

    	Arrays.stream(catalogObjects).forEach(catalogObject -> catalogObjectsMap.put(catalogObject.getId(), catalogObject));

    	return catalogObjectsMap;
    }
}
