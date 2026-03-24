package com.cryptovault.model;

import java.time.Instant;

public class FileMetadata {

    private String id;
    private String userId;
    private String filename;
    private String originalFilename;
    private long size;
    private String mimeType;
    private String encryptedKey;
    private String fileHash;
    private String storagePath;
    private int version;
    private Instant createdAt;
    private Instant updatedAt;

    public FileMetadata() {}

    public FileMetadata(String id, String userId, String filename, String originalFilename,
                        long size, String mimeType, String encryptedKey, String fileHash,
                        String storagePath, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.filename = filename;
        this.originalFilename = originalFilename;
        this.size = size;
        this.mimeType = mimeType;
        this.encryptedKey = encryptedKey;
        this.fileHash = fileHash;
        this.storagePath = storagePath;
        this.version = 1;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getEncryptedKey() { return encryptedKey; }
    public void setEncryptedKey(String encryptedKey) { this.encryptedKey = encryptedKey; }

    public String getFileHash() { return fileHash; }
    public void setFileHash(String fileHash) { this.fileHash = fileHash; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
