package com.cryptovault.service;

import com.cryptovault.model.FileMetadata;
import com.cryptovault.security.EncryptionUtil;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.firebase.cloud.StorageClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class FileService {

    private static final String COLLECTION = "files";

    private final Firestore firestore;
    private final StorageClient storageClient;
    private final EncryptionUtil encryptionUtil;

    public FileService(Firestore firestore, StorageClient storageClient, EncryptionUtil encryptionUtil) {
        this.firestore = firestore;
        this.storageClient = storageClient;
        this.encryptionUtil = encryptionUtil;
    }

    public FileMetadata uploadFile(String userId, MultipartFile file)
            throws IOException, GeneralSecurityException, ExecutionException, InterruptedException {
        byte[] fileBytes = file.getBytes();

        String fileHash = encryptionUtil.computeSha256Hash(fileBytes);
        SecretKey aesKey = encryptionUtil.generateAesKey();
        byte[] encryptedBytes = encryptionUtil.encryptWithAes(fileBytes, aesKey);
        String encryptedKey = encryptionUtil.encodeToBase64(aesKey.getEncoded());

        String fileId = UUID.randomUUID().toString();
        String storagePath = "files/" + userId + "/" + fileId;

        BlobId blobId = BlobId.of(storageClient.bucket().getName(), storagePath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("application/octet-stream")
                .build();
        storageClient.bucket().create(storagePath, encryptedBytes, "application/octet-stream");

        FileMetadata metadata = new FileMetadata(
                fileId, userId, file.getOriginalFilename(), file.getOriginalFilename(),
                file.getSize(), file.getContentType(), encryptedKey, fileHash, storagePath, Instant.now()
        );

        firestore.collection(COLLECTION).document(fileId).set(toMap(metadata));

        return metadata;
    }

    public byte[] downloadFile(String fileId, String userId)
            throws ExecutionException, InterruptedException, IOException, GeneralSecurityException {
        FileMetadata metadata = getFileMetadata(fileId);
        if (metadata == null) throw new IllegalArgumentException("File not found");
        if (!metadata.getUserId().equals(userId)) throw new SecurityException("Access denied");

        byte[] encryptedBytes = storageClient.bucket().get(metadata.getStoragePath())
                .getContent();

        byte[] aesKeyBytes = encryptionUtil.decodeFromBase64(metadata.getEncryptedKey());
        SecretKey aesKey = new javax.crypto.spec.SecretKeySpec(aesKeyBytes, "AES");
        return encryptionUtil.decryptWithAes(encryptedBytes, aesKey);
    }

    public byte[] downloadFileByShare(FileMetadata metadata)
            throws IOException, GeneralSecurityException {
        byte[] encryptedBytes = storageClient.bucket().get(metadata.getStoragePath()).getContent();
        byte[] aesKeyBytes = encryptionUtil.decodeFromBase64(metadata.getEncryptedKey());
        SecretKey aesKey = new javax.crypto.spec.SecretKeySpec(aesKeyBytes, "AES");
        return encryptionUtil.decryptWithAes(encryptedBytes, aesKey);
    }

    public List<FileMetadata> listUserFiles(String userId) throws ExecutionException, InterruptedException {
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

    public FileMetadata getFileMetadata(String fileId) throws ExecutionException, InterruptedException {
        var doc = firestore.collection(COLLECTION).document(fileId).get().get();
        if (!doc.exists()) return null;
        return fromMap(doc.getData());
    }

    public void deleteFile(String fileId, String userId) throws ExecutionException, InterruptedException {
        FileMetadata metadata = getFileMetadata(fileId);
        if (metadata == null) throw new IllegalArgumentException("File not found");
        if (!metadata.getUserId().equals(userId)) throw new SecurityException("Access denied");

        storageClient.bucket().get(metadata.getStoragePath()).delete();
        firestore.collection(COLLECTION).document(fileId).delete();
    }

    public FileMetadata renameFile(String fileId, String userId, String newName)
            throws ExecutionException, InterruptedException {
        FileMetadata metadata = getFileMetadata(fileId);
        if (metadata == null) throw new IllegalArgumentException("File not found");
        if (!metadata.getUserId().equals(userId)) throw new SecurityException("Access denied");

        firestore.collection(COLLECTION).document(fileId)
                .update("filename", newName, "updatedAt", Instant.now().toString());
        metadata.setFilename(newName);
        return metadata;
    }

    public long getUserStorageUsage(String userId) throws ExecutionException, InterruptedException {
        return listUserFiles(userId).stream()
                .mapToLong(FileMetadata::getSize)
                .sum();
    }

    private Map<String, Object> toMap(FileMetadata metadata) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", metadata.getId());
        map.put("userId", metadata.getUserId());
        map.put("filename", metadata.getFilename());
        map.put("originalFilename", metadata.getOriginalFilename());
        map.put("size", metadata.getSize());
        map.put("mimeType", metadata.getMimeType());
        map.put("encryptedKey", metadata.getEncryptedKey());
        map.put("fileHash", metadata.getFileHash());
        map.put("storagePath", metadata.getStoragePath());
        map.put("version", metadata.getVersion());
        map.put("createdAt", metadata.getCreatedAt().toString());
        map.put("updatedAt", metadata.getUpdatedAt().toString());
        return map;
    }

    private FileMetadata fromMap(Map<String, Object> map) {
        FileMetadata metadata = new FileMetadata();
        metadata.setId((String) map.get("id"));
        metadata.setUserId((String) map.get("userId"));
        metadata.setFilename((String) map.get("filename"));
        metadata.setOriginalFilename((String) map.get("originalFilename"));
        if (map.get("size") != null) metadata.setSize((Long) map.get("size"));
        metadata.setMimeType((String) map.get("mimeType"));
        metadata.setEncryptedKey((String) map.get("encryptedKey"));
        metadata.setFileHash((String) map.get("fileHash"));
        metadata.setStoragePath((String) map.get("storagePath"));
        if (map.get("version") != null) metadata.setVersion(((Long) map.get("version")).intValue());
        if (map.get("createdAt") != null) metadata.setCreatedAt(Instant.parse((String) map.get("createdAt")));
        if (map.get("updatedAt") != null) metadata.setUpdatedAt(Instant.parse((String) map.get("updatedAt")));
        return metadata;
    }
}
