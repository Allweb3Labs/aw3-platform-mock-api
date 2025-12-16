package com.aw3.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing OAuth account information
 * Stores OAuth tokens and provider-specific data
 * Personal data stored only in backend database, never on-chain
 */
@Entity
@Table(name = "oauth_accounts", indexes = {
    @Index(name = "idx_anonymous_id", columnList = "anonymousId"),
    @Index(name = "idx_provider_user", columnList = "provider,providerUserId"),
    @Index(name = "idx_user_id", columnList = "userId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthAccount {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Anonymous ID generated using SHA-256 hash
     * This is the only identifier that should be stored on-chain
     */
    @Column(nullable = false, unique = true, length = 64)
    private String anonymousId;
    
    /**
     * OAuth provider: twitter, discord, or telegram
     */
    @Column(nullable = false, length = 20)
    private String provider;
    
    /**
     * User ID from the OAuth provider
     * Stored in backend only, never on-chain
     */
    @Column(nullable = false)
    private String providerUserId;
    
    /**
     * Username from the OAuth provider
     */
    @Column(length = 255)
    private String username;
    
    /**
     * Display name (for Discord global_name)
     */
    @Column(length = 255)
    private String displayName;
    
    /**
     * Email (if provided by OAuth scope)
     */
    @Column(length = 255)
    private String email;
    
    /**
     * Avatar URL
     */
    @Column(length = 512)
    private String avatarUrl;
    
    /**
     * OAuth access token (encrypted)
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String accessToken;
    
    /**
     * OAuth refresh token (encrypted)
     */
    @Column(columnDefinition = "TEXT")
    private String refreshToken;
    
    /**
     * Token expiration timestamp
     */
    private LocalDateTime tokenExpiresAt;
    
    /**
     * OAuth scopes granted
     */
    @Column(length = 512)
    private String scopes;
    
    /**
     * Link to the main User entity
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;
    
    /**
     * Additional metadata (JSON format)
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
    /**
     * Account verification status
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean verified = false;
    
    /**
     * Account creation timestamp
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Last update timestamp
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if the token is expired or about to expire (within 1 hour)
     */
    public boolean isTokenExpired() {
        if (tokenExpiresAt == null) {
            return false;
        }
        return LocalDateTime.now().plusHours(1).isAfter(tokenExpiresAt);
    }
}

