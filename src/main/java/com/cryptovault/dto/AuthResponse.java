package com.cryptovault.dto;

public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String userId;
    private String email;
    private String name;
    private String role;
    private boolean twoFactorRequired;
    private boolean twoFactorEnabled;

    public AuthResponse() {}

    public static AuthResponse ofTokens(String accessToken, String refreshToken,
                                        String userId, String email, String name, String role) {
        AuthResponse response = new AuthResponse();
        response.accessToken = accessToken;
        response.refreshToken = refreshToken;
        response.userId = userId;
        response.email = email;
        response.name = name;
        response.role = role;
        response.twoFactorRequired = false;
        return response;
    }

    public static AuthResponse twoFactorRequired(String userId) {
        AuthResponse response = new AuthResponse();
        response.userId = userId;
        response.twoFactorRequired = true;
        return response;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isTwoFactorRequired() { return twoFactorRequired; }
    public void setTwoFactorRequired(boolean twoFactorRequired) { this.twoFactorRequired = twoFactorRequired; }

    public boolean isTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }
}
