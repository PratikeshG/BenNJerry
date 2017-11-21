package tntfireworks.reporting;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import util.TimeManager;

public abstract class TntReportFile {
	private static final String DEFAULT_DATE_FORMAT = "MM-dd-yy";
    private String fileDate;
    private String fileHeader;

    public TntReportFile(String timeZone, String dateFormat, String fileHeader) {
    	this.fileHeader = fileHeader;
    	this.fileDate = setFileDate(timeZone, dateFormat);
    }

    public TntReportFile(String timeZone, String fileHeader) {
    	this(timeZone, DEFAULT_DATE_FORMAT, fileHeader);
    }

    public String setFileDate(String timeZone, String dateFormat) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        String fileDate;
		try {
			fileDate = TimeManager.toSimpleDateTimeInTimeZone(cal, timeZone, dateFormat);
		} catch (ParseException e) {
			e.printStackTrace();
			throw new RuntimeException ("Invalid timeZoneId provided to TimeManager.toSimpleDateTimeInTimeZone" + e);
		}

        return fileDate;
    }

    public String getFileDate() {
        return fileDate;
    }

    public String getFileHeader() {
        return fileHeader;
    }

    protected String formatTotal(int gpv) {
        NumberFormat n = NumberFormat.getCurrencyInstance(Locale.US);
        return n.format(gpv / 100.0).replaceAll(",", "");
    }
}
