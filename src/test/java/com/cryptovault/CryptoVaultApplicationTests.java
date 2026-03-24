package com.cryptovault;

import com.cryptovault.security.EncryptionUtil;
import com.cryptovault.security.TotpUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.*;

class CryptoVaultApplicationTests {

    private EncryptionUtil encryptionUtil;
    private TotpUtil totpUtil;

    @BeforeEach
    void setUp() {
        encryptionUtil = new EncryptionUtil();
        totpUtil = new TotpUtil();
    }

    @Test
    void testAes256EncryptionRoundTrip() throws Exception {
        byte[] plaintext = "Hello, Crypto-Vault! AES-256 encryption test.".getBytes();
        SecretKey key = encryptionUtil.generateAesKey();
        byte[] encrypted = encryptionUtil.encryptWithAes(plaintext, key);
        assertNotNull(encrypted);
        assertFalse(java.util.Arrays.equals(plaintext, encrypted));
        byte[] decrypted = encryptionUtil.decryptWithAes(encrypted, key);
        assertArrayEquals(plaintext, decrypted);
    }

    @Test
    void testRsaKeyEncryption() throws Exception {
        var keyPair = encryptionUtil.generateRsaKeyPair();
        assertNotNull(keyPair.getPublic());
        assertNotNull(keyPair.getPrivate());

        SecretKey aesKey = encryptionUtil.generateAesKey();
        byte[] encryptedKey = encryptionUtil.encryptKeyWithRsa(aesKey, keyPair.getPublic());
        SecretKey decryptedKey = encryptionUtil.decryptKeyWithRsa(encryptedKey, keyPair.getPrivate());
        assertArrayEquals(aesKey.getEncoded(), decryptedKey.getEncoded());
    }

    @Test
    void testSha256HashIsDeterministic() throws Exception {
        byte[] data = "test data for hashing".getBytes();
        String hash1 = encryptionUtil.computeSha256Hash(data);
        String hash2 = encryptionUtil.computeSha256Hash(data);
        assertNotNull(hash1);
        assertEquals(hash1, hash2);
    }

    @Test
    void testBase64EncodeDecode() {
        byte[] original = "test bytes for base64".getBytes();
        String encoded = encryptionUtil.encodeToBase64(original);
        assertNotNull(encoded);
        byte[] decoded = encryptionUtil.decodeFromBase64(encoded);
        assertArrayEquals(original, decoded);
    }

    @Test
    void testTotpSecretGeneration() {
        String secret1 = totpUtil.generateSecret();
        String secret2 = totpUtil.generateSecret();
        assertNotNull(secret1);
        assertFalse(secret1.isBlank());
        assertNotEquals(secret1, secret2, "Each secret should be unique");
    }

    @Test
    void testTotpQrCodeUrl() {
        String secret = totpUtil.generateSecret();
        String qrUrl = totpUtil.getQrCodeUrl(secret, "test@example.com", "CryptoVault");
        assertTrue(qrUrl.startsWith("otpauth://totp/"));
        assertTrue(qrUrl.contains(secret));
        assertTrue(qrUrl.contains("CryptoVault"));
    }
}
