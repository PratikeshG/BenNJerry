package util;

public class SequentialRecord {
    private String locationId;
    private String recordId;
    private String deviceId;
    private int recordNumber;
    private String recordCreatedAt;

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getRecordNumber() {
        return recordNumber;
    }

    public void setRecordNumber(int recordNumber) {
        this.recordNumber = recordNumber;
    }

    public String getRecordCreatedAt() {
        return recordCreatedAt;
    }

    public void setRecordCreatedAt(String recordCreatedAt) {
        this.recordCreatedAt = recordCreatedAt;
    }
}
