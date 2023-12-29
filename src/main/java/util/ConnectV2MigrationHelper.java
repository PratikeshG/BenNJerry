package util;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.squareup.connect.Device;
import com.squareup.connect.Money;
import com.squareup.connect.PaymentDiscount;
import com.squareup.connect.PaymentItemDetail;
import com.squareup.connect.PaymentItemization;
import com.squareup.connect.PaymentModifier;
import com.squareup.connect.PaymentTax;
import com.squareup.connect.Refund;
import com.squareup.connect.v2.CashPaymentDetails;
import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.Customer;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.OrderLineItem;
import com.squareup.connect.v2.OrderLineItemAppliedDiscount;
import com.squareup.connect.v2.OrderLineItemAppliedTax;
import com.squareup.connect.v2.OrderLineItemDiscount;
import com.squareup.connect.v2.OrderLineItemModifier;
import com.squareup.connect.v2.OrderLineItemTax;
import com.squareup.connect.v2.OrderReturn;
import com.squareup.connect.v2.OrderReturnLineItem;
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
        //pratikesh
        //Arrays.stream(payments).forEach(payment -> paymentsMap.put(payment.getId(), payment));
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
//pratikesh
        String[] catalogObjectIds = null;//Stream.of(orders).map(Order::getLineItems).filter(Objects::nonNull)
 //               .flatMap(Stream::of).map(OrderLineItem::getCatalogObjectId).filter(Objects::nonNull)
   //             .collect(Collectors.toSet()).toArray(new String[0]);

        CatalogObject[] catalogObjects = squareClientV2.catalog().batchRetrieve(catalogObjectIds, false);
        Map<String, CatalogObject> catalogObjectsMap = new HashMap<>();
//pratikesh
//        Arrays.stream(catalogObjects)
//                .forEach(catalogObject -> catalogObjectsMap.put(catalogObject.getId(), catalogObject));

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
        dateFilter.setCreatedAt(timeRange);
        searchFilter.setDateTimeFilter(dateFilter);

        searchSort.setSortField("CREATED_AT");
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

	public static SearchOrdersQuery getOrdersQuery(Map<String, String> params) {
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

        return orderQuery;
	}

	/*
	 * Searches orders for a given time period. We include OPEN orders in this search because
	 * OPEN orders can also have valid tenders. In order to search OPEN orders, sort field must
	 * be set to CREATED_AT instead of CLOSED_AT because OPEN orders do not have a CLOSED_AT time stamp.
	 */
	public static Order[] getOrders(SquareClientV2 squareClientV2, String locationId, Map<String, String> params, boolean allowCashTransactions) throws Exception {
		SearchOrdersQuery orderQuery = getOrdersQuery(params);
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

	public static Order[] getOrdersWithExchanges(SquareClientV2 squareClientV2, String locationId, Map<String, String> params, boolean allowCashTransactions) throws Exception {
		SearchOrdersQuery orderQuery = getOrdersQuery(params);

        Order[] allOrders = squareClientV2.orders().search(locationId, orderQuery);
        List<Order> orders = new ArrayList<Order>();
        for(Order order : allOrders) {
        	if((allowCashTransactions && hasValidTender(order)) || hasValidCardTender(order) || isExchange(order)) {
        		orders.add(order);
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
        //pratikesh
		Map<String, Payment> tenderToPayment = null;//Arrays.stream(payments).collect(Collectors.toMap(Payment::getId, Function.identity()));
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
		  	//pratikesh
		  	//Arrays.stream(relatedItems).forEach(relatedItem -> catalogMap.put(relatedItem.getId(), relatedItem));
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
        //pratikesh
	  	//Arrays.stream(categories).forEach(category -> categoriesMap.put(category.getId(), category));
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

    public static com.squareup.connect.Payment toV1Payment(Order order, Map<String, CatalogObject> catalogMap,
    		Map<String, CatalogObject> lineItemCategories, Map<String, Payment> tenderToPayment,
			Customer customer) {
    	com.squareup.connect.Payment payment = new com.squareup.connect.Payment();
    	int totalMoney = order.getTotalMoney() != null ? order.getTotalMoney().getAmount() : 0;
    	int totalTaxMoney = order.getTotalTaxMoney() != null ? order.getTotalTaxMoney().getAmount() : 0;
    	int totalDiscountMoney = order.getTotalDiscountMoney() != null ? order.getTotalDiscountMoney().getAmount() : 0;
    	int totalTipMoney = order.getTotalTipMoney() != null ? order.getTotalTipMoney().getAmount() : 0;
    	int netAmounts = order.getNetAmounts() != null && order.getNetAmounts().getTotalMoney() != null ? order.getNetAmounts().getTotalMoney().getAmount() : 0;
    	payment.setId(order.getId());
    	payment.setCreatedAt(order.getCreatedAt());
    	payment.setGrossSalesMoney(new Money(totalMoney - totalTaxMoney + totalDiscountMoney - totalTipMoney));
    	payment.setDiscountMoney(new Money(-totalDiscountMoney));
    	payment.setNetSalesMoney(new Money(totalMoney - totalTaxMoney - totalTipMoney));
    	payment.setTaxMoney(new Money(totalTaxMoney));
    	payment.setTipMoney(new Money(totalTipMoney));
    	payment.setTotalCollectedMoney(new Money(netAmounts));
    	int totalProcessingFee = 0;//order.getTenders() != null ? Arrays.stream(order.getTenders())
//         		.map(Tender::getProcessingFeeMoney)
//         		.filter(Objects::nonNull)
//         		.mapToInt(com.squareup.connect.v2.Money::getAmount)
//         		.sum() : 0;
    	payment.setProcessingFeeMoney(new Money(-totalProcessingFee));
    	payment.setNetTotalMoney(new Money(netAmounts - totalProcessingFee));
    	setTenders(order, payment, tenderToPayment);
    	setPaymentDetails(order, payment, catalogMap, lineItemCategories, tenderToPayment, customer);
    	int totalInclusiveTaxMoney = 0;//payment.getInclusiveTax().length > 0 ? Arrays.stream(payment.getInclusiveTax())
//    			.map(PaymentTax::getAppliedMoney)
//    			.filter(Objects::nonNull)
//    			.mapToInt(Money::getAmount)
//    			.sum() : 0;
    	payment.setInclusiveTaxMoney(new Money(totalInclusiveTaxMoney));
    	int totalAdditiveTaxMoney = 0;//payment.getAdditiveTax().length > 0 ? Arrays.stream(payment.getAdditiveTax())
//    			.map(PaymentTax::getAppliedMoney)
//    			.filter(Objects::nonNull)
//    			.mapToInt(Money::getAmount)
//    			.sum() : 0;
    	payment.setAdditiveTaxMoney(new Money(totalAdditiveTaxMoney));
    	if(order.getTenders() != null) {
    		for(Tender tender : order.getTenders()) {
    			Payment v2Payment = tenderToPayment.get(tender.getId());
    			if(v2Payment != null && v2Payment.getDeviceDetails() != null) {
    		    	Device device = new Device();
    				device.setId(v2Payment.getDeviceDetails().getDeviceId());
    				device.setName(v2Payment.getDeviceDetails().getDeviceName());
    				payment.setDevice(device);
    				break;
    			}
    		}
    	}

    	return payment;
    }

    public static void setTenders(Order order, com.squareup.connect.Payment payment, Map<String, Payment> tenderToPayment) {
		List<com.squareup.connect.Tender> v1Tenders = new ArrayList<>();
    	if(order.getTenders() != null) {
    		for(Tender tender : order.getTenders()) {
    			Payment v2Payment = tenderToPayment.get(tender.getId());
    			com.squareup.connect.Tender v1Tender = new com.squareup.connect.Tender();
    			v1Tender.setId(tender.getId());
    			String type = v2Payment.getSourceType();
    			String name = "";
    			switch(type) {
    				case Tender.TENDER_TYPE_WALLET:
	    			case Tender.TENDER_TYPE_CARD:
	    				name = "Credit Card";
	    				break;
	    			case Tender.TENDER_TYPE_CASH:
	    				name = "Cash";
	    				break;
	    			case Tender.TENDER_TYPE_OTHER:
	    				name = "Other";
	    				break;
    				default:
    					name = "Unknown";
    					break;
    			}

    			// type "CARD" is different for v1 and v2
    			v1Tender.setName(name);
    			if(type.equals(Tender.TENDER_TYPE_CARD))
    				type = "CREDIT_CARD";
    			v1Tender.setType(type);
    			v1Tender.setEmployeeId(v2Payment.getTeamMemberId());
    			v1Tender.setReceiptUrl(v2Payment.getReceiptUrl());
    			if(v2Payment.getCardDetails() != null && v2Payment.getCardDetails().getCard() != null) {
    				v1Tender.setCardBrand(v2Payment.getCardDetails().getCard().getCardBrand());
    				v1Tender.setPanSuffix(v2Payment.getCardDetails().getCard().getLast4());
    				v1Tender.setEntryMethod(v2Payment.getCardDetails().getEntryMethod());
    			}
    			v1Tender.setPaymentNote(v2Payment.getNote());
    			v1Tender.setTotalMoney(new Money(v2Payment.getTotalMoney().getAmount()));
    			if(v2Payment.getCashDetails() != null) {
    				CashPaymentDetails cashDetails = v2Payment.getCashDetails();
    				if(cashDetails.getChangeBackMoney() != null) {
        				v1Tender.setChangeBackMoney(new Money(cashDetails.getChangeBackMoney().getAmount()));
    				}
    				if(cashDetails.getBuyerSuppliedMoney() != null) {
        				v1Tender.setTenderedMoney(new Money(cashDetails.getBuyerSuppliedMoney().getAmount()));
    				}
    			}
    			v1Tender.setExchange(isExchange(order));
    			if(isExchange(order)) {
    				// set the "isExchange" flag of the actual tender to be false, because we will be creating another
    				// v1 Tender object with the exchanged amount - this tender's "isExchange" flag should be set as true.
    				v1Tender.setExchange(false);
    				int netAmounts = order.getNetAmounts().getTotalMoney().getAmount();
    			      // for up exchanges, we use the return lineitem as the additional payment to the tender payment.
    			      // for example, the original item was $20, but customer exchanges for $50 item, then the tender
    			      // will pay $30, and count the returned $20 as the extra payment.
				    if(netAmounts > 0) {
				    	for(OrderReturn orderReturn : order.getReturns()) {
				    		for(OrderReturnLineItem lineItem : orderReturn.getReturnLineItems()) {
			    				v1Tenders.add(getExchangeTender(new Money(lineItem.getTotalMoney().getAmount())));
				    		}
				    	}
				    }
				    // for even and down exchanges, we just take the order lineitem (which is the item the customer
				    // wants to exchange for) as a payment, and later in the refunds we adjust. For example,
				    // if the customer bought a $200 item, but wants to exchange for a $80 item,
				    // then we count both $200 and $80 as payments. Later refunds, we refund the difference of $120
				    // as the payment refund, as well as the extra $80 that we charged ($200 - $120)
				    else {
				        for(OrderLineItem lineItem : order.getLineItems()) {
		    				v1Tenders.add(getExchangeTender(new Money(lineItem.getTotalMoney().getAmount())));
				        }
				    }
    			}
    			v1Tenders.add(v1Tender);
    		}
    	}
    	payment.setTender(v1Tenders.toArray(new com.squareup.connect.Tender[0]));
    }

    public static com.squareup.connect.Tender getExchangeTender(Money totalMoney) {
    	com.squareup.connect.Tender exchangeTender = new com.squareup.connect.Tender();
		exchangeTender.setExchange(true);
		exchangeTender.setTotalMoney(totalMoney);
		exchangeTender.setType("OTHER");
		exchangeTender.setName("Other");
		return exchangeTender;
    }

    public static void setPaymentDetails(Order order, com.squareup.connect.Payment payment, Map<String, CatalogObject> catalogMap,
    		Map<String, CatalogObject> lineItemCategories, Map<String, Payment> tenderToPayment,
			Customer customer) {
    	List<PaymentItemization> itemizations = new ArrayList<>();
    	List<PaymentTax> inclusiveTax = new ArrayList<>();
		List<PaymentTax> additiveTax = new ArrayList<>();

    	if(order.getLineItems() != null) {
    		for(OrderLineItem lineItem : order.getLineItems()) {
    			//itemization details
    			PaymentItemization itemization = new PaymentItemization();
    			itemization.setName(lineItem.getName());
    			itemization.setQuantity(Double.parseDouble(lineItem.getQuantity()));
    			itemization.setItemizationType(lineItem.getItemType());
    			CatalogObject itemVariation = catalogMap.get(lineItem.getCatalogObjectId());
    			CatalogObject category = lineItemCategories.get(lineItem.getCatalogObjectId());
    			PaymentItemDetail detail = new PaymentItemDetail();
    			detail.setCategoryName(category != null && category.getCategoryData() != null ? category.getCategoryData().getName() : "");
    			detail.setSku(itemVariation != null && itemVariation.getItemVariationData() != null ? itemVariation.getItemVariationData().getSku() : "");
    			detail.setItemVariationId(lineItem.getCatalogObjectId());
    			detail.setItemId(itemVariation != null && itemVariation.getItemVariationData() != null ? itemVariation.getItemVariationData().getItemId() : "");
    			itemization.setItemDetail(detail);
    			itemization.setItemVariationName(lineItem.getVariationName());
    			itemization.setNotes(lineItem.getNote());
    			itemization.setTotalMoney(new Money(lineItem.getTotalMoney().getAmount()));
    			itemization.setSingleQuantityMoney(new Money(lineItem.getBasePriceMoney().getAmount()));
    			itemization.setGrossSalesMoney(new Money(lineItem.getGrossSalesMoney().getAmount()));
    			itemization.setDiscountMoney(new Money(-lineItem.getTotalDiscountMoney().getAmount()));
    			itemization.setNetSalesMoney(new Money(lineItem.getGrossSalesMoney().getAmount() - lineItem.getTotalDiscountMoney().getAmount()));
    			//taxes
    			Map<String, OrderLineItemTax> lineItemTaxes = getOrderLineItemTaxMap(order);
    			List<PaymentTax> paymentTaxes = new ArrayList<>();
    			if(lineItem.getAppliedTaxes() != null) {
					for (OrderLineItemAppliedTax lineItemAppliedTax : lineItem.getAppliedTaxes()) {
						OrderLineItemTax lineItemTax = lineItemTaxes.get(lineItemAppliedTax.getTaxUid());
						PaymentTax v1Tax = new PaymentTax();
						v1Tax.setName(lineItemTax.getName());
						v1Tax.setAppliedMoney(new Money(lineItemAppliedTax.getAppliedMoney().getAmount()));
						double rate = Double.parseDouble(lineItemTax.getPercentage());
					    double reformattedNumber = rate / 100;
					    DecimalFormat df = new DecimalFormat("0.00000000");
						v1Tax.setRate(df.format(reformattedNumber));
						v1Tax.setInclusionType(lineItemTax.getType());
						v1Tax.setFeeId(lineItemAppliedTax.getTaxUid());
						paymentTaxes.add(v1Tax);
						if (lineItemTax.getType().equals("ADDITIVE")) {
							additiveTax.add(v1Tax);
						} else if(lineItemTax.getType().equals("INCLUSIVE")) {
							inclusiveTax.add(v1Tax);
						}
					}
				}
    			itemization.setTaxes(paymentTaxes.toArray(new PaymentTax[0]));

    			//discounts
    			Map<String, OrderLineItemDiscount> lineItemDiscounts = getOrderLineItemDiscountMap(order);
    			List<PaymentDiscount> paymentDiscounts = new ArrayList<>();
    			if(lineItem.getAppliedDiscounts() != null) {
    				for(OrderLineItemAppliedDiscount lineItemAppliedDiscount : lineItem.getAppliedDiscounts()) {
    					OrderLineItemDiscount lineItemDiscount = lineItemDiscounts.get(lineItemAppliedDiscount.getDiscountUid());
    					PaymentDiscount v1Discount = new PaymentDiscount();
    					v1Discount.setName(lineItemDiscount.getName());
    					v1Discount.setAppliedMoney(new Money(-lineItemAppliedDiscount.getAppliedMoney().getAmount()));
    					v1Discount.setDiscountId(lineItemAppliedDiscount.getDiscountUid());
    					paymentDiscounts.add(v1Discount);
    				}
    			}
    			itemization.setDiscounts(paymentDiscounts.toArray(new PaymentDiscount[0]));


    			//modifiers
    			List<PaymentModifier> paymentModifiers = new ArrayList<>();
    			if(lineItem.getModifiers() != null) {
    				for(OrderLineItemModifier lineItemModifier : lineItem.getModifiers()) {
    					PaymentModifier v1Modifier = new PaymentModifier();
    					v1Modifier.setName(lineItemModifier.getName());
    					v1Modifier.setAppliedMoney(new Money(lineItemModifier.getTotalPriceMoney().getAmount()));
    					v1Modifier.setModifierOptionId(lineItemModifier.getCatalogObjectId());
    				}
    			}
    			itemization.setModifiers(paymentModifiers.toArray(new PaymentModifier[0]));

    			itemizations.add(itemization);
    		}
    	}
    	payment.setItemizations(itemizations.toArray(new PaymentItemization[0]));
    	payment.setInclusiveTax(inclusiveTax.toArray(new PaymentTax[0]));
    	payment.setAdditiveTax(additiveTax.toArray(new PaymentTax[0]));
    }

    public static Map<String, OrderLineItemTax> getOrderLineItemTaxMap(Order order) {
        return null;//pratikesh//order != null && order.getTaxes() != null ? Arrays.stream(order.getTaxes()).collect(Collectors.toMap(OrderLineItemTax::getUid, tax -> tax)) : new HashMap<>();
    }

    public static Map<String, OrderLineItemDiscount> getOrderLineItemDiscountMap(Order order) {
        return null;//pratikesh//order != null && order.getDiscounts() != null ? Arrays.stream(order.getDiscounts()).collect(Collectors.toMap(OrderLineItemDiscount::getUid, discount -> discount)) : new HashMap<>();
    }

    public static boolean isExchange(Order order) {
    	return order != null && order.getLineItems() != null && order.getLineItems().length > 0 && order.getReturns() != null && order.getReturns().length > 0;
    }

    public static List<Refund> toV1Refunds(PaymentRefund[] paymentRefunds, SquareClientV2 clientV2, String locationId, Map<String, String> params) throws Exception {
        List<Refund> refunds = new ArrayList<>();
        Order[] orders = getRefundedAndExchangedOrders(clientV2, locationId, params, paymentRefunds);
        for(Order order : orders) {
        	Order sourceOrder = order.getReturns() != null && order.getReturns().length > 0 ? clientV2.orders().retrieve(locationId, order.getReturns()[0].getSourceOrderId()) : null;
          //exchange
        	if(isExchange(order)) {
	            int netAmount = order.getNetAmounts() != null ? order.getNetAmounts().getTotalMoney().getAmount() : 0;
	            int returnAmount = order.getReturnAmounts() != null ? order.getReturnAmounts().getTotalMoney().getAmount() : 0;
	            // if it's an down exchange (paying customer back after an exchange), we need to refund the difference
	            // i.e. customer bought $200 item, and exchanged it for $80. When we process transactions,
	            // we process both items as transactions, so the total incoming payment would be $280.
	            // Refunds needs to process the paymentRefund of $120, as well as the difference between
	            // the down exchange and the payment refund, which would be $200 - $120 = $80
	            if(netAmount <= 0) {
	              returnAmount += netAmount;
	            }
	            // exchanges in V2 do not have ids (in V1, exchange is technically a payment) so for refund, use orderId
	            Refund refund = new Refund();
	            refund.setCreatedAt(order.getCreatedAt());
	            refund.setProcessedAt(order.getCreatedAt());
	            //exchanges are returned goods
	            refund.setReason("Returned Goods");
	            refund.setRefundedMoney(new Money(-returnAmount));
	            // exchanges in V2 do not have paymentIds. Substituting it with order Id instead
	            String paymentId = order.getTenders() != null && order.getTenders().length > 0 ? order.getTenders()[0].getId() : order.getId();
	            refund.setPaymentId(paymentId);
	            refund.setType(getRefundType(refund, sourceOrder));
	            refunds.add(refund);
           }
	          // refund
	       if(order.getRefunds() != null) {
	            for(com.squareup.connect.v2.Refund v2Refund : order.getRefunds()) {
	            	 Refund refund = new Refund();
	                 refund.setCreatedAt(v2Refund.getCreatedAt());
	                 refund.setProcessedAt(v2Refund.getCreatedAt());
	                 refund.setReason(v2Refund.getReason());
	                 refund.setRefundedMoney(new Money(-v2Refund.getAmountMoney().getAmount()));
	                 // exchanges in V2 do not have paymentIds. Substituting it with order Id instead
	                 refund.setPaymentId(v2Refund.getTenderId());
	                 refund.setType(getRefundType(refund, sourceOrder));
	                 refunds.add(refund);
	            }
	        }
	    }
        return refunds;
     }

    public static String getRefundType(Refund refund, Order sourceOrder) {
    	if(sourceOrder == null) {
    		return "CUSTOM";
    	}
    	if(-refund.getRefundedMoney().getAmount() < sourceOrder.getTotalMoney().getAmount()) {
    		return "PARTIAL";
    	}
    	return "FULL";
    }

    public static Order[] getRefundedAndExchangedOrders(SquareClientV2 squareClientV2, String locationId, Map<String, String> params, PaymentRefund[] refunds) throws Exception {
		SearchOrdersQuery orderQuery = getOrdersQuery(params);

        Order[] allOrders = squareClientV2.orders().search(locationId, orderQuery);
        List<Order> orders = new ArrayList<Order>();
        Set<String> existingOrderIds = new HashSet<>();
        for(Order order : allOrders) {
        	if(isExchange(order)) {
        		orders.add(order);
        		existingOrderIds.add(order.getId());
        	}
        }
        Set<String> refundedOrderIds = new HashSet<>();
        if(refunds != null) {
        	for(PaymentRefund refund : refunds) {
        		if(!existingOrderIds.contains(refund.getOrderId()))
        			refundedOrderIds.add(refund.getOrderId());
        	}
        }
        Order[] refundedOrders = squareClientV2.orders().batchRetrieve(locationId, refundedOrderIds.toArray(new String[0]));
        for(Order refundedOrder : refundedOrders) {
        	orders.add(refundedOrder);
        }
        return orders.toArray(new Order[0]);
	}

}





