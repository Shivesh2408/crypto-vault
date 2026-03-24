package com.cryptovault.service;

import com.cryptovault.model.AuditLog;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class AuditService {

    private static final String COLLECTION = "audit_logs";

    private final Firestore firestore;

    public AuditService(Firestore firestore) {
        this.firestore = firestore;
    }

    public void log(String userId, String action, String resource, String resourceId, String ipAddress) {
        AuditLog log = new AuditLog(
                UUID.randomUUID().toString(),
                userId, action, resource, resourceId, ipAddress, Instant.now()
        );
        Map<String, Object> data = toMap(log);
        firestore.collection(COLLECTION).document(log.getId()).set(data);
    }

    public List<AuditLog> getLogs(int limit) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION)
                .orderBy("timestamp", com.google.cloud.firestore.Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .get()
                .getDocuments()
                .stream()
                .map(doc -> fromMap(doc.getData()))
                .collect(Collectors.toList());
    }

    public List<AuditLog> getLogsByUser(String userId) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", com.google.cloud.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .get()
                .getDocuments()
                .stream()
                .map(doc -> fromMap(doc.getData()))
                .collect(Collectors.toList());
    }

    private Map<String, Object> toMap(AuditLog log) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", log.getId());
        map.put("userId", log.getUserId());
        map.put("action", log.getAction());
        map.put("resource", log.getResource());
        map.put("resourceId", log.getResourceId());
        map.put("ipAddress", log.getIpAddress());
        map.put("timestamp", log.getTimestamp().toString());
        return map;
    }

    private AuditLog fromMap(Map<String, Object> map) {
        AuditLog log = new AuditLog();
        log.setId((String) map.get("id"));
        log.setUserId((String) map.get("userId"));
        log.setAction((String) map.get("action"));
        log.setResource((String) map.get("resource"));
        log.setResourceId((String) map.get("resourceId"));
        log.setIpAddress((String) map.get("ipAddress"));
        if (map.get("timestamp") != null) {
            log.setTimestamp(Instant.parse((String) map.get("timestamp")));
        }
        return log;
    }
}
