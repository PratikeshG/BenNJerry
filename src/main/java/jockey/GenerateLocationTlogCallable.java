package jockey;

import java.sql.Connection;
import java.sql.DriverManager;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.squareup.connect.Employee;
import com.squareup.connect.Payment;
import com.squareup.connect.SquareClient;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.OrderLineItem;
import com.squareup.connect.v2.OrderLineItemDiscount;
import com.squareup.connect.v2.OrderReturn;
import com.squareup.connect.v2.OrderReturnLineItem;
import com.squareup.connect.v2.OrderReturnLineItemDiscount;
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

    private static String DEFAULT_UPC = "";
    private static String DEFAULT_SKU = "";
    private static String DEFAULT_DISPLAY_NAME = "Custom Amount";
    private static String DEFUALT_CASHIER_ID = "9001";
    private static String DEFUALT_DEVICE_ID = "9";
    private static String DEFUALT_OTHER_TENDER_CODE = "ZZ";
    private static String DEFAULT_NO_TENDER_SALE_CODE = "7E";
    private static int MAX_TRANSACTION_NUMBER = 99999;

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
        String apiUrl = message.getProperty(Constants.API_URL, PropertyScope.SESSION);

        Employee[] employees = (Employee[]) message.getProperty(Constants.EMPLOYEES, PropertyScope.SESSION);
        Map<String, Employee> employeeCache = new HashMap<String, Employee>();
        for (Employee e : employees) {
            if (e == null) {
                continue; // TODO - bug in v1 employee list code -- migrate to V2 teams
            }
            employeeCache.put(e.getId(), e);
        }

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

        List<Order> v2Orders = Arrays.asList(clientV2.orders().search(location.getId(), orderQuery));

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

        // Get existing transaction numbers for Orders
        Map<String, Integer> existingTransactionNumbers = new HashMap<String, Integer>();
        if (orderIds.size() > 0) {
            ArrayList<Map<String, String>> existingOrderRecords = databaseApi.queryOrdersById(orderIds);
            for (Map<String, String> orderRecord : existingOrderRecords) {
                existingTransactionNumbers.put(orderRecord.get("orderId"),
                        Integer.parseInt(orderRecord.get("orderNumber")));
            }
        }

        ArrayList<SalesOrder> salesOrders = new ArrayList<SalesOrder>();

        for (Order order : v2Orders) {
            String registerNumber = getRegisterNumberFromOrder(order, v1PaymentCache);
            int transactionNumber = getTransactionNumber(order, registerNumber, nextDeviceTransactionNumbers,
                    existingTransactionNumbers);

            SalesOrder returnOrder = createSalesOrder(location, employeeCache, v1PaymentCache, tenderCache, order,
                    transactionNumber);
            salesOrders.add(returnOrder);
        }

        databaseApi.updateOrderNumbersForLocation(salesOrders, location.getId());
        databaseApi.close();

        return salesOrders;
    }

    private int getTransactionNumber(Order order, String registerNumber,
            Map<String, Integer> nextDeviceTransactionNumbers, Map<String, Integer> existingTransactionNumbers) {
        int nextTransactionNumberForDevice = nextDeviceTransactionNumbers.getOrDefault(registerNumber, 1);
        int transactionNumber = existingTransactionNumbers.getOrDefault(order.getId(), nextTransactionNumberForDevice);

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

    private SalesOrder createSalesOrder(Location location, Map<String, Employee> employeeCache,
            Map<String, Payment> v1PaymentCache, Map<String, Tender> tenderCache, Order order, int transactionNumber)
            throws Exception {
        SalesOrder so = new SalesOrder();

        so.setThirdPartyOrderId(order.getId());
        so.setTransactionNumber("" + transactionNumber);
        so.setStoreNumber(getStoreNumber(location.getName()));
        so.setDateCreated(formatDate(location, order.getCreatedAt().replace(".000Z", "Z")));
        so.setDateCompleted(formatDate(location, order.getClosedAt().replace(".000Z", "Z")));
        so.setSalesOrderCode(getOrderCode(order, v1PaymentCache));
        so.setRegisterNumber(getRegisterNumberFromOrder(order, v1PaymentCache));

        // the kind of shit you need to do because of our fragmented API
        String cashierId = DEFUALT_CASHIER_ID;
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
                sop.setPaymentCode(getPaymentCode(tender, v1PaymentCache));
                sop.setPaymentId(tender.getId());
                salesOrderPayments.add(sop);
            }
        }
        if (order.getRefunds() != null) {
            for (Refund tenderRefund : order.getRefunds()) {
                SalesOrderPayment sop = new SalesOrderPayment();
                sop.setAmount(Util.getAmountAsXmlDecimal(-1 * tenderRefund.getAmountMoney().getAmount()));
                sop.setPaymentCode(
                        getPaymentCode(getOriginalTenderFromRefundTender(tenderCache, tenderRefund), v1PaymentCache));
                sop.setPaymentId(tenderRefund.getId());
                salesOrderPayments.add(sop);
            }
        }
        so.setPayments(salesOrderPayments.toArray(new SalesOrderPayment[salesOrderPayments.size()]));

        ArrayList<SalesOrderLineItem> salesOrderLineItems = new ArrayList<SalesOrderLineItem>();
        int i = 1;

        if (order.getReturns() != null) {
            for (OrderReturn orderReturn : order.getReturns()) {
                for (OrderReturnLineItem lineItem : orderReturn.getReturnLineItems()) {
                    SalesOrderLineItem soli = new SalesOrderLineItem();

                    int qty = Integer.parseInt(lineItem.getQuantity());
                    int extendedPrice = (lineItem.getTotalMoney().getAmount() - lineItem.getTotalTaxMoney().getAmount())
                            * -1;

                    soli.setThirdPartyLineItemId(lineItem.getUid());
                    soli.setLineNumber("" + i++);
                    soli.setLineCode(getReturnLineCode(lineItem));
                    soli.setSku((lineItem.getSku() == null) ? DEFAULT_SKU : lineItem.getSku());
                    soli.setUpc((lineItem.getCatalogObjectId() == null) ? DEFAULT_UPC
                            : getUpcFromItemVariationName(lineItem.getVariationName()));
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

        if (order.getLineItems() != null) {
            for (OrderLineItem lineItem : order.getLineItems()) {
                SalesOrderLineItem soli = new SalesOrderLineItem();

                int qty = Integer.parseInt(lineItem.getQuantity());
                int extendedPrice = lineItem.getTotalMoney().getAmount() - lineItem.getTotalTaxMoney().getAmount();

                soli.setThirdPartyLineItemId(lineItem.getUid());
                soli.setLineNumber("" + i++);
                soli.setLineCode(getSaleLineCode(lineItem));
                soli.setSku((lineItem.getSku() == null) ? DEFAULT_SKU : lineItem.getSku());
                soli.setUpc((lineItem.getCatalogObjectId() == null) ? DEFAULT_UPC
                        : getUpcFromItemVariationName(lineItem.getVariationName()));
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

        System.out.println(
                "refund: " + refund.getTransactionId() + " --- " + refund.getId() + " ---" + refund.getTenderId());
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
        return order.getNetAmounts().getTaxMoney().getAmount();
    }

    private int getOrderTotal(Order order) {
        return order.getNetAmounts().getTotalMoney().getAmount();
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
                    for (OrderReturnLineItem lineItem : orderReturn.getReturnLineItems()) {
                        if (lineItemReturnHasEmployeeDiscount(lineItem)) {
                            return "14";
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

    private String getReturnLineCode(OrderReturnLineItem lineItem) {
        // "11"=Return; "14"=EmployeeReturn
        if (lineItemReturnHasEmployeeDiscount(lineItem)) {
            return "14";
        }
        return "11";
    }

    private String getSaleLineCode(OrderLineItem lineItem) {
        // "01"=Sale; "02"=BudgetSale; "03"=AltBudgetSale; "04"=EmployeeSale
        if (lineItemHasEmployeeDiscount(lineItem)) {
            return "04";
        } else if (lineItemIsDonation(lineItem)) {
            return "DO";
        }

        return "01";
    }

    private String getExternalEmployeeId(Map<String, Employee> employeeCache, String creatorId) {
        return (employeeCache.get(creatorId) != null) ? employeeCache.get(creatorId).getExternalId()
                : DEFUALT_CASHIER_ID;
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

    private String formatDate(Location location, String date) throws ParseException {
        return TimeManager.toSimpleDateTimeInTimeZone(date, location.getTimezone(), "yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    }

    private String getPaymentCode(com.squareup.connect.v2.Tender tender, Map<String, Payment> v1PaymentCache) {
        String CASH = "CA";
        String CHECK = "CH";
        String VISA = "VI";
        String MASTERCARD = "MA";
        String DISCOVER = "DI";
        String AMEX = "AX";
        String OTHER_BRAND = "OB";
        String BUDGET_TRANSACTION = "BB";
        String JCB = "OT";
        String SQUARE_GIFT_CARD = "SQ";
        String INTERAC_EFTPOS_FELICA = "DF";

        if (tender == null) {
            return DEFUALT_OTHER_TENDER_CODE;
        }

        if (tender.getType().equals("CASH")) {
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
                        return DEFUALT_OTHER_TENDER_CODE;
                    }
                }
            }
            return DEFUALT_OTHER_TENDER_CODE;
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
            return DEFUALT_OTHER_TENDER_CODE;
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
            return DEFUALT_DEVICE_ID;
        }
    }

    private String getRegisterNumberFromPayment(Payment payment) {
        try {
            Integer.parseInt(payment.getDevice().getName());
        } catch (NumberFormatException nfe) {
            return DEFUALT_DEVICE_ID;
        }

        return payment.getDevice().getName();
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
            getStoreNumber(location.getName());
        } catch (Exception e) {
            return true;
        }

        return false;
    }
}