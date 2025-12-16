package com.aw3.platform.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Telegram authentication
 * Telegram uses a different flow with widget data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelegramAuthRequest {
    
    /**
     * Telegram user ID
     */
    @NotBlank(message = "User ID is required")
    private String id;
    
    /**
     * First name
     */
    @NotBlank(message = "First name is required")
    private String firstName;
    
    /**
     * Last name (optional)
     */
    private String lastName;
    
    /**
     * Username (optional)
     */
    private String username;
    
    /**
     * Photo URL (optional)
     */
    private String photoUrl;
    
    /**
     * Auth date (Unix timestamp)
     */
    @NotNull(message = "Auth date is required")
    private Long authDate;
    
    /**
     * Hash for verification
     */
    @NotBlank(message = "Hash is required")
    private String hash;
}

