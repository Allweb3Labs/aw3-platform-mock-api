package com.aw3.platform.entity;

import com.aw3.platform.entity.enums.FeeType;
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
 * Platform Fee entity for tracking all fee transactions
 * 
 * Records every fee transaction with full calculation transparency
 * calculation_snapshot stores: base rate, multipliers, discounts applied
 */
@Entity
@Table(name = "platform_fees", indexes = {
    @Index(name = "idx_platform_fees_charged_at", columnList = "charged_at"),
    @Index(name = "idx_platform_fees_project", columnList = "project_id, charged_at"),
    @Index(name = "idx_platform_fees_type", columnList = "fee_type, charged_at"),
    @Index(name = "idx_platform_fees_campaign", columnList = "campaign_id")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformFee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "fee_id", updatable = false, nullable = false)
    private UUID feeId;

    @Column(name = "campaign_id")
    private UUID campaignId;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "creator_id")
    private UUID creatorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false, length = 50)
    private FeeType feeType;

    @Column(name = "base_amount", precision = 20, scale = 2, nullable = false)
    private BigDecimal baseAmount;

    @Column(name = "discount_amount", precision = 20, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "final_amount", precision = 20, scale = 2, nullable = false)
    private BigDecimal finalAmount;

    @Column(name = "calculation_snapshot", columnDefinition = "JSON")
    private String calculationSnapshot;

    @Column(name = "payment_status", length = 20)
    @Builder.Default
    private String paymentStatus = "PENDING";

    @Column(name = "transaction_hash", length = 100)
    private String transactionHash;

    @CreatedDate
    @Column(name = "charged_at", nullable = false, updatable = false)
    private Instant chargedAt;

    @Column(name = "paid_at")
    private Instant paidAt;
}

