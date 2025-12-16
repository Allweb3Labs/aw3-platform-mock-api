package com.aw3.platform.dto.creator;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Reputation Response DTO
 * 
 * GET /api/creator/reputation/me response format
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReputationResponse {

    private UUID userId;
    private ReputationInfo reputation;
    private BreakdownInfo breakdown;
    private StatsInfo stats;
    private BenefitsInfo benefits;
    private NextTierInfo nextTier;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReputationInfo {
        private BigDecimal score;
        private String tier;
        private Integer rank;
        private BigDecimal percentile;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BreakdownInfo {
        private Integer campaignCompletion;
        private Integer qualityScore;
        private Integer cvpiPerformance;
        private Integer clientSatisfaction;
        private Integer communityEngagement;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatsInfo {
        private Integer totalCampaigns;
        private Integer completedCampaigns;
        private BigDecimal completionRate;
        private BigDecimal avgCVPI;
        private BigDecimal avgClientRating;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BenefitsInfo {
        private Boolean priorityApplications;
        private Boolean higherPayoutPotential;
        private Boolean accessToExclusiveCampaigns;
        private BigDecimal reducedPlatformFees;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NextTierInfo {
        private String tier;
        private BigDecimal requiredScore;
        private BigDecimal pointsNeeded;
        private Integer estimatedCampaigns;
    }
}

