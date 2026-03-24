package com.cryptovault.controller;

import com.cryptovault.dto.ApiResponse;
import com.cryptovault.model.FileMetadata;
import com.cryptovault.service.AuditService;
import com.cryptovault.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;
    private final AuditService auditService;

    public FileController(FileService fileService, AuditService auditService) {
        this.fileService = fileService;
        this.auditService = auditService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileMetadata>> uploadFile(
            @AuthenticationPrincipal String userId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        try {
            FileMetadata metadata = fileService.uploadFile(userId, file);
            auditService.log(userId, "FILE_UPLOAD", "file", metadata.getId(), request.getRemoteAddr());
            return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", metadata));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Upload failed: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FileMetadata>>> listFiles(
            @AuthenticationPrincipal String userId) {
        try {
            List<FileMetadata> files = fileService.listUserFiles(userId);
            return ResponseEntity.ok(ApiResponse.success("Files retrieved", files));
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to list files"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FileMetadata>> getFileDetails(
            @AuthenticationPrincipal String userId,
            @PathVariable String id) {
        try {
            FileMetadata metadata = fileService.getFileMetadata(id);
            if (metadata == null) return ResponseEntity.notFound().build();
            if (!metadata.getUserId().equals(userId)) {
                return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
            }
            return ResponseEntity.ok(ApiResponse.success("File details", metadata));
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to get file"));
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadFile(
            @AuthenticationPrincipal String userId,
            @PathVariable String id,
            HttpServletRequest request) {
        try {
            FileMetadata metadata = fileService.getFileMetadata(id);
            if (metadata == null) return ResponseEntity.notFound().build();

            byte[] decryptedBytes = fileService.downloadFile(id, userId);
            auditService.log(userId, "FILE_DOWNLOAD", "file", id, request.getRemoteAddr());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", metadata.getFilename());
            return ResponseEntity.ok().headers(headers).body(decryptedBytes);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @AuthenticationPrincipal String userId,
            @PathVariable String id,
            HttpServletRequest request) {
        try {
            fileService.deleteFile(id, userId);
            auditService.log(userId, "FILE_DELETE", "file", id, request.getRemoteAddr());
            return ResponseEntity.ok(ApiResponse.success("File deleted", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body(ApiResponse.error("Delete failed"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FileMetadata>> renameFile(
            @AuthenticationPrincipal String userId,
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        try {
            String newName = body.get("filename");
            if (newName == null || newName.isBlank()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Filename is required"));
            }
            FileMetadata updated = fileService.renameFile(id, userId, newName);
            return ResponseEntity.ok(ApiResponse.success("File renamed", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body(ApiResponse.error("Rename failed"));
        }
    }
}
