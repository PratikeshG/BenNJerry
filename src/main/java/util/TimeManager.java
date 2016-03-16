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
	
	public static Map<String,String> getPastDayInterval(int range, int offset, String timeZoneId) {
		// timeZoneId is expected to be "GMT-08:00", or something similar, as
		// outlined at https://docs.oracle.com/javase/7/docs/api/java/util/TimeZone.html.
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId));
		
		String beginTime = "";
		String endTime = "";
		
		c.add(Calendar.DATE, -offset);
		
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
		
        HashMap<String,String> m = new HashMap<String,String>();
        m.put("begin_time", beginTime);
        m.put("end_time", endTime);
        
        return m;
	}
	
	public static Map<String,String> getPastTimeInterval(int seconds, int offset, String timeZoneId) {
		// timeZoneId is expected to be "GMT-08:00", or something similar, as
		// outlined at https://docs.oracle.com/javase/7/docs/api/java/util/TimeZone.html.
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId));
		
		if (offset > 0) {
			c.add(Calendar.SECOND, -offset);
		}
		
		String endTime = toIso8601(c, timeZoneId);
		
		c.add(Calendar.SECOND, -seconds);
		
		String beginTime = toIso8601(c, timeZoneId);
		
		HashMap<String,String> m = new HashMap<String,String>();
		m.put("begin_time", beginTime);
		m.put("end_time", endTime);
		
		return m;
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
	    String offset = String.format("%02d:%02d", Math.abs(offsetInMillis / 3600000), Math.abs((offsetInMillis / 60000) % 60));
	    String zone = (offsetInMillis >= 0 ? "+" : "-") + offset;
	    
	    if (zone.equals("+00:00")) {
	    	zone = "Z";
	    }
		
		return year + "-" + month + "-" + day + "T" + hour + ":" + minute + ":" + second + zone;
	}
	
	public static Calendar toCalendar(final String iso8601string) throws ParseException {
        Calendar calendar = GregorianCalendar.getInstance();
        String s = iso8601string.replace("Z", "+00:00");
        
        try {
            s = s.substring(0, 22) + s.substring(23); // to get rid of the ":"
        } catch (IndexOutOfBoundsException e) {
            throw new ParseException("Invalid length", 0);
        }
        
        Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(s);
        calendar.setTime(date);
        return calendar;
    }
}
