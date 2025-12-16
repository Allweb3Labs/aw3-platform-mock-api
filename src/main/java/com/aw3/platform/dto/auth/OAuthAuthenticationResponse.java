package com.aw3.platform.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for successful OAuth authentication
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthAuthenticationResponse {
    
    /**
     * Anonymous user identifier (SHA-256 hash)
     * This is safe to store on-chain
     */
    private String anonymousId;
    
    /**
     * OAuth provider
     */
    private String provider;
    
    /**
     * Username from provider (not stored on-chain)
     */
    private String username;
    
    /**
     * Display name
     */
    private String displayName;
    
    /**
     * Avatar URL
     */
    private String avatarUrl;
    
    /**
     * JWT token for session management
     */
    private String accessToken;
    
    /**
     * Verification status
     */
    private boolean verified;
    
    /**
     * Token expiry timestamp
     */
    private String expiresAt;
}

