package com.cryptovault.security;

import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;

@Component
public class TotpUtil {

    private static final int OTP_LENGTH = 6;
    private static final int TIME_STEP_SECONDS = 30;
    private static final int WINDOW = 1;

    public String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] secretBytes = new byte[20];
        random.nextBytes(secretBytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(secretBytes).replace("=", "");
    }

    public String getQrCodeUrl(String secret, String email, String issuer) {
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
                issuer, email, secret, issuer
        );
    }

    public boolean verifyCode(String secret, String code) {
        long currentTime = Instant.now().getEpochSecond() / TIME_STEP_SECONDS;
        for (int i = -WINDOW; i <= WINDOW; i++) {
            String generatedCode = generateCode(secret, currentTime + i);
            if (generatedCode.equals(code)) {
                return true;
            }
        }
        return false;
    }

    private String generateCode(String secret, long timeCounter) {
        try {
            Base32 base32 = new Base32();
            byte[] secretBytes = base32.decode(secret.toUpperCase());
            byte[] timeBytes = longToBytes(timeCounter);

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(secretBytes, "HmacSHA1"));
            byte[] hash = mac.doFinal(timeBytes);

            int offset = hash[hash.length - 1] & 0x0F;
            int otp = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);

            otp = otp % (int) Math.pow(10, OTP_LENGTH);
            return String.format("%0" + OTP_LENGTH + "d", otp);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to generate TOTP code", e);
        }
    }

    private byte[] longToBytes(long value) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        return result;
    }
}
