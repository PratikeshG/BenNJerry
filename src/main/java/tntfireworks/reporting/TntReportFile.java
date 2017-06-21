package tntfireworks.reporting;

import java.text.NumberFormat;
import java.util.Locale;

public abstract class TntReportFile {
    private String fileDate;
    private String fileHeader;

    public TntReportFile(String fileDate, String fileHeader) {
        this.fileDate = fileDate;
        this.fileHeader = fileHeader;
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
