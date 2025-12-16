package com.aw3.platform.entity;

import com.aw3.platform.entity.enums.CampaignStatus;
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
 * Campaign entity with project ownership
 * 
 * Ownership Rules:
 * - project_id establishes ownership
 * - Only the owning project can edit campaign details
 * - Creators can only view campaigns with status = ACTIVE
 * - Admins can view all campaigns regardless of status
 */
@Entity
@Table(name = "campaigns", indexes = {
    @Index(name = "idx_campaigns_project", columnList = "project_id"),
    @Index(name = "idx_campaigns_status", columnList = "status, created_at"),
    @Index(name = "idx_campaigns_deadline", columnList = "deadline")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "campaign_id", updatable = false, nullable = false)
    private UUID campaignId;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "kpi_targets", columnDefinition = "JSON")
    private String kpiTargets;

    @Column(name = "budget_amount", precision = 20, scale = 2, nullable = false)
    private BigDecimal budgetAmount;

    @Column(name = "budget_token", length = 20, nullable = false)
    @Builder.Default
    private String budgetToken = "USDC";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CampaignStatus status = CampaignStatus.DRAFT;

    @Column(name = "deadline", nullable = false)
    private Instant deadline;

    @Column(name = "contract_address", length = 42)
    private String contractAddress;

    @Column(name = "chain_id")
    private Integer chainId;

    @Column(name = "escrow_balance", precision = 20, scale = 2)
    @Builder.Default
    private BigDecimal escrowBalance = BigDecimal.ZERO;

    @Column(name = "service_fee", precision = 20, scale = 2)
    private BigDecimal serviceFee;

    @Column(name = "oracle_fee", precision = 20, scale = 2)
    private BigDecimal oracleFee;

    @Column(name = "total_fee", precision = 20, scale = 2)
    private BigDecimal totalFee;

    @Column(name = "fee_estimate_id", length = 100)
    private String feeEstimateId;

    @Column(name = "aw3_token_payment_enabled")
    @Builder.Default
    private Boolean aw3TokenPaymentEnabled = false;

    @Column(name = "required_reputation", precision = 10, scale = 2)
    private BigDecimal requiredReputation;

    @Column(name = "number_of_creators")
    private Integer numberOfCreators;

    @Column(name = "complexity", length = 20)
    private String complexity;

    @Column(name = "campaign_metadata", columnDefinition = "JSON")
    private String campaignMetadata;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "contract_address", length = 42)
    private String contractAddress;

    @Column(name = "chain_id")
    private Integer chainId;

    @Column(name = "service_fee", precision = 20, scale = 2)
    private BigDecimal serviceFee;

    @Column(name = "oracle_fee", precision = 20, scale = 2)
    private BigDecimal oracleFee;

    @Column(name = "escrow_balance", precision = 20, scale = 2)
    private BigDecimal escrowBalance;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "completion_date")
    private Instant completionDate;
}

