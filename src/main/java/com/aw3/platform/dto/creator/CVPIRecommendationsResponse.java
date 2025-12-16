package com.aw3.platform.dto.creator;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * CVPI Recommendations Response DTO
 * 
 * GET /api/creator/cvpi/recommendations response format
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CVPIRecommendationsResponse {

    private List<RecommendationItem> recommendations;
    private ProfileInsights profileInsights;
    private String algorithmVersion;
    private Instant lastUpdated;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationItem {
        private UUID campaignId;
        private String title;
        private String category;
        private BigDecimal budget;
        private BigDecimal estimatedCVPI;
        private BigDecimal matchScore;
        private List<String> matchReasons;
        private BigDecimal projectedImpact;
        private Integer requiredReputation;
        private Integer yourReputation;
        private Instant deadline;
        private ProjectInfo projectInfo;
        private Boolean isEligible;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectInfo {
        private String name;
        private Boolean verified;
        private BigDecimal reputation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileInsights {
        private List<String> strongestCategories;
        private BigDecimal avgHistoricalCVPI;
        private BigDecimal successRate;
    }
}

