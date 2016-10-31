package vfcorp;

public class PLUDatabaseToSquareRequest {
    private boolean processingPluFile;
    private String processingFileName;
    private VFCDeployment deployment;

    public boolean isProcessingPluFile() {
        return processingPluFile;
    }

    public void setProcessingPluFile(boolean processingPluFile) {
        this.processingPluFile = processingPluFile;
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
