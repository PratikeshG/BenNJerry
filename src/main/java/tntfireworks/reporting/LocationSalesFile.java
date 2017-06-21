package tntfireworks.reporting;

import java.util.Calendar;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.v2.Transaction;

import util.TimeManager;

public class LocationSalesFile extends TntReportFile {
    private static Logger logger = LoggerFactory.getLogger(LocationSalesFile.class);

    private static String SALES_FILE_HEADER = String.format("%s, %s, %s, %s, %s, %s\n",
            "Location Number", "RBU", "Daily Sales (CREDIT ONLY)", "YTD Sales (CREDIT ONLY)",
            "Daily Sales (CASH/CREDIT)",
            "YTD Sales (CASH/CREDIT)");
    private Map<String, String> dayTimeInterval;
    private String locationNumber;
    private String rbu;
    private int creditDailySales;
    private int cashDailySales;
    private int creditTotalSales;
    private int cashTotalSales;

    public LocationSalesFile(String fileDate, Map<String, String> dayTimeInterval, String locationNumber, String rbu) {
        super(fileDate, SALES_FILE_HEADER);
        this.dayTimeInterval = dayTimeInterval;
        this.locationNumber = locationNumber;
        this.rbu = rbu;
        this.creditDailySales = 0;
        this.creditTotalSales = 0;
        this.cashDailySales = 0;
        this.cashTotalSales = 0;
    }

    public void addTransaction(Transaction transaction) {
        try {
            // use calendar objects to daily interval
            Calendar beginTime = TimeManager.toCalendar(dayTimeInterval.get("begin_time"));
            Calendar endTime = TimeManager.toCalendar(dayTimeInterval.get("end_time"));
            Calendar transactionTime = TimeManager.toCalendar(transaction.getCreatedAt());

            // loop through tenders and add entries
            for (com.squareup.connect.v2.Tender tender : transaction.getTenders()) {
                if (tender.getType().equals("CASH")) {
                    if (beginTime.compareTo(transactionTime) <= 0 && endTime.compareTo(transactionTime) > 0) {
                        cashDailySales += tender.getAmountMoney().getAmount();
                    }
                    cashTotalSales += tender.getAmountMoney().getAmount();
                }
                if (tender.getType().equals("CREDIT")) {
                    if (beginTime.compareTo(transactionTime) <= 0 && endTime.compareTo(transactionTime) > 0) {
                        creditDailySales += tender.getAmountMoney().getAmount();
                    }
                    creditTotalSales += tender.getAmountMoney().getAmount();
                }
            }

        } catch (Exception e) {
            logger.error("Exception from aggregating sales data for SalesFile: " + e);
        }
    }

    private int getCashCreditDaily() {
        return creditDailySales + cashDailySales;
    }

    private int getCashCreditTotal() {
        return creditTotalSales + cashTotalSales;
    }

    public String getFileEntry() {
        String fileRow = String.format("%s, %s, %s, %s, %s, %s \n", locationNumber, rbu, formatTotal(creditDailySales),
                formatTotal(creditTotalSales), formatTotal(getCashCreditDaily()), formatTotal(getCashCreditTotal()));
        return fileRow;
    }
}
