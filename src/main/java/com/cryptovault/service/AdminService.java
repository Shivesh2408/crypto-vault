package com.cryptovault.service;

import com.cryptovault.model.SecurityAlert;
import com.cryptovault.model.User;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class AdminService {

    private static final String ALERTS_COLLECTION = "security_alerts";

    private final Firestore firestore;
    private final AuthService authService;
    private final FileService fileService;
    private final AuditService auditService;

    public AdminService(Firestore firestore, AuthService authService,
                        FileService fileService, AuditService auditService) {
        this.firestore = firestore;
        this.authService = authService;
        this.fileService = fileService;
        this.auditService = auditService;
    }

    public List<User> getAllUsers() throws ExecutionException, InterruptedException {
        return authService.listAllUsers();
    }

    public User updateUserRole(String userId, String newRole) throws ExecutionException, InterruptedException {
        List<String> validRoles = List.of("ADMIN", "USER", "AUDITOR");
        if (!validRoles.contains(newRole)) throw new IllegalArgumentException("Invalid role");

        firestore.collection("users").document(userId)
                .update("role", newRole, "updatedAt", Instant.now().toString());
        return authService.findUserById(userId);
    }

    public Map<String, Object> getAnalytics() throws ExecutionException, InterruptedException {
        List<User> users = getAllUsers();
        long totalFiles = firestore.collection("files").get().get().size();
        long totalShares = firestore.collection("shares").get().get().size();

        long totalStorage = users.stream()
                .mapToLong(u -> {
                    try {
                        return fileService.getUserStorageUsage(u.getId());
                    } catch (Exception e) {
                        return 0L;
                    }
                })
                .sum();

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalUsers", users.size());
        analytics.put("totalFiles", totalFiles);
        analytics.put("totalShares", totalShares);
        analytics.put("totalStorageBytes", totalStorage);
        analytics.put("timestamp", Instant.now().toString());
        return analytics;
    }

    public List<SecurityAlert> getAlerts() throws ExecutionException, InterruptedException {
        return firestore.collection(ALERTS_COLLECTION)
                .orderBy("timestamp", com.google.cloud.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .get()
                .getDocuments()
                .stream()
                .map(doc -> fromAlertMap(doc.getData()))
                .toList();
    }

    public void createAlert(String userId, String type, String description, String severity) {
        String alertId = UUID.randomUUID().toString();
        SecurityAlert alert = new SecurityAlert(alertId, userId, type, description, severity, Instant.now());
        firestore.collection(ALERTS_COLLECTION).document(alertId).set(toAlertMap(alert));
    }

    private Map<String, Object> toAlertMap(SecurityAlert alert) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", alert.getId());
        map.put("userId", alert.getUserId());
        map.put("type", alert.getType());
        map.put("description", alert.getDescription());
        map.put("severity", alert.getSeverity());
        map.put("resolved", alert.isResolved());
        map.put("timestamp", alert.getTimestamp().toString());
        return map;
    }

    private SecurityAlert fromAlertMap(Map<String, Object> map) {
        SecurityAlert alert = new SecurityAlert();
        alert.setId((String) map.get("id"));
        alert.setUserId((String) map.get("userId"));
        alert.setType((String) map.get("type"));
        alert.setDescription((String) map.get("description"));
        alert.setSeverity((String) map.get("severity"));
        if (map.get("resolved") != null) alert.setResolved((Boolean) map.get("resolved"));
        if (map.get("timestamp") != null) alert.setTimestamp(Instant.parse((String) map.get("timestamp")));
        return alert;
    }
}
