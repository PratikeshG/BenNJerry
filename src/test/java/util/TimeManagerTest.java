package util;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Test;

public class TimeManagerTest {
	
	@Test
	public void getPastDayInterval_ZeroRangeAndZeroOffset_ReturnsCurrentDay() {
		String timeZoneId = "UTC";
		Map<String,String> results = TimeManager.getPastDayInterval(0, 0, timeZoneId);
		
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId));
		String endTime = TimeManager.toIso8601(c, timeZoneId);
		
		c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, 0);
        String beginTime = TimeManager.toIso8601(c, timeZoneId);

		assertEquals("beginning times align", beginTime, results.get("begin_time"));
		assertEquals("ending times align", endTime, results.get("end_time"));
	}
	
	@Test
	public void getPastDayInterval_OneRangeAndZeroOffset_ReturnsCurrentDay() {
		String timeZoneId = "UTC";
		Map<String,String> results = TimeManager.getPastDayInterval(1, 0, timeZoneId);
		
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId));
		String endTime = TimeManager.toIso8601(c, timeZoneId);
		
		c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, 0);
        String beginTime = TimeManager.toIso8601(c, timeZoneId);

		assertEquals("beginning times align", beginTime, results.get("begin_time"));
		assertEquals("ending times align", endTime, results.get("end_time"));
	}
	
	@Test
	public void getPastDayInterval_ZeroRangeAndOneOffset_ReturnsPreviousDay() {
		String timeZoneId = "UTC";
		Map<String,String> results = TimeManager.getPastDayInterval(0, 1, timeZoneId);
		
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId));
		c.set(Calendar.MILLISECOND, 999);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.add(Calendar.DATE, -1);
        String endTime = TimeManager.toIso8601(c, timeZoneId);
        
		c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, 0);
        String beginTime = TimeManager.toIso8601(c, timeZoneId);

		assertEquals("beginning times align", beginTime, results.get("begin_time"));
		assertEquals("ending times align", endTime, results.get("end_time"));
	}
	
	@Test
	public void getPastDayInterval_OneRangeAndOneOffset_ReturnsPreviousDay() {
		String timeZoneId = "UTC";
		Map<String,String> results = TimeManager.getPastDayInterval(1, 1, timeZoneId);
		
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId));
		c.set(Calendar.MILLISECOND, 999);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.add(Calendar.DATE, -1);
        String endTime = TimeManager.toIso8601(c, timeZoneId);
        
		c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, 0);
        String beginTime = TimeManager.toIso8601(c, timeZoneId);

		assertEquals("beginning times align", beginTime, results.get("begin_time"));
		assertEquals("ending times align", endTime, results.get("end_time"));
	}
	
	@Test
	public void getPastDayInterval_TwoRangeAndOneOffset_ReturnsPreviousTwoDays() {
		String timeZoneId = "UTC";
		Map<String,String> results = TimeManager.getPastDayInterval(2, 1, timeZoneId);
		
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId));
		c.set(Calendar.MILLISECOND, 999);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.add(Calendar.DATE, -1);
        String endTime = TimeManager.toIso8601(c, timeZoneId);
		
		c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, 0);
		c.add(Calendar.DATE, -1);
        String beginTime = TimeManager.toIso8601(c, timeZoneId);

		assertEquals("beginning times align", beginTime, results.get("begin_time"));
		assertEquals("ending times align", endTime, results.get("end_time"));
	}
	
	@Test
	public void getPastDayInterval_TwoRangeAndZeroOffset_ReturnsCurrentDayAndYesterday() {
		String timeZoneId = "UTC";
		Map<String,String> results = TimeManager.getPastDayInterval(2, 0, timeZoneId);
		
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId));
		String endTime = TimeManager.toIso8601(c, timeZoneId);
		
		c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.add(Calendar.DATE, -1);
        String beginTime = TimeManager.toIso8601(c, timeZoneId);

		assertEquals("beginning times align", beginTime, results.get("begin_time"));
		assertEquals("ending times align", endTime, results.get("end_time"));
	}
	
	@Test
	public void getPastDayInterval_ZeroRangeAndZeroOffsetInEasternTime_ReturnsCurrentDayAdjustedForTime() {
		String timeZoneId = "America/New_York";
		Map<String,String> results = TimeManager.getPastDayInterval(0, 0, timeZoneId);
		
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId));
		String endTime = TimeManager.toIso8601(c, timeZoneId);
		
		c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, 0);
        String beginTime = TimeManager.toIso8601(c, timeZoneId);

		assertEquals("beginning times align", beginTime, results.get("begin_time"));
		assertEquals("ending times align", endTime, results.get("end_time"));
	}
}
