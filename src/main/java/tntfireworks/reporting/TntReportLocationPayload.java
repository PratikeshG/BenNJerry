package tntfireworks.reporting;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.TimeManager;

public abstract class TntReportLocationPayload {
    private static final String DEFAULT_DATE_FORMAT = "MM-dd-yy";
    private String payloadDate;
    private String payloadHeader;
    protected String locationNumber;
    protected String locationName;
    protected String city;
    protected String state;
    protected String zip;
    protected String rbu;
    protected String saName;

    public TntReportLocationPayload(String timeZone, String dateFormat, String locationName,
            List<Map<String, String>> dbLocationRows, String payloadHeader) {
        this.payloadHeader = payloadHeader;
        this.locationName = locationName.replaceAll(",", "");
        this.locationNumber = findLocationNumber(locationName);
        this.payloadDate = setPayloadDate(timeZone, dateFormat);
        this.rbu = "";
        this.city = "";
        this.state = "";
        this.zip = "";

        for (Map<String, String> row : dbLocationRows) {
            if (locationNumber.equals(row.get("locationNumber"))) {
                this.city = row.get("city");
                this.state = row.get("state");
                this.rbu = row.get("rbu");
                this.zip = row.get("zip");
                this.saName = row.get("saName");
            }
        }
    }

    public TntReportLocationPayload(String timeZone, String locationNumber, List<Map<String, String>> dbLocationRows,
            String payloadHeader) {
        this(timeZone, DEFAULT_DATE_FORMAT, locationNumber, dbLocationRows, payloadHeader);
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

    /*
     * Helper function to parse location number
     *
     * - per TNT spec, all upcoming seasons will follow new naming convention
     * location name = TNT location number - old seasons followed convention of
     * 'NAME (#LocationNumber)'
     *
     */
    private String findLocationNumber(String locationName) {
        String locationNumber = "";

        // old location name = 'NAME (#Location Number)'
        String oldPattern = "\\w+\\s*\\(#([a-zA-Z0-9\\s]+)\\)";
        Pattern p = Pattern.compile(oldPattern);
        Matcher m = p.matcher(locationName);

        if (m.find()) {
            locationNumber = m.group(1);
        } else {
            if (!locationName.equals("")) {
                locationNumber = locationName;
            }
        }

        return locationNumber;
    }
}
