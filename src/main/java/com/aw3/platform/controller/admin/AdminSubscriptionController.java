package com.aw3.platform.controller.admin;

import com.aw3.platform.dto.common.ApiResponse;
import com.aw3.platform.security.CustomUserDetails;
import com.aw3.platform.service.AdminSubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Admin Portal - Subscription Management Endpoints
 * 
 * Business Rules:
 * - All subscription changes logged for audit
 * - Plan changes cannot affect users mid-billing cycle (grandfather existing subscriptions)
 * - DAO approval required for major tier restructuring
 */
@RestController
@RequestMapping("/admin/subscriptions")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminSubscriptionController {

    private final AdminSubscriptionService subscriptionService;

    /**
     * GET /api/admin/subscriptions/plans
     * List all subscription plans
     */
    @GetMapping("/plans")
    public ApiResponse<SubscriptionPlansResponse> getSubscriptionPlans(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String userType) {
        
        log.info("Admin {} fetching subscription plans", userDetails.getUserId());
        
        SubscriptionPlansResponse plans = subscriptionService.getSubscriptionPlans(userType);
        
        return ApiResponse.success(plans);
    }

    /**
     * PUT /api/admin/subscriptions/plans/{planId}
     * Update subscription plan settings
     */
    @PutMapping("/plans/{planId}")
    public ApiResponse<SubscriptionPlan> updateSubscriptionPlan(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID planId,
            @Valid @RequestBody SubscriptionPlanUpdateRequest request) {
        
        log.info("Admin {} updating subscription plan {}", userDetails.getUserId(), planId);
        
        SubscriptionPlan plan = subscriptionService.updateSubscriptionPlan(
                userDetails.getUserId(), planId, request);
        
        return ApiResponse.success(plan);
    }

    /**
     * GET /api/admin/subscriptions/analytics
     * Get subscription analytics overview
     */
    @GetMapping("/analytics")
    public ApiResponse<SubscriptionAnalyticsResponse> getSubscriptionAnalytics(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String period) {
        
        log.info("Admin {} fetching subscription analytics", userDetails.getUserId());
        
        SubscriptionAnalyticsResponse analytics = subscriptionService.getSubscriptionAnalytics(period);
        
        return ApiResponse.success(analytics);
    }

    /**
     * GET /api/admin/subscriptions/users
     * List users by subscription status
     */
    @GetMapping("/users")
    public ApiResponse<SubscribedUsersResponse> getSubscribedUsers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String tier,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Admin {} fetching subscribed users", userDetails.getUserId());
        
        SubscribedUsersResponse users = subscriptionService.getSubscribedUsers(tier, status, page, size);
        
        return ApiResponse.success(users);
    }

    /**
     * POST /api/admin/subscriptions/users/{userId}/override
     * Override subscription for specific user (admin privilege)
     */
    @PostMapping("/users/{userId}/override")
    public ApiResponse<SubscriptionOverrideResponse> overrideUserSubscription(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID userId,
            @Valid @RequestBody SubscriptionOverrideRequest request) {
        
        log.info("Admin {} overriding subscription for user {}", userDetails.getUserId(), userId);
        
        SubscriptionOverrideResponse response = subscriptionService.overrideUserSubscription(
                userDetails.getUserId(), userId, request);
        
        return ApiResponse.success(response);
    }

    // DTOs

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SubscriptionPlansResponse {
        private List<SubscriptionPlan> creatorPlans;
        private List<SubscriptionPlan> projectPlans;
        private List<SubscriptionPlan> validatorPlans;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SubscriptionPlan {
        private UUID planId;
        private String name;
        private String tier; // FREE, PRO, BUSINESS, ENTERPRISE
        private String userType; // CREATOR, PROJECT, VALIDATOR
        private BigDecimal monthlyPrice;
        private BigDecimal annualPrice;
        private String currency;
        private List<String> features;
        private PlanLimits limits;
        private Boolean active;
        private Instant createdAt;
        private Instant updatedAt;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PlanLimits {
        private Integer maxCampaigns;
        private Integer maxApplicationsPerMonth;
        private Integer analyticsRetentionDays;
        private Boolean prioritySupport;
        private Boolean apiAccess;
        private BigDecimal platformFeeDiscount;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SubscriptionPlanUpdateRequest {
        private BigDecimal monthlyPrice;
        private BigDecimal annualPrice;
        private List<String> features;
        private PlanLimits limits;
        private Boolean active;
        private String daoProposalId; // Required for major changes
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SubscriptionAnalyticsResponse {
        private SubscriptionStats overall;
        private List<TierBreakdown> byTier;
        private List<RevenueItem> revenueHistory;
        private ChurnMetrics churn;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SubscriptionStats {
        private Long totalSubscribers;
        private Long activeSubscribers;
        private Long trialUsers;
        private BigDecimal mrr; // Monthly Recurring Revenue
        private BigDecimal arr; // Annual Recurring Revenue
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TierBreakdown {
        private String tier;
        private Long count;
        private BigDecimal revenue;
        private BigDecimal percentage;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RevenueItem {
        private String month;
        private BigDecimal revenue;
        private Long newSubscribers;
        private Long churned;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ChurnMetrics {
        private BigDecimal monthlyChurnRate;
        private BigDecimal annualChurnRate;
        private Long atRiskUsers;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SubscribedUsersResponse {
        private List<SubscribedUser> users;
        private PaginationInfo pagination;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SubscribedUser {
        private UUID userId;
        private String displayName;
        private String email;
        private String tier;
        private String status;
        private Instant subscribedAt;
        private Instant expiresAt;
        private BigDecimal lifetimeValue;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PaginationInfo {
        private Long total;
        private Integer page;
        private Integer size;
        private Integer totalPages;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SubscriptionOverrideRequest {
        private String tier;
        private Integer durationDays;
        private String reason;
        private Boolean freeUpgrade;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SubscriptionOverrideResponse {
        private UUID userId;
        private String previousTier;
        private String newTier;
        private Instant expiresAt;
        private String overrideReason;
        private UUID adminId;
        private Instant appliedAt;
    }
}

