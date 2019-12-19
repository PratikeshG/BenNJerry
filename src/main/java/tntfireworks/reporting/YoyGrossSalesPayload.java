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
            "Daily Refunds", "Daily Gross Sales", "# Daily Transactions", "Average Daily Gross Sale Per Transaction");
    private static final String PREVIOUS_YEAR_DAILY_COLS = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s",
            "Prior Year Daily Cash Sales", "Prior Year Daily Credit Sales", "Prior Year Daily Total Collected Sales",
            "Prior Year Daily Taxes", "Prior Year Daily Discounts", "Prior Year Daily Refunds",
            "Prior Year Daily Gross Sales", "Prior Year # Daily Transactions",
            "Prior Year Average Daily Gross Sale Per Transaction");;
    private static final String CURRENT_YEAR_SEASONAL_COLS = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s",
            "Cash Sales (YTD)", "Credit Sales (YTD)", "Total Collected Sales (YTD)", "Taxes (YTD)", "Discounts (YTD)",
            "Refunds (YTD)", "Gross Sales (YTD)", "# Transactions (YTD)", "Average Gross Sale Per Transaction (YTD)");
    private static final String PREVIOUS_YEAR_SEASONAL_COLS = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s",
            "Prior Year Cash Sales (YTD)", "Prior Year Credit Sales (YTD)", "Prior Year Total Collected Sales (YTD)",
            "Prior Year Taxes (YTD)", "Prior Year Discounts (YTD)", "Prior Year Refunds (YTD)",
            "Prior Year Gross Sales (YTD)", "Prior Year # Transactions (YTD)",
            "Prior Year Average Gross Sale Per Transaction (YTD)");
    private static final String YOY_GROSS_SALES_FILE_HEADER = String.format("%s, %s, %s, %s, %s\n", LOCATION_COLS,
            CURRENT_YEAR_DAILY_COLS, PREVIOUS_YEAR_DAILY_COLS, CURRENT_YEAR_SEASONAL_COLS, PREVIOUS_YEAR_SEASONAL_COLS);
    private GrossSalesPayload currentYearPayload;
    private GrossSalesPayload previousYearPayload;

    public YoyGrossSalesPayload(String timeZone, Map<String, String> dayTimeInterval,
            TntLocationDetails locationDetails, GrossSalesPayload currentYearPayload,
            GrossSalesPayload previousYearPayload) throws ParseException {
        super(timeZone, locationDetails, YOY_GROSS_SALES_FILE_HEADER);
        this.currentYearPayload = currentYearPayload;
        this.previousYearPayload = previousYearPayload;
    }

    public String getRow() {
        String row = String.format("%s, %s, %s, %s, %s\n", getLocationDetailsRow(),
                getCurrentYearDailyTotalsRow(currentYearPayload), getPreviousYearDailyTotalsRow(previousYearPayload),
                getCurrentYearSeasonTotalsRow(currentYearPayload), getPreviousYearSeasonTotalsRow(previousYearPayload));
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
                formatTotal(payload.dailyRefundTotals), formatTotal(payload.dailyGrossSales),
                payload.dailyTransactionCount, formatTotal(payload.avgDailyGross));
    }

    private String getSeasonTotalsRow(GrossSalesPayload payload) {
        return String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s", formatTotal(payload.seasonCashTotals),
                formatTotal(payload.seasonCreditTotals), formatTotal(payload.seasonTotalCollected),
                formatTotal(payload.seasonTaxTotals), formatTotal(payload.seasonDiscountTotals),
                formatTotal(payload.seasonRefundTotals), formatTotal(payload.seasonGrossSales),
                payload.seasonTransactionCount, formatTotal(payload.avgSeasonGross));
    }
}
