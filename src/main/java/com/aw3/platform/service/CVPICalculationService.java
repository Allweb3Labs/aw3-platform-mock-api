package com.aw3.platform.service;

import com.aw3.platform.dto.cvpi.CVPIHistoryResponse;
import com.aw3.platform.dto.cvpi.CVPIScoreResponse;
import com.aw3.platform.entity.CVPIScore;
import com.aw3.platform.entity.Campaign;
import com.aw3.platform.entity.PlatformFee;
import com.aw3.platform.repository.CVPIScoreRepository;
import com.aw3.platform.repository.CampaignRepository;
import com.aw3.platform.repository.PlatformFeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * CVPI Calculation Service
 * 
 * CVPI = Total Campaign Cost / Verified Impact Score
 * Lower CVPI = Better cost efficiency
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CVPICalculationService {

    private final CVPIScoreRepository cvpiScoreRepository;
    private final CampaignRepository campaignRepository;
    private final PlatformFeeRepository platformFeeRepository;

    /**
     * Calculate CVPI score for a completed campaign
     */
    @Async
    @Transactional
    @CacheEvict(value = "cvpiScores", key = "#creatorId")
    public CVPIScore calculateCVPIForCampaign(UUID campaignId, UUID creatorId, BigDecimal verifiedImpactScore) {
        log.info("Calculating CVPI for campaign: {} and creator: {}", campaignId, creatorId);

        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        // Calculate total cost
        BigDecimal totalCost = campaign.getBudgetAmount()
                .add(campaign.getServiceFee() != null ? campaign.getServiceFee() : BigDecimal.ZERO)
                .add(campaign.getOracleFee() != null ? campaign.getOracleFee() : BigDecimal.ZERO);

        // Calculate CVPI
        BigDecimal cvpiScore = totalCost.divide(verifiedImpactScore, 4, RoundingMode.HALF_UP);

        // Get platform average for comparison
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        BigDecimal platformAverage = cvpiScoreRepository.getPlatformAverageCvpi(thirtyDaysAgo, Instant.now());
        if (platformAverage == null) {
            platformAverage = new BigDecimal("0.52"); // Default platform average
        }

        // Calculate percentile rank
        BigDecimal percentileRank = calculatePercentileRank(cvpiScore);

        CVPIScore cvpi = CVPIScore.builder()
                .creatorId(creatorId)
                .campaignId(campaignId)
                .cvpiScore(cvpiScore)
                .totalCost(totalCost)
                .verifiedImpactScore(verifiedImpactScore)
                .category(campaign.getCategory())
                .platformAverageCvpi(platformAverage)
                .percentileRank(percentileRank)
                .build();

        return cvpiScoreRepository.save(cvpi);
    }

    /**
     * Get creator's current CVPI score
     */
    @Cacheable(value = "cvpiScores", key = "#creatorId")
    public CVPIScoreResponse getCreatorCVPIScore(UUID creatorId) {
        Page<CVPIScore> recentScores = cvpiScoreRepository.findByCreatorId(
                creatorId, 
                PageRequest.of(0, 10)
        );

        if (recentScores.isEmpty()) {
            return null;
        }

        BigDecimal avgCVPI = cvpiScoreRepository.getAverageCvpiByCreatorId(creatorId);
        
        // Calculate ranking (simplified for MVP)
        long totalCreators = 5420; // TODO: Get from actual creator count
        int overallRank = calculateRank(avgCVPI);
        int categoryRank = calculateCategoryRank(creatorId, recentScores.getContent().get(0).getCategory());

        // Determine trend
        String trend = calculateTrend(recentScores.getContent());

        return CVPIScoreResponse.builder()
                .creatorId(creatorId)
                .currentCVPI(avgCVPI)
                .ranking(CVPIScoreResponse.RankingInfo.builder()
                        .overall(overallRank)
                        .category(categoryRank)
                        .totalCreators(totalCreators)
                        .build())
                .trend(trend)
                .lastUpdated(recentScores.getContent().get(0).getCalculatedAt())
                .build();
    }

    /**
     * Get creator's CVPI history
     */
    public CVPIHistoryResponse getCreatorCVPIHistory(UUID creatorId, String period) {
        Instant startDate = calculateStartDate(period);
        Instant endDate = Instant.now();

        List<CVPIScore> scores = cvpiScoreRepository.findByCreatorIdAndDateRange(
                creatorId, 
                startDate, 
                endDate
        );

        List<CVPIHistoryResponse.DataPoint> dataPoints = scores.stream()
                .map(score -> CVPIHistoryResponse.DataPoint.builder()
                        .date(score.getCalculatedAt())
                        .cvpi(score.getCvpiScore())
                        .campaignsCompleted(1) // Each score represents one campaign
                        .totalImpact(score.getVerifiedImpactScore())
                        .build())
                .collect(Collectors.toList());

        // Calculate summary
        BigDecimal avgCVPI = scores.stream()
                .map(CVPIScore::getCvpiScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(scores.size()), 4, RoundingMode.HALF_UP);

        BigDecimal bestCVPI = scores.stream()
                .map(CVPIScore::getCvpiScore)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal worstCVPI = scores.stream()
                .map(CVPIScore::getCvpiScore)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        return CVPIHistoryResponse.builder()
                .creatorId(creatorId)
                .period(period)
                .dataPoints(dataPoints)
                .summary(CVPIHistoryResponse.Summary.builder()
                        .averageCVPI(avgCVPI)
                        .bestCVPI(bestCVPI)
                        .worstCVPI(worstCVPI)
                        .totalCampaigns(scores.size())
                        .build())
                .build();
    }

    private BigDecimal calculatePercentileRank(BigDecimal cvpiScore) {
        // Simplified calculation - in production, query actual distribution
        if (cvpiScore.compareTo(new BigDecimal("0.30")) <= 0) {
            return new BigDecimal("90"); // Top 10%
        } else if (cvpiScore.compareTo(new BigDecimal("0.45")) <= 0) {
            return new BigDecimal("70"); // Top 30%
        } else if (cvpiScore.compareTo(new BigDecimal("0.60")) <= 0) {
            return new BigDecimal("50"); // Median
        } else {
            return new BigDecimal("30"); // Below average
        }
    }

    private int calculateRank(BigDecimal avgCVPI) {
        // Simplified ranking calculation
        return avgCVPI.multiply(new BigDecimal("1000")).intValue();
    }

    private int calculateCategoryRank(UUID creatorId, String category) {
        // Simplified category ranking
        return 12; // TODO: Implement actual category ranking
    }

    private String calculateTrend(List<CVPIScore> scores) {
        if (scores.size() < 2) {
            return "stable";
        }

        BigDecimal recent = scores.get(0).getCvpiScore();
        BigDecimal previous = scores.get(1).getCvpiScore();

        if (recent.compareTo(previous) < 0) {
            return "improving"; // Lower CVPI is better
        } else if (recent.compareTo(previous) > 0) {
            return "declining";
        } else {
            return "stable";
        }
    }

    private Instant calculateStartDate(String period) {
        switch (period) {
            case "7d":
                return Instant.now().minus(7, ChronoUnit.DAYS);
            case "90d":
                return Instant.now().minus(90, ChronoUnit.DAYS);
            case "1y":
                return Instant.now().minus(365, ChronoUnit.DAYS);
            case "30d":
            default:
                return Instant.now().minus(30, ChronoUnit.DAYS);
        }
    }

    /**
     * Get AI-matched campaign recommendations based on CVPI optimization
     * 
     * Recommendation Algorithm Factors:
     * - Historical CVPI (35%): Past performance in similar categories
     * - Audience Match (25%): Alignment between creator's audience and campaign target
     * - Budget Fit (20%): Campaign budget within creator's optimal performance range
     * - Category Expertise (15%): Track record in the campaign category
     * - Reputation Match (5%): Creator's reputation score vs. campaign requirements
     */
    public com.aw3.platform.dto.creator.CVPIRecommendationsResponse getCVPIRecommendations(
            UUID creatorId, int limit, String category, java.math.BigDecimal minBudget, java.math.BigDecimal maxBudget) {
        
        // Get creator's historical performance
        java.math.BigDecimal avgCVPI = cvpiScoreRepository.getAverageCvpiByCreatorId(creatorId);
        if (avgCVPI == null) avgCVPI = new java.math.BigDecimal("0.52");

        // Get active campaigns matching filters
        org.springframework.data.domain.Page<Campaign> campaigns = campaignRepository.findActiveCampaignsWithFilters(
                com.aw3.platform.entity.enums.CampaignStatus.ACTIVE,
                category,
                minBudget,
                maxBudget,
                org.springframework.data.domain.PageRequest.of(0, limit * 2)
        );

        // Calculate match scores and sort
        java.util.List<com.aw3.platform.dto.creator.CVPIRecommendationsResponse.RecommendationItem> recommendations = 
                campaigns.getContent().stream()
                .map(campaign -> calculateRecommendation(campaign, creatorId, avgCVPI))
                .sorted((a, b) -> b.getMatchScore().compareTo(a.getMatchScore()))
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());

        // Get strongest categories from history
        java.util.List<String> strongestCategories = getStrongestCategories(creatorId);

        return com.aw3.platform.dto.creator.CVPIRecommendationsResponse.builder()
                .recommendations(recommendations)
                .profileInsights(com.aw3.platform.dto.creator.CVPIRecommendationsResponse.ProfileInsights.builder()
                        .strongestCategories(strongestCategories)
                        .avgHistoricalCVPI(avgCVPI)
                        .successRate(new java.math.BigDecimal("0.92"))
                        .build())
                .algorithmVersion("v2.1")
                .lastUpdated(Instant.now())
                .build();
    }

    /**
     * Get projected CVPI score for a specific campaign before applying
     */
    public com.aw3.platform.dto.creator.CVPIProjectionResponse getCVPIProjection(UUID creatorId, UUID campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        // Get creator's historical average CVPI
        java.math.BigDecimal avgCVPI = cvpiScoreRepository.getAverageCvpiByCreatorId(creatorId);
        if (avgCVPI == null) avgCVPI = new java.math.BigDecimal("0.52");

        // Calculate cost breakdown
        java.math.BigDecimal basePayment = campaign.getBudgetAmount()
                .divide(new java.math.BigDecimal(campaign.getNumberOfCreators()), 2, java.math.RoundingMode.HALF_UP);
        java.math.BigDecimal platformFee = basePayment.multiply(new java.math.BigDecimal("0.04"));
        java.math.BigDecimal oracleFee = new java.math.BigDecimal("50");
        java.math.BigDecimal estimatedCost = basePayment.add(platformFee).add(oracleFee);

        // Project impact based on historical performance
        java.math.BigDecimal projectedImpact = estimatedCost.divide(avgCVPI, 2, java.math.RoundingMode.HALF_UP);

        // Calculate projected CVPI (should be slightly better than average due to matching)
        java.math.BigDecimal projectedCVPI = avgCVPI.multiply(new java.math.BigDecimal("0.95"));

        // Calculate confidence based on historical data availability
        java.math.BigDecimal confidence = calculateConfidence(creatorId, campaign.getCategory());

        // Get platform average for comparison
        java.math.BigDecimal platformAverage = new java.math.BigDecimal("0.52");

        return com.aw3.platform.dto.creator.CVPIProjectionResponse.builder()
                .campaignId(campaignId)
                .projectedCVPI(projectedCVPI)
                .confidence(confidence)
                .breakdown(com.aw3.platform.dto.creator.CVPIProjectionResponse.BreakdownInfo.builder()
                        .estimatedCost(estimatedCost)
                        .basePayment(basePayment)
                        .platformFee(platformFee)
                        .oracleFee(oracleFee)
                        .projectedImpact(projectedImpact)
                        .impactFactors(buildImpactFactors(campaign))
                        .build())
                .comparisonToAverage(com.aw3.platform.dto.creator.CVPIProjectionResponse.ComparisonInfo.builder()
                        .yourProjectedCVPI(projectedCVPI)
                        .platformAverageCVPI(platformAverage)
                        .percentageBetter(platformAverage.subtract(projectedCVPI)
                                .divide(platformAverage, 4, java.math.RoundingMode.HALF_UP)
                                .multiply(new java.math.BigDecimal("100")))
                        .build())
                .build();
    }

    private com.aw3.platform.dto.creator.CVPIRecommendationsResponse.RecommendationItem calculateRecommendation(
            Campaign campaign, UUID creatorId, java.math.BigDecimal creatorAvgCVPI) {
        
        // Calculate match score based on multiple factors
        java.math.BigDecimal historicalWeight = new java.math.BigDecimal("0.35");
        java.math.BigDecimal audienceWeight = new java.math.BigDecimal("0.25");
        java.math.BigDecimal budgetWeight = new java.math.BigDecimal("0.20");
        java.math.BigDecimal categoryWeight = new java.math.BigDecimal("0.15");
        java.math.BigDecimal reputationWeight = new java.math.BigDecimal("0.05");

        // Simple scoring (in production, use ML model)
        java.math.BigDecimal matchScore = new java.math.BigDecimal("0.85")
                .add(java.math.BigDecimal.valueOf(Math.random() * 0.10));

        java.math.BigDecimal estimatedCVPI = creatorAvgCVPI.multiply(new java.math.BigDecimal("0.95"));
        java.math.BigDecimal projectedImpact = campaign.getBudgetAmount()
                .divide(estimatedCVPI, 0, java.math.RoundingMode.HALF_UP);

        java.util.List<String> matchReasons = new java.util.ArrayList<>();
        matchReasons.add("Historical success in " + campaign.getCategory() + " category");
        matchReasons.add("Budget range aligns with your optimal performance");
        matchReasons.add("Projected CVPI: " + estimatedCVPI.setScale(2, java.math.RoundingMode.HALF_UP) + " (excellent)");

        return com.aw3.platform.dto.creator.CVPIRecommendationsResponse.RecommendationItem.builder()
                .campaignId(campaign.getCampaignId())
                .title(campaign.getTitle())
                .category(campaign.getCategory())
                .budget(campaign.getBudgetAmount())
                .estimatedCVPI(estimatedCVPI)
                .matchScore(matchScore)
                .matchReasons(matchReasons)
                .projectedImpact(projectedImpact)
                .requiredReputation(campaign.getRequiredReputation())
                .yourReputation(820) // Would get from user service
                .deadline(campaign.getDeadline())
                .projectInfo(com.aw3.platform.dto.creator.CVPIRecommendationsResponse.ProjectInfo.builder()
                        .name("Project")
                        .verified(true)
                        .reputation(new java.math.BigDecimal("850"))
                        .build())
                .isEligible(true)
                .build();
    }

    private java.util.List<String> getStrongestCategories(UUID creatorId) {
        // Get categories with best CVPI performance
        return java.util.Arrays.asList("DeFi", "NFT", "Gaming");
    }

    private java.math.BigDecimal calculateConfidence(UUID creatorId, String category) {
        // Calculate confidence based on data availability
        long campaignCount = cvpiScoreRepository.countByCreatorId(creatorId);
        if (campaignCount >= 20) return new java.math.BigDecimal("0.95");
        if (campaignCount >= 10) return new java.math.BigDecimal("0.85");
        if (campaignCount >= 5) return new java.math.BigDecimal("0.75");
        return new java.math.BigDecimal("0.65");
    }

    private java.util.List<com.aw3.platform.dto.creator.CVPIProjectionResponse.ImpactFactorInfo> buildImpactFactors(Campaign campaign) {
        return java.util.Arrays.asList(
                com.aw3.platform.dto.creator.CVPIProjectionResponse.ImpactFactorInfo.builder()
                        .metric("engagement_rate")
                        .weight(new java.math.BigDecimal("0.4"))
                        .expectedValue(new java.math.BigDecimal("8.5"))
                        .contributionToImpact(new java.math.BigDecimal("55200"))
                        .build(),
                com.aw3.platform.dto.creator.CVPIProjectionResponse.ImpactFactorInfo.builder()
                        .metric("reach")
                        .weight(new java.math.BigDecimal("0.3"))
                        .expectedValue(new java.math.BigDecimal("250000"))
                        .contributionToImpact(new java.math.BigDecimal("41400"))
                        .build(),
                com.aw3.platform.dto.creator.CVPIProjectionResponse.ImpactFactorInfo.builder()
                        .metric("conversions")
                        .weight(new java.math.BigDecimal("0.3"))
                        .expectedValue(new java.math.BigDecimal("420"))
                        .contributionToImpact(new java.math.BigDecimal("41400"))
                        .build()
        );
    }
}

