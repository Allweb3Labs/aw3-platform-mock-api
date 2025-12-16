package com.aw3.platform.controller.admin;

import com.aw3.platform.dto.common.ApiResponse;
import com.aw3.platform.security.CustomUserDetails;
import com.aw3.platform.service.AdminReputationService;
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
 * Admin Portal - Reputation Management Endpoints
 * 
 * Business Rules:
 * - Manual adjustments require documented reason
 * - All changes logged in audit trail
 * - Large adjustments (>20 points) require approval
 * - Reputation affects fee discounts and trust scores
 */
@RestController
@RequestMapping("/admin/reputation")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminReputationController {

    private final AdminReputationService reputationService;

    /**
     * GET /api/admin/reputation/users
     * List users with reputation data
     */
    @GetMapping("/users")
    public ApiResponse<ReputationUsersResponse> getReputationUsers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String tier,
            @RequestParam(required = false) Integer minScore,
            @RequestParam(required = false) Integer maxScore,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Admin {} fetching reputation users", userDetails.getUserId());
        
        ReputationUsersResponse users = reputationService.getReputationUsers(
                role, tier, minScore, maxScore, sortBy, page, size);
        
        return ApiResponse.success(users);
    }

    /**
     * GET /api/admin/reputation/users/{userId}
     * Get detailed reputation history for user
     */
    @GetMapping("/users/{userId}")
    public ApiResponse<UserReputationDetailResponse> getUserReputationDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID userId) {
        
        log.info("Admin {} fetching reputation detail for user {}", userDetails.getUserId(), userId);
        
        UserReputationDetailResponse detail = reputationService.getUserReputationDetail(userId);
        
        return ApiResponse.success(detail);
    }

    /**
     * POST /api/admin/reputation/users/{userId}/adjust
     * Manually adjust user reputation
     */
    @PostMapping("/users/{userId}/adjust")
    public ApiResponse<ReputationAdjustmentResponse> adjustReputation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID userId,
            @Valid @RequestBody ReputationAdjustmentRequest request) {
        
        log.info("Admin {} adjusting reputation for user {} by {}", 
                userDetails.getUserId(), userId, request.getAdjustment());
        
        ReputationAdjustmentResponse response = reputationService.adjustReputation(
                userDetails.getUserId(), userId, request);
        
        return ApiResponse.success(response);
    }

    /**
     * GET /api/admin/reputation/algorithm
     * Get reputation algorithm configuration
     */
    @GetMapping("/algorithm")
    public ApiResponse<ReputationAlgorithmConfig> getAlgorithmConfig(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Admin {} fetching reputation algorithm config", userDetails.getUserId());
        
        ReputationAlgorithmConfig config = reputationService.getAlgorithmConfig();
        
        return ApiResponse.success(config);
    }

    /**
     * PUT /api/admin/reputation/algorithm
     * Update reputation algorithm settings (requires DAO approval for major changes)
     */
    @PutMapping("/algorithm")
    public ApiResponse<ReputationAlgorithmConfig> updateAlgorithmConfig(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateAlgorithmRequest request) {
        
        log.info("Admin {} updating reputation algorithm config", userDetails.getUserId());
        
        ReputationAlgorithmConfig config = reputationService.updateAlgorithmConfig(
                userDetails.getUserId(), request);
        
        return ApiResponse.success(config);
    }

    /**
     * GET /api/admin/reputation/analytics
     * Get reputation distribution analytics
     */
    @GetMapping("/analytics")
    public ApiResponse<ReputationAnalyticsResponse> getReputationAnalytics(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String period) {
        
        log.info("Admin {} fetching reputation analytics", userDetails.getUserId());
        
        ReputationAnalyticsResponse analytics = reputationService.getReputationAnalytics(role, period);
        
        return ApiResponse.success(analytics);
    }

    // DTOs

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReputationUsersResponse {
        private List<ReputationUserSummary> users;
        private ReputationStats stats;
        private PaginationInfo pagination;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReputationUserSummary {
        private UUID userId;
        private String displayName;
        private String walletAddress;
        private String role;
        private BigDecimal reputationScore;
        private String tier;
        private BigDecimal changeLastMonth;
        private Integer completedTasks;
        private Integer disputes;
        private Instant lastActivity;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReputationStats {
        private BigDecimal averageScore;
        private BigDecimal medianScore;
        private Long totalUsers;
        private TierDistribution tierDistribution;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TierDistribution {
        private Long diamond;
        private Long platinum;
        private Long gold;
        private Long silver;
        private Long bronze;
        private Long newcomer;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserReputationDetailResponse {
        private UUID userId;
        private String displayName;
        private String role;
        private BigDecimal currentScore;
        private String tier;
        private List<ReputationHistoryItem> history;
        private List<ReputationEvent> recentEvents;
        private ReputationBreakdown breakdown;
        private List<ManualAdjustment> manualAdjustments;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReputationHistoryItem {
        private String date;
        private BigDecimal score;
        private String tier;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReputationEvent {
        private Instant timestamp;
        private String type;
        private String description;
        private BigDecimal change;
        private UUID relatedEntityId;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReputationBreakdown {
        private BigDecimal taskCompletion;
        private BigDecimal qualityScore;
        private BigDecimal timelinessScore;
        private BigDecimal disputeRecord;
        private BigDecimal communityFeedback;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ManualAdjustment {
        private Instant timestamp;
        private BigDecimal adjustment;
        private String reason;
        private UUID adminId;
        private String adminName;
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
    public static class ReputationAdjustmentRequest {
        private BigDecimal adjustment; // positive or negative
        private String reason;
        private String category; // BONUS, PENALTY, CORRECTION, OTHER
        private String approvalId; // Required for large adjustments
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReputationAdjustmentResponse {
        private UUID userId;
        private BigDecimal previousScore;
        private BigDecimal adjustment;
        private BigDecimal newScore;
        private String previousTier;
        private String newTier;
        private String reason;
        private UUID adminId;
        private Instant timestamp;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReputationAlgorithmConfig {
        private Weights weights;
        private TierThresholds tierThresholds;
        private DecaySettings decay;
        private BonusMultipliers bonuses;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class Weights {
        private BigDecimal taskCompletion;
        private BigDecimal qualityScore;
        private BigDecimal timelinessScore;
        private BigDecimal disputeRecord;
        private BigDecimal communityFeedback;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TierThresholds {
        private Integer diamond;
        private Integer platinum;
        private Integer gold;
        private Integer silver;
        private Integer bronze;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DecaySettings {
        private Boolean enabled;
        private Integer inactivityDaysBeforeDecay;
        private BigDecimal dailyDecayRate;
        private Integer minimumScore;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BonusMultipliers {
        private BigDecimal consecutiveCompletions;
        private BigDecimal earlyDelivery;
        private BigDecimal highQuality;
        private BigDecimal referralBonus;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UpdateAlgorithmRequest {
        private Weights weights;
        private TierThresholds tierThresholds;
        private DecaySettings decay;
        private BonusMultipliers bonuses;
        private String daoProposalId; // Required for major changes
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReputationAnalyticsResponse {
        private DistributionStats distribution;
        private List<TrendItem> trendData;
        private List<RoleBreakdown> byRole;
        private MovementStats movement;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DistributionStats {
        private BigDecimal average;
        private BigDecimal median;
        private BigDecimal standardDeviation;
        private List<HistogramBucket> histogram;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class HistogramBucket {
        private Integer rangeStart;
        private Integer rangeEnd;
        private Long count;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TrendItem {
        private String month;
        private BigDecimal averageScore;
        private Long totalUsers;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RoleBreakdown {
        private String role;
        private BigDecimal averageScore;
        private Long userCount;
        private TierDistribution tierDistribution;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MovementStats {
        private Long usersImproved;
        private Long usersDeclined;
        private Long tierUpgrades;
        private Long tierDowngrades;
        private BigDecimal averageChange;
    }
}
