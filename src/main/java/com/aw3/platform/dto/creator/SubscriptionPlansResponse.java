package com.aw3.platform.dto.creator;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Subscription Plans Response DTO
 * 
 * GET /api/creator/subscriptions/plans response format
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionPlansResponse {

    private List<PlanInfo> plans;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanInfo {
        private String tier;
        private String name;
        private BigDecimal price;
        private String billingCycle;
        private BenefitsInfo benefits;
        private Boolean recommended;
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
}

