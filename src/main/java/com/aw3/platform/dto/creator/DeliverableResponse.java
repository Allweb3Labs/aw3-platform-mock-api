package com.aw3.platform.dto.creator;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Deliverable Response DTO
 * 
 * GET /api/creator/deliverables/{id} response format
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliverableResponse {

    private UUID deliverableId;
    private UUID campaignId;
    private String campaignTitle;
    private UUID applicationId;
    private String type;
    private String status;
    private Instant submittedAt;
    private Instant reviewedAt;
    private Instant approvedAt;
    private String contentUrl;
    private String platform;
    private String description;
    private MetricsInfo metrics;
    private OracleVerificationInfo oracleVerification;
    private PaymentInfo payment;
    private SPCNFTInfo spcNFT;
    private String reviewNotes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricsInfo {
        private CurrentMetrics current;
        private TargetMetrics target;
        private AchievementMetrics achievement;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentMetrics {
        private Integer views;
        private Integer likes;
        private Integer comments;
        private Integer shares;
        private BigDecimal engagement;
        private Integer conversions;
        private Instant lastUpdated;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TargetMetrics {
        private BigDecimal engagement;
        private Integer reach;
        private Integer conversions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AchievementMetrics {
        private BigDecimal engagement;
        private BigDecimal reach;
        private BigDecimal conversions;
        private BigDecimal overall;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OracleVerificationInfo {
        private String status;
        private String verifiedBy;
        private Instant verifiedAt;
        private BigDecimal confidenceScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInfo {
        private BigDecimal baseAmount;
        private BigDecimal performanceBonus;
        private BigDecimal totalEarned;
        private String status;
        private String txHash;
        private Instant paidAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SPCNFTInfo {
        private Boolean minted;
        private String tokenId;
        private String contractAddress;
        private Instant mintedAt;
    }
}

