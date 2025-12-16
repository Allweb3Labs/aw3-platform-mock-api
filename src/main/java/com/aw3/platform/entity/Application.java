package com.aw3.platform.entity;

import com.aw3.platform.entity.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Application entity - junction between creators and campaigns
 * 
 * Ownership Rules:
 * - creator_id identifies the applicant (owned by creator)
 * - campaign_id identifies the target campaign
 * - Projects can only see applications for campaigns they own
 * - Creators can only see their own applications
 * - Status changes: Creators create (PENDING), Projects approve/reject
 */
@Entity
@Table(name = "applications", indexes = {
    @Index(name = "idx_applications_creator", columnList = "creator_id"),
    @Index(name = "idx_applications_campaign", columnList = "campaign_id"),
    @Index(name = "idx_applications_lookup", columnList = "campaign_id, creator_id, status"),
    @Index(name = "idx_applications_status", columnList = "status, applied_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "application_id", updatable = false, nullable = false)
    private UUID applicationId;

    @Column(name = "campaign_id", nullable = false)
    private UUID campaignId;

    @Column(name = "creator_id", nullable = false)
    private UUID creatorId;

    @Column(name = "proposed_rate", precision = 20, scale = 2)
    private BigDecimal proposedRate;

    @Column(name = "proposal", columnDefinition = "TEXT")
    private String proposal;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(name = "portfolio_links", columnDefinition = "JSON")
    private String portfolioLinks;

    @Column(name = "relevant_experience", columnDefinition = "TEXT")
    private String relevantExperience;

    @Column(name = "estimated_completion_days")
    private Integer estimatedCompletionDays;

    @Column(name = "match_score", precision = 5, scale = 4)
    private BigDecimal matchScore;

    @CreatedDate
    @Column(name = "applied_at", nullable = false, updatable = false)
    private Instant appliedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}

