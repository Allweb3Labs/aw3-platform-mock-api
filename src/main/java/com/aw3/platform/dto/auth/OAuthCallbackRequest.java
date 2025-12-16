package com.aw3.platform.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for OAuth callback handling
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthCallbackRequest {
    
    /**
     * Authorization code from OAuth provider
     */
    @NotBlank(message = "Authorization code is required")
    private String code;
    
    /**
     * State parameter for CSRF protection
     */
    @NotBlank(message = "State parameter is required")
    private String state;
    
    /**
     * Error code if authorization failed
     */
    private String error;
    
    /**
     * Error description if authorization failed
     */
    private String errorDescription;
}

