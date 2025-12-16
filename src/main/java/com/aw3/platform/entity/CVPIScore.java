package com.aw3.platform.entity;

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
 * CVPI Score entity for tracking creator performance efficiency
 * 
 * CVPI = Total Campaign Cost / Verified Impact Score
 * Lower CVPI = Better cost efficiency
 */
@Entity
@Table(name = "cvpi_scores", indexes = {
    @Index(name = "idx_cvpi_creator", columnList = "creator_id, calculated_at"),
    @Index(name = "idx_cvpi_campaign", columnList = "campaign_id"),
    @Index(name = "idx_cvpi_score", columnList = "cvpi_score")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVPIScore {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cvpi_id", updatable = false, nullable = false)
    private UUID cvpiId;

    @Column(name = "creator_id", nullable = false)
    private UUID creatorId;

    @Column(name = "campaign_id", nullable = false)
    private UUID campaignId;

    @Column(name = "cvpi_score", precision = 10, scale = 4, nullable = false)
    private BigDecimal cvpiScore;

    @Column(name = "total_cost", precision = 20, scale = 2, nullable = false)
    private BigDecimal totalCost;

    @Column(name = "verified_impact_score", precision = 20, scale = 2, nullable = false)
    private BigDecimal verifiedImpactScore;

    @Column(name = "kpi_achievements", columnDefinition = "JSON")
    private String kpiAchievements;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "platform_average_cvpi", precision = 10, scale = 4)
    private BigDecimal platformAverageCvpi;

    @Column(name = "percentile_rank", precision = 5, scale = 2)
    private BigDecimal percentileRank;

    @Column(name = "verification_id", length = 100)
    private String verificationId;

    @CreatedDate
    @Column(name = "calculated_at", nullable = false, updatable = false)
    private Instant calculatedAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}

