package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class TimeManager {

    /*
     * Example output for getPastDayInterval() Assuming current time is: 11/03,
     * Time: ~2:50pm
     *
     * TimeManager.getPastDayInterval(0, 0, "GMT-00:00").get("begin_time") =
     * 2016-11-03T00:00:00Z TimeManager.getPastDayInterval(0, 0,
     * "GMT-00:00").get("end_time") = 2016-11-03T21:45:00Z
     *
     * TimeManager.getPastDayInterval(0, 0, "GMT-07:00").get("begin_time") =
     * 2016-11-03T00:00:00-07:00 TimeManager.getPastDayInterval(0, 0,
     * "GMT-07:00").get("end_time") = 2016-11-03T14:53:12-07:00
     *
     * TimeManager.getPastDayInterval(1, 0, "GMT-07:00").get("begin_time") =
     * 2016-11-03T00:00:00-07:00 TimeManager.getPastDayInterval(1, 0,
     * "GMT-07:00").get("end_time") = 2016-11-03T14:56:28-07:00
     *
     * TimeManager.getPastDayInterval(0, -1, "GMT-07:00").get("begin_time") =
     * 2016-11-04T00:00:00-07:00 TimeManager.getPastDayInterval(0, -1,
     * "GMT-07:00").get("end_time") = 2016-11-04T14:58:41-07:00
     *
     * TimeManager.getPastDayInterval(0, 1, "GMT-07:00").get("begin_time") =
     * 2016-11-02T00:00:00-07:00 TimeManager.getPastDayInterval(0, 1,
     * "GMT-07:00").get("end_time") = 2016-11-02T23:59:59-07:00
     *
     * NOTE: this produces the same result as above with different parameters!!
     * TimeManager.getPastDayInterval(1, 1, "GMT-07:00").get("begin_time") =
     * 2016-11-02T00:00:00-07:00 TimeManager.getPastDayInterval(1, 1,
     * "GMT-07:00").get("end_time") = 2016-11-02T23:59:59-07:00
     *
     * TimeManager.getPastDayInterval(0, 1, "GMT-07:00").get("begin_time") =
     * 2016-11-02T00:00:00-07:00 TimeManager.getPastDayInterval(0, 0,
     * "GMT-07:00").get("begin_time") = 2016-11-03T00:00:00-07:00
     */
    public static Map<String, String> getPastDayInterval(int range, int offset, String timeZoneId) {
        // timeZoneId is expected to be "GMT-08:00", or something similar, as
        // outlined at
        // https://docs.oracle.com/javase/7/docs/api/java/util/TimeZone.html.
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId));

        String beginTime = "";
        String endTime = "";

        // subtract offset from current date
        c.add(Calendar.DATE, -offset);

        // note: if offset = 0, calendar is not set to current time
        //       therefore, value is currently defaulting to 00:00:00
        if (offset > 0) {
            c.set(Calendar.MILLISECOND, 999);
            c.set(Calendar.SECOND, 59);
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.HOUR_OF_DAY, 23);
        }

        endTime = toIso8601(c, timeZoneId);

        if (range == 0) {
            range = 1;
        }

        c.add(Calendar.DATE, -range + 1);

        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, 0);

        beginTime = toIso8601(c, timeZoneId);

        HashMap<String, String> m = new HashMap<String, String>();
        m.put("begin_time", beginTime);
        m.put("end_time", endTime);

        return m;
    }

    public static Map<String, String> getYearToDateInterval(String timeZoneId) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId));

        String endTime = toIso8601(c, timeZoneId);

        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.DAY_OF_YEAR, 1);

        String beginTime = toIso8601(c, timeZoneId);

        HashMap<String, String> m = new HashMap<String, String>();
        m.put("begin_time", beginTime);
        m.put("end_time", endTime);

        return m;
    }

    public static Map<String, String> getPastMonthInterval(int offset, String timeZoneId) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId));

        String beginTime = "";
        String endTime = "";

        c.add(Calendar.MONTH, -offset);

        // End date of calendar month
        c.set(Calendar.DATE, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        c.set(Calendar.MILLISECOND, 999);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.HOUR_OF_DAY, 23);
        endTime = toIso8601(c, timeZoneId);

        // Start date of calendar month
        c.set(Calendar.DATE, 1);
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, 0);
        beginTime = toIso8601(c, timeZoneId);

        HashMap<String, String> m = new HashMap<String, String>();
        m.put(Constants.BEGIN_TIME, beginTime);
        m.put(Constants.END_TIME, endTime);

        return m;
    }

    public static Map<String, String> getPastTimeInterval(int seconds, int offset, String timeZoneId) {
        // timeZoneId is expected to be "GMT-08:00", or something similar, as
        // outlined at
        // https://docs.oracle.com/javase/7/docs/api/java/util/TimeZone.html.
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId));

        if (offset > 0) {
            c.add(Calendar.SECOND, -offset);
        }

        String endTime = toIso8601(c, timeZoneId);

        c.add(Calendar.SECOND, -seconds);

        String beginTime = toIso8601(c, timeZoneId);

        HashMap<String, String> m = new HashMap<String, String>();
        m.put("begin_time", beginTime);
        m.put("end_time", endTime);

        return m;
    }

    public static String getIso8601HoursAgo(int hours, String timeZoneId) {
        // timeZoneId is expected to be "GMT-08:00", or something similar, as
        // outlined at https://docs.oracle.com/javase/7/docs/api/java/util/TimeZone.html.
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId));
        c.add(Calendar.HOUR, -hours);
        return toIso8601(c, timeZoneId);
    }

    public static String toIso8601(Calendar c, String timeZoneId) {
        String year = String.format("%04d", c.get(Calendar.YEAR));
        String month = String.format("%02d", c.get(Calendar.MONTH) + 1);
        String day = String.format("%02d", c.get(Calendar.DATE));
        String hour = String.format("%02d", c.get(Calendar.HOUR_OF_DAY));
        String minute = String.format("%02d", c.get(Calendar.MINUTE));
        String second = String.format("%02d", c.get(Calendar.SECOND));

        TimeZone tz = TimeZone.getTimeZone(timeZoneId);
        int offsetInMillis = tz.getOffset(c.getTimeInMillis());
        String offset = String.format("%02d:%02d", Math.abs(offsetInMillis / 3600000),
                Math.abs((offsetInMillis / 60000) % 60));
        String zone = (offsetInMillis >= 0 ? "+" : "-") + offset;

        if (zone.equals("+00:00")) {
            zone = "Z";
        }

        return year + "-" + month + "-" + day + "T" + hour + ":" + minute + ":" + second + zone;
    }

    public static Calendar toCalendar(final String iso8601string) throws ParseException {
        Calendar calendar = GregorianCalendar.getInstance();
        String s = iso8601string.replace("Z", "+00:00");
        int idx = s.indexOf(".");
        // check for milliseconds and drop
        if(idx != -1) {
        	int timezoneIdx = s.indexOf("+") != -1 ? s.indexOf("+") : s.indexOf("-");
        	s = s.substring(0, idx) + s.substring(timezoneIdx);
        }
        try {
            s = s.substring(0, 22) + s.substring(23); // to get rid of the ":"
        } catch (IndexOutOfBoundsException e) {
            throw new ParseException("Invalid length", 0);
        }

        Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(s);
        calendar.setTime(date);
        return calendar;
    }

    public static String toSimpleDateTimeInTimeZone(String iso8601string, String timeZoneId, String format)
            throws ParseException {
        if (timeZoneId == null) {
            throw new ParseException("time zone is necessary", 0);
        }

        Calendar cal = TimeManager.toCalendar(iso8601string);
        cal.setTimeZone(TimeZone.getTimeZone(timeZoneId));

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(TimeZone.getTimeZone(timeZoneId));
        String formatted = sdf.format(cal.getTime());

        return formatted;
    }

    public static String toSimpleDateTimeInTimeZone(Calendar calendar, String timeZoneId, String format)
            throws ParseException {
        if (timeZoneId == null) {
            throw new ParseException("time zone is necessary", 0);
        }

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(TimeZone.getTimeZone(timeZoneId));
        String formatted = sdf.format(calendar.getTime());

        return formatted;
    }

    public static String dateFormatFromRFC3339(String RFC3339, String timeZoneId, String format) throws ParseException {
        if (timeZoneId == null) {
            throw new ParseException("time zone is necessary", 0);
        }

        // Customer objects can return any of the following values:
        // 2016-03-23T20:21:54Z
        // 2016-03-23T20:21:54.8Z
        // 2016-03-23T20:21:54.85Z
        // 2016-03-23T20:21:54.859Z
        String parseFormat = RFC3339.contains(".") ? "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" : "yyyy-MM-dd'T'HH:mm:ss";

        SimpleDateFormat sdf = new SimpleDateFormat(parseFormat);
        Date d = sdf.parse(RFC3339);
        sdf.setTimeZone(TimeZone.getTimeZone(timeZoneId));
        sdf.applyPattern(format);
        String formatted = sdf.format(d);

        return formatted;
    }

    /**
     * Takes a ISO8601 timestamp in UTC and outputs the local time 8601 time
     * stamp (without offset).
     */
    public static String convertToLocalTime(String utcTime, String timeZone) throws ParseException {
        return toSimpleDateTimeInTimeZone(utcTime, timeZone, "yyyy-MM-dd'T'HH:mm:ss");
    }
}
