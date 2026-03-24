package com.cryptovault.model;

import java.time.Instant;

public class AuditLog {

    private String id;
    private String userId;
    private String action;
    private String resource;
    private String resourceId;
    private String ipAddress;
    private String userAgent;
    private Instant timestamp;

    public AuditLog() {}

    public AuditLog(String id, String userId, String action, String resource,
                    String resourceId, String ipAddress, Instant timestamp) {
        this.id = id;
        this.userId = userId;
        this.action = action;
        this.resource = resource;
        this.resourceId = resourceId;
        this.ipAddress = ipAddress;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
