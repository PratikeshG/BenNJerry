package tntfireworks.reporting;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.v2.Refund;
import com.squareup.connect.v2.Tender;
import com.squareup.connect.v2.Transaction;

import util.TimeManager;

public class LocationSalesPayload extends TntReportPayload {
    private static Logger logger = LoggerFactory.getLogger(LocationSalesPayload.class);

    private static String LOCATION_SALES_FILE_HEADER = String.format("%s, %s, %s, %s, %s, %s\n",
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

    public LocationSalesPayload(String timeZone, Map<String, String> dayTimeInterval, String locationNumber, String rbu) {
        super(timeZone, LOCATION_SALES_FILE_HEADER);
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

            // loop through refunds to add to totals later
            Map<String, Integer> tenderToRefund = new HashMap<String, Integer>();
            if (transaction.getRefunds() != null) {
                for (Refund refund : transaction.getRefunds()) {
                    if (refund != null && refund.getTenderId() != null && refund.getAmountMoney() != null) {
                        tenderToRefund.put(refund.getTenderId(), refund.getAmountMoney().getAmount());
                    }
                }
            }

            // loop through tenders and add entries
            for (Tender tender : transaction.getTenders()) {
                if (tender.getType().equals("CASH")) {
                    if (beginTime.compareTo(transactionTime) <= 0 && endTime.compareTo(transactionTime) > 0) {
                        cashDailySales += tender.getAmountMoney().getAmount();
                        if (tenderToRefund.containsKey(tender.getId())) {
                            cashDailySales -= tenderToRefund.get(tender.getId());
                        }
                    }
                    cashTotalSales += tender.getAmountMoney().getAmount();
                    if (tenderToRefund.containsKey(tender.getId())) {
                        cashTotalSales -= tenderToRefund.get(tender.getId());
                    }
                }
                if (tender.getType().equals("CARD")) {
                    if (beginTime.compareTo(transactionTime) <= 0 && endTime.compareTo(transactionTime) > 0) {
                        creditDailySales += tender.getAmountMoney().getAmount();
                        if (tenderToRefund.containsKey(tender.getId())) {
                            creditDailySales -= tenderToRefund.get(tender.getId());
                        }
                    }
                    creditTotalSales += tender.getAmountMoney().getAmount();
                    if (tenderToRefund.containsKey(tender.getId())) {
                        creditTotalSales -= tenderToRefund.get(tender.getId());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception from aggregating sales/payload data for LoationSales: " + e);
        }
    }

    private int getCashCreditDaily() {
        return creditDailySales + cashDailySales;
    }

    private int getCashCreditTotal() {
        return creditTotalSales + cashTotalSales;
    }

    public String getPayloadEntry() {
        String row = String.format("%s, %s, %s, %s, %s, %s \n", locationNumber, rbu, formatTotal(creditDailySales),
                formatTotal(creditTotalSales), formatTotal(getCashCreditDaily()), formatTotal(getCashCreditTotal()));
        return row;
    }
}
