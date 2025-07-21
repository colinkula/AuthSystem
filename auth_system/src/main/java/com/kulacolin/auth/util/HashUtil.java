package com.kulacolin.auth.util;

import com.password4j.Password;
import com.password4j.Hash;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class HashUtil {

    // ------------------ Argon2 for passwords ------------------

    public static String argon2(String input) {
        Hash hash = Password.hash(input)
                            .addRandomSalt()
                            .withArgon2();
        return hash.getResult(); // Encodes salt + parameters + hash
    }

    public static boolean argon2Matches(String input, String hashedPassword) {
        return Password.check(input, hashedPassword).withArgon2();
    }

    // ------------------ SHA-256 for tokens ------------------

    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            // Use Base64 instead of hex to save space
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public static boolean sha256Matches(String input, String expectedHashBase64) {
        if (expectedHashBase64 == null) return false;
        String computed = sha256(input);
        return constantTimeEquals(computed, expectedHashBase64);
    }
    
    // Prevent timing attacks by not short-circuiting string comparison
    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
