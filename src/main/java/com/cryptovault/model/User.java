package com.cryptovault.model;

import java.time.Instant;
import java.util.List;

public class User {

    private String id;
    private String email;
    private String name;
    private String hashedPassword;
    private String role; // ADMIN, USER, AUDITOR
    private String twoFactorSecret;
    private boolean twoFactorEnabled;
    private Instant createdAt;
    private Instant updatedAt;
    private List<String> refreshTokens;

    public User() {}

    public User(String id, String email, String name, String hashedPassword, String role, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.hashedPassword = hashedPassword;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.twoFactorEnabled = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getHashedPassword() { return hashedPassword; }
    public void setHashedPassword(String hashedPassword) { this.hashedPassword = hashedPassword; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getTwoFactorSecret() { return twoFactorSecret; }
    public void setTwoFactorSecret(String twoFactorSecret) { this.twoFactorSecret = twoFactorSecret; }

    public boolean isTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<String> getRefreshTokens() { return refreshTokens; }
    public void setRefreshTokens(List<String> refreshTokens) { this.refreshTokens = refreshTokens; }
}
