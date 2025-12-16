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
 * Subscription Response DTO
 * 
 * GET /api/creator/subscriptions/current response format
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionResponse {

    private UUID userId;
    private SubscriptionInfo subscription;
    private BenefitsInfo benefits;
    private UsageInfo usage;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscriptionInfo {
        private String tier;
        private String status;
        private Instant startDate;
        private Instant renewalDate;
        private Boolean autoRenew;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BenefitsInfo {
        private Integer maxActiveCampaigns;
        private Boolean prioritySupport;
        private Boolean advancedAnalytics;
        private String cvpiInsights;
        private Boolean earlyAccessFeatures;
        private Boolean customBranding;
        private Boolean portfolioShowcase;
        private Boolean dedicatedAccountManager;
        private Boolean customContractTerms;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsageInfo {
        private Integer activeCampaigns;
        private Integer completedCampaigns;
        private Integer monthlyApplications;
    }
}

