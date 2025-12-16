package com.aw3.platform.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for OAuth initiation
 * Contains the authorization URL for redirect
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthInitiateResponse {
    
    /**
     * Authorization URL to redirect user to
     */
    private String authorizationUrl;
    
    /**
     * State parameter (returned for reference, already in URL)
     */
    private String state;
    
    /**
     * OAuth provider
     */
    private String provider;
    
    /**
     * Expires at timestamp
     */
    private String expiresAt;
}

