package com.cryptovault.service;

import com.cryptovault.model.Share;
import com.google.cloud.firestore.Firestore;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class ShareService {

    private static final String COLLECTION = "shares";

    private final Firestore firestore;
    private final PasswordEncoder passwordEncoder;

    public ShareService(Firestore firestore, PasswordEncoder passwordEncoder) {
        this.firestore = firestore;
        this.passwordEncoder = passwordEncoder;
    }

    public Share createShare(String fileId, String userId, long expiresInHours,
                             int downloadLimit, String password, String sharedWithEmail)
            throws ExecutionException, InterruptedException {
        String shareId = UUID.randomUUID().toString();
        String token = UUID.randomUUID().toString().replace("-", "");
        Instant expiresAt = Instant.now().plusSeconds(expiresInHours * 3600);

        Share share = new Share(shareId, fileId, userId, token, expiresAt, downloadLimit, Instant.now());

        if (password != null && !password.isBlank()) {
            share.setHashedPassword(passwordEncoder.encode(password));
        }
        if (sharedWithEmail != null && !sharedWithEmail.isBlank()) {
            share.setSharedWithEmail(sharedWithEmail);
        }

        firestore.collection(COLLECTION).document(shareId).set(toMap(share));
        return share;
    }

    public Share getShareByToken(String token) throws ExecutionException, InterruptedException {
        var docs = firestore.collection(COLLECTION)
                .whereEqualTo("token", token)
                .limit(1)
                .get()
                .get()
                .getDocuments();
        if (docs.isEmpty()) return null;
        return fromMap(docs.get(0).getData());
    }

    public boolean validateShare(Share share, String password) {
        if (share.isRevoked()) return false;
        if (Instant.now().isAfter(share.getExpiresAt())) return false;
        if (share.getDownloadLimit() > 0 && share.getDownloadCount() >= share.getDownloadLimit()) return false;
        if (share.getHashedPassword() != null && !passwordEncoder.matches(password, share.getHashedPassword())) return false;
        return true;
    }

    public void incrementDownloadCount(String shareId) throws ExecutionException, InterruptedException {
        var docRef = firestore.collection(COLLECTION).document(shareId);
        var snapshot = docRef.get().get();
        if (snapshot.exists()) {
            long count = snapshot.getLong("downloadCount") != null ? snapshot.getLong("downloadCount") : 0;
            docRef.update("downloadCount", count + 1);
        }
    }

    public void revokeShare(String shareId, String userId) throws ExecutionException, InterruptedException {
        var doc = firestore.collection(COLLECTION).document(shareId).get().get();
        if (!doc.exists()) throw new IllegalArgumentException("Share not found");
        Share share = fromMap(doc.getData());
        if (!share.getUserId().equals(userId)) throw new SecurityException("Access denied");
        firestore.collection(COLLECTION).document(shareId).update("revoked", true);
    }

    public List<Share> getUserShares(String userId) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.cloud.firestore.Query.Direction.DESCENDING)
                .get()
                .get()
                .getDocuments()
                .stream()
                .map(doc -> fromMap(doc.getData()))
                .toList();
    }

    private Map<String, Object> toMap(Share share) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", share.getId());
        map.put("fileId", share.getFileId());
        map.put("userId", share.getUserId());
        map.put("token", share.getToken());
        map.put("expiresAt", share.getExpiresAt().toString());
        map.put("downloadLimit", share.getDownloadLimit());
        map.put("downloadCount", share.getDownloadCount());
        map.put("hashedPassword", share.getHashedPassword());
        map.put("sharedWithEmail", share.getSharedWithEmail());
        map.put("revoked", share.isRevoked());
        map.put("createdAt", share.getCreatedAt().toString());
        return map;
    }

    private Share fromMap(Map<String, Object> map) {
        Share share = new Share();
        share.setId((String) map.get("id"));
        share.setFileId((String) map.get("fileId"));
        share.setUserId((String) map.get("userId"));
        share.setToken((String) map.get("token"));
        if (map.get("expiresAt") != null) share.setExpiresAt(Instant.parse((String) map.get("expiresAt")));
        if (map.get("downloadLimit") != null) share.setDownloadLimit(((Long) map.get("downloadLimit")).intValue());
        if (map.get("downloadCount") != null) share.setDownloadCount(((Long) map.get("downloadCount")).intValue());
        share.setHashedPassword((String) map.get("hashedPassword"));
        share.setSharedWithEmail((String) map.get("sharedWithEmail"));
        if (map.get("revoked") != null) share.setRevoked((Boolean) map.get("revoked"));
        if (map.get("createdAt") != null) share.setCreatedAt(Instant.parse((String) map.get("createdAt")));
        return share;
    }
}
