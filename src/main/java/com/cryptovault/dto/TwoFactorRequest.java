package com.cryptovault.dto;

import jakarta.validation.constraints.NotBlank;

public class TwoFactorRequest {

    @NotBlank(message = "TOTP code is required")
    private String totpCode;

    private String userId;

    public String getTotpCode() { return totpCode; }
    public void setTotpCode(String totpCode) { this.totpCode = totpCode; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
