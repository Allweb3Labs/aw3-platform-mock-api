package com.aw3.platform.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for platform-wide metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformMetricsResponse {
    private Users users;
    private Campaigns campaigns;
    private Financials financials;
    private Performance performance;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Users {
        private Long total;
        private Long active;
        private Long creators;
        private Long projects;
        private Long validators;
        private String growth;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Campaigns {
        private Long total;
        private Long active;
        private Long completed;
        private Double avgDuration;
        private Double completionRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Financials {
        private Double totalVolume;
        private Double platformRevenue;
        private Double avgCampaignBudget;
        private Double treasuryBalance;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Performance {
        private Double avgCVPI;
        private Double medianCVPI;
        private String cvpiTrend;
        private Double avgCreatorReputationScore;
    }
}

