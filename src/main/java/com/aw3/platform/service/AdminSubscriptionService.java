package com.aw3.platform.service;

import com.aw3.platform.controller.admin.AdminSubscriptionController.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Admin Subscription Management Service
 * 
 * Business Rules:
 * - All subscription changes are audit logged
 * - Plan changes cannot affect users mid-billing cycle
 * - DAO approval required for major tier restructuring
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminSubscriptionService {

    public SubscriptionPlansResponse getSubscriptionPlans(String userType) {
        // TODO: Fetch from database
        return SubscriptionPlansResponse.builder()
                .creatorPlans(List.of(
                        buildPlan("FREE", "Creator", BigDecimal.ZERO),
                        buildPlan("PRO", "Creator", BigDecimal.valueOf(19.99)),
                        buildPlan("BUSINESS", "Creator", BigDecimal.valueOf(49.99))
                ))
                .projectPlans(List.of(
                        buildPlan("FREE", "Project", BigDecimal.ZERO),
                        buildPlan("PRO", "Project", BigDecimal.valueOf(99.99)),
                        buildPlan("ENTERPRISE", "Project", BigDecimal.valueOf(299.99))
                ))
                .validatorPlans(List.of(
                        buildPlan("STANDARD", "Validator", BigDecimal.ZERO)
                ))
                .build();
    }

    public SubscriptionPlan updateSubscriptionPlan(UUID adminId, UUID planId, SubscriptionPlanUpdateRequest request) {
        log.info("Admin {} updating subscription plan {}", adminId, planId);
        // TODO: Implement update logic with audit logging
        return buildPlan("PRO", "Creator", request.getMonthlyPrice());
    }

    public SubscriptionAnalyticsResponse getSubscriptionAnalytics(String period) {
        // TODO: Implement analytics from database
        return SubscriptionAnalyticsResponse.builder()
                .overall(SubscriptionStats.builder()
                        .totalSubscribers(1500L)
                        .activeSubscribers(1200L)
                        .trialUsers(100L)
                        .mrr(BigDecimal.valueOf(45000))
                        .arr(BigDecimal.valueOf(540000))
                        .build())
                .byTier(List.of(
                        TierBreakdown.builder()
                                .tier("FREE")
                                .count(800L)
                                .revenue(BigDecimal.ZERO)
                                .percentage(BigDecimal.valueOf(53))
                                .build(),
                        TierBreakdown.builder()
                                .tier("PRO")
                                .count(500L)
                                .revenue(BigDecimal.valueOf(30000))
                                .percentage(BigDecimal.valueOf(33))
                                .build(),
                        TierBreakdown.builder()
                                .tier("ENTERPRISE")
                                .count(200L)
                                .revenue(BigDecimal.valueOf(15000))
                                .percentage(BigDecimal.valueOf(14))
                                .build()
                ))
                .churn(ChurnMetrics.builder()
                        .monthlyChurnRate(BigDecimal.valueOf(2.5))
                        .annualChurnRate(BigDecimal.valueOf(25))
                        .atRiskUsers(50L)
                        .build())
                .build();
    }

    public SubscribedUsersResponse getSubscribedUsers(String tier, String status, int page, int size) {
        // TODO: Implement from database with pagination
        return SubscribedUsersResponse.builder()
                .users(List.of())
                .pagination(PaginationInfo.builder()
                        .total(0L)
                        .page(page)
                        .size(size)
                        .totalPages(0)
                        .build())
                .build();
    }

    public SubscriptionOverrideResponse overrideUserSubscription(UUID adminId, UUID userId, SubscriptionOverrideRequest request) {
        log.info("Admin {} overriding subscription for user {} to {}", adminId, userId, request.getTier());
        // TODO: Implement override logic with audit logging
        return SubscriptionOverrideResponse.builder()
                .userId(userId)
                .previousTier("FREE")
                .newTier(request.getTier())
                .expiresAt(Instant.now().plusSeconds(86400L * request.getDurationDays()))
                .overrideReason(request.getReason())
                .adminId(adminId)
                .appliedAt(Instant.now())
                .build();
    }

    private SubscriptionPlan buildPlan(String tier, String userType, BigDecimal monthlyPrice) {
        return SubscriptionPlan.builder()
                .planId(UUID.randomUUID())
                .name(tier + " " + userType)
                .tier(tier)
                .userType(userType.toUpperCase())
                .monthlyPrice(monthlyPrice)
                .annualPrice(monthlyPrice.multiply(BigDecimal.valueOf(10)))
                .currency("USD")
                .features(List.of("Feature 1", "Feature 2"))
                .limits(PlanLimits.builder()
                        .maxCampaigns(tier.equals("FREE") ? 5 : 100)
                        .maxApplicationsPerMonth(tier.equals("FREE") ? 10 : 1000)
                        .analyticsRetentionDays(tier.equals("FREE") ? 30 : 365)
                        .prioritySupport(!tier.equals("FREE"))
                        .apiAccess(!tier.equals("FREE"))
                        .platformFeeDiscount(tier.equals("ENTERPRISE") ? BigDecimal.valueOf(0.20) : BigDecimal.ZERO)
                        .build())
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}

