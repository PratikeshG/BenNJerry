package tntfireworks;

public class SyncToDatabaseRequest {
    private String originalFilename;
    private String processingFilename;
    private String processingFullPath;

    public SyncToDatabaseRequest (String originalFilename, String processingFilename, String processingFullPath) {        
        this.originalFilename = originalFilename;
        this.processingFilename = processingFilename;
        this.processingFullPath = processingFullPath;        
    }
        
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
    
    public void setProcessingPath (String processingFullPath) {
        this.processingFullPath = processingFullPath;
    }
    
    public String getProcessingPath() {
        return processingFullPath;
    }
}