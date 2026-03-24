package com.cryptovault.controller;

import com.cryptovault.dto.ApiResponse;
import com.cryptovault.dto.ShareRequest;
import com.cryptovault.model.FileMetadata;
import com.cryptovault.model.Share;
import com.cryptovault.service.AuditService;
import com.cryptovault.service.FileService;
import com.cryptovault.service.ShareService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/share")
public class ShareController {

    private final ShareService shareService;
    private final FileService fileService;
    private final AuditService auditService;

    public ShareController(ShareService shareService, FileService fileService, AuditService auditService) {
        this.shareService = shareService;
        this.fileService = fileService;
        this.auditService = auditService;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Share>> createShare(
            @AuthenticationPrincipal String userId,
            @RequestBody ShareRequest request,
            HttpServletRequest httpRequest) {
        try {
            long expiresInHours = request.getExpiresInHours() > 0 ? request.getExpiresInHours() : 24;
            Share share = shareService.createShare(
                    request.getFileId(), userId, expiresInHours,
                    request.getDownloadLimit(), request.getPassword(), request.getSharedWithEmail()
            );
            auditService.log(userId, "SHARE_CREATE", "share", share.getId(), httpRequest.getRemoteAddr());
            return ResponseEntity.ok(ApiResponse.success("Share created", share));
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body(ApiResponse.error("Share creation failed"));
        }
    }

    @GetMapping("/{token}")
    public ResponseEntity<?> accessShare(
            @PathVariable String token,
            @RequestParam(required = false) String password,
            HttpServletRequest httpRequest) {
        try {
            Share share = shareService.getShareByToken(token);
            if (share == null) return ResponseEntity.notFound().build();

            if (!shareService.validateShare(share, password)) {
                return ResponseEntity.status(403).body(ApiResponse.error("Share is expired, revoked, or password is wrong"));
            }

            FileMetadata metadata = fileService.getFileMetadata(share.getFileId());
            if (metadata == null) return ResponseEntity.notFound().build();

            byte[] decryptedBytes = fileService.downloadFileByShare(metadata);
            shareService.incrementDownloadCount(share.getId());
            auditService.log(share.getUserId(), "SHARE_DOWNLOAD", "share", share.getId(), httpRequest.getRemoteAddr());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", metadata.getFilename());
            return ResponseEntity.ok().headers(headers).body(decryptedBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to access share"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> revokeShare(
            @AuthenticationPrincipal String userId,
            @PathVariable String id,
            HttpServletRequest httpRequest) {
        try {
            shareService.revokeShare(id, userId);
            auditService.log(userId, "SHARE_REVOKE", "share", id, httpRequest.getRemoteAddr());
            return ResponseEntity.ok(ApiResponse.success("Share revoked", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body(ApiResponse.error("Revoke failed"));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<Share>>> getShareHistory(
            @AuthenticationPrincipal String userId) {
        try {
            List<Share> shares = shareService.getUserShares(userId);
            return ResponseEntity.ok(ApiResponse.success("Share history", shares));
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to get history"));
        }
    }
}
