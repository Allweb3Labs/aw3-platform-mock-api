package com.aw3.platform.service;

import com.aw3.platform.controller.creator.CreatorReputationController;
import com.aw3.platform.dto.creator.ReputationResponse;
import com.aw3.platform.entity.ReputationRecord;
import com.aw3.platform.entity.User;
import com.aw3.platform.exception.NotFoundException;
import com.aw3.platform.repository.ApplicationRepository;
import com.aw3.platform.repository.CVPIScoreRepository;
import com.aw3.platform.repository.ReputationRecordRepository;
import com.aw3.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Reputation Service
 * 
 * Reputation Tiers:
 * - S: 900+ (Elite, 40% fee discount)
 * - A: 800-899 (30% fee discount)
 * - B: 700-799 (20% fee discount)
 * - C: 600-699 (10% fee discount)
 * - Newcomer: <600 (No discount)
 * 
 * Reputation Breakdown Components:
 * - Campaign Completion (max 200)
 * - Quality Score (max 250)
 * - CVPI Performance (max 250)
 * - Client Satisfaction (max 200)
 * - Community Engagement (max 100)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReputationService {

    private final UserRepository userRepository;
    private final ReputationRecordRepository reputationRecordRepository;
    private final ApplicationRepository applicationRepository;
    private final CVPIScoreRepository cvpiScoreRepository;

    /**
     * Get creator's current reputation details
     */
    public ReputationResponse getCreatorReputation(UUID creatorId) {
        User user = userRepository.findById(creatorId)
                .orElseThrow(() -> new NotFoundException("Creator not found"));

        BigDecimal score = user.getReputationScore() != null ? user.getReputationScore() : BigDecimal.ZERO;
        String tier = calculateTier(score);
        
        // Get campaign statistics
        long totalCampaigns = applicationRepository.countByCreatorId(creatorId);
        long completedCampaigns = applicationRepository.countCompletedByCreatorId(creatorId);
        BigDecimal completionRate = totalCampaigns > 0 
                ? BigDecimal.valueOf(completedCampaigns * 100.0 / totalCampaigns).setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Get CVPI average
        BigDecimal avgCVPI = cvpiScoreRepository.getAverageCvpiByCreatorId(creatorId);
        if (avgCVPI == null) avgCVPI = BigDecimal.ZERO;

        // Calculate rank and percentile
        int rank = calculateRank(score);
        BigDecimal percentile = calculatePercentile(score);

        // Get reputation breakdown
        ReputationResponse.BreakdownInfo breakdown = calculateBreakdown(creatorId, score);

        // Calculate next tier requirements
        ReputationResponse.NextTierInfo nextTier = calculateNextTier(score, tier);

        return ReputationResponse.builder()
                .userId(creatorId)
                .reputation(ReputationResponse.ReputationInfo.builder()
                        .score(score)
                        .tier(tier)
                        .rank(rank)
                        .percentile(percentile)
                        .build())
                .breakdown(breakdown)
                .stats(ReputationResponse.StatsInfo.builder()
                        .totalCampaigns((int) totalCampaigns)
                        .completedCampaigns((int) completedCampaigns)
                        .completionRate(completionRate)
                        .avgCVPI(avgCVPI)
                        .avgClientRating(new BigDecimal("4.7")) // Mock for MVP
                        .build())
                .benefits(getBenefitsForTier(tier))
                .nextTier(nextTier)
                .build();
    }

    /**
     * Get reputation history for a time period
     */
    public CreatorReputationController.ReputationHistoryResponse getReputationHistory(
            UUID creatorId, String period, int limit) {
        
        User user = userRepository.findById(creatorId)
                .orElseThrow(() -> new NotFoundException("Creator not found"));

        Instant startDate = calculateStartDate(period);
        
        List<ReputationRecord> records = reputationRecordRepository.findByUserIdAndDateRange(
                creatorId, startDate, Instant.now(), PageRequest.of(0, limit)
        ).getContent();

        // Calculate score changes
        BigDecimal currentScore = user.getReputationScore() != null ? user.getReputationScore() : BigDecimal.ZERO;
        int scoreChange30d = calculateScoreChange(records, 30);
        int scoreChange90d = calculateScoreChange(records, 90);

        List<CreatorReputationController.HistoryItem> historyItems = records.stream()
                .map(record -> CreatorReputationController.HistoryItem.builder()
                        .date(record.getCreatedAt())
                        .action(record.getActionType())
                        .campaignId(record.getCampaignId())
                        .scoreChange(record.getScoreChange())
                        .newScore(record.getNewScore())
                        .reason(record.getReason())
                        .build())
                .collect(Collectors.toList());

        // Generate timeline data
        List<CreatorReputationController.TimelineItem> timeline = generateTimeline(records);

        return CreatorReputationController.ReputationHistoryResponse.builder()
                .currentScore(currentScore)
                .scoreChange30d(scoreChange30d)
                .scoreChange90d(scoreChange90d)
                .history(historyItems)
                .timeline(timeline)
                .build();
    }

    /**
     * Get creator's SPC NFT collection
     */
    public CreatorReputationController.SPCListResponse getCreatorSPCNFTs(
            UUID creatorId, String status, int limit, int offset) {
        
        // Mock SPC data for MVP
        List<CreatorReputationController.SPCItem> spcs = new ArrayList<>();
        
        for (int i = 0; i < Math.min(limit, 5); i++) {
            spcs.add(CreatorReputationController.SPCItem.builder()
                    .spcId(UUID.randomUUID())
                    .tokenId(String.valueOf(12345 + i))
                    .campaignId(UUID.randomUUID())
                    .campaignTitle("Campaign " + (i + 1))
                    .contractAddress("0x5678...efgh")
                    .chainId(8453)
                    .mintedAt(Instant.now().minus(i * 7, ChronoUnit.DAYS))
                    .metadata(CreatorReputationController.SPCMetadata.builder()
                            .image("https://cdn.aw3.io/spc/" + (12345 + i) + ".png")
                            .cvpi(new BigDecimal("68"))
                            .performanceScore(new BigDecimal("113.3"))
                            .category("DeFi")
                            .build())
                    .status("owned")
                    .marketValue(new BigDecimal("250"))
                    .royaltyPercentage(new BigDecimal("5"))
                    .royaltiesEarned(new BigDecimal("12.50"))
                    .build());
        }

        return CreatorReputationController.SPCListResponse.builder()
                .totalSPCs(40)
                .ownedSPCs(32)
                .listedSPCs(5)
                .soldSPCs(3)
                .totalRoyaltiesEarned(new BigDecimal("450"))
                .spcs(spcs)
                .build();
    }

    private String calculateTier(BigDecimal score) {
        int scoreInt = score.intValue();
        if (scoreInt >= 900) return "S";
        if (scoreInt >= 800) return "A";
        if (scoreInt >= 700) return "B";
        if (scoreInt >= 600) return "C";
        return "Newcomer";
    }

    private int calculateRank(BigDecimal score) {
        // Simplified ranking calculation
        return Math.max(1, 5420 - score.intValue() * 5);
    }

    private BigDecimal calculatePercentile(BigDecimal score) {
        int scoreInt = score.intValue();
        if (scoreInt >= 900) return new BigDecimal("99");
        if (scoreInt >= 800) return new BigDecimal("95");
        if (scoreInt >= 700) return new BigDecimal("85");
        if (scoreInt >= 600) return new BigDecimal("70");
        return new BigDecimal("50");
    }

    private ReputationResponse.BreakdownInfo calculateBreakdown(UUID creatorId, BigDecimal totalScore) {
        // Distribute score across components (simplified)
        int total = totalScore.intValue();
        return ReputationResponse.BreakdownInfo.builder()
                .campaignCompletion(Math.min(200, total * 200 / 1000))
                .qualityScore(Math.min(250, total * 250 / 1000))
                .cvpiPerformance(Math.min(250, total * 250 / 1000))
                .clientSatisfaction(Math.min(200, total * 200 / 1000))
                .communityEngagement(Math.min(100, total * 100 / 1000))
                .build();
    }

    private ReputationResponse.NextTierInfo calculateNextTier(BigDecimal score, String currentTier) {
        int scoreInt = score.intValue();
        
        return switch (currentTier) {
            case "A" -> ReputationResponse.NextTierInfo.builder()
                    .tier("S")
                    .requiredScore(new BigDecimal("900"))
                    .pointsNeeded(new BigDecimal(900 - scoreInt))
                    .estimatedCampaigns((900 - scoreInt) / 15)
                    .build();
            case "B" -> ReputationResponse.NextTierInfo.builder()
                    .tier("A")
                    .requiredScore(new BigDecimal("800"))
                    .pointsNeeded(new BigDecimal(800 - scoreInt))
                    .estimatedCampaigns((800 - scoreInt) / 15)
                    .build();
            case "C" -> ReputationResponse.NextTierInfo.builder()
                    .tier("B")
                    .requiredScore(new BigDecimal("700"))
                    .pointsNeeded(new BigDecimal(700 - scoreInt))
                    .estimatedCampaigns((700 - scoreInt) / 15)
                    .build();
            case "Newcomer" -> ReputationResponse.NextTierInfo.builder()
                    .tier("C")
                    .requiredScore(new BigDecimal("600"))
                    .pointsNeeded(new BigDecimal(600 - scoreInt))
                    .estimatedCampaigns((600 - scoreInt) / 15)
                    .build();
            default -> null; // S tier - already at top
        };
    }

    private ReputationResponse.BenefitsInfo getBenefitsForTier(String tier) {
        return switch (tier) {
            case "S" -> ReputationResponse.BenefitsInfo.builder()
                    .priorityApplications(true)
                    .higherPayoutPotential(true)
                    .accessToExclusiveCampaigns(true)
                    .reducedPlatformFees(new BigDecimal("0.40"))
                    .build();
            case "A" -> ReputationResponse.BenefitsInfo.builder()
                    .priorityApplications(true)
                    .higherPayoutPotential(true)
                    .accessToExclusiveCampaigns(true)
                    .reducedPlatformFees(new BigDecimal("0.30"))
                    .build();
            case "B" -> ReputationResponse.BenefitsInfo.builder()
                    .priorityApplications(true)
                    .higherPayoutPotential(true)
                    .accessToExclusiveCampaigns(false)
                    .reducedPlatformFees(new BigDecimal("0.20"))
                    .build();
            case "C" -> ReputationResponse.BenefitsInfo.builder()
                    .priorityApplications(false)
                    .higherPayoutPotential(false)
                    .accessToExclusiveCampaigns(false)
                    .reducedPlatformFees(new BigDecimal("0.10"))
                    .build();
            default -> ReputationResponse.BenefitsInfo.builder()
                    .priorityApplications(false)
                    .higherPayoutPotential(false)
                    .accessToExclusiveCampaigns(false)
                    .reducedPlatformFees(BigDecimal.ZERO)
                    .build();
        };
    }

    private Instant calculateStartDate(String period) {
        return switch (period) {
            case "30d" -> Instant.now().minus(30, ChronoUnit.DAYS);
            case "1y" -> Instant.now().minus(365, ChronoUnit.DAYS);
            case "all" -> Instant.EPOCH;
            default -> Instant.now().minus(90, ChronoUnit.DAYS);
        };
    }

    private int calculateScoreChange(List<ReputationRecord> records, int days) {
        Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);
        return records.stream()
                .filter(r -> r.getCreatedAt().isAfter(cutoff))
                .mapToInt(ReputationRecord::getScoreChange)
                .sum();
    }

    private List<CreatorReputationController.TimelineItem> generateTimeline(List<ReputationRecord> records) {
        // Group by month and generate timeline
        Map<String, List<ReputationRecord>> byMonth = records.stream()
                .collect(Collectors.groupingBy(r -> {
                    Instant date = r.getCreatedAt();
                    return date.toString().substring(0, 7); // YYYY-MM format
                }));

        return byMonth.entrySet().stream()
                .sorted(Map.Entry.<String, List<ReputationRecord>>comparingByKey().reversed())
                .limit(6)
                .map(entry -> {
                    List<ReputationRecord> monthRecords = entry.getValue();
                    BigDecimal lastScore = monthRecords.isEmpty() ? BigDecimal.ZERO 
                            : monthRecords.get(0).getNewScore();
                    
                    return CreatorReputationController.TimelineItem.builder()
                            .month(entry.getKey())
                            .score(lastScore)
                            .campaignsCompleted(monthRecords.size())
                            .build();
                })
                .collect(Collectors.toList());
    }
}

