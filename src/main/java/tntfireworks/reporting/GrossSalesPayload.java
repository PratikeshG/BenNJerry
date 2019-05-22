package tntfireworks.reporting;

import java.util.Calendar;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.Payment;

import util.TimeManager;

/*
 * Report 9 - "Gross Sales Report" - Emailed daily
 *
 * Report 9 contains columns for both daily and seasonal gross sales (TNT defines gross sales as sales amount before discounts and taxes.
 * Each row in the report represents sales for a single location. The season range is determined as the period between the current
 * date for the report (unless explicitly set elsewhere) and the set start date for the season as defined in
 * tntfireworks.reporting.properties file as 'startOfSeason'. In order to calculate the seasonal gross sales amount for each
 * location, payment and transaction data is pulled for the entire season range.
 *
 */
public class GrossSalesPayload extends TntReportLocationPayload {
    private static Logger logger = LoggerFactory.getLogger(GrossSalesPayload.class);

    private static final String GROSS_SALES_FILE_HEADER = String.format("%s, %s, %s, %s, %s\n", "Location Number",
            "Gross Sales (Daily)", "Gross Sales (YTD)", "Daily Transactions", "YTD Transactions");
    private Map<String, String> dayTimeInterval;
    private int dailyGrossSales;
    private int seasonGrossSales;
    private int dailyTransactionCount;
    private int seasonTransactionCount;

    public GrossSalesPayload(String timeZone, Map<String, String> dayTimeInterval, TntLocationDetails locationDetails) {
        super(timeZone, locationDetails, GROSS_SALES_FILE_HEADER);
        this.dayTimeInterval = dayTimeInterval;
        this.dailyGrossSales = 0;
        this.seasonGrossSales = 0;
        this.dailyTransactionCount = 0;
        this.seasonTransactionCount = 0;
    }

    public void addEntry(Payment payment) {
        try {
            // use calendar objects to daily interval
            Calendar beginTime = TimeManager.toCalendar(dayTimeInterval.get(util.Constants.BEGIN_TIME));
            Calendar endTime = TimeManager.toCalendar(dayTimeInterval.get(util.Constants.END_TIME));
            Calendar transactionTime = TimeManager.toCalendar(payment.getCreatedAt());

            // if payment occurred during current day, add to daily totals
            // NOTE: TNT defines "Gross Sales" as sales amount before taxes and discounts
            // V1 Payment.net_sales_money is total sales excluding taxes
            if (payment.getNetSalesMoney() != null) {
                if (isDailyTransaction(beginTime, endTime, transactionTime)) {
                    dailyGrossSales += payment.getNetSalesMoney().getAmount();
                    dailyTransactionCount++;
                }
                seasonGrossSales += payment.getNetSalesMoney().getAmount();
                seasonTransactionCount++;
            }
        } catch (Exception e) {
            logger.error("Calendar Exception from aggregating sales/payload data for Gross Sales: " + e);
        }
    }

    public String getRow() {
        String row = String.format("%s, %s, %s, %s, %s\n", locationDetails.locationNumber, formatTotal(dailyGrossSales),
                formatTotal(seasonGrossSales), dailyTransactionCount, seasonTransactionCount);
        return row;
    }

    private boolean isDailyTransaction(Calendar beginTime, Calendar endTime, Calendar transactionTime) {
        return beginTime.compareTo(transactionTime) <= 0 && endTime.compareTo(transactionTime) > 0;
    }
}
