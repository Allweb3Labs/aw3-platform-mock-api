package com.aw3.platform.entity;

import com.aw3.platform.entity.enums.UserRole;
import com.aw3.platform.entity.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * User entity with polymorphic user_type for portal access control
 * 
 * Ownership Rules:
 * - Users own their own profiles
 * - Only the user or admin can modify user data
 * - user_type determines portal access (CREATOR, PROJECT, VALIDATOR, ADMIN)
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_wallet", columnList = "wallet_address", unique = true),
    @Index(name = "idx_users_did", columnList = "did_identifier", unique = true),
    @Index(name = "idx_users_email", columnList = "email")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    @Column(name = "wallet_address", unique = true, nullable = false, length = 42)
    private String walletAddress;

    @Column(name = "did_identifier", unique = true, nullable = false, length = 100)
    private String didIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, length = 20)
    private UserRole userRole;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "profile_data", columnDefinition = "JSON")
    private String profileData;

    @Column(name = "reputation_score", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal reputationScore = BigDecimal.ZERO;

    @Column(name = "cumulative_spend", precision = 20, scale = 2)
    @Builder.Default
    private BigDecimal cumulativeSpend = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "kyc_verified")
    @Builder.Default
    private Boolean kycVerified = false;

    @Column(name = "social_verifications", columnDefinition = "JSON")
    private String socialVerifications;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @PrePersist
    protected void onCreate() {
        if (reputationScore == null) {
            reputationScore = BigDecimal.ZERO;
        }
        if (cumulativeSpend == null) {
            cumulativeSpend = BigDecimal.ZERO;
        }
        if (status == null) {
            status = UserStatus.ACTIVE;
        }
    }
}

