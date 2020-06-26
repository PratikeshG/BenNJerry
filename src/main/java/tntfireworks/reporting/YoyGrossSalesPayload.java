package tntfireworks.reporting;

import java.text.ParseException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YoyGrossSalesPayload extends TntReportLocationPayload {
    private static Logger logger = LoggerFactory.getLogger(YoyGrossSalesPayload.class);
    private static final String LOCATION_COLS = String.format("%s, %s, %s, %s", "Daily Sales Date", "Location Number",
            "RBU", "State");
    private static final String CURRENT_YEAR_DAILY_COLS = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s",
            "Daily Cash Sales", "Daily Credit Sales", "Daily Total Collected Sales", "Daily Taxes", "Daily Discounts",
            "Daily Refunds", "Daily Gross Sales", "# Daily CC Transactions",
            "Average Daily Gross Sale Per Transaction");
    private static final String PREVIOUS_YEAR_DAILY_COLS = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s",
            "Prior Year Daily Cash Sales", "Prior Year Daily Credit Sales", "Prior Year Daily Total Collected Sales",
            "Prior Year Daily Taxes", "Prior Year Daily Discounts", "Prior Year Daily Refunds",
            "Prior Year Daily Gross Sales", "Prior Year # Daily CC Transactions",
            "Prior Year Average Daily Gross Sale Per Transaction");;
    private static final String CURRENT_YEAR_SEASONAL_COLS = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s",
            "Cash Sales (YTD)", "Credit Sales (YTD)", "Total Collected Sales (YTD)", "Taxes (YTD)", "Discounts (YTD)",
            "Refunds (YTD)", "Gross Sales (YTD)", "# CC Transactions (YTD)",
            "Average Gross Sale Per Transaction (YTD)");
    private static final String PREVIOUS_YEAR_SEASONAL_COLS = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s",
            "Prior Year Cash Sales (YTD)", "Prior Year Credit Sales (YTD)", "Prior Year Total Collected Sales (YTD)",
            "Prior Year Taxes (YTD)", "Prior Year Discounts (YTD)", "Prior Year Refunds (YTD)",
            "Prior Year Gross Sales (YTD)", "Prior Year # CC Transactions (YTD)",
            "Prior Year Average Gross Sale Per Transaction (YTD)");
    private static final String DAILY_VARIANCE_COLS = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s",
            "Daily Cash Variance", "Daily Credit Variance", "Daily Total Collected Variance",
            "Daily Gross Sales Variance", "Daily CC Count Variance", "Daily Average Gross Per Transaction Variance",
            "Daily Cash Variance %", "Daily Credit Variance %", "Daily Total Collected Variance %",
            "Daily Gross Sales Variance %", "Daily CC Transaction Count Variance %",
            "Daily Average Gross Per Transaction Variance %");
    private static final String SEASON_VARIANCE_COLS = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s",
            "Cash Variance (YTD)", "Credit Variance (YTD)", "Total Collected Variance (YTD)",
            "Gross Sales Variance (YTD)", "Transaction Count Variance (YTD)",
            "Average Gross / Transaction Variance (YTD)", "Cash Variance % (YTD)", "Credit Variance % (YTD)",
            "Total Collected Variance % (YTD)", "Gross Sales Variance % (YTD)", "CC Transaction Count Variance % (YTD)",
            "Average Gross / Transaction Variance % (YTD)");
    private static final String CASH_COUNT_COLS = String.format("%s, %s, %s, %s", "# Daily Cash Transactions",
            "Prior Year # Daily Cash Transactions", "# Cash Transactions (YTD)",
            "Prior Year # Cash Transactions (YTD)");
    private static final String YOY_GROSS_SALES_FILE_HEADER = String.format("%s, %s, %s, %s, %s, %s, %s, %s\n",
            LOCATION_COLS, CURRENT_YEAR_DAILY_COLS, PREVIOUS_YEAR_DAILY_COLS, DAILY_VARIANCE_COLS,
            CURRENT_YEAR_SEASONAL_COLS, PREVIOUS_YEAR_SEASONAL_COLS, SEASON_VARIANCE_COLS, CASH_COUNT_COLS);
    private GrossSalesPayload currentYearPayload;
    private GrossSalesPayload previousYearPayload;
    private int dailyCashVariance;
    private int dailyCreditVariance;
    private int dailyTotalCollectedVariance;
    private int dailyGrossSalesVariance;
    private int dailyTransactionCountVariance;
    private int dailyCreditCountVariance;
    private int dailyAvgGrossVariance;
    private String dailyCashVariancePercentage;
    private String dailyCreditVariancePercentage;
    private String dailyTotalCollectedVariancePercentage;
    private String dailyGrossSalesVariancePercentage;
    private String dailyTransactionCountVariancePercentage;
    private String dailyCreditCountVariancePercentage;
    private String dailyAvgGrossVariancePercentage;
    private int seasonCashVariance;
    private int seasonCreditVariance;
    private int seasonTotalCollectedVariance;
    private int seasonGrossSalesVariance;
    private int seasonTransactionCountVariance;
    private int seasonCreditCountVariance;
    private int seasonAvgGrossVariance;
    private String seasonCashVariancePercentage;
    private String seasonCreditVariancePercentage;
    private String seasonTotalCollectedVariancePercentage;
    private String seasonGrossSalesVariancePercentage;
    private String seasonTransactionCountVariancePercentage;
    private String seasonCreditCountVariancePercentage;
    private String seasonAvgGrossVariancePercentage;

    public YoyGrossSalesPayload(String timeZone, Map<String, String> dayTimeInterval,
            TntLocationDetails locationDetails, GrossSalesPayload currentYearPayload,
            GrossSalesPayload previousYearPayload) throws ParseException {
        super(timeZone, locationDetails, YOY_GROSS_SALES_FILE_HEADER);
        this.currentYearPayload = currentYearPayload;
        this.previousYearPayload = previousYearPayload;

        // calculate daily and seasonal variance
        calculateDailyVariance();
        calculateSeasonVariance();
    }

    private void calculateDailyVariance() {
        // calculate dollar differences
        dailyCashVariance = currentYearPayload.dailyCashTotals - previousYearPayload.dailyCashTotals;
        dailyCreditVariance = currentYearPayload.dailyCreditTotals - previousYearPayload.dailyCreditTotals;
        dailyTotalCollectedVariance = currentYearPayload.dailyTotalCollected - previousYearPayload.dailyTotalCollected;
        dailyGrossSalesVariance = currentYearPayload.dailyGrossSales - previousYearPayload.dailyGrossSales;
        dailyTransactionCountVariance = currentYearPayload.dailyTransactionCount
                - previousYearPayload.dailyTransactionCount;
        dailyCreditCountVariance = currentYearPayload.dailyCreditCount - previousYearPayload.dailyCreditCount;
        dailyAvgGrossVariance = currentYearPayload.avgDailyGross - previousYearPayload.avgDailyGross;

        // calculate percent difference which is defined as:
        // ==> variance percentage = amount variance / prior year amount
        dailyCashVariancePercentage = calculatePercentVariance(dailyCashVariance, previousYearPayload.dailyCashTotals);
        dailyCreditVariancePercentage = calculatePercentVariance(dailyCreditVariance,
                previousYearPayload.dailyCreditTotals);
        dailyTotalCollectedVariancePercentage = calculatePercentVariance(dailyTotalCollectedVariance,
                previousYearPayload.dailyTotalCollected);
        dailyGrossSalesVariancePercentage = calculatePercentVariance(dailyGrossSalesVariance,
                previousYearPayload.dailyGrossSales);
        dailyTransactionCountVariancePercentage = calculatePercentVariance(dailyTransactionCountVariance,
                previousYearPayload.dailyTransactionCount);
        dailyCreditCountVariancePercentage = calculatePercentVariance(dailyCreditCountVariance,
                previousYearPayload.dailyCreditCount);
        dailyAvgGrossVariancePercentage = calculatePercentVariance(dailyAvgGrossVariance,
                previousYearPayload.avgDailyGross);
    }

    private void calculateSeasonVariance() {
        // calculate dollar differences
        seasonCashVariance = currentYearPayload.seasonCashTotals - previousYearPayload.seasonCashTotals;
        seasonCreditVariance = currentYearPayload.seasonCreditTotals - previousYearPayload.seasonCreditTotals;
        seasonTotalCollectedVariance = currentYearPayload.seasonTotalCollected
                - previousYearPayload.seasonTotalCollected;
        seasonGrossSalesVariance = currentYearPayload.seasonGrossSales - previousYearPayload.seasonGrossSales;
        seasonTransactionCountVariance = currentYearPayload.seasonTransactionCount
                - previousYearPayload.seasonTransactionCount;
        seasonCreditCountVariance = currentYearPayload.seasonCreditCount - previousYearPayload.seasonCreditCount;
        seasonAvgGrossVariance = currentYearPayload.avgSeasonGross - previousYearPayload.avgSeasonGross;

        // calculate percent difference which is defined as:
        // ==> variance percentage = amount variance / prior year amount
        seasonCashVariancePercentage = calculatePercentVariance(seasonCashVariance,
                previousYearPayload.seasonCashTotals);
        seasonCreditVariancePercentage = calculatePercentVariance(seasonCreditVariance,
                previousYearPayload.seasonCreditTotals);
        seasonTotalCollectedVariancePercentage = calculatePercentVariance(seasonTotalCollectedVariance,
                previousYearPayload.seasonTotalCollected);
        seasonGrossSalesVariancePercentage = calculatePercentVariance(seasonGrossSalesVariance,
                previousYearPayload.seasonGrossSales);
        seasonTransactionCountVariancePercentage = calculatePercentVariance(seasonTransactionCountVariance,
                previousYearPayload.seasonTransactionCount);
        seasonCreditCountVariancePercentage = calculatePercentVariance(seasonCreditCountVariance,
                previousYearPayload.seasonCreditCount);
        seasonAvgGrossVariancePercentage = calculatePercentVariance(seasonAvgGrossVariance,
                previousYearPayload.avgSeasonGross);
    }

    private String calculatePercentVariance(int varianceAmount, int priorYearAmount) {
        String result = "N/A";
        if (priorYearAmount > 0) {
            // calculate percent difference which is defined as:
            //      variance percentage = (amount variance / prior year amount) * 100
            //      and round to 2 decimals
            double percentVariance = (varianceAmount * 100.0) / priorYearAmount;
            percentVariance = Math.round(percentVariance * 100.0) / 100.0;
            result = String.valueOf(percentVariance);
        }

        return result;
    }

    public String getRow() {
        String row = String.format("%s, %s, %s, %s, %s, %s, %s, %s\n", getLocationDetailsRow(),
                getCurrentYearDailyTotalsRow(currentYearPayload), getPreviousYearDailyTotalsRow(previousYearPayload),
                getDailyVarianceRow(), getCurrentYearSeasonTotalsRow(currentYearPayload),
                getPreviousYearSeasonTotalsRow(previousYearPayload), getSeasonVarianceRow(), getCashCountRow());
        return row;
    }

    private String getLocationDetailsRow() {
        return String.format("%s, %s, %s, %s", currentYearPayload.dailySalesDate, locationDetails.locationNumber,
                locationDetails.rbu, locationDetails.state);
    }

    private String getCurrentYearDailyTotalsRow(GrossSalesPayload payload) {
        return getDailyTotalsRow(payload);
    }

    private String getCurrentYearSeasonTotalsRow(GrossSalesPayload payload) {
        return getSeasonTotalsRow(payload);
    }

    private String getPreviousYearDailyTotalsRow(GrossSalesPayload payload) {
        return getDailyTotalsRow(payload);
    }

    private String getPreviousYearSeasonTotalsRow(GrossSalesPayload payload) {
        return getSeasonTotalsRow(payload);
    }

    private String getDailyTotalsRow(GrossSalesPayload payload) {
        return String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s", formatTotal(payload.dailyCashTotals),
                formatTotal(payload.dailyCreditTotals), formatTotal(payload.dailyTotalCollected),
                formatTotal(payload.dailyTaxTotals), formatTotal(payload.dailyDiscountTotals),
                formatTotal(payload.dailyRefundTotals), formatTotal(payload.dailyGrossSales), payload.dailyCreditCount,
                formatTotal(payload.avgDailyGross));
    }

    private String getSeasonTotalsRow(GrossSalesPayload payload) {
        return String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s", formatTotal(payload.seasonCashTotals),
                formatTotal(payload.seasonCreditTotals), formatTotal(payload.seasonTotalCollected),
                formatTotal(payload.seasonTaxTotals), formatTotal(payload.seasonDiscountTotals),
                formatTotal(payload.seasonRefundTotals), formatTotal(payload.seasonGrossSales),
                payload.seasonCreditCount, formatTotal(payload.avgSeasonGross));
    }

    private String getDailyVarianceRow() {
        return String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s", formatTotal(dailyCashVariance),
                formatTotal(dailyCreditVariance), formatTotal(dailyTotalCollectedVariance),
                formatTotal(dailyGrossSalesVariance), dailyTransactionCountVariance, formatTotal(dailyAvgGrossVariance),
                dailyCashVariancePercentage, dailyCreditVariancePercentage, dailyTotalCollectedVariancePercentage,
                dailyGrossSalesVariancePercentage, dailyCreditCountVariancePercentage, dailyAvgGrossVariancePercentage);
    }

    private String getSeasonVarianceRow() {
        return String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s", formatTotal(seasonCashVariance),
                formatTotal(seasonCreditVariance), formatTotal(seasonTotalCollectedVariance),
                formatTotal(seasonGrossSalesVariance), seasonTransactionCountVariance,
                formatTotal(seasonAvgGrossVariance), seasonCashVariancePercentage, seasonCreditVariancePercentage,
                seasonTotalCollectedVariancePercentage, seasonGrossSalesVariancePercentage,
                seasonCreditCountVariancePercentage, seasonAvgGrossVariancePercentage);
    }

    private String getCashCountRow() {
        return String.format("%s, %s, %s, %s", currentYearPayload.dailyCashCount, previousYearPayload.dailyCashCount,
                currentYearPayload.seasonCashCount, previousYearPayload.seasonCashCount);
    }
}
