package tntfireworks.reporting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.v2.Payment;
import com.squareup.connect.v2.PaymentRefund;
import com.squareup.connect.v2.ProcessingFee;
import com.squareup.connect.v2.Tender;
import com.squareup.connect.v2.Money;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.OrderLineItem;

import util.ConnectV2MigrationHelper;
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
    private static final String GROSS_SALES_FILE_HEADER = String.format(
            "%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s\n",
            "Daily Sales Date", "Location Number", "RBU", "State", "Daily Cash Sales", "Daily Credit Sales",
            "Daily Total Collected Sales", "Daily Taxes", "Daily Discounts", "Daily Refunds", "Daily Gross Sales",
            "# Daily Transactions", "Average Daily Gross Sale Per Transaction", "Cash Sales (YTD)",
            "Credit Sales (YTD)", "Total Collected Sales (YTD)", "Taxes (YTD)", "Discounts (YTD)", "Refunds (YTD)",
            "Gross Sales (YTD)", "# Transactions (YTD)", "Average Gross Sale Per Transaction (YTD)");
    private Calendar beginTime;
    private Calendar endTime;
    protected int dailyGrossSales;
    protected int seasonGrossSales;
    protected int dailyTransactionCount;
    protected int seasonTransactionCount;
    protected int dailyCreditCount;
    protected int seasonCreditCount;
    protected int dailyCashCount;
    protected int seasonCashCount;
    protected int dailyCreditTotals;
    protected int seasonCreditTotals;
    protected int dailyCashTotals;
    protected int seasonCashTotals;
    protected int dailyTotalCollected;
    protected int seasonTotalCollected;
    protected int dailyTaxTotals;
    protected int seasonTaxTotals;
    protected int dailyDiscountTotals;
    protected int seasonDiscountTotals;
    protected int dailyRefundTotals;
    protected int seasonRefundTotals;
    protected String dailySalesDate;
    protected int avgDailyGross;
    protected int avgSeasonGross;

    public GrossSalesPayload(String timeZone, int offset, Map<String, String> dayTimeInterval, TntLocationDetails locationDetails)
            throws ParseException {
        super(timeZone, offset, locationDetails, GROSS_SALES_FILE_HEADER);
        // init report values
        this.dailyGrossSales = 0;
        this.seasonGrossSales = 0;
        this.dailyTransactionCount = 0;
        this.seasonTransactionCount = 0;
        this.dailyCreditCount = 0;
        this.seasonCreditCount = 0;
        this.dailyCashCount = 0;
        this.seasonCashCount = 0;
        this.dailyCreditTotals = 0;
        this.seasonCreditTotals = 0;
        this.dailyCashTotals = 0;
        this.seasonCashTotals = 0;
        this.dailyTotalCollected = 0;
        this.seasonTotalCollected = 0;
        this.dailyTaxTotals = 0;
        this.seasonTaxTotals = 0;
        this.dailyDiscountTotals = 0;
        this.seasonDiscountTotals = 0;
        this.dailyRefundTotals = 0;
        this.seasonRefundTotals = 0;
        this.avgDailyGross = 0;
        this.avgSeasonGross = 0;
        this.dailySalesDate = getDailySalesDate(dayTimeInterval);

        // init day begin and end times
        beginTime = TimeManager.toCalendar(dayTimeInterval.get(util.Constants.BEGIN_TIME));
        endTime = TimeManager.toCalendar(dayTimeInterval.get(util.Constants.END_TIME));

    }

    public void addOrder(Order order, Map<String, Payment> tenderToPayment, Map<String, List<PaymentRefund>> orderToRefundsMap) {
        try {
            // use calendar objects to daily interval
            Calendar transactionTime = TimeManager.toCalendar(order.getCreatedAt());

            // add payment details to totals
            addTotalCollectedMoney(order, transactionTime, tenderToPayment);
            addDiscountMoney(order, transactionTime);
            addRefundedMoney(order, transactionTime, orderToRefundsMap);
            addTaxMoney(order, transactionTime);
            addGrossSalesMoney(order, transactionTime);
            addTenderTotals(order, transactionTime, tenderToPayment);

            // calculate daily and seasonal averages as defined by TNT
            // avg gross = gross sales / transaction count
            avgDailyGross = calculateAvgGrossSales(dailyGrossSales, dailyTransactionCount);
            avgSeasonGross = calculateAvgGrossSales(seasonGrossSales, seasonTransactionCount);
        } catch (Exception e) {
            logger.error("Calendar Exception from aggregating sales/payload data for Gross Sales: " + e);
        }
    }

    private int calculateAvgGrossSales(int gross, int count) {
        int resultPrecision = 0;
        int divPrecision = 2;
        BigDecimal result = BigDecimal.valueOf(0);

        // if there are >0 transactions, avg = gross/count
        // else return avg gross = 0
        if (count > 0) {
            // TNT requirement is to use HALF_UP rounding
            result = BigDecimal.valueOf(gross).divide(BigDecimal.valueOf(count), divPrecision, RoundingMode.HALF_UP);
        }

        return result.setScale(resultPrecision, RoundingMode.HALF_UP).intValue();
    }

    private void addTotalCollectedMoney(Order order, Calendar transactionTime, Map<String, Payment> tenderToPayment) {
    	//if net amount is negative, don't add it to total collected money, because it is a refund order
        if (order.getNetAmounts() != null && order.getNetAmounts().getTotalMoney() != null && order.getNetAmounts().getTotalMoney().getAmount() > 0) {
            if (isDailyTransaction(beginTime, endTime, transactionTime)) {
                dailyTotalCollected += order.getNetAmounts().getTotalMoney().getAmount();
                dailyTransactionCount++;
            }

            seasonTotalCollected += order.getNetAmounts().getTotalMoney().getAmount();
            seasonTransactionCount++;
        }
    }

    private void addDiscountMoney(Order order, Calendar transactionTime) {
        if (order.getTotalDiscountMoney() != null) {
            if (isDailyTransaction(beginTime, endTime, transactionTime)) {
                dailyDiscountTotals -= order.getTotalDiscountMoney().getAmount();
            }
            seasonDiscountTotals -= order.getTotalDiscountMoney().getAmount();
        }
    }

    private void addRefundedMoney(Order order, Calendar transactionTime, Map<String, List<PaymentRefund>> orderToRefundsMap) {
        if(orderToRefundsMap.containsKey(order.getId()) ) {
            List<PaymentRefund> refunds = orderToRefundsMap.get(order.getId());
            for(PaymentRefund refund : refunds) {
                if (refund.getAmountMoney() != null) {
                    if (isDailyTransaction(beginTime, endTime, transactionTime)) {
                        dailyRefundTotals -= refund.getAmountMoney().getAmount();
                    }
                    seasonRefundTotals -= refund.getAmountMoney().getAmount();
                }
            }
        }
    }

    private void addTaxMoney(Order order, Calendar transactionTime) {
        if (order.getTotalTaxMoney() != null) {
            if (isDailyTransaction(beginTime, endTime, transactionTime)) {
                dailyTaxTotals += order.getTotalTaxMoney().getAmount();
            }
            seasonTaxTotals += order.getTotalTaxMoney().getAmount();
        }
    }

    private void addGrossSalesMoney(Order order, Calendar transactionTime) {
        // NOTE: TNT defines "Gross Sales" as sales amount before taxes and discounts, refunds
        // V1 Payment.gross_sales_money is total sales before taxes, discounts, and refunds
    	int totalMoney = order.getTotalMoney() != null ? order.getTotalMoney().getAmount() : 0;
    	int totalTaxMoney = order.getTotalTaxMoney() != null ? order.getTotalTaxMoney().getAmount() : 0;
    	int totalDiscountMoney = order.getTotalDiscountMoney() != null ? order.getTotalDiscountMoney().getAmount() : 0;
    	int totalTipMoney = order.getTotalTipMoney() != null ? order.getTotalTipMoney().getAmount() : 0;
        int grossSales = totalMoney - totalTaxMoney + totalDiscountMoney - totalTipMoney;
        if (isDailyTransaction(beginTime, endTime, transactionTime)) {
            dailyGrossSales += grossSales;
        }
        seasonGrossSales += grossSales;
    }

    private void addTenderTotals(Order order, Calendar transactionTime, Map<String, Payment> tenderToPayment) {
        // loop through payment tenders for cash / credit totals
    	if(order.getTenders() != null) {
    		for (Tender tender : order.getTenders()) {
    			Payment payment = tenderToPayment.get(tender.getId());
                if (payment.getTotalMoney() != null) {
                    if (ConnectV2MigrationHelper.isCardPayment(tender)) {
                        if (isDailyTransaction(beginTime, endTime, transactionTime)) {
                            dailyCreditTotals += payment.getTotalMoney().getAmount();
                            dailyCreditCount++;
                        }
                        seasonCreditTotals += payment.getTotalMoney().getAmount();
                        seasonCreditCount++;
                    }

                    if (tender.getType().equals(Tender.TENDER_TYPE_CASH)) {
                        if (isDailyTransaction(beginTime, endTime, transactionTime)) {
                            dailyCashTotals += payment.getTotalMoney().getAmount();
                            dailyCashCount++;
                        }
                        seasonCashTotals += payment.getTotalMoney().getAmount();
                        seasonCashCount++;
                    }
                }
            }
    	}
    }

    public String getRow() {
        String row = String.format(
                "%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s\n",
                dailySalesDate, locationDetails.locationNumber, locationDetails.rbu, locationDetails.state,
                formatCurrencyTotal(dailyCashTotals), formatCurrencyTotal(dailyCreditTotals), formatCurrencyTotal(dailyTotalCollected),
                formatCurrencyTotal(dailyTaxTotals), formatCurrencyTotal(dailyDiscountTotals), formatCurrencyTotal(dailyRefundTotals),
                formatCurrencyTotal(dailyGrossSales), dailyTransactionCount, formatCurrencyTotal(avgDailyGross),
                formatCurrencyTotal(seasonCashTotals), formatCurrencyTotal(seasonCreditTotals), formatCurrencyTotal(seasonTotalCollected),
                formatCurrencyTotal(seasonTaxTotals), formatCurrencyTotal(seasonDiscountTotals), formatCurrencyTotal(seasonRefundTotals),
                formatCurrencyTotal(seasonGrossSales), seasonTransactionCount, formatCurrencyTotal(avgSeasonGross));
        return row;
    }

    private String getDailySalesDate(Map<String, String> dayTimeInterval) throws ParseException {
        Calendar reportDate = TimeManager.toCalendar(dayTimeInterval.get(util.Constants.BEGIN_TIME));
        String year = String.format("%04d", reportDate.get(Calendar.YEAR));
        String month = String.format("%02d", reportDate.get(Calendar.MONTH) + 1);
        String day = String.format("%02d", reportDate.get(Calendar.DATE));

        return String.format("%s/%s/%s", month, day, year);
    }

    private boolean isDailyTransaction(Calendar beginTime, Calendar endTime, Calendar transactionTime) {
        return beginTime.compareTo(transactionTime) <= 0 && endTime.compareTo(transactionTime) > 0;
    }
}
