package tntfireworks.reporting;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.Payment;
import com.squareup.connect.PaymentItemization;
import com.squareup.connect.Refund;

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
    public ItemSalesPayload(String timeZone, Map<String, String> dayTimeInterval, TntLocationDetails locationDetails)
            throws ParseException {
        super(timeZone, "MM/dd/yyyy", locationDetails, ITEM_SALES_FILE_HEADER);
        this.itemSalesPayloadEntries = new HashMap<String, ItemSalesPayloadEntry>();
        this.dayTimeInterval = dayTimeInterval;
        this.dailySalesDate = getDailySalesDate();
    }

    public void addPayment(Payment payment, List<Map<String, String>> dbItemRows) {
        try {
            // NOTE: V1 payments does not show item-level discounts for percentages, therefore use discount_money on payment obj
            addDiscountEntry(payment);
            addItemizationEntries(payment, dbItemRows);
            addPartialRefundEntry(payment);
        } catch (Exception e) {
            logger.error("ParseException from TimeManager: " + e.getCause());
        }
    }

    private void addDiscountEntry(Payment payment) throws ParseException {
        // quantity of 1 is used to track total number of discounted payments
        if (payment.getDiscountMoney() != null && payment.getDiscountMoney().getAmount() < 0) {
            addEntry(payment.getCreatedAt(), DISCOUNT_ITEM_NUMBER, payment.getDiscountMoney().getAmount(),
                    DEFAULT_ITEM_QTY, DISCOUNT_ITEM_NUMBER, DISCOUNT_ITEM_DESCRIPTION, DISCOUNT_ITEM_SKU);
        }
    }

    private void addItemizationEntries(Payment payment, List<Map<String, String>> dbItemRows) throws ParseException {
        // loop through payment itemizations and add to itemSalesPayloadEntries
        for (PaymentItemization itemization : payment.getItemizations()) {
            // add or update sale entries
            String itemNumber = DEFAULT_ITEM_NUMBER;
            String itemDesc = DEFAULT_ITEM_DESCRIPTION;
            String itemSku = DEFAULT_ITEM_SKU;

            // assign square item sku if it exists
            if (itemization.getItemDetail() != null && itemization.getItemDetail().getSku() != null) {
                itemSku = itemization.getItemDetail().getSku();
            }

            // lookup tnt-specific item number and description based on matching sku
            for (Map<String, String> row : dbItemRows) {
                if (itemSku.equals(row.get(TntDatabaseApi.DB_MKT_PLAN_UPC_COLUMN))) {
                    itemNumber = row.get(TntDatabaseApi.DB_MKT_PLAN_ITEM_NUMBER_COLUMN);
                    itemDesc = row.get(TntDatabaseApi.DB_MKT_PLAN_ITEM_DESCRIPTION_COLUMN);
                    break;
                }
            }

            addEntry(payment.getCreatedAt(), itemNumber, itemization.getGrossSalesMoney().getAmount(),
                    itemization.getQuantity(), itemNumber, itemDesc, itemSku);
        }
    }

    private void addPartialRefundEntry(Payment payment) throws ParseException {
        for (Refund refund : payment.getRefunds()) {
            if (refund.getType().equals(Refund.TYPE_PARTIAL) && refund.getRefundedMoney() != null) {
                addEntry(payment.getCreatedAt(), PARTIAL_REFUND_ITEM_NUMBER, refund.getRefundedMoney().getAmount(),
                        DEFAULT_ITEM_QTY, PARTIAL_REFUND_ITEM_NUMBER, PARTIAL_REFUND_ITEM_DESCRIPTION,
                        PARTIAL_REFUND_ITEM_SKU);
            }
        }
    }

    public List<String> getRows() throws ParseException {
        ArrayList<String> rows = new ArrayList<String>();

        for (ItemSalesPayloadEntry payloadEntry : itemSalesPayloadEntries.values()) {
            String row = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n", dailySalesDate,
                    locationDetails.locationNumber, locationDetails.rbu, payloadEntry.itemNumber,
                    payloadEntry.itemDescription, formatTotal(payloadEntry.dailySales), payloadEntry.dailySalesCounter,
                    formatTotal(payloadEntry.totalSales), payloadEntry.totalSalesCounter, payloadEntry.itemSku);
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
