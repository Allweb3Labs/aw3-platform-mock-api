package com.aw3.platform.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for OAuth account information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthAccountInfoResponse {
    
    /**
     * Anonymous ID
     */
    private String anonymousId;
    
    /**
     * Provider
     */
    private String provider;
    
    /**
     * Username
     */
    private String username;
    
    /**
     * Display name
     */
    private String displayName;
    
    /**
     * Email (if available)
     */
    private String email;
    
    /**
     * Avatar URL
     */
    private String avatarUrl;
    
    /**
     * Scopes granted
     */
    private List<String> scopes;
    
    /**
     * Verification status
     */
    private boolean verified;
    
    /**
     * Token expiration
     */
    private LocalDateTime tokenExpiresAt;
    
    /**
     * Account created at
     */
    private LocalDateTime createdAt;
    
    /**
     * Account updated at
     */
    private LocalDateTime updatedAt;
}

