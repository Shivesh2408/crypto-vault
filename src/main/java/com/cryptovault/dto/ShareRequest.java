package com.cryptovault.dto;

public class ShareRequest {

    private String fileId;
    private long expiresInHours;
    private int downloadLimit;
    private String password;
    private String sharedWithEmail;

    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }

    public long getExpiresInHours() { return expiresInHours; }
    public void setExpiresInHours(long expiresInHours) { this.expiresInHours = expiresInHours; }

    public int getDownloadLimit() { return downloadLimit; }
    public void setDownloadLimit(int downloadLimit) { this.downloadLimit = downloadLimit; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getSharedWithEmail() { return sharedWithEmail; }
    public void setSharedWithEmail(String sharedWithEmail) { this.sharedWithEmail = sharedWithEmail; }
}
