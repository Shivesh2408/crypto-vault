package com.cryptovault.model;

import java.time.Instant;

public class Share {

    private String id;
    private String fileId;
    private String userId;
    private String token;
    private Instant expiresAt;
    private int downloadLimit;
    private int downloadCount;
    private String hashedPassword;
    private String sharedWithEmail;
    private boolean revoked;
    private Instant createdAt;

    public Share() {}

    public Share(String id, String fileId, String userId, String token,
                 Instant expiresAt, int downloadLimit, Instant createdAt) {
        this.id = id;
        this.fileId = fileId;
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.downloadLimit = downloadLimit;
        this.downloadCount = 0;
        this.revoked = false;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public int getDownloadLimit() { return downloadLimit; }
    public void setDownloadLimit(int downloadLimit) { this.downloadLimit = downloadLimit; }

    public int getDownloadCount() { return downloadCount; }
    public void setDownloadCount(int downloadCount) { this.downloadCount = downloadCount; }

    public String getHashedPassword() { return hashedPassword; }
    public void setHashedPassword(String hashedPassword) { this.hashedPassword = hashedPassword; }

    public String getSharedWithEmail() { return sharedWithEmail; }
    public void setSharedWithEmail(String sharedWithEmail) { this.sharedWithEmail = sharedWithEmail; }

    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
