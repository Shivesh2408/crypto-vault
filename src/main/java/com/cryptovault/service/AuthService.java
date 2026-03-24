package com.cryptovault.service;

import com.cryptovault.dto.AuthResponse;
import com.cryptovault.dto.LoginRequest;
import com.cryptovault.dto.RegisterRequest;
import com.cryptovault.model.User;
import com.cryptovault.security.JwtTokenProvider;
import com.cryptovault.security.TotpUtil;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class AuthService {

    private static final String COLLECTION = "users";

    private final Firestore firestore;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TotpUtil totpUtil;

    public AuthService(Firestore firestore, PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider, TotpUtil totpUtil) {
        this.firestore = firestore;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.totpUtil = totpUtil;
    }

    public AuthResponse register(RegisterRequest request) throws ExecutionException, InterruptedException {
        if (findUserByEmail(request.getEmail()) != null) {
            throw new IllegalArgumentException("Email already in use");
        }

        String userId = UUID.randomUUID().toString();
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(userId, request.getEmail(), request.getName(),
                hashedPassword, "USER", Instant.now());

        firestore.collection(COLLECTION).document(userId).set(toMap(user));

        String accessToken = jwtTokenProvider.generateAccessToken(userId, user.getEmail(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

        storeRefreshToken(userId, refreshToken);

        return AuthResponse.ofTokens(accessToken, refreshToken, userId,
                user.getEmail(), user.getName(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) throws ExecutionException, InterruptedException {
        User user = findUserByEmail(request.getEmail());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getHashedPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        if (user.isTwoFactorEnabled()) {
            if (request.getTotpCode() == null || request.getTotpCode().isBlank()) {
                return AuthResponse.twoFactorRequired(user.getId());
            }
            if (!totpUtil.verifyCode(user.getTwoFactorSecret(), request.getTotpCode())) {
                throw new IllegalArgumentException("Invalid 2FA code");
            }
        }

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        storeRefreshToken(user.getId(), refreshToken);

        AuthResponse response = AuthResponse.ofTokens(accessToken, refreshToken,
                user.getId(), user.getEmail(), user.getName(), user.getRole());
        response.setTwoFactorEnabled(user.isTwoFactorEnabled());
        return response;
    }

    public Map<String, String> setupTwoFactor(String userId) throws ExecutionException, InterruptedException {
        User user = findUserById(userId);
        if (user == null) throw new IllegalArgumentException("User not found");

        String secret = totpUtil.generateSecret();
        String qrUrl = totpUtil.getQrCodeUrl(secret, user.getEmail(), "CryptoVault");

        firestore.collection(COLLECTION).document(userId)
                .update("twoFactorSecret", secret);

        Map<String, String> result = new HashMap<>();
        result.put("secret", secret);
        result.put("qrUrl", qrUrl);
        return result;
    }

    public void verifyAndEnableTwoFactor(String userId, String totpCode)
            throws ExecutionException, InterruptedException {
        User user = findUserById(userId);
        if (user == null) throw new IllegalArgumentException("User not found");
        if (user.getTwoFactorSecret() == null) throw new IllegalStateException("2FA not set up");

        if (!totpUtil.verifyCode(user.getTwoFactorSecret(), totpCode)) {
            throw new IllegalArgumentException("Invalid TOTP code");
        }

        firestore.collection(COLLECTION).document(userId)
                .update("twoFactorEnabled", true);
    }

    public AuthResponse refreshToken(String refreshToken) throws ExecutionException, InterruptedException {
        if (!jwtTokenProvider.isTokenValid(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = findUserById(userId);
        if (user == null) throw new IllegalArgumentException("User not found");

        String newAccessToken = jwtTokenProvider.generateAccessToken(
                userId, user.getEmail(), user.getRole());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);

        storeRefreshToken(userId, newRefreshToken);

        return AuthResponse.ofTokens(newAccessToken, newRefreshToken,
                userId, user.getEmail(), user.getName(), user.getRole());
    }

    public User findUserById(String userId) throws ExecutionException, InterruptedException {
        var doc = firestore.collection(COLLECTION).document(userId).get().get();
        if (!doc.exists()) return null;
        return fromMap(doc.getData());
    }

    public User findUserByEmail(String email) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> docs = firestore.collection(COLLECTION)
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .get()
                .getDocuments();
        if (docs.isEmpty()) return null;
        return fromMap(docs.get(0).getData());
    }

    public void updateUserProfile(String userId, String name) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(userId)
                .update("name", name, "updatedAt", Instant.now().toString());
    }

    public List<User> listAllUsers() throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION).get().get()
                .getDocuments().stream()
                .map(doc -> fromMap(doc.getData()))
                .toList();
    }

    private void storeRefreshToken(String userId, String refreshToken)
            throws ExecutionException, InterruptedException {
        var docRef = firestore.collection(COLLECTION).document(userId);
        var snapshot = docRef.get().get();
        List<String> tokens = new ArrayList<>();
        if (snapshot.exists() && snapshot.get("refreshTokens") != null) {
            @SuppressWarnings("unchecked")
            List<String> existing = (List<String>) snapshot.get("refreshTokens");
            if (existing != null) tokens.addAll(existing);
        }
        tokens.add(refreshToken);
        // Keep only last 5 refresh tokens
        if (tokens.size() > 5) {
            tokens = tokens.subList(tokens.size() - 5, tokens.size());
        }
        docRef.update("refreshTokens", tokens);
    }

    private Map<String, Object> toMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("email", user.getEmail());
        map.put("name", user.getName());
        map.put("hashedPassword", user.getHashedPassword());
        map.put("role", user.getRole());
        map.put("twoFactorEnabled", user.isTwoFactorEnabled());
        map.put("createdAt", user.getCreatedAt().toString());
        map.put("updatedAt", user.getUpdatedAt().toString());
        return map;
    }

    private User fromMap(Map<String, Object> map) {
        User user = new User();
        user.setId((String) map.get("id"));
        user.setEmail((String) map.get("email"));
        user.setName((String) map.get("name"));
        user.setHashedPassword((String) map.get("hashedPassword"));
        user.setRole((String) map.get("role"));
        user.setTwoFactorSecret((String) map.get("twoFactorSecret"));
        if (map.get("twoFactorEnabled") != null) {
            user.setTwoFactorEnabled((Boolean) map.get("twoFactorEnabled"));
        }
        if (map.get("createdAt") != null) {
            user.setCreatedAt(Instant.parse((String) map.get("createdAt")));
        }
        if (map.get("updatedAt") != null) {
            user.setUpdatedAt(Instant.parse((String) map.get("updatedAt")));
        }
        return user;
    }
}
