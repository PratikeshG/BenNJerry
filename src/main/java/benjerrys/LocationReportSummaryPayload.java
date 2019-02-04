package benjerrys;

public class LocationReportSummaryPayload {
    private String locationName;
    private int totalTransactions;
    private int totalRecipients;
    private boolean processed;

    public LocationReportSummaryPayload(String locationName) {
        this.setLocationName(locationName);
        this.setTotalTransactions(0);
        this.setTotalRecipients(0);
        this.isProcessed(false);
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public int getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(int totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public int getTotalRecipients() {
        return totalRecipients;
    }

    public void setTotalRecipients(int totalRecipients) {
        this.totalRecipients = totalRecipients;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void isProcessed(boolean processed) {
        this.processed = processed;
    }
}
