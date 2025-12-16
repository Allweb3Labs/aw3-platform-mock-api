package com.aw3.platform.util;

import lombok.experimental.UtilityClass;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for OAuth cryptographic operations
 * Handles anonymous ID generation, PKCE, and Telegram auth verification
 */
@UtilityClass
public class OAuthCryptoUtil {
    
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    /**
     * Generate anonymous ID using SHA-256 hash
     * Format: SHA256(provider:userId:salt)
     * 
     * @param provider OAuth provider name
     * @param providerUserId User ID from provider
     * @param salt Security salt (from environment)
     * @return 64-character hex string
     */
    public static String generateAnonymousId(String provider, String providerUserId, String salt) {
        try {
            String input = String.format("%s:%s:%s", provider, providerUserId, salt);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    /**
     * Generate PKCE code verifier
     * Random 43-128 character string
     * 
     * @return Base64 URL-encoded random string
     */
    public static String generateCodeVerifier() {
        byte[] code = new byte[32];
        SECURE_RANDOM.nextBytes(code);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(code);
    }
    
    /**
     * Generate PKCE code challenge from verifier
     * Challenge = BASE64URL(SHA256(verifier))
     * 
     * @param codeVerifier The code verifier
     * @return Base64 URL-encoded challenge
     */
    public static String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    /**
     * Generate random state parameter for CSRF protection
     * 
     * @return 32-character hex string
     */
    public static String generateState() {
        byte[] state = new byte[16];
        SECURE_RANDOM.nextBytes(state);
        return bytesToHex(state);
    }
    
    /**
     * Verify Telegram authentication data
     * Implements Telegram's hash verification algorithm
     * 
     * @param authData Authentication data map
     * @param hash Provided hash
     * @param botToken Telegram bot token
     * @return true if hash is valid
     */
    public static boolean verifyTelegramAuth(String dataCheckString, String hash, String botToken) {
        try {
            // Create secret key from bot token
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] secretKey = sha256.digest(botToken.getBytes(StandardCharsets.UTF_8));
            
            // Calculate HMAC-SHA256
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "HmacSHA256");
            hmac.init(secretKeySpec);
            byte[] hmacHash = hmac.doFinal(dataCheckString.getBytes(StandardCharsets.UTF_8));
            
            // Convert to hex and compare
            String calculatedHash = bytesToHex(hmacHash);
            return calculatedHash.equals(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error verifying Telegram authentication", e);
        }
    }
    
    /**
     * Convert byte array to hex string
     * 
     * @param bytes Byte array
     * @return Hex string
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    
    /**
     * Generate nonce for additional security
     * 
     * @return Random nonce string
     */
    public static String generateNonce() {
        byte[] nonce = new byte[16];
        SECURE_RANDOM.nextBytes(nonce);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(nonce);
    }
}

