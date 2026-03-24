package com.cryptovault.controller;

import com.cryptovault.dto.*;
import com.cryptovault.service.AuditService;
import com.cryptovault.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuditService auditService;

    public AuthController(AuthService authService, AuditService auditService) {
        this.authService = authService;
        this.auditService = auditService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        try {
            AuthResponse response = authService.register(request);
            auditService.log(response.getUserId(), "REGISTER", "auth", null,
                    httpRequest.getRemoteAddr());
            return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body(ApiResponse.error("Registration failed"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        try {
            AuthResponse response = authService.login(request);
            if (response.getUserId() != null) {
                auditService.log(response.getUserId(), "LOGIN", "auth", null,
                        httpRequest.getRemoteAddr());
            }
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body(ApiResponse.error("Login failed"));
        }
    }

    @PostMapping("/setup-2fa")
    public ResponseEntity<ApiResponse<Map<String, String>>> setupTwoFactor(
            @AuthenticationPrincipal String userId,
            HttpServletRequest httpRequest) {
        try {
            Map<String, String> result = authService.setupTwoFactor(userId);
            auditService.log(userId, "SETUP_2FA", "auth", null, httpRequest.getRemoteAddr());
            return ResponseEntity.ok(ApiResponse.success("2FA setup initiated", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<ApiResponse<Void>> verifyTwoFactor(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody TwoFactorRequest request,
            HttpServletRequest httpRequest) {
        try {
            authService.verifyAndEnableTwoFactor(userId, request.getTotpCode());
            auditService.log(userId, "VERIFY_2FA", "auth", null, httpRequest.getRemoteAddr());
            return ResponseEntity.ok(ApiResponse.success("2FA enabled successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("2FA verification failed"));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        try {
            AuthResponse response = authService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Token refresh failed"));
        }
    }
}
