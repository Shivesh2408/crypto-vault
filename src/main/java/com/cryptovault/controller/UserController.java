package com.cryptovault.controller;

import com.cryptovault.dto.ApiResponse;
import com.cryptovault.model.AuditLog;
import com.cryptovault.model.User;
import com.cryptovault.service.AuditService;
import com.cryptovault.service.AuthService;
import com.cryptovault.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final AuthService authService;
    private final FileService fileService;
    private final AuditService auditService;

    public UserController(AuthService authService, FileService fileService, AuditService auditService) {
        this.authService = authService;
        this.fileService = fileService;
        this.auditService = auditService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<User>> getProfile(@AuthenticationPrincipal String userId) {
        try {
            User user = authService.findUserById(userId);
            if (user == null) return ResponseEntity.notFound().build();
            user.setHashedPassword(null);
            user.setTwoFactorSecret(null);
            user.setRefreshTokens(null);
            return ResponseEntity.ok(ApiResponse.success("Profile retrieved", user));
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to get profile"));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            @AuthenticationPrincipal String userId,
            @RequestBody Map<String, String> body) {
        try {
            String name = body.get("name");
            if (name == null || name.isBlank()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Name is required"));
            }
            authService.updateUserProfile(userId, name);
            return ResponseEntity.ok(ApiResponse.success("Profile updated", null));
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body(ApiResponse.error("Update failed"));
        }
    }

    @GetMapping("/activity")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getActivity(@AuthenticationPrincipal String userId) {
        try {
            List<AuditLog> logs = auditService.getLogsByUser(userId);
            return ResponseEntity.ok(ApiResponse.success("Activity retrieved", logs));
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to get activity"));
        }
    }

    @GetMapping("/storage")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStorageUsage(
            @AuthenticationPrincipal String userId) {
        try {
            long usageBytes = fileService.getUserStorageUsage(userId);
            Map<String, Object> result = new HashMap<>();
            result.put("usageBytes", usageBytes);
            result.put("usageMB", usageBytes / (1024.0 * 1024.0));
            return ResponseEntity.ok(ApiResponse.success("Storage usage", result));
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to get storage usage"));
        }
    }
}
