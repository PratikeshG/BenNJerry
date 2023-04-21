package tntfireworks.reporting;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.Money;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.OrderLineItem;
import com.squareup.connect.v2.PaymentRefund;

import tntfireworks.TntDatabaseApi;
import util.TimeManager;

/*
 * "Item Sales Report" - Emailed daily
 *
 * Report 7 contains item-level sales data. Daily sales for each item is counted and aggregated, and reported for each
 * location. The "YTD" sales amount/quantity, or seasonal amount/quantity, is also tracked. Each row of the report
 * includes the item number, daily totals, seasonal totals, and location number. Because itemized transaction
 * information is not included in the Connect V2 Transaction endpoint, payment data is pulled from Connect V1 Payments.
 *
 */
public class ItemSalesPayload extends TntReportLocationPayload {
    private static Logger logger = LoggerFactory.getLogger(ItemSalesPayload.class);
    private static final String ITEM_SALES_FILE_HEADER = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s\n",
            "Daily Sales Date", "Location Number", "RBU", "Item Number", "Item Description", "Daily Sales Amount",
            "Daily Sales Quantity", "YTD Sales Amount", "YTD Sales Quantity", "Item SKU");
    private static final String DEFAULT_ITEM_NUMBER = "CUSTOM ITEM";
    private static final String DEFAULT_ITEM_DESCRIPTION = "CUSTOM AMOUNT TOTALS FOR LOCATION";
    private static final String DEFAULT_ITEM_SKU = "N/A";
    private static final int DEFAULT_ITEM_QTY = 1;
    private static final String DISCOUNT_ITEM_NUMBER = "DISCOUNT LINE ITEM";
    private static final String DISCOUNT_ITEM_DESCRIPTION = "DISCOUNT TOTALS FOR LOCATION";
    private static final String DISCOUNT_ITEM_SKU = "N/A";
    private static final String PARTIAL_REFUND_ITEM_NUMBER = "PARTIAL REFUND LINE ITEM";
    private static final String PARTIAL_REFUND_ITEM_DESCRIPTION = "PARTIAL REFUND TOTALS FOR LOCATION";
    private static final String PARTIAL_REFUND_ITEM_SKU = "N/A";
    private Map<String, ItemSalesPayloadEntry> itemSalesPayloadEntries;
    private Map<String, String> dayTimeInterval;
    private String dailySalesDate;

    // ItemSalesPayload represent item sales for a single Square location
    public ItemSalesPayload(String timeZone, int offset, Map<String, String> dayTimeInterval, TntLocationDetails locationDetails)
            throws ParseException {
        super(timeZone, offset, locationDetails, ITEM_SALES_FILE_HEADER);
        this.itemSalesPayloadEntries = new HashMap<String, ItemSalesPayloadEntry>();
        this.dayTimeInterval = dayTimeInterval;
        this.dailySalesDate = getDailySalesDate();
    }

    // Add Order (used after migration off /v1/payments
	public void addOrder(Order order, Map<String, CatalogObject> catalogObjects,
			Map<String, List<PaymentRefund>> orderToRefundsMap, List<Map<String, String>> dbItemRows) {
		if(order != null) {
			try {
				List<PaymentRefund> refunds = orderToRefundsMap.getOrDefault(order.getId(), Collections.EMPTY_LIST);
				// If refunds sum up to full refund, ignore the order
				int totalRefundAmount = refunds.stream()
						.map(PaymentRefund::getAmountMoney)
						.mapToInt(Money::getAmount)
						.sum();

				if (order.getTotalMoney() != null && totalRefundAmount == order.getTotalMoney().getAmount()) {
					return;
				}

				addDiscountEntry(order);
				addItemizationEntries(order, catalogObjects, dbItemRows);
		        addPartialRefundEntries(refunds, order);

		    } catch (Exception e) {
		        logger.error("Exception processing order for ItemSalesPayload report. Cause: " + e.getCause() + ", Message: " + e.getMessage());
		    }
		}
	}

    /**
     * After /v2/payments/ migration -- discount is contained on Order
     */
    private void addDiscountEntry(Order order) throws ParseException {
        // quantity of 1 is used to track total number of discounted payments
        if (order.getTotalDiscountMoney() != null && order.getTotalDiscountMoney().getAmount() > 0) {
            addEntry(order.getCreatedAt(), DISCOUNT_ITEM_NUMBER, -order.getTotalDiscountMoney().getAmount(),
                    DEFAULT_ITEM_QTY, DISCOUNT_ITEM_NUMBER, DISCOUNT_ITEM_DESCRIPTION, DISCOUNT_ITEM_SKU);
        }
    }

    /**
     * After /v2/payments/ migration -- itemization entries are all contained within the order line items
     */
    private void addItemizationEntries(Order order, Map<String, CatalogObject> catalogObjects, List<Map<String, String>> dbItemRows) throws ParseException {
        // loop through payment itemizations and add to itemSalesPayloadEntries
    	if(order.getLineItems() != null) {
    		for (OrderLineItem lineItem : order.getLineItems()) {
                // add or update sale entries
                String itemNumber = DEFAULT_ITEM_NUMBER;
                String itemDesc = DEFAULT_ITEM_DESCRIPTION;
                String itemSku = DEFAULT_ITEM_SKU;

                // assign square item sku if it exists
                if (lineItem.getCatalogObjectId() != null
                		&& catalogObjects.containsKey(lineItem.getCatalogObjectId())) {
                	CatalogObject catalogObject = catalogObjects.get(lineItem.getCatalogObjectId());
                	if (catalogObject.getItemVariationData() != null
                		&& catalogObject.getItemVariationData().getSku() != null) {
                		itemSku = catalogObject.getItemVariationData().getSku();
                	}
                }

                // lookup tnt-specific item number and description based on matching sku
                for (Map<String, String> row : dbItemRows) {
                    if (itemSku.equals(row.get(TntDatabaseApi.DB_MKT_PLAN_UPC_COLUMN))) {
                        itemNumber = row.get(TntDatabaseApi.DB_MKT_PLAN_ITEM_NUMBER_COLUMN);
                        itemDesc = row.get(TntDatabaseApi.DB_MKT_PLAN_ITEM_DESCRIPTION_COLUMN);
                        break;
                    }
                }

                double quantity = Double.parseDouble(lineItem.getQuantity());

                addEntry(order.getCreatedAt(), itemNumber, lineItem.getGrossSalesMoney() != null ?
                		lineItem.getGrossSalesMoney().getAmount()
                		: 0,
                		quantity, itemNumber, itemDesc, itemSku);
            }
    	}
    }

    // Partial refund on order is if the refund doesn't equal the order's amount
    private void addPartialRefundEntries(List<PaymentRefund> refunds, Order order) throws ParseException {
    	// TODO implement
    	if(refunds != null) {
    		for (PaymentRefund refund : refunds) {
                if (refund.getAmountMoney()!= null && order.getTotalMoney() != null &&
                		refund.getAmountMoney().getAmount() != order.getTotalMoney().getAmount()) {
                    addEntry(refund.getCreatedAt(), PARTIAL_REFUND_ITEM_NUMBER, -refund.getAmountMoney().getAmount(),
                            DEFAULT_ITEM_QTY, PARTIAL_REFUND_ITEM_NUMBER, PARTIAL_REFUND_ITEM_DESCRIPTION,
                            PARTIAL_REFUND_ITEM_SKU);
                }
            }
    	}
    }

    public List<String> getRows() throws ParseException {
        ArrayList<String> rows = new ArrayList<String>();

        for (ItemSalesPayloadEntry payloadEntry : itemSalesPayloadEntries.values()) {
            String row = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n", dailySalesDate,
                    locationDetails.locationNumber, locationDetails.rbu, payloadEntry.itemNumber,
                    payloadEntry.itemDescription, formatDecimalTotal(payloadEntry.dailySales),
                    payloadEntry.dailySalesCounter, formatDecimalTotal(payloadEntry.totalSales),
                    payloadEntry.totalSalesCounter, payloadEntry.itemSku);
            rows.add(row);
        }

        return rows;
    }

    private String getDailySalesDate() throws ParseException {
        Calendar reportDate = TimeManager.toCalendar(dayTimeInterval.get(util.Constants.BEGIN_TIME));
        String year = String.format("%04d", reportDate.get(Calendar.YEAR));
        String month = String.format("%02d", reportDate.get(Calendar.MONTH) + 1);
        String day = String.format("%02d", reportDate.get(Calendar.DATE));

        return String.format("%s/%s/%s", month, day, year);
    }

    // represents a single row in the csv report
    private void addEntry(String createdAt, String key, int amount, double quantity, String itemNumber, String itemDesc,
            String itemSku) throws ParseException {
        // use calendar objects to daily interval
        Calendar beginTime = TimeManager.toCalendar(dayTimeInterval.get(util.Constants.BEGIN_TIME));
        Calendar endTime = TimeManager.toCalendar(dayTimeInterval.get(util.Constants.END_TIME));
        Calendar paymentTime = TimeManager.toCalendar(createdAt);

        ItemSalesPayloadEntry entry = null;
        if (itemSalesPayloadEntries.containsKey(key)) {
            entry = itemSalesPayloadEntries.get(key);
        } else {
            entry = new ItemSalesPayloadEntry(itemNumber, itemDesc, itemSku);
        }

        // determine if this entry should be included in "daily" total
        if (beginTime.compareTo(paymentTime) <= 0 && endTime.compareTo(paymentTime) > 0) {
            entry.addDailySales(amount, quantity);
        }

        // add to entry amount to total
        entry.addTotalSales(amount, quantity);
        itemSalesPayloadEntries.put(key, entry);
    }

    private class ItemSalesPayloadEntry {
        private String itemNumber;
        private String itemDescription;
        private String itemSku;
        private int dailySales;
        private int totalSales;
        private double dailySalesCounter;
        private double totalSalesCounter;

        private ItemSalesPayloadEntry(String itemNumber, String itemDesc, String itemSku) {
            this.itemNumber = itemNumber;
            this.itemDescription = itemDesc;
            this.itemSku = itemSku;
            this.dailySales = 0;
            this.totalSales = 0;
            this.dailySalesCounter = 0;
            this.totalSalesCounter = 0;
        }

        private void addDailySales(int amount, double quantity) {
            dailySalesCounter += quantity;
            dailySales += amount;
        }

        private void addTotalSales(int amount, double quantity) {
            totalSalesCounter += quantity;
            totalSales += amount;
        }
    }
}
