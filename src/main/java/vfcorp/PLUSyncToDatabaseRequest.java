package vfcorp;

public class PLUSyncToDatabaseRequest {
    private String originalFileName;
    private String processingFileName;
    private VFCDeployment deployment;

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

    public VFCDeployment getDeployment() {
        return deployment;
    }

    public void setDeployment(VFCDeployment deployment) {
        this.deployment = deployment;
    }
}
