package tntfireworks;

public class SyncToDatabaseRequest {
    // file instance variables
    private String originalFilename;
    private String processingFilename;
    private String processingFullPath;
    private String archiveFullPath;

    // sftp instance variables
    private String sftpHost;
    private int sftpPort;
    private String sftpUser;
    private String sftpPassword;

    public SyncToDatabaseRequest(String originalFilename, String processingFilename, String processingFullPath,
            String archiveFullPath) {
        this.originalFilename = originalFilename;
        this.processingFilename = processingFilename;
        this.processingFullPath = processingFullPath;
        this.archiveFullPath = archiveFullPath;
    }

    public void setSftpHost(String sftpHost) {
        this.sftpHost = sftpHost;
    }

    public String getSftpHost() {
        return sftpHost;
    }

    public void setSftpPort(int sftpPort) {
        this.sftpPort = sftpPort;
    }

    public int getSftpPort() {
        return sftpPort;
    }

    public void setSftpUser(String sftpUser) {
        this.sftpUser = sftpUser;
    }

    public String getSftpUser() {
        return sftpUser;
    }

    public void setSftpPassword(String sftpPassword) {
        this.sftpPassword = sftpPassword;
    }

    public String getSftpPassword() {
        return sftpPassword;
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