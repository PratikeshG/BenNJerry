package tntfireworks.reporting;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.connect.v2.Tender;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.Payment;

import util.TimeManager;

/*
 * Report 5/6 - "Location Sales Report" - Emailed daily
 *
 * Report 5 and 6 were merged into one report. Report 5 initially contained aggregate daily and "YTD" seasonal credit
 * card sales information per location. Report 6 was the same report, but included cash sales information.
 *
 * Report 5 now contains columns for both 1) credit card sales and 2) credit card + cash sales.  Each row in the report
 * represents aggregate sales for a single location. The season range is determined as the period between the current
 * date for the report (unless explicitly set elsewhere) and the set start date for the season as defined in
 * tntfireworks.reporting.properties file as 'startOfSeason'. In order to calculate the seasonal sales amount for each
 * location, payment and order data is pulled for the entire season range.
 *
 */
public class LocationSalesPayload extends TntReportLocationPayload {
    private static Logger logger = LoggerFactory.getLogger(LocationSalesPayload.class);

    private static final String LOCATION_SALES_FILE_HEADER = String.format("%s, %s, %s, %s, %s, %s, %s, %s\n",
            "Location Number", "RBU", "City", "Sales Associate Number", "Daily Sales (CREDIT ONLY)",
            "YTD Sales (CREDIT ONLY)", "Daily Sales (CASH/CREDIT)", "YTD Sales (CASH/CREDIT)");
    private Map<String, String> dayTimeInterval;
    private int creditDailySales;
    private int cashDailySales;
    private int creditTotalSales;
    private int cashTotalSales;

    public LocationSalesPayload(String timeZone, int offset, Map<String, String> dayTimeInterval,
            TntLocationDetails locationDetails) {
        super(timeZone, offset, locationDetails, LOCATION_SALES_FILE_HEADER);
        this.dayTimeInterval = dayTimeInterval;
        this.creditDailySales = 0;
        this.creditTotalSales = 0;
        this.cashDailySales = 0;
        this.cashTotalSales = 0;
    }

    public void addEntry(Order order, Map<String, Payment> tenderToPayment) {
        try {
            // use calendar objects to daily interval
            Calendar beginTime = TimeManager.toCalendar(dayTimeInterval.get(util.Constants.BEGIN_TIME));
            Calendar endTime = TimeManager.toCalendar(dayTimeInterval.get(util.Constants.END_TIME));
            Calendar orderTime = TimeManager.toCalendar(order.getCreatedAt());

            // loop through tenders and add tender amounts to payload
            if(order.getTenders() != null) {
                for (Tender tender : order.getTenders()) {
                    if (isValidTender(tender)) {
                        addTenderAmountToPayload(isDailyOrder(beginTime, endTime, orderTime), tender,
                                getTenderToRefundMap(order, tenderToPayment));
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Calendar Exception from aggregating sales/payload data for LocationSales: " + e);
        }
    }

    private int getCashCreditDaily() {
        return creditDailySales + cashDailySales;
    }

    private int getCashCreditTotal() {
        return creditTotalSales + cashTotalSales;
    }

    public String getRow() {
        String row = String.format("%s, %s, %s, %s, %s, %s, %s, %s \n", locationDetails.locationNumber,
                locationDetails.rbu, locationDetails.city, locationDetails.saNumber, formatCurrencyTotal(creditDailySales),
                formatCurrencyTotal(creditTotalSales), formatCurrencyTotal(getCashCreditDaily()), formatCurrencyTotal(getCashCreditTotal()));
        return row;
    }

    private void addTenderAmountToPayload(boolean isDailyOrder, Tender tender,
            Map<String, Integer> tenderToRefund) {

        // add cash tender amounts
        if (tender.getType().equals(Tender.TENDER_TYPE_CASH)) {
            if (isDailyOrder) {
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

        // add card tender amounts
        if (tender.getType().equals(Tender.TENDER_TYPE_CARD)) {
            if (isDailyOrder) {
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

    private boolean isDailyOrder(Calendar beginTime, Calendar endTime, Calendar orderTime) {
        return beginTime.compareTo(orderTime) <= 0 && endTime.compareTo(orderTime) > 0;
    }

    private Map<String, Integer> getTenderToRefundMap(Order order, Map<String, Payment> tenderToPayment) {
        Map<String, Integer> tenderToRefund = new HashMap<String, Integer>();

        if(order.getTenders() != null) {
        	for(Tender tender : order.getTenders()) {
        		Payment payment = tenderToPayment.get(tender.getId());
        		if(isValidPayment(payment)) {
        			tenderToRefund.put(payment.getId(), payment.getRefundedMoney().getAmount());
        		}
        	}
        }

        return tenderToRefund;
    }

    private boolean isValidTender(Tender tender) {
        return tender != null && tender.getAmountMoney() != null;
    }

    private boolean isValidPayment(Payment payment) {
    	return payment != null && payment.getRefundedMoney() != null;
    }
}
