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

public class ItemSalesFile extends TntReportFile {
    private static Logger logger = LoggerFactory.getLogger(ItemSalesFile.class);
    private static String START_OF_SEASON = "";
    private static String ITEM_SALES_FILE_HEADER = String.format("%s, %s, %s, %s, %s, %s, %s, %s\n",
            "Location Number", "RBU", "Item Number", "Item Description",
            "Daily Sales Amount", "Daily Sales Quantity", "YTD Sales Amount", "YTD Sales Quantity");
    private Map<String, ItemSalesFileEntry> itemSalesFileEntries;
    private Map<String, String> dayTimeInterval;
    private String locationNumber;
    private String rbu;

    public ItemSalesFile(String fileDate, Map<String, String> dayTimeInterval, String locationNumber, String rbu) {
        super(fileDate, ITEM_SALES_FILE_HEADER);
        this.itemSalesFileEntries = new HashMap<String, ItemSalesFileEntry>();
        this.dayTimeInterval = dayTimeInterval;
        this.locationNumber = locationNumber;
        this.rbu = rbu;
    }

    public void addFileEntry(Payment payment, List<Map<String, String>> dbItemRows) {
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
                int saleAmount = itemization.getGrossSalesMoney().getAmount();

                ItemSalesFileEntry updateEntry = null;
                if (itemSalesFileEntries.containsKey(key)) {
                    updateEntry = itemSalesFileEntries.get(key);
                } else {
                    updateEntry = new ItemSalesFileEntry(itemNumber,
                            itemDesc, saleAmount);
                }

                // determine if this payment should be included in "daily" total
                if (beginTime.compareTo(paymentTime) <= 0 && endTime.compareTo(paymentTime) > 0) {
                    updateEntry.addDailySales(saleAmount);
                }

                // add to sale amount to total
                updateEntry.addTotalSales(saleAmount);
                itemSalesFileEntries.put(key, updateEntry);

            }
        } catch (Exception e) {
            logger.error("Exception from TimeManager: " + e);
        }
    }

    public List<String> getFileEntries() {
        ArrayList<String> fileEntries = new ArrayList<String>();
        for (ItemSalesFileEntry fileEntry : itemSalesFileEntries.values()) {
            String fileRow = String.format("%s, %s, %s, %s, %s, %s, %s, %s \n", locationNumber, rbu,
                    fileEntry.itemNumber, fileEntry.itemDescription, formatTotal(fileEntry.dailySales),
                    fileEntry.dailySalesCounter, formatTotal(fileEntry.totalSales), fileEntry.totalSalesCounter);
            fileEntries.add(fileRow);
        }

        return fileEntries;
    }

    private class ItemSalesFileEntry {
        private String itemNumber;
        private String itemDescription;
        private int dailySales;
        private int totalSales;
        private int dailySalesCounter;
        private int totalSalesCounter;

        private ItemSalesFileEntry(String itemNumber, String itemDesc,
                int initialAmount) {
            this.itemNumber = itemNumber;
            this.itemDescription = itemDesc;
            this.dailySales = 0;
            this.totalSales = 0;
            this.dailySalesCounter = 0;
            this.totalSalesCounter = 0;
        }

        public void addDailySales(int amount) {
            dailySalesCounter++;
            dailySales += amount;
        }

        public void addTotalSales(int amount) {
            totalSalesCounter++;
            totalSales += amount;
        }
    }
}
