package tntfireworks;

public class DatabaseToSquareRequest {
    private Boolean processingFlag;
    private String processingFilename;
    private String processingFullPath;
    private String archiveFullPath;

    public boolean isProcessing() {
        return processingFlag;
    }

    public void setProcessingFlag(Boolean processingFlag) {
        this.processingFlag = processingFlag;
    }

    public String getProcessingFilename() {
        return processingFilename;
    }

    public void setProcessingFilename(String processingFilename) {
        this.processingFilename = processingFilename;
    }

    public void setProcessingPath(String processingFullPath) {
        this.processingFullPath = processingFullPath;
    }

    public String getProcessingPath() {
        return processingFullPath;
    }

    public void setArchivePath(String archiveFullPath) {
        this.archiveFullPath = archiveFullPath;
    }

    public String getArchivePath() {
        return archiveFullPath;
    }
}
