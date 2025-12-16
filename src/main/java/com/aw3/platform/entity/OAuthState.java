package com.aw3.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for storing OAuth state and PKCE verifiers
 * Used for CSRF protection and PKCE flow
 */
@Entity
@Table(name = "oauth_states")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthState {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * State parameter for CSRF protection
     */
    @Column(nullable = false, unique = true, length = 64)
    private String state;
    
    /**
     * OAuth provider
     */
    @Column(nullable = false, length = 20)
    private String provider;
    
    /**
     * PKCE code verifier (for Twitter OAuth)
     */
    @Column(length = 128)
    private String codeVerifier;
    
    /**
     * PKCE code challenge
     */
    @Column(length = 128)
    private String codeChallenge;
    
    /**
     * User's wallet address (if available at initiation)
     */
    @Column(length = 42)
    private String walletAddress;
    
    /**
     * IP address for security tracking
     */
    @Column(length = 45)
    private String ipAddress;
    
    /**
     * User agent for security tracking
     */
    @Column(length = 512)
    private String userAgent;
    
    /**
     * Expiration timestamp (states expire after 10 minutes)
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    /**
     * Creation timestamp
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Whether this state has been used
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean used = false;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (expiresAt == null) {
            // States expire after 10 minutes by default
            expiresAt = LocalDateTime.now().plusMinutes(10);
        }
    }
    
    /**
     * Check if the state is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}

