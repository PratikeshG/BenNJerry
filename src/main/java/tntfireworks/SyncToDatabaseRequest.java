package tntfireworks;

public class SyncToDatabaseRequest {
    private String originalFilename;
    private String processingFilename;

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getProcessingFilename() {
        return processingFilename;
    }

    public void setProcessingFilename(String processingFilename) {
        this.processingFilename = processingFilename;
    }
    
}