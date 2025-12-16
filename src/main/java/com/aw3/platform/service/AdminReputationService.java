package com.aw3.platform.service;

import com.aw3.platform.controller.admin.AdminReputationController.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Admin Reputation Management Service
 * 
 * Business Rules:
 * - Manual adjustments require documented reason
 * - All changes logged in audit trail
 * - Large adjustments (>20 points) require approval
 * - Reputation affects fee discounts and trust scores
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminReputationService {

    private final AuditService auditService;

    public ReputationUsersResponse getReputationUsers(String role, String tier, Integer minScore,
            Integer maxScore, String sortBy, int page, int size) {
        // TODO: Implement from database
        return ReputationUsersResponse.builder()
                .users(List.of())
                .stats(ReputationStats.builder()
                        .averageScore(BigDecimal.valueOf(65))
                        .medianScore(BigDecimal.valueOf(62))
                        .totalUsers(5000L)
                        .tierDistribution(TierDistribution.builder()
                                .diamond(50L)
                                .platinum(200L)
                                .gold(800L)
                                .silver(1500L)
                                .bronze(1500L)
                                .newcomer(950L)
                                .build())
                        .build())
                .pagination(PaginationInfo.builder()
                        .total(5000L)
                        .page(page)
                        .size(size)
                        .totalPages(250)
                        .build())
                .build();
    }

    public UserReputationDetailResponse getUserReputationDetail(UUID userId) {
        // TODO: Implement from database
        return UserReputationDetailResponse.builder()
                .userId(userId)
                .displayName("User Name")
                .role("CREATOR")
                .currentScore(BigDecimal.valueOf(72))
                .tier("GOLD")
                .history(List.of())
                .recentEvents(List.of())
                .breakdown(ReputationBreakdown.builder()
                        .taskCompletion(BigDecimal.valueOf(80))
                        .qualityScore(BigDecimal.valueOf(75))
                        .timelinessScore(BigDecimal.valueOf(70))
                        .disputeRecord(BigDecimal.valueOf(90))
                        .communityFeedback(BigDecimal.valueOf(65))
                        .build())
                .manualAdjustments(List.of())
                .build();
    }

    public ReputationAdjustmentResponse adjustReputation(UUID adminId, UUID userId, ReputationAdjustmentRequest request) {
        log.info("Admin {} adjusting reputation for user {} by {}", 
                adminId, userId, request.getAdjustment());

        // Check if large adjustment requires approval
        if (request.getAdjustment().abs().compareTo(BigDecimal.valueOf(20)) > 0 
                && request.getApprovalId() == null) {
            throw new IllegalArgumentException("Large adjustments (>20 points) require approval");
        }

        BigDecimal previousScore = BigDecimal.valueOf(72);
        BigDecimal newScore = previousScore.add(request.getAdjustment());

        // Clamp score between 0 and 100
        if (newScore.compareTo(BigDecimal.ZERO) < 0) {
            newScore = BigDecimal.ZERO;
        } else if (newScore.compareTo(BigDecimal.valueOf(100)) > 0) {
            newScore = BigDecimal.valueOf(100);
        }

        String previousTier = getTierForScore(previousScore);
        String newTier = getTierForScore(newScore);

        // TODO: Apply to database
        auditService.logAction(adminId, "REPUTATION_ADJUSTED", userId,
                String.format("Adjustment: %s, Reason: %s", request.getAdjustment(), request.getReason()));

        return ReputationAdjustmentResponse.builder()
                .userId(userId)
                .previousScore(previousScore)
                .adjustment(request.getAdjustment())
                .newScore(newScore)
                .previousTier(previousTier)
                .newTier(newTier)
                .reason(request.getReason())
                .adminId(adminId)
                .timestamp(Instant.now())
                .build();
    }

    public ReputationAlgorithmConfig getAlgorithmConfig() {
        return ReputationAlgorithmConfig.builder()
                .weights(Weights.builder()
                        .taskCompletion(BigDecimal.valueOf(0.30))
                        .qualityScore(BigDecimal.valueOf(0.25))
                        .timelinessScore(BigDecimal.valueOf(0.20))
                        .disputeRecord(BigDecimal.valueOf(0.15))
                        .communityFeedback(BigDecimal.valueOf(0.10))
                        .build())
                .tierThresholds(TierThresholds.builder()
                        .diamond(90)
                        .platinum(75)
                        .gold(60)
                        .silver(40)
                        .bronze(20)
                        .build())
                .decay(DecaySettings.builder()
                        .enabled(true)
                        .inactivityDaysBeforeDecay(30)
                        .dailyDecayRate(BigDecimal.valueOf(0.1))
                        .minimumScore(10)
                        .build())
                .bonuses(BonusMultipliers.builder()
                        .consecutiveCompletions(BigDecimal.valueOf(1.1))
                        .earlyDelivery(BigDecimal.valueOf(1.05))
                        .highQuality(BigDecimal.valueOf(1.15))
                        .referralBonus(BigDecimal.valueOf(5))
                        .build())
                .build();
    }

    public ReputationAlgorithmConfig updateAlgorithmConfig(UUID adminId, UpdateAlgorithmRequest request) {
        log.info("Admin {} updating reputation algorithm config", adminId);

        if (request.getDaoProposalId() == null) {
            throw new IllegalArgumentException("DAO proposal ID required for algorithm changes");
        }

        auditService.logAction(adminId, "REPUTATION_ALGORITHM_UPDATED", null,
                "DAO Proposal: " + request.getDaoProposalId());

        return getAlgorithmConfig();
    }

    public ReputationAnalyticsResponse getReputationAnalytics(String role, String period) {
        return ReputationAnalyticsResponse.builder()
                .distribution(DistributionStats.builder()
                        .average(BigDecimal.valueOf(65))
                        .median(BigDecimal.valueOf(62))
                        .standardDeviation(BigDecimal.valueOf(18))
                        .histogram(List.of())
                        .build())
                .trendData(List.of())
                .byRole(List.of())
                .movement(MovementStats.builder()
                        .usersImproved(350L)
                        .usersDeclined(120L)
                        .tierUpgrades(45L)
                        .tierDowngrades(20L)
                        .averageChange(BigDecimal.valueOf(2.3))
                        .build())
                .build();
    }

    private String getTierForScore(BigDecimal score) {
        int s = score.intValue();
        if (s >= 90) return "DIAMOND";
        if (s >= 75) return "PLATINUM";
        if (s >= 60) return "GOLD";
        if (s >= 40) return "SILVER";
        if (s >= 20) return "BRONZE";
        return "NEWCOMER";
    }
}
