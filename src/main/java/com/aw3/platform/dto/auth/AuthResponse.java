package com.aw3.platform.dto.auth;

import com.aw3.platform.entity.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Authentication response DTO containing JWT and user information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Long expiresIn; // seconds

    private UserInfo user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private UUID userId;
        private String walletAddress;
        private String didIdentifier;
        private UserRole userRole;
        private String username;
        private String displayName;
        private String email;
        private String avatarUrl;
        
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        private Instant createdAt;
    }
}

