package com.cryptovault.security;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Component
public class EncryptionUtil {

    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final String RSA_ALGORITHM = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    public SecretKey generateAesKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(AES_KEY_SIZE, new SecureRandom());
        return keyGen.generateKey();
    }

    public KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048, new SecureRandom());
        return keyPairGen.generateKeyPair();
    }

    public byte[] encryptWithAes(byte[] data, SecretKey key)
            throws GeneralSecurityException {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        byte[] encryptedData = cipher.doFinal(data);

        ByteBuffer buffer = ByteBuffer.allocate(iv.length + encryptedData.length);
        buffer.put(iv);
        buffer.put(encryptedData);
        return buffer.array();
    }

    public byte[] decryptWithAes(byte[] encryptedDataWithIv, SecretKey key)
            throws GeneralSecurityException {
        ByteBuffer buffer = ByteBuffer.wrap(encryptedDataWithIv);
        byte[] iv = new byte[GCM_IV_LENGTH];
        buffer.get(iv);
        byte[] encryptedData = new byte[buffer.remaining()];
        buffer.get(encryptedData);

        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        return cipher.doFinal(encryptedData);
    }

    public byte[] encryptKeyWithRsa(SecretKey aesKey, PublicKey rsaPublicKey)
            throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
        return cipher.doFinal(aesKey.getEncoded());
    }

    public SecretKey decryptKeyWithRsa(byte[] encryptedKey, PrivateKey rsaPrivateKey)
            throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey);
        byte[] decryptedKey = cipher.doFinal(encryptedKey);
        return new SecretKeySpec(decryptedKey, "AES");
    }

    public String computeSha256Hash(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        return Base64.encodeBase64URLSafeString(hash);
    }

    public String encodeToBase64(byte[] data) {
        return Base64.encodeBase64String(data);
    }

    public byte[] decodeFromBase64(String data) {
        return Base64.decodeBase64(data);
    }

    public PublicKey decodePublicKey(String base64PublicKey) throws GeneralSecurityException {
        byte[] keyBytes = Base64.decodeBase64(base64PublicKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
    }

    public PrivateKey decodePrivateKey(String base64PrivateKey) throws GeneralSecurityException {
        byte[] keyBytes = Base64.decodeBase64(base64PrivateKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }
}
