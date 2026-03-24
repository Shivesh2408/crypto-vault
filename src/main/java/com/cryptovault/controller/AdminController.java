package com.cryptovault.controller;

import com.cryptovault.dto.ApiResponse;
import com.cryptovault.model.AuditLog;
import com.cryptovault.model.SecurityAlert;
import com.cryptovault.model.User;
import com.cryptovault.service.AdminService;
import com.cryptovault.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final AuditService auditService;

    public AdminController(AdminService adminService, AuditService auditService) {
        this.adminService = adminService;
        this.auditService = auditService;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getUsers() {
        try {
            List<User> users = adminService.getAllUsers();
            return ResponseEntity.ok(ApiResponse.success("Users retrieved", users));
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to get users"));
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<User>> updateUserRole(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        try {
            String role = body.get("role");
            if (role == null || role.isBlank()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Role is required"));
            }
            User updated = adminService.updateUserRole(id, role);
            return ResponseEntity.ok(ApiResponse.success("User role updated", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body(ApiResponse.error("Update failed"));
        }
    }

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAnalytics() {
        try {
            Map<String, Object> analytics = adminService.getAnalytics();
            return ResponseEntity.ok(ApiResponse.success("Analytics retrieved", analytics));
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to get analytics"));
        }
    }

    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getLogs(
            @RequestParam(defaultValue = "100") int limit) {
        try {
            List<AuditLog> logs = auditService.getLogs(limit);
            return ResponseEntity.ok(ApiResponse.success("Logs retrieved", logs));
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to get logs"));
        }
    }

    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<SecurityAlert>>> getAlerts() {
        try {
            List<SecurityAlert> alerts = adminService.getAlerts();
            return ResponseEntity.ok(ApiResponse.success("Alerts retrieved", alerts));
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to get alerts"));
        }
    }
}
