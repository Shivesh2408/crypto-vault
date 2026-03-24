package com.cryptovault.model;

import java.time.Instant;

public class SecurityAlert {

    private String id;
    private String userId;
    private String type;
    private String description;
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL
    private boolean resolved;
    private Instant timestamp;

    public SecurityAlert() {}

    public SecurityAlert(String id, String userId, String type, String description, String severity, Instant timestamp) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.description = description;
        this.severity = severity;
        this.resolved = false;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
