package jockey;

import java.sql.Connection;
import java.sql.DriverManager;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.Payment;
import com.squareup.connect.SquareClient;
import com.squareup.connect.v2.GiftCardActivity;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.OrderEntry;
import com.squareup.connect.v2.OrderLineItem;
import com.squareup.connect.v2.OrderLineItemDiscount;
import com.squareup.connect.v2.OrderReturn;
import com.squareup.connect.v2.OrderReturnLineItem;
import com.squareup.connect.v2.OrderReturnLineItemDiscount;
import com.squareup.connect.v2.PaymentRefund;
import com.squareup.connect.v2.Refund;
import com.squareup.connect.v2.SearchOrdersDateTimeFilter;
import com.squareup.connect.v2.SearchOrdersFilter;
import com.squareup.connect.v2.SearchOrdersQuery;
import com.squareup.connect.v2.SearchOrdersSort;
import com.squareup.connect.v2.SearchOrdersStateFilter;
import com.squareup.connect.v2.SquareClientV2;
import com.squareup.connect.v2.Tender;
import com.squareup.connect.v2.TimeRange;

import util.Constants;
import util.SquarePayload;
import util.TimeManager;

public class GenerateLocationTlogCallable implements Callable {
    @Value("${encryption.key.tokens}")
    private String encryptionKey;

    @Value("${jockey.api.url}")
    private String apiUrl;
    @Value("${jockey.sftp.ip}")
    private String sftpHost;
    @Value("${jockey.sftp.port}")
    private int sftpPort;
    @Value("${jockey.sftp.user}")
    private String sftpUser;
    @Value("${jockey.sftp.password}")
    private String sftpPassword;
    @Value("${jockey.sftp.path}")
    private String sftpPath;

    @Value("jdbc:mysql://${mysql.ip}:${mysql.port}/${mysql.database}")
    private String databaseUrl;
    @Value("${mysql.user}")
    private String databaseUser;
    @Value("${mysql.password}")
    private String databasePassword;

    private static String VAR_LOCATION_OVERRIDE = "locationOverride";
    private static String ITEM_TYPE_GIFT_CARD = "GIFT_CARD";

    private static String DEFAULT_UPC = "";
    private static String DEFAULT_SKU = "";
    private static String DEFAULT_DISPLAY_NAME = "Custom Amount";
    private static String DEFAULT_CASHIER_ID = "9001";
    private static String DEFAULT_DEVICE_ID = "9";
    private static String DEFAULT_OTHER_TENDER_CODE = "ZZ";
    private static String DEFAULT_NO_TENDER_SALE_CODE = "7E";
    private static String GIFT_CARD_SKU = "GIFTCARD";
    private static int MAX_TRANSACTION_NUMBER = 99999;

    private static final Set<String> SKIP_LOCATIONS = new HashSet<String>(Arrays.asList(new String[] {}));

    private static Logger logger = LoggerFactory.getLogger(GenerateLocationTlogCallable.class);

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        MuleMessage message = eventContext.getMessage();

        Location location = (Location) message.getPayload();
        String locationOverride = message.getProperty(VAR_LOCATION_OVERRIDE, PropertyScope.SESSION);

        // Skip location processing?
        if (isLocationSkipped(location, locationOverride)) {
            logger.info("Skipping location: " + location.getName());
            return 0;
        }

        message.setProperty("storeNumber", getStoreNumber(location.getName()), PropertyScope.INVOCATION);

        SquarePayload squarePayload = (SquarePayload) message.getProperty(Constants.SQUARE_PAYLOAD,
                PropertyScope.SESSION);
        int range = Integer.parseInt(message.getProperty(Constants.RANGE, PropertyScope.SESSION));
        int offset = Integer.parseInt(message.getProperty(Constants.OFFSET, PropertyScope.SESSION));

        Map<String, String> employeeCache = (Map<String, String>) message.getProperty(Constants.EMPLOYEES,
                PropertyScope.SESSION);

        String accessToken = squarePayload.getAccessToken(this.encryptionKey);
        String merchantId = squarePayload.getMerchantId();
        Map<String, String> dateParams = TimeManager.getPastDayInterval(range, offset, location.getTimezone());

        SquareClient clientV1 = new SquareClient(accessToken, apiUrl, "v1", merchantId);

        // Retrieve v1 payment details (to to mapped to V2 Orders on tender ID)
        Payment[] v1Payments = clientV1.payments().list(dateParams);
        Map<String, Payment> v1PaymentCache = new HashMap<String, Payment>();
        for (Payment v1Payment : v1Payments) {
            v1PaymentCache.put(v1Payment.getId(), v1Payment); // it's possible for a payment to not have a tender

            String v2OrderId = v1Payment.getPaymentUrl().split("transactions/", 2)[1];
            v1PaymentCache.put(v2OrderId, v1Payment);

            for (com.squareup.connect.Tender v1Tender : v1Payment.getTender()) {
                v1PaymentCache.put(v1Tender.getId(), v1Payment);
            }
        }

        SquareClientV2 clientV2 = new SquareClientV2(apiUrl, accessToken);
        clientV2.setLogInfo("JOCKEY: " + location.getId());

        SearchOrdersQuery orderQuery = new SearchOrdersQuery();
        SearchOrdersFilter searchFilter = new SearchOrdersFilter();
        SearchOrdersSort searchSort = new SearchOrdersSort();
        orderQuery.setFilter(searchFilter);
        orderQuery.setSort(searchSort);

        SearchOrdersStateFilter stateFilter = new SearchOrdersStateFilter();
        stateFilter.setStates(new String[] { "COMPLETED" });
        searchFilter.setStateFilter(stateFilter);

        SearchOrdersDateTimeFilter dateFilter = new SearchOrdersDateTimeFilter();
        TimeRange timeRange = new TimeRange();
        timeRange.setStartAt(dateParams.getOrDefault("begin_time", ""));
        timeRange.setEndAt(dateParams.getOrDefault("end_time", ""));
        dateFilter.setClosedAt(timeRange);
        searchFilter.setDateTimeFilter(dateFilter);

        searchSort.setSortField("CLOSED_AT");
        searchSort.setSortOrder("ASC");

        // Get OrderEntries, then batch review all Orders by ID
        List<OrderEntry> v2OrderEntries = Arrays
                .asList(clientV2.orders().searchOrderEntries(location.getId(), orderQuery));

        ArrayList<String> orderEntryIds = new ArrayList<String>();
        for (OrderEntry e : v2OrderEntries) {
            orderEntryIds.add(e.getOrderId());
        }
        List<Order> v2Orders = Arrays.asList(clientV2.orders().batchRetrieve(location.getId(),
                orderEntryIds.toArray(new String[orderEntryIds.size()])));

        Collections.sort(v2Orders, new Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                return o1.getClosedAt().compareTo(o2.getClosedAt());
            }
        });

        // Establish database connection for Order numbers
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
        JockeyDatabaseApi databaseApi = new JockeyDatabaseApi(conn);

        // Get latest transaction numbers
        Map<String, Integer> nextDeviceTransactionNumbers = new HashMap<String, Integer>();
        ArrayList<Map<String, String>> records = databaseApi.queryOrderNumbersForLocation(location.getId());
        System.out.println("LOCATION: " + location.getId());
        for (Map<String, String> record : records) {
            nextDeviceTransactionNumbers.put(record.get("deviceId"),
                    Integer.parseInt(record.get("lastOrderNumber")) + 1);
        }
        ArrayList<String> orderIds = new ArrayList<String>();

        // get full order details for refunded orders in this period
        Map<String, Tender> tenderCache = new HashMap<String, Tender>();
        Map<String, Order> sourceOrderCache = new HashMap<String, Order>();

        for (Order order : v2Orders) {
            if (order.getTenders() != null) {
                for (Tender t : order.getTenders()) {
                    tenderCache.put(t.getId(), t);
                }
            }

            // represents the refund of money (with a source payment)
            if (order.getRefunds() != null && order.getRefunds().length > 0) {
                for (Refund refund : order.getRefunds()) {
                    Order refundedOrder = clientV2.orders().retrieve(refund.getLocationId(), refund.getTransactionId());

                    if (refundedOrder.getTenders() != null) {
                        for (Tender rt : refundedOrder.getTenders()) {
                            tenderCache.put(rt.getId(), rt);
                        }
                    }

                    Payment refundedV1Payment = clientV1.payments().retrieve(refund.getTransactionId());
                    v1PaymentCache.put(refund.getTransactionId(), refundedV1Payment);
                    v1PaymentCache.put(refundedV1Payment.getId(), refundedV1Payment);
                    for (com.squareup.connect.Tender v1RefundedTender : refundedV1Payment.getTender()) {
                        v1PaymentCache.put(v1RefundedTender.getId(), refundedV1Payment);
                    }
                }
            }

            // represents the return of items (with a source order)
            if (order.getReturns() != null && order.getReturns().length > 0) {
                for (OrderReturn r : order.getReturns()) {
                    Order sourceOrder = clientV2.orders().retrieve(order.getLocationId(), r.getSourceOrderId());
                    sourceOrderCache.put(sourceOrder.getId(), sourceOrder);
                    sourceOrderCache.put(order.getId(), sourceOrder);

                    if (sourceOrder.getTenders() != null) {
                        for (Tender rt : sourceOrder.getTenders()) {
                            tenderCache.put(rt.getId(), rt);
                        }
                    }
                }
            }
            orderIds.add(order.getId());
        }

        // Get details on unlinked refunds to gift card tenders
        Map<String, Boolean> giftCardActivityCache = new HashMap<String, Boolean>();

        HashMap<String, String> gcQuery = new HashMap<String, String>();
        gcQuery.put("begin_time", dateParams.getOrDefault("begin_time", ""));
        gcQuery.put("end_time", dateParams.getOrDefault("end_time", ""));

        clientV2 = new SquareClientV2(apiUrl, accessToken);
        clientV2.setVersion("2021-06-16");

        GiftCardActivity[] gcActivities = clientV2.giftCards().listActivities(gcQuery);

        for (GiftCardActivity gcActivity : gcActivities) {
            if (gcActivity.getType().equals("REFUND")) {
                giftCardActivityCache.put(gcActivity.getRefundActivityDetails().getPaymentId(), true);
            } else if (gcActivity.getType().equals("UNLINKED_ACTIVITY_REFUND")) {
                giftCardActivityCache.put(gcActivity.getUnlinkedActivityRefundActivityDetails().getPaymentId(), true);
            }
        }

        ArrayList<SalesOrder> salesOrders = new ArrayList<SalesOrder>();

        // HANDLE UNLINKED REFUNDS SEPARATELY FROM ORDERS WHILE IN ALPHA
        HashMap<String, String> refundParams = new HashMap<String, String>();
        refundParams.put("begin_time", dateParams.getOrDefault("begin_time", ""));
        refundParams.put("end_time", dateParams.getOrDefault("end_time", ""));
        refundParams.put("location_id", location.getId());

        clientV2 = new SquareClientV2(apiUrl, accessToken);
        clientV2.setVersion("2021-06-16");
        PaymentRefund[] v2Refunds = clientV2.refunds().listPaymentRefunds(refundParams);

        for (PaymentRefund refund : v2Refunds) {
            if (refund.isUnlinked()) {
                orderIds.add(refund.getId()); // for lookup of existing transaction ID cache
                salesOrders.add(createSalesOrderFromUnlinkedRefund(location, refund));
            }
        }

        // Get existing transaction numbers for Orders
        Map<String, Integer> existingTransactionNumbers = new HashMap<String, Integer>();
        if (orderIds.size() > 0) {
            ArrayList<Map<String, String>> existingOrderRecords = databaseApi.queryOrdersById(orderIds);
            for (Map<String, String> orderRecord : existingOrderRecords) {
                existingTransactionNumbers.put(orderRecord.get("recordId"),
                        Integer.parseInt(orderRecord.get("recordNumber")));
            }
        }

        for (Order order : v2Orders) {
            SalesOrder sOrder = createSalesOrder(location, employeeCache, v1PaymentCache, tenderCache,
                    giftCardActivityCache, order);
            salesOrders.add(sOrder);
        }

        Collections.sort(salesOrders, new Comparator<SalesOrder>() {
            @Override
            public int compare(SalesOrder so1, SalesOrder so2) {
                return so1.getDateCompleted().compareTo(so2.getDateCompleted());
            }
        });
        for (SalesOrder so : salesOrders) {
            int transactionNumber = getTransactionNumber(so.getThirdPartyOrderId(), so.getRegisterNumber(),
                    nextDeviceTransactionNumbers, existingTransactionNumbers);
            so.setTransactionNumber("" + transactionNumber);
        }

        databaseApi.updateOrderNumbersForLocation(salesOrders, location.getId());
        databaseApi.close();

        return salesOrders;
    }

    private int getTransactionNumber(String id, String registerNumber,
            Map<String, Integer> nextDeviceTransactionNumbers, Map<String, Integer> existingTransactionNumbers) {
        int nextTransactionNumberForDevice = nextDeviceTransactionNumbers.getOrDefault(registerNumber, 1);
        int transactionNumber = existingTransactionNumbers.getOrDefault(id, nextTransactionNumberForDevice);

        // increment if we used a new number
        if (transactionNumber == nextTransactionNumberForDevice) {
            int incrementedTransactionNumber = nextTransactionNumberForDevice + 1;
            if (incrementedTransactionNumber > MAX_TRANSACTION_NUMBER) {
                incrementedTransactionNumber = 1;
            }
            nextDeviceTransactionNumbers.put(registerNumber, incrementedTransactionNumber);
        }

        return transactionNumber;
    }

    private WeaklyTypedProperty[] getLineItemDiscountProperties(OrderLineItem lineItem) {
        ArrayList<WeaklyTypedProperty> discountProperties = new ArrayList<WeaklyTypedProperty>();

        if (lineItem.getDiscounts() == null) {
            return new WeaklyTypedProperty[] {};
        }

        for (OrderLineItemDiscount discount : lineItem.getDiscounts()) {
            String discountType = discount.getScope().equals("ORDER") ? "order-level-discount" : "line-level-discount";

            WeaklyTypedProperty wtp = new WeaklyTypedProperty();
            wtp.setName(discount.getName());
            wtp.setType(discountType);
            wtp.setValue(Util.getAmountAsXmlDecimal(discount.getAppliedMoney().getAmount()));

            discountProperties.add(wtp);
        }

        return discountProperties.toArray(new WeaklyTypedProperty[discountProperties.size()]);
    }

    private WeaklyTypedProperty[] getLineItemDiscountProperties(OrderReturnLineItem lineItem) {
        ArrayList<WeaklyTypedProperty> discountProperties = new ArrayList<WeaklyTypedProperty>();

        if (lineItem.getReturnDiscounts() == null) {
            return new WeaklyTypedProperty[] {};
        }

        for (OrderReturnLineItemDiscount discount : lineItem.getReturnDiscounts()) {
            String discountType = discount.getScope().equals("ORDER") ? "order-level-discount" : "line-level-discount";

            WeaklyTypedProperty wtp = new WeaklyTypedProperty();
            wtp.setName(discount.getName());
            wtp.setType(discountType);
            wtp.setValue(Util.getAmountAsXmlDecimal(discount.getAppliedMoney().getAmount()));

            discountProperties.add(wtp);
        }

        return discountProperties.toArray(new WeaklyTypedProperty[discountProperties.size()]);
    }

    private SalesOrder createSalesOrderFromUnlinkedRefund(Location location, PaymentRefund refund) throws Exception {

        SalesOrder so = new SalesOrder();

        so.setThirdPartyOrderId(refund.getId());
        so.setTransactionNumber("0000");
        so.setStoreNumber(getStoreNumber(location.getName()));
        so.setDateCreated(formatDate(location, refund.getCreatedAt().replace(".000Z", "Z")));
        so.setDateCompleted(formatDate(location, refund.getUpdatedAt().replace(".000Z", "Z")));
        so.setSalesOrderCode("11"); // standard refund code
        so.setRegisterNumber(DEFAULT_DEVICE_ID);
        so.setCashier(DEFAULT_CASHIER_ID);
        so.setShippingTotal(Util.getAmountAsXmlDecimal(0));
        so.setTaxTotal(Util.getAmountAsXmlDecimal(0));
        so.setTotal(Util.getAmountAsXmlDecimal(refund.getAmountMoney().getAmount()));

        ArrayList<SalesOrderPayment> salesOrderPayments = new ArrayList<SalesOrderPayment>();
        SalesOrderPayment sop = new SalesOrderPayment();
        sop.setAmount(Util.getAmountAsXmlDecimal(-1 * refund.getAmountMoney().getAmount()));
        sop.setPaymentCode(getUnlinkedRefundPaymentCode(refund));
        sop.setPaymentId(refund.getId());
        salesOrderPayments.add(sop);

        so.setPayments(salesOrderPayments.toArray(new SalesOrderPayment[salesOrderPayments.size()]));

        ArrayList<SalesOrderLineItem> salesOrderLineItems = new ArrayList<SalesOrderLineItem>();

        // Make sure at least one itemization to include
        // ex: goods{V}TVJ590PFH;86,GLJ778XGS;94,XHN185XSK;79,XXO628EJB;34,MOK907CC2;48,BJP280HJZ;4
        String itemizationValues = refund.getReason().split("\\}")[1];

        if (itemizationValues.length() > 0 && itemizationValues.contains(";")) {
            int i = 1;
            for (String lineItem : itemizationValues.split(",")) {
                String itemSku = lineItem.split(";")[0];
                String itemQty = lineItem.split(";")[1];

                SalesOrderLineItem soli = new SalesOrderLineItem();

                soli.setLineNumber("" + i++);
                soli.setLineCode("11");
                soli.setSku(itemSku);
                soli.setUpc(itemSku);
                soli.setQuantity(itemQty);
                soli.setDisplayName(itemSku);
                salesOrderLineItems.add(soli);

                // no data
                soli.setWeaklyTypedProperties(new WeaklyTypedProperty[0]);
            }
        }

        so.setLineItems(salesOrderLineItems.toArray(new SalesOrderLineItem[salesOrderLineItems.size()]));

        return so;
    }

    private SalesOrder createSalesOrder(Location location, Map<String, String> employeeCache,
            Map<String, Payment> v1PaymentCache, Map<String, Tender> tenderCache,
            Map<String, Boolean> giftCardActivityCache, Order order) throws Exception {
        SalesOrder so = new SalesOrder();

        so.setThirdPartyOrderId(order.getId());
        so.setStoreNumber(getStoreNumber(location.getName()));
        so.setDateCreated(formatDate(location, order.getCreatedAt().replace(".000Z", "Z")));
        so.setDateCompleted(formatDate(location, order.getClosedAt().replace(".000Z", "Z")));
        so.setSalesOrderCode(getOrderCode(order, v1PaymentCache));
        so.setRegisterNumber(getRegisterNumberFromOrder(order, v1PaymentCache));

        // the kind of shit you need to do because of our fragmented API
        String cashierId = DEFAULT_CASHIER_ID;
        Payment v1Payment = v1PaymentCache.get(getFirstTenderId(order));
        if (v1Payment != null && v1Payment.getTender() != null && v1Payment.getTender().length > 0) {
            cashierId = getExternalEmployeeId(employeeCache, v1Payment.getTender()[0].getEmployeeId());
        }
        so.setCashier(cashierId);

        so.setShippingTotal(Util.getAmountAsXmlDecimal(0));
        so.setTaxTotal(Util.getAmountAsXmlDecimal(getOrderTaxTotal(order)));
        so.setTotal(Util.getAmountAsXmlDecimal(getOrderTotal(order)));

        ArrayList<SalesOrderPayment> salesOrderPayments = new ArrayList<SalesOrderPayment>();

        if (order.getTenders() != null) {
            for (com.squareup.connect.v2.Tender tender : order.getTenders()) {
                SalesOrderPayment sop = new SalesOrderPayment();
                sop.setAmount(Util.getAmountAsXmlDecimal(tender.getAmountMoney().getAmount()));
                sop.setPaymentCode(getPaymentCode(false, tender, v1PaymentCache, giftCardActivityCache));
                sop.setPaymentId(tender.getId());
                salesOrderPayments.add(sop);
            }
        }
        if (order.getRefunds() != null) {
            for (Refund tenderRefund : order.getRefunds()) {
                SalesOrderPayment sop = new SalesOrderPayment();
                sop.setAmount(Util.getAmountAsXmlDecimal(-1 * tenderRefund.getAmountMoney().getAmount()));
                sop.setPaymentCode(getPaymentCode(true, getOriginalTenderFromRefundTender(tenderCache, tenderRefund),
                        v1PaymentCache, giftCardActivityCache));
                sop.setPaymentId(tenderRefund.getId());
                salesOrderPayments.add(sop);
            }
        }
        so.setPayments(salesOrderPayments.toArray(new SalesOrderPayment[salesOrderPayments.size()]));

        ArrayList<SalesOrderLineItem> salesOrderLineItems = new ArrayList<SalesOrderLineItem>();
        int i = 1;

        if (order.getReturns() != null) {
            for (OrderReturn orderReturn : order.getReturns()) {
                if (orderReturn.getReturnLineItems() != null) {
                    for (OrderReturnLineItem lineItem : orderReturn.getReturnLineItems()) {
                        SalesOrderLineItem soli = new SalesOrderLineItem();

                        int qty = Integer.parseInt(lineItem.getQuantity());
                        int extendedPrice = (lineItem.getTotalMoney().getAmount()
                                - lineItem.getTotalTaxMoney().getAmount()) * -1;

                        soli.setThirdPartyLineItemId(lineItem.getUid());
                        soli.setLineNumber("" + i++);
                        soli.setLineCode(getReturnLineCode(lineItem));
                        soli.setSku(getSku(lineItem));
                        soli.setUpc(getUpc(lineItem));
                        soli.setQuantity(lineItem.getQuantity());
                        soli.setDisplayName((lineItem.getCatalogObjectId() == null) ? DEFAULT_DISPLAY_NAME
                                : lineItem.getVariationName());
                        soli.setListPrice(Util.getAmountAsXmlDecimal(lineItem.getBasePriceMoney().getAmount()));
                        soli.setPlacedPrice(Util.getAmountAsXmlDecimal(lineItem.getBasePriceMoney().getAmount()));
                        soli.setDiscountedItemPrice(Util.getAmountAsXmlDecimal(extendedPrice / qty));
                        soli.setExtendedPrice(Util.getAmountAsXmlDecimal(extendedPrice));
                        soli.setOrderLevelDiscountAmount(Util.getAmountAsXmlDecimal(sumOrderLevelDiscounts(lineItem)));
                        soli.setLineItemDiscountAmount(Util.getAmountAsXmlDecimal(sumItemLevelDiscounts(lineItem)));

                        soli.setWeaklyTypedProperties(getLineItemDiscountProperties(lineItem));

                        salesOrderLineItems.add(soli);
                    }
                }
            }
        }

        if (order.getLineItems() != null) {
            for (OrderLineItem lineItem : order.getLineItems()) {
                SalesOrderLineItem soli = new SalesOrderLineItem();

                int qty = Integer.parseInt(lineItem.getQuantity());
                int extendedPrice = lineItem.getTotalMoney().getAmount() - lineItem.getTotalTaxMoney().getAmount();

                soli.setThirdPartyLineItemId(lineItem.getUid());
                soli.setLineNumber("" + i++);
                soli.setLineCode(getSaleLineCode(lineItem));
                soli.setSku(getSku(lineItem));
                soli.setUpc(getUpc(lineItem));
                soli.setQuantity(lineItem.getQuantity());
                soli.setDisplayName(
                        (lineItem.getCatalogObjectId() == null) ? DEFAULT_DISPLAY_NAME : lineItem.getVariationName());
                soli.setListPrice(Util.getAmountAsXmlDecimal(lineItem.getBasePriceMoney().getAmount()));
                soli.setPlacedPrice(Util.getAmountAsXmlDecimal(lineItem.getBasePriceMoney().getAmount()));
                soli.setDiscountedItemPrice(Util.getAmountAsXmlDecimal(extendedPrice / qty));
                soli.setExtendedPrice(Util.getAmountAsXmlDecimal(extendedPrice));
                soli.setOrderLevelDiscountAmount(Util.getAmountAsXmlDecimal(sumOrderLevelDiscounts(lineItem)));
                soli.setLineItemDiscountAmount(Util.getAmountAsXmlDecimal(sumItemLevelDiscounts(lineItem)));

                soli.setWeaklyTypedProperties(getLineItemDiscountProperties(lineItem));

                salesOrderLineItems.add(soli);
            }
        }

        so.setLineItems(salesOrderLineItems.toArray(new SalesOrderLineItem[salesOrderLineItems.size()]));

        return so;
    }

    private int sumItemLevelDiscounts(OrderLineItem lineItem) {
        int discountTotal = 0;

        if (lineItem.getDiscounts() == null) {
            return discountTotal;
        }

        for (OrderLineItemDiscount discount : lineItem.getDiscounts()) {
            if (discount.getScope().equals("LINE_ITEM")) {
                discountTotal += discount.getAppliedMoney().getAmount();
            }
        }

        return discountTotal;
    }

    private int sumItemLevelDiscounts(OrderReturnLineItem lineItem) {
        int discountTotal = 0;

        if (lineItem.getReturnDiscounts() == null) {
            return discountTotal;
        }

        for (OrderReturnLineItemDiscount discount : lineItem.getReturnDiscounts()) {
            if (discount.getScope().equals("LINE_ITEM")) {
                discountTotal += discount.getAppliedMoney().getAmount();
            }
        }

        return discountTotal;
    }

    private int sumOrderLevelDiscounts(OrderLineItem lineItem) {
        int discountTotal = 0;

        if (lineItem.getDiscounts() == null) {
            return discountTotal;
        }

        for (OrderLineItemDiscount discount : lineItem.getDiscounts()) {
            if (discount.getScope().equals("ORDER")) {
                discountTotal += discount.getAppliedMoney().getAmount();
            }
        }

        return discountTotal;
    }

    private int sumOrderLevelDiscounts(OrderReturnLineItem lineItem) {
        int discountTotal = 0;

        if (lineItem.getReturnDiscounts() == null) {
            return discountTotal;
        }

        for (OrderReturnLineItemDiscount discount : lineItem.getReturnDiscounts()) {
            if (discount.getScope().equals("ORDER")) {
                discountTotal += discount.getAppliedMoney().getAmount();
            }
        }

        return discountTotal;
    }

    private Tender getOriginalTenderFromRefundTender(Map<String, Tender> tenderCache, Refund refund) throws Exception {
        Tender refundedTender = tenderCache.get(refund.getTenderId());
        return refundedTender;
    }

    private String getFirstTenderId(Order order) {
        String tenderId = "";
        if (order.getTenders() != null && order.getTenders().length > 0) {
            tenderId = order.getTenders()[0].getId();
        } else if (order.getRefunds() != null && order.getRefunds().length > 0) {
            tenderId = order.getRefunds()[0].getTenderId();
        }
        return tenderId;
    }

    private int getOrderTaxTotal(Order order) {
        int t = 0;

        if (order.getNetAmounts() != null && order.getNetAmounts().getTaxMoney() != null) {
            t = order.getNetAmounts().getTaxMoney().getAmount();
        }

        return t;
    }

    private int getOrderTotal(Order order) {
        int t = 0;

        if (order.getNetAmounts() != null && order.getNetAmounts().getTotalMoney() != null) {
            t = order.getNetAmounts().getTotalMoney().getAmount();
        }

        return t;
    }

    private boolean isExchangeOrder(Order order) {
        if ((order.getLineItems() != null && order.getLineItems().length > 0)
                && (order.getReturns() != null && order.getReturns().length > 0)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isReturnOrder(Order order) {
        if (order.getLineItems() == null && order.getRefunds() != null && order.getRefunds().length > 0) {
            return true;
        } else {
            return false;
        }
    }

    private String getOrderCode(Order order, Map<String, Payment> v1PaymentCache) {
        //"01"=Sale; "02"=BudgetSale; "03"=AltBudgetSale; "04"=EmployeeSale; "11"=Return; "14"=EmployeeReturn
        // Returns and Exchanges share the same code rule(s)

        // return
        if (isReturnOrder(order) || isExchangeOrder(order)) {
            if (order.getReturns() != null) {
                for (OrderReturn orderReturn : order.getReturns()) {
                    if (orderReturn.getReturnLineItems() != null) {
                        for (OrderReturnLineItem lineItem : orderReturn.getReturnLineItems()) {
                            if (lineItemReturnHasEmployeeDiscount(lineItem)) {
                                return "14";
                            }
                        }
                    }
                }
            }
            return "11";
        } else {
            // sale
            if (order.getLineItems() == null && order.getTotalMoney().getAmount() == 0) {
                return DEFAULT_NO_TENDER_SALE_CODE;
            }

            for (Tender tender : order.getTenders()) {
                if (!tender.getType().equals("OTHER")) {
                    continue;
                }

                Payment v1Payment = v1PaymentCache.get(tender.getId());
                if (v1Payment != null) {
                    com.squareup.connect.Tender[] v1Tenders = v1Payment.getTender();
                    for (com.squareup.connect.Tender v1T : v1Tenders) {
                        if (v1T.getId().equals(tender.getId())) {
                            if (v1T.getName() != null && v1T.getName().equals("CUSTOM")) {
                                return "03";
                            }
                        }
                    }
                }
            }

            for (OrderLineItem lineItem : order.getLineItems()) {
                if (lineItemHasEmployeeDiscount(lineItem)) {
                    return "04";
                }
            }
            return "01";
        }
    }

    private boolean lineItemReturnHasEmployeeDiscount(OrderReturnLineItem lineItem) {
        if (lineItem.getReturnDiscounts() != null) {
            for (OrderReturnLineItemDiscount d : lineItem.getReturnDiscounts()) {
                if (d.getName().toLowerCase().contains("employee")) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean lineItemHasEmployeeDiscount(OrderLineItem lineItem) {
        if (lineItem.getDiscounts() != null) {
            for (OrderLineItemDiscount d : lineItem.getDiscounts()) {
                if (d.getName().toLowerCase().contains("employee")) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean lineItemIsDonation(OrderLineItem lineItem) {
        if (lineItem.getName() != null && lineItem.getName().toLowerCase().contains("family donation")) {
            return true;
        }

        return false;
    }

    private boolean returnLineItemIsDonation(OrderReturnLineItem lineItem) {
        if (lineItem.getName() != null && lineItem.getName().toLowerCase().contains("family donation")) {
            return true;
        }

        return false;
    }

    private String getReturnLineCode(OrderReturnLineItem lineItem) {
        // "11"=Return; "14"=EmployeeReturn
        if (returnLineItemIsDonation(lineItem)) {
            return "DO";
        } else if (lineItemReturnHasEmployeeDiscount(lineItem)) {
            return "14";
        }
        return "11";
    }

    private String getSaleLineCode(OrderLineItem lineItem) {
        // "01"=Sale; "02"=BudgetSale; "03"=AltBudgetSale; "04"=EmployeeSale
        if (lineItemIsDonation(lineItem)) {
            return "DO";
        } else if (lineItemHasEmployeeDiscount(lineItem)) {
            return "04";
        }

        return "01";
    }

    private String getExternalEmployeeId(Map<String, String> employeeCache, String creatorId) {
        return (employeeCache.get(creatorId) != null) ? employeeCache.get(creatorId) : DEFAULT_CASHIER_ID;
    }

    // Item Variation name format: 100086878 | 1372 NoPanty Line Micro Hi Br | Toasted Beige | 6
    //                                 ^ upc
    private String getUpcFromItemVariationName(String name) {
        String upc = DEFAULT_UPC;
        try {
            upc = name.split(" ", 2)[0];
        } catch (Exception e) {
            // bad input
            logger.debug("Invalid Sku on object: " + name);

            if (upc.length() != 9) {
                logger.debug("Invalid Sku length on object: " + name);
            }
        }
        return upc;
    }

    private String getSku(OrderLineItem lineItem) {
        if (isGiftCardItem(lineItem)) {
            return GIFT_CARD_SKU;
        }

        return (lineItem.getSku() == null) ? DEFAULT_SKU : lineItem.getSku();
    }

    private String getSku(OrderReturnLineItem lineItem) {
        if (isGiftCardReturnItem(lineItem)) {
            return GIFT_CARD_SKU;
        }

        return (lineItem.getSku() == null) ? DEFAULT_SKU : lineItem.getSku();
    }

    private String getUpc(OrderLineItem lineItem) {
        if (isGiftCardItem(lineItem)) {
            return GIFT_CARD_SKU;
        }

        return (lineItem.getCatalogObjectId() == null) ? DEFAULT_UPC
                : getUpcFromItemVariationName(lineItem.getVariationName());
    }

    private String getUpc(OrderReturnLineItem lineItem) {
        if (isGiftCardReturnItem(lineItem)) {
            return GIFT_CARD_SKU;
        }

        return (lineItem.getCatalogObjectId() == null) ? DEFAULT_UPC
                : getUpcFromItemVariationName(lineItem.getVariationName());
    }

    private boolean isGiftCardItem(OrderLineItem lineItem) {
        return lineItem.getItemType().equals(ITEM_TYPE_GIFT_CARD);
    }

    private boolean isGiftCardReturnItem(OrderReturnLineItem lineItem) {
        return lineItem.getItemType().equals(ITEM_TYPE_GIFT_CARD);
    }

    private String formatDate(Location location, String date) throws ParseException {
        Instant instant = Instant.parse(date);

        ZoneId z = ZoneId.of(location.getTimezone());
        ZonedDateTime zdt = instant.atZone(z);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        String formattedString = zdt.format(formatter);

        return formattedString;
    }

    private String getPaymentCode(boolean isRefund, com.squareup.connect.v2.Tender tender,
            Map<String, Payment> v1PaymentCache, Map<String, Boolean> giftCardActivityCache) {
        String CASH = "CA";
        String CHECK = "CH";
        String VISA = "VI";
        String MASTERCARD = "MA";
        String DISCOVER = "DI";
        String AMEX = "AX";
        String OTHER_BRAND = "OB";
        String BUDGET_TRANSACTION = "BB";
        String JCB = "OT";
        String INTERAC_EFTPOS_FELICA = "DF";
        String GIFT_CARD_REDEMPTION = "G2";
        String GIFT_CARD_MERCHANDISE_CREDIT = "DR";

        if (tender == null) {
            return DEFAULT_OTHER_TENDER_CODE;
        }

        // This tender activity is actually a gift card refund (linked or unlinked)
        if (isRefund && giftCardActivityCache.get(tender.getId()) != null) {
            return GIFT_CARD_MERCHANDISE_CREDIT;
        } else if (tender.getType().equals("SQUARE_GIFT_CARD")) {
            return GIFT_CARD_REDEMPTION;
        } else if (tender.getType().equals("CASH")) {
            return CASH;
        } else if (tender.getType().equals("OTHER")) {
            Payment v1Payment = v1PaymentCache.get(tender.getId());
            if (v1Payment != null) {
                com.squareup.connect.Tender[] v1Tenders = v1Payment.getTender();
                for (com.squareup.connect.Tender v1T : v1Tenders) {
                    if (v1T.getId().equals(tender.getId())) {
                        if (v1T.getName() != null && v1T.getName().equals("CHECK")) {
                            return CHECK;
                        } else if (v1T.getName() != null && v1T.getName().equals("CUSTOM")) {
                            return BUDGET_TRANSACTION;
                        }
                        return DEFAULT_OTHER_TENDER_CODE;
                    }
                }
            }
            return DEFAULT_OTHER_TENDER_CODE;
        } else if (tender.getType().equals("CARD") && tender.getCardDetails() != null
                && tender.getCardDetails().getCard() != null) {
            // credit card
            switch (tender.getCardDetails().getCard().getCardBrand()) {
                case "VISA":
                    return VISA;
                case "MASTERCARD":
                    return MASTERCARD;
                case "AMERICAN_EXPRESS":
                    return AMEX;
                case "CHINA_UNIONPAY":
                    //pass through
                case "DISCOVER_DINERS":
                    //pass through:
                case "DISCOVER":
                    return DISCOVER;
                case "JCB":
                    return JCB;
                case "INTERAC": // pass through
                case "EFTPOS": // pass through
                case "FELICA":
                    return INTERAC_EFTPOS_FELICA;
                default:
                    return OTHER_BRAND;
            }
        } else {
            return DEFAULT_OTHER_TENDER_CODE;
        }
    }

    private String getUnlinkedRefundPaymentCode(PaymentRefund refund) {
        String CASH = "CA";
        String VISA = "VI";
        String MASTERCARD = "MA";
        String DISCOVER = "DI";
        String AMEX = "AX";
        String OTHER_BRAND = "OB";

        if (refund == null) {
            return DEFAULT_OTHER_TENDER_CODE;
        }

        if (refund.getDestinationType().equals("CASH")) {
            return CASH;
        } else if (refund.getDestinationType().equals("CARD")) {

            // get brand code from encoded reason code
            // ex: Accidental charge{V}12345678;1
            String reasonCode = refund.getReason().split("\\{")[1];
            String brandCode = reasonCode.split("\\}")[0];

            switch (brandCode) {
                case "V":
                    return VISA;
                case "M":
                    return MASTERCARD;
                case "A":
                    return AMEX;
                case "D":
                    return DISCOVER;
                default:
                    return OTHER_BRAND;
            }
        } else {
            return DEFAULT_OTHER_TENDER_CODE;
        }
    }

    private String getStoreNumber(String storeName) throws Exception {
        String storeNumber = storeName.split(" ", 2)[0];
        try {
            Integer.parseInt(storeNumber);
        } catch (NumberFormatException nfe) {
            String errorMsg = "Invalid store number for location: " + storeName;
            logger.info(errorMsg);
            throw new Exception(errorMsg);
        }

        return storeNumber;
    }

    private String getRegisterNumberFromOrder(Order order, Map<String, Payment> v1PaymentCache) {
        try {
            String tenderId = getFirstTenderId(order);
            return getRegisterNumberFromPayment(v1PaymentCache.get(tenderId));
        } catch (Exception e) {
            return DEFAULT_DEVICE_ID;
        }
    }

    private String getRegisterNumberFromPayment(Payment payment) {
        try {
            String parsedDeviceId = payment.getDevice().getName().split("-", 2)[0].trim();
            Integer.parseInt(parsedDeviceId);
            return parsedDeviceId;
        } catch (Exception e) {
            return DEFAULT_DEVICE_ID;
        }
    }

    private boolean isLocationSkipped(Location location, String locationOverride) {
        // check for override
        if (locationOverride.length() > 0 && !location.getId().equals(locationOverride)) {
            return true;
        }

        // not active
        if (!location.getStatus().equals(Location.LOCATION_STATUS_ACTIVE)) {
            return true;
        }

        // not valid store ID
        try {
            String storeNumber = getStoreNumber(location.getName());

            if (SKIP_LOCATIONS.contains(storeNumber)) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }

        return false;
    }
}
