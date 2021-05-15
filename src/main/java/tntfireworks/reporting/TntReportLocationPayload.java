package tntfireworks.reporting;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import util.TimeManager;

public abstract class TntReportLocationPayload {
    private static final String DEFAULT_DATE_FORMAT = "MM-dd-yy";
    private static final int MAX_FRACTION_DIGITS = 2;
    private static final int MIN_FRACTION_DIGITS = 2;
    private String payloadDate;
    private String payloadHeader;
    protected TntLocationDetails locationDetails;

    public TntReportLocationPayload(String timeZone, int offset, String dateFormat, TntLocationDetails locationDetails,
            String payloadHeader) {
        this.payloadHeader = payloadHeader;
        this.locationDetails = locationDetails;
        this.payloadDate = setPayloadDate(timeZone, dateFormat, offset);
    }

    public TntReportLocationPayload(String timeZone, int offset, TntLocationDetails locationDetails, String payloadHeader) {
        this(timeZone, offset, DEFAULT_DATE_FORMAT, locationDetails, payloadHeader);
    }

    public String setPayloadDate(String timeZone, String dateFormat, int offset) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        cal.add(Calendar.DATE, -offset);
        String payloadDate;
        try {
            payloadDate = TimeManager.toSimpleDateTimeInTimeZone(cal, timeZone, dateFormat);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid timeZoneId provided to TimeManager.toSimpleDateTimeInTimeZone" + e);
        }

        return payloadDate;
    }

    public String getPayloadDate() {
        return payloadDate;
    }

    public void setPayloadDate(String payloadDate) {
    	this.payloadDate = payloadDate;
    }

    public String getPayloadHeader() {
        return payloadHeader;
    }

    public String formatCurrencyTotal(int totals) {
        NumberFormat n = NumberFormat.getCurrencyInstance(Locale.US);

        return n.format(totals / 100.0).replaceAll(",", "");
    }

    public String formatDecimalTotal(int totals) {
        NumberFormat n = NumberFormat.getInstance(Locale.US);
        n.setMaximumFractionDigits(MAX_FRACTION_DIGITS);
        n.setMinimumFractionDigits(MIN_FRACTION_DIGITS);
        n.setRoundingMode(RoundingMode.HALF_EVEN); // HALF_EVEN = Banker's Rounding

        return n.format(totals / 100.0).replaceAll(",", "");
    }
}
