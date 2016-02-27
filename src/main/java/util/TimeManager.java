package util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class TimeManager {
	
	public static Map<String,String> getPastDayInterval(String timeZoneId) {
		// timeZoneId is expected to be "GMT-08:00", or something similar, as
		// outlined at https://docs.oracle.com/javase/7/docs/api/java/util/TimeZone.html.
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId));
		
		c.set(Calendar.MILLISECOND, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.HOUR_OF_DAY, 0);
		
		String endTime = format(c);
		
		c.add(Calendar.DATE, -1);
		
		String beginTime = format(c);
		
		HashMap<String,String> m = new HashMap<String,String>();
		m.put("begin_time", beginTime);
		m.put("end_time", endTime);
		
		return m;
	}
	
	private static String format(Calendar c1) {
		Calendar c2 = (Calendar) c1.clone();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        TimeZone tz = c2.getTimeZone();

        int offset = tz.getRawOffset();
        if (offset != 0) {
            int hours = Math.abs((offset / (60 * 1000)) / 60);
            int minutes = Math.abs((offset / (60 * 1000)) % 60);
            c2.add(Calendar.HOUR_OF_DAY, hours);
            c2.add(Calendar.MINUTE, minutes);
        }
        return format.format(c2.getTime()) + "Z";
    }
}
