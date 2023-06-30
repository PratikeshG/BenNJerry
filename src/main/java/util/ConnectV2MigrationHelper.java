package util;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.OrderLineItem;
import com.squareup.connect.v2.Payment;
import com.squareup.connect.v2.PaymentRefund;
import com.squareup.connect.v2.SearchOrdersDateTimeFilter;
import com.squareup.connect.v2.SearchOrdersFilter;
import com.squareup.connect.v2.SearchOrdersQuery;
import com.squareup.connect.v2.SearchOrdersSort;
import com.squareup.connect.v2.SearchOrdersStateFilter;
import com.squareup.connect.v2.SquareClientV2;
import com.squareup.connect.v2.Tender;
import com.squareup.connect.v2.TimeRange;

/**
 * Helper methods for use in Square Connect v1 -> v2 migration
 */
public class ConnectV2MigrationHelper {

    private ConnectV2MigrationHelper() {
    };

    /**
     * Given a list of Orders, return a map of Order id to the refunds
     * associated with the order.
     * Since refunds of an order create a new order record thats not linked to
     * the original order, refunds are
     * fetched by first fetching associated payments with the order, then
     * fetching refunds associated with those payments.
     * 
     * @param order
     * @return Map of Order Id to list of PaymentRefunds associated with Order
     *         Id
     */
    public static Map<String, List<PaymentRefund>> getRefundsForOrders(SquareClientV2 client, Order[] orders,
            Payment[] payments) throws Exception {
        Map<String, List<PaymentRefund>> result = new HashMap<>();

        Map<String, Payment> paymentsMap = new HashMap<>();
        Arrays.stream(payments).forEach(payment -> paymentsMap.put(payment.getId(), payment));
        for (Order order : orders) {
            if (order != null && order.getTenders() != null) {
                for (Tender tender : order.getTenders()) {
                    Payment payment;
                    if (paymentsMap.get(tender.getId()) != null) {
                        payment = paymentsMap.get(tender.getId());
                    } else {
                        payment = client.payments().get(tender.getId());
                    }
                    if (payment != null && payment.getRefundIds() != null) {
                        for (String refundId : payment.getRefundIds()) {
                            PaymentRefund refund = client.refunds().retrieve(refundId);
                            if (refund != null && !refund.getStatus().equals("FAILED")) {
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
     * Fetches all catalog objects associated with the given orders and returns
     * a map of catalog object id to catalog object
     *
     * @param squareClientV2
     * @param orders
     * @return Map of Catalog Object ID to catalog object
     */
    public static Map<String, CatalogObject> getCatalogObjectsForOrder(SquareClientV2 squareClientV2, Order[] orders)
            throws Exception {

        String[] catalogObjectIds = Stream.of(orders).map(Order::getLineItems).filter(Objects::nonNull)
                .flatMap(Stream::of).map(OrderLineItem::getCatalogObjectId).filter(Objects::nonNull)
                .collect(Collectors.toSet()).toArray(new String[0]);

        CatalogObject[] catalogObjects = squareClientV2.catalog().batchRetrieve(catalogObjectIds, false);
        Map<String, CatalogObject> catalogObjectsMap = new HashMap<>();

        Arrays.stream(catalogObjects)
                .forEach(catalogObject -> catalogObjectsMap.put(catalogObject.getId(), catalogObject));

        return catalogObjectsMap;
    }

    public static boolean isCardPayment(Tender tender) {
        return (tender != null && (Tender.TENDER_TYPE_CARD.equals(tender.getType())
                || Tender.TENDER_TYPE_WALLET.equals(tender.getType())));
    }

    public static Order[] getOrders(SquareClientV2 squareClientV2, String locationId, Map<String, String> params)
            throws Exception {
        SearchOrdersQuery orderQuery = new SearchOrdersQuery();
        SearchOrdersFilter searchFilter = new SearchOrdersFilter();
        SearchOrdersSort searchSort = new SearchOrdersSort();
        orderQuery.setFilter(searchFilter);
        orderQuery.setSort(searchSort);

        SearchOrdersStateFilter stateFilter = new SearchOrdersStateFilter();
        stateFilter.setStates(new String[] { "COMPLETED", "OPEN" });
        searchFilter.setStateFilter(stateFilter);

        SearchOrdersDateTimeFilter dateFilter = new SearchOrdersDateTimeFilter();
        TimeRange timeRange = new TimeRange();
        timeRange.setStartAt(params.get(util.Constants.BEGIN_TIME));
        timeRange.setEndAt(params.get(util.Constants.END_TIME));
        dateFilter.setUpdatedAt(timeRange);
        searchFilter.setDateTimeFilter(dateFilter);

        searchSort.setSortField("UPDATED_AT");
        searchSort.setSortOrder(params.get(util.Constants.SORT_ORDER_V2));

        Order[] allOrders = squareClientV2.orders().search(locationId, orderQuery);
        List<Order> orders = new ArrayList<Order>();
        for (Order order : allOrders) {
            if (hasValidTender(order)) {
                orders.add(order);
            }
        }
        return orders.toArray(new Order[0]);
    }

    public static Payment[] getPaymentsV2(SquareClientV2 squareClientV2, String locationId, Map<String, String> params) throws Exception {
		params.put("location_id", locationId);
		// All v2 payments are tied to exactly one tender, no filtering needed
		return squareClientV2.payments().list(params);
	}

	public static PaymentRefund[] getPaymentRefunds(SquareClientV2 squareClientV2, Map<String, String> params) throws Exception {
		return squareClientV2.refunds().listPaymentRefunds(params);
	}

	/*
	 * Searches orders for a given time period. We include OPEN orders in this search because
	 * OPEN orders can also have valid tenders. In order to search OPEN orders, sort field must
	 * be set to CREATED_AT instead of CLOSED_AT because OPEN orders do not have a CLOSED_AT time stamp.
	 */
	public static Order[] getOrders(SquareClientV2 squareClientV2, String locationId, Map<String, String> params, boolean allowCashTransactions) throws Exception {
		SearchOrdersQuery orderQuery = new SearchOrdersQuery();
		SearchOrdersFilter searchFilter = new SearchOrdersFilter();
        SearchOrdersSort searchSort = new SearchOrdersSort();
        orderQuery.setFilter(searchFilter);
        orderQuery.setSort(searchSort);

        SearchOrdersStateFilter stateFilter = new SearchOrdersStateFilter();
        stateFilter.setStates(new String[] { "COMPLETED", "OPEN" });
        searchFilter.setStateFilter(stateFilter);

        SearchOrdersDateTimeFilter dateFilter = new SearchOrdersDateTimeFilter();
        TimeRange timeRange = new TimeRange();
        timeRange.setStartAt(params.get(util.Constants.BEGIN_TIME));
        timeRange.setEndAt(params.get(util.Constants.END_TIME));
        dateFilter.setCreatedAt(timeRange);
        searchFilter.setDateTimeFilter(dateFilter);

        searchSort.setSortField("CREATED_AT");
        searchSort.setSortOrder(params.get(util.Constants.SORT_ORDER_V2));


        Order[] allOrders = squareClientV2.orders().search(locationId, orderQuery);
        List<Order> orders = new ArrayList<Order>();
        for(Order order : allOrders) {
        	if(allowCashTransactions) {
        		if(hasValidTender(order)) {
            		orders.add(order);
            	}
        	} else {
        		if(hasValidCardTender(order)) {
        			orders.add(order);
        		}
        	}

        }
        return orders.toArray(new Order[0]);
    }

	private static boolean hasValidTender(Order order) {
		if(order.getTenders() != null) {
			for(Tender tender : order.getTenders()) {
				// check if tender is NO_SALE
				if(!tender.getType().equals(Tender.TENDER_TYPE_NO_SALE)) {
					//check if tender was a card payment, and if it was not voided or failed
					if(tender.getCardDetails() == null || (!tender.getCardDetails().getStatus().equals("VOIDED") && !tender.getCardDetails().getStatus().equals("FAILED"))) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static boolean hasValidCardTender(Order order) {
		if(order.getTenders() != null) {
			for(Tender tender : order.getTenders()) {
				// check if tender is NO_SALE
				if(!tender.getType().equals(Tender.TENDER_TYPE_CASH) && !tender.getType().equals(Tender.TENDER_TYPE_NO_SALE)) {
					//check if tender was a card payment, and if it was not voided or failed
					if(tender.getCardDetails() == null || (!tender.getCardDetails().getStatus().equals("VOIDED") && !tender.getCardDetails().getStatus().equals("FAILED"))) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static Map<String, Payment> getTenderToPayment(Order[] orders, Payment[] payments, SquareClientV2 squareClientV2, Map<String, String> params) throws Exception {
        Map<String, Payment> tenderToPayment = Arrays.stream(payments).collect(Collectors.toMap(Payment::getId, Function.identity()));
        for(Order order : orders) {
        	if(order != null && order.getTenders() != null) {
        		for(Tender tender : order.getTenders()) {
        			if(!tenderToPayment.containsKey(tender.getId())) {
        				Payment payment = squareClientV2.payments().get(tender.getId());
        				tenderToPayment.put(tender.getId(), payment);
        			}
        		}
        	}
        }
        return tenderToPayment;
	}

	public static Customer getCustomer(Order order, SquareClientV2 clientv2) throws Exception {
        if (order != null && order.getTenders() != null && order.getTenders().length > 0 && order.getTenders()[0].getCustomerId() != null) {
            return clientv2.customers().retrieve(order.getTenders()[0].getCustomerId());
        }
        return null;
    }

	public static Map<String, CatalogObject> getCatalogMap(String[] itemVariationIds, SquareClientV2 clientV2) throws Exception {
	        CatalogObject[] relatedItems = clientV2.catalog().batchRetrieve(itemVariationIds, true);
		  	Map<String, CatalogObject> catalogMap = new HashMap<>();
		  	Arrays.stream(relatedItems).forEach(relatedItem -> catalogMap.put(relatedItem.getId(), relatedItem));
		  	return catalogMap;
	}

    public static String[] getItemVariationIds(Order[] orders) {
    	Set<String> set = new HashSet<String>();
    	if(orders != null) {
    		for(Order order : orders) {
        		if(order != null && order.getLineItems() != null) {
    	            for(OrderLineItem orderLineItem : order.getLineItems()) {
        	        	if(orderLineItem != null) {
        	        		String catalogObjectId = orderLineItem.getCatalogObjectId();
    	              		if(catalogObjectId != null) {
    	              			// the catalogObjectId from an orderLineItem translates to the itemVariationId
    	              			set.add(catalogObjectId);
    	              		}
    	      			}
    	        	}
    	        }
    		}
    	}
    	String[] ids = new String[set.size()];
        set.toArray(ids);

        return ids;
    }

    public static Map<String, CatalogObject> getCategoriesMap(SquareClientV2 clientV2) throws Exception {
    	Map<String, CatalogObject> categoriesMap = new HashMap<>();
	  	CatalogObject[] categories = clientV2.catalog().listCategories();
        Arrays.stream(categories).forEach(category -> categoriesMap.put(category.getId(), category));
        return categoriesMap;
    }

    public static Map<String, CatalogObject> getItemVariationIdToCategory(String[] itemVariationIdList,
    		Map<String, CatalogObject> catalogMap,
    		Map<String, CatalogObject> categoriesMap) {
	  	Map<String, CatalogObject> itemVariationIdToCategory = new HashMap<>();
    	if(itemVariationIdList != null) {
    		for(String itemVariationId : itemVariationIdList) {
    			CatalogObject itemVariation = catalogMap.get(itemVariationId);
    			if(itemVariation != null && itemVariation.getItemVariationData() != null && itemVariation.getItemVariationData().getItemId() != null) {
    				CatalogObject item = catalogMap.get(itemVariation.getItemVariationData().getItemId());
    				if(item != null && item.getItemData() != null && item.getItemData().getCategoryId() != null) {
    					CatalogObject category = categoriesMap.get(item.getItemData().getCategoryId());
    			        itemVariationIdToCategory.put(itemVariationId, category);
    				}
    			}
    		}
    	}

    	return itemVariationIdToCategory;
    }
}
