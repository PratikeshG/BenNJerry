package vfcorp;

import util.SquarePayload;

public class VfcDeployment {
    private String deployment;
    private String storeId;
    private String timeZone;
    private String pluPath;
    private boolean pluFiltered;
    private String tlogPath;
    private int tlogRange;
    private int tlogOffset;
    private SquarePayload squarePayload;

    public String getDeployment() {
        return deployment;
    }

    public void setDeployment(String deployment) {
        this.deployment = deployment;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getPluPath() {
        return pluPath;
    }

    public void setPluPath(String pluPath) {
        this.pluPath = pluPath;
    }

    public boolean isPluFiltered() {
        return pluFiltered;
    }

    public void setPluFiltered(boolean pluFiltered) {
        this.pluFiltered = pluFiltered;
    }

    public String getTlogPath() {
        return tlogPath;
    }

    public void setTlogPath(String tlogPath) {
        this.tlogPath = tlogPath;
    }

    public int getTlogRange() {
        return tlogRange;
    }

    public void setTlogRange(int tlogRange) {
        this.tlogRange = tlogRange;
    }

    public int getTlogOffset() {
        return tlogOffset;
    }

    public void setTlogOffset(int tlogOffset) {
        this.tlogOffset = tlogOffset;
    }

    public SquarePayload getSquarePayload() {
        return squarePayload;
    }

    public void setSquarePayload(SquarePayload squarePayload) {
        this.squarePayload = squarePayload;
    }
}
