package com.aw3.platform.entity;

import com.aw3.platform.entity.enums.DeliverableStatus;
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
 * Deliverable entity for creator submissions
 * 
 * Ownership Rules:
 * - creator_id identifies who submitted the deliverable
 * - application_id links to the accepted application
 * - Creators can only submit deliverables for campaigns they are accepted into
 * - Projects can view deliverables for their campaigns
 */
@Entity
@Table(name = "deliverables", indexes = {
    @Index(name = "idx_deliverables_application", columnList = "application_id"),
    @Index(name = "idx_deliverables_creator", columnList = "creator_id"),
    @Index(name = "idx_deliverables_campaign", columnList = "campaign_id"),
    @Index(name = "idx_deliverables_status", columnList = "status, submitted_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Deliverable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "deliverable_id", updatable = false, nullable = false)
    private UUID deliverableId;

    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Column(name = "campaign_id", nullable = false)
    private UUID campaignId;

    @Column(name = "creator_id", nullable = false)
    private UUID creatorId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "content_url", length = 1000)
    private String contentUrl;

    @Column(name = "proof_urls", columnDefinition = "JSON")
    private String proofUrls;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private DeliverableStatus status = DeliverableStatus.PENDING;

    @Column(name = "milestone_id", length = 100)
    private String milestoneId;

    @Column(name = "payment_amount", precision = 20, scale = 2)
    private BigDecimal paymentAmount;

    @CreatedDate
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "kpi_results", columnDefinition = "TEXT")
    private String kpiResults;

    @Column(name = "oracle_verification_id", length = 100)
    private String oracleVerificationId;

    @Column(name = "payment_amount", precision = 20, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "verified_by")
    private UUID verifiedBy;

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "kpi_results", columnDefinition = "JSON")
    private String kpiResults;

    @Column(name = "oracle_verification_id", length = 100)
    private String oracleVerificationId;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}

