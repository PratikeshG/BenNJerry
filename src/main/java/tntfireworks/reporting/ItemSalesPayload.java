package tntfireworks.reporting;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.Payment;
import com.squareup.connect.PaymentItemization;

import util.TimeManager;

public class ItemSalesPayload extends TntReportLocationPayload {
    private static Logger logger = LoggerFactory.getLogger(ItemSalesPayload.class);
    private static final String ITEM_SALES_FILE_HEADER = String.format("%s, %s, %s, %s, %s, %s, %s, %s\n",
            "Location Number", "RBU", "Item Number", "Item Description", "Daily Sales Amount", "Daily Sales Quantity",
            "YTD Sales Amount", "YTD Sales Quantity");
    private Map<String, ItemSalesPayloadEntry> itemSalesPayloadEntries;
    private Map<String, String> dayTimeInterval;
    private Calendar beginTime;
    private Calendar endTime;

    public ItemSalesPayload(String timeZone, Map<String, String> dayTimeInterval, String locationName,
            List<Map<String, String>> dbLocationRows) {
        super(timeZone, locationName, dbLocationRows, ITEM_SALES_FILE_HEADER);
        this.itemSalesPayloadEntries = new HashMap<String, ItemSalesPayloadEntry>();
        this.dayTimeInterval = dayTimeInterval;
    }

    public void addEntry(Payment payment, List<Map<String, String>> dbItemRows) {
        try {
            // loop through payment itemizations and add to map
            for (PaymentItemization itemization : payment.getItemizations()) {
                // key = location number + item number
                String itemNumber = "N/A";
                String itemDesc = "CUSTOM AMOUNT";
                for (Map<String, String> row : dbItemRows) {
                    if (itemization.getItemDetail().getSku().equals(row.get("upc"))) {
                        itemNumber = row.get("itemNumber");
                        itemDesc = row.get("itemDescription");
                    }
                }

                // use calendar objects to daily interval
                Calendar beginTime = TimeManager.toCalendar(dayTimeInterval.get("begin_time"));
                Calendar endTime = TimeManager.toCalendar(dayTimeInterval.get("end_time"));
                Calendar paymentTime = TimeManager.toCalendar(payment.getCreatedAt());

                // key = <locationNumber><itemNumber>
                String key = String.format("%s%s", locationNumber, itemNumber);
                int saleAmount = itemization.getTotalMoney().getAmount();

                ItemSalesPayloadEntry updateEntry = null;
                if (itemSalesPayloadEntries.containsKey(key)) {
                    updateEntry = itemSalesPayloadEntries.get(key);
                } else {
                    updateEntry = new ItemSalesPayloadEntry(itemNumber, itemDesc, saleAmount);
                }

                // determine if this payment should be included in "daily" total
                if (beginTime.compareTo(paymentTime) <= 0 && endTime.compareTo(paymentTime) > 0) {
                    updateEntry.addDailySales(saleAmount, itemization.getQuantity());
                }

                // add to sale amount to total
                updateEntry.addTotalSales(saleAmount, itemization.getQuantity());
                itemSalesPayloadEntries.put(key, updateEntry);
            }
        } catch (Exception e) {
            logger.error("Exception from TimeManager: " + e);
        }
    }

    public List<String> getRows() {
        ArrayList<String> rows = new ArrayList<String>();

        for (ItemSalesPayloadEntry payloadEntry : itemSalesPayloadEntries.values()) {
            String row = String.format("%s, %s, %s, %s, %s, %s, %s, %s \n", locationNumber, rbu,
                    payloadEntry.itemNumber, payloadEntry.itemDescription, formatTotal(payloadEntry.dailySales),
                    payloadEntry.dailySalesCounter, formatTotal(payloadEntry.totalSales),
                    payloadEntry.totalSalesCounter);
            rows.add(row);
        }

        return rows;
    }

    private class ItemSalesPayloadEntry {
        private String itemNumber;
        private String itemDescription;
        private int dailySales;
        private int totalSales;
        private double dailySalesCounter;
        private double totalSalesCounter;

        private ItemSalesPayloadEntry(String itemNumber, String itemDesc, int initialAmount) {
            this.itemNumber = itemNumber;
            this.itemDescription = itemDesc;
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
