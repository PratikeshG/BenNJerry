package tntfireworks.reporting;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import util.TimeManager;

public abstract class TntReportLocationPayload {
    private static final String DEFAULT_DATE_FORMAT = "MM-dd-yy";
    private String payloadDate;
    private String payloadHeader;
    protected TntLocationDetails locationDetails;

    public TntReportLocationPayload(String timeZone, String dateFormat, TntLocationDetails locationDetails,
            String payloadHeader) {
        this.payloadHeader = payloadHeader;
        this.locationDetails = locationDetails;
        this.payloadDate = setPayloadDate(timeZone, dateFormat);
    }

    public TntReportLocationPayload(String timeZone, TntLocationDetails locationDetails, String payloadHeader) {
        this(timeZone, DEFAULT_DATE_FORMAT, locationDetails, payloadHeader);
    }

    public String setPayloadDate(String timeZone, String dateFormat) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
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

    public String getPayloadHeader() {
        return payloadHeader;
    }

    public String formatTotal(int gpv) {
        NumberFormat n = NumberFormat.getCurrencyInstance(Locale.US);
        return n.format(gpv / 100.0).replaceAll(",", "");
    }
}
