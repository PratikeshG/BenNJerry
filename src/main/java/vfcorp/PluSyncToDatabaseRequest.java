package vfcorp;

public class PluSyncToDatabaseRequest {
    private String originalFileName;
    private String processingFileName;
    private VfcDeployment deployment;

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getProcessingFileName() {
        return processingFileName;
    }

    public void setProcessingFileName(String processingFileName) {
        this.processingFileName = processingFileName;
    }

    public VfcDeployment getDeployment() {
        return deployment;
    }

    public void setDeployment(VfcDeployment deployment) {
        this.deployment = deployment;
    }
}