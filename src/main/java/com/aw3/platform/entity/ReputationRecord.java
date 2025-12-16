package com.aw3.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Reputation Record entity - time-series data with dual ownership
 * 
 * Ownership Rules:
 * - user_id indicates whose reputation is recorded
 * - campaign_id indicates which campaign generated the record
 * - Creators can view their own records (read-only)
 * - Projects can view records for their campaigns
 * - Admins can view and manually adjust all records
 */
@Entity
@Table(name = "reputation_records", indexes = {
    @Index(name = "idx_reputation_user", columnList = "user_id, recorded_at"),
    @Index(name = "idx_reputation_campaign", columnList = "campaign_id"),
    @Index(name = "idx_reputation_type", columnList = "record_type, recorded_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReputationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "record_id", updatable = false, nullable = false)
    private UUID recordId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "campaign_id")
    private UUID campaignId;

    @Column(name = "record_type", nullable = false, length = 50)
    private String recordType; // CAMPAIGN_COMPLETION, MANUAL_ADJUSTMENT, DISPUTE_RESOLUTION, etc.

    @Column(name = "score_delta", precision = 10, scale = 2, nullable = false)
    private BigDecimal scoreDelta;

    @Column(name = "previous_score", precision = 10, scale = 2)
    private BigDecimal previousScore;

    @Column(name = "new_score", precision = 10, scale = 2)
    private BigDecimal newScore;

    @Column(name = "performance_metrics", columnDefinition = "JSON")
    private String performanceMetrics;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "adjusted_by")
    private UUID adjustedBy; // For manual adjustments by admins

    @CreatedDate
    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt;

    @Column(name = "verification_hash", length = 100)
    private String verificationHash; // Link to blockchain verification
}

