package tntfireworks.reporting;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import util.TimeManager;

public abstract class TntReportPayload {
	private static final String DEFAULT_DATE_FORMAT = "MM-dd-yy";
    private String payloadDate;
    private String payloadHeader;

    public TntReportPayload(String timeZone, String dateFormat, String payloadHeader) {
    	this.payloadHeader = payloadHeader;
    	this.payloadDate = setPayloadDate(timeZone, dateFormat);
    }

    public TntReportPayload(String timeZone, String payloadHeader) {
    	this(timeZone, DEFAULT_DATE_FORMAT, payloadHeader);
    }

    public String setPayloadDate(String timeZone, String dateFormat) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        String payloadDate;
		try {
			payloadDate = TimeManager.toSimpleDateTimeInTimeZone(cal, timeZone, dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
			throw new RuntimeException ("Invalid timeZoneId provided to TimeManager.toSimpleDateTimeInTimeZone" + e);
		}

        return payloadDate;
    }

    public String getPayloadDate() {
        return payloadDate;
    }

    public String getPayloadHeader() {
        return payloadHeader;
    }

    protected String formatTotal(int gpv) {
        NumberFormat n = NumberFormat.getCurrencyInstance(Locale.US);
        return n.format(gpv / 100.0).replaceAll(",", "");
    }
}
