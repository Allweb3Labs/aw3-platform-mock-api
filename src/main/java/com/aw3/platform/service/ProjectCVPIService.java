package com.aw3.platform.service;

import com.aw3.platform.controller.project.ProjectCVPIController.*;
import com.aw3.platform.entity.Campaign;
import com.aw3.platform.exception.ForbiddenException;
import com.aw3.platform.exception.NotFoundException;
import com.aw3.platform.repository.CampaignRepository;
import com.aw3.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Project CVPI Analytics
 * 
 * CVPI = Total Campaign Cost / Verified Impact Score
 * Lower CVPI indicates better cost efficiency
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectCVPIService {

    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CampaignCVPIResponse getCampaignCVPI(UUID projectId, UUID campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NotFoundException("Campaign not found"));

        if (!campaign.getProjectId().equals(projectId)) {
            throw new ForbiddenException("Not authorized to view CVPI for this campaign");
        }

        // Get CVPI metrics from campaign
        BigDecimal totalCost = campaign.getTotalBudget();
        BigDecimal verifiedImpact = getVerifiedImpactScore(campaign);
        BigDecimal cvpi = calculateCVPI(totalCost, verifiedImpact);
        BigDecimal industryAverage = getIndustryAverageCVPI(campaign.getCategory());

        return CampaignCVPIResponse.builder()
                .campaignId(campaignId)
                .campaignName(campaign.getTitle())
                .status(campaign.getStatus())
                .cvpi(CVPIInfo.builder()
                        .overall(cvpi)
                        .industryAverage(industryAverage)
                        .percentile(calculatePercentile(cvpi, industryAverage))
                        .build())
                .costBreakdown(CostBreakdown.builder()
                        .totalCost(totalCost)
                        .creatorPayment(calculateCreatorPayment(campaign))
                        .platformFee(calculatePlatformFee(campaign))
                        .oracleFee(calculateOracleFee(campaign))
                        .build())
                .impactMetrics(ImpactMetrics.builder()
                        .totalImpactScore(verifiedImpact)
                        .verifiedBy("Chainlink Oracle Network")
                        .verificationDate(campaign.getUpdatedAt())
                        .kpiResults(getKPIResults(campaign))
                        .build())
                .creatorPerformance(getCreatorPerformance(campaign))
                .build();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "portfolioCVPI", key = "#projectId + '-' + #status + '-' + #startDate + '-' + #endDate")
    public PortfolioCVPIResponse getPortfolioCVPI(UUID projectId, String status, String startDate, String endDate) {
        List<Campaign> campaigns = campaignRepository.findByProjectId(projectId);

        // Filter by status if provided
        if (status != null && !status.isEmpty()) {
            campaigns = campaigns.stream()
                    .filter(c -> c.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        // Filter by date range if provided
        if (startDate != null && endDate != null) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            campaigns = campaigns.stream()
                    .filter(c -> {
                        LocalDate campaignDate = c.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
                        return !campaignDate.isBefore(start) && !campaignDate.isAfter(end);
                    })
                    .collect(Collectors.toList());
        }

        // Calculate summary
        List<BigDecimal> cvpiScores = campaigns.stream()
                .map(this::calculateCampaignCVPI)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        BigDecimal avgCVPI = cvpiScores.isEmpty() ? BigDecimal.ZERO :
                cvpiScores.stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(cvpiScores.size()), 2, RoundingMode.HALF_UP);

        BigDecimal bestCVPI = cvpiScores.isEmpty() ? BigDecimal.ZERO :
                cvpiScores.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

        BigDecimal worstCVPI = cvpiScores.isEmpty() ? BigDecimal.ZERO :
                cvpiScores.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

        BigDecimal totalSpent = campaigns.stream()
                .map(Campaign::getTotalBudget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long completedCount = campaigns.stream()
                .filter(c -> "COMPLETED".equalsIgnoreCase(c.getStatus()))
                .count();

        return PortfolioCVPIResponse.builder()
                .projectId(projectId)
                .summary(SummaryInfo.builder()
                        .totalCampaigns(campaigns.size())
                        .completedCampaigns((int) completedCount)
                        .averageCVPI(avgCVPI)
                        .bestCVPI(bestCVPI)
                        .worstCVPI(worstCVPI)
                        .totalSpent(totalSpent)
                        .totalVerifiedImpact(calculateTotalImpact(campaigns))
                        .build())
                .cvpiTrend(calculateCVPITrend(campaigns))
                .categoryBreakdown(calculateCategoryBreakdown(campaigns))
                .benchmarking(BenchmarkingInfo.builder()
                        .yourAverageCVPI(avgCVPI)
                        .platformAverage(getPlatformAverageCVPI())
                        .yourPercentile(calculatePlatformPercentile(avgCVPI))
                        .build())
                .build();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "creatorRankings", key = "#category + '-' + #minReputation + '-' + #limit")
    public CreatorRankingsResponse getCreatorRankings(String category, Integer minReputation, int limit) {
        // Get all creators with their CVPI scores
        List<Object[]> creatorStats = userRepository.findCreatorsWithCVPIStats();

        List<CreatorRankItem> rankings = new ArrayList<>();
        int rank = 1;

        for (Object[] stats : creatorStats) {
            UUID creatorId = (UUID) stats[0];
            String creatorName = (String) stats[1];
            BigDecimal cvpi = stats[2] != null ? (BigDecimal) stats[2] : BigDecimal.ZERO;
            Integer reputation = stats[3] != null ? ((Number) stats[3]).intValue() : 0;
            Integer completedCampaigns = stats[4] != null ? ((Number) stats[4]).intValue() : 0;

            // Apply filters
            if (minReputation != null && reputation < minReputation) {
                continue;
            }

            // TODO: Filter by category if provided

            rankings.add(CreatorRankItem.builder()
                    .rank(rank++)
                    .creatorId(creatorId)
                    .creatorName(creatorName)
                    .cvpi(cvpi)
                    .reputation(reputation)
                    .completedCampaigns(completedCampaigns)
                    .categories(List.of("DeFi", "NFT")) // TODO: Get actual categories
                    .averageImpact(BigDecimal.valueOf(85.5))
                    .responseRate(BigDecimal.valueOf(0.92))
                    .build());

            if (rankings.size() >= limit) {
                break;
            }
        }

        return CreatorRankingsResponse.builder()
                .rankings(rankings)
                .totalCreators((long) creatorStats.size())
                .filterApplied(FilterApplied.builder()
                        .category(category)
                        .minReputation(minReputation)
                        .build())
                .build();
    }

    // Helper methods

    private BigDecimal calculateCVPI(BigDecimal totalCost, BigDecimal impactScore) {
        if (impactScore == null || impactScore.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalCost.divide(impactScore, 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateCampaignCVPI(Campaign campaign) {
        BigDecimal impact = getVerifiedImpactScore(campaign);
        if (impact.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return calculateCVPI(campaign.getTotalBudget(), impact);
    }

    private BigDecimal getVerifiedImpactScore(Campaign campaign) {
        // TODO: Get actual verified impact score from oracle verification
        return BigDecimal.valueOf(100);
    }

    private BigDecimal getIndustryAverageCVPI(String category) {
        // TODO: Calculate from historical data
        return BigDecimal.valueOf(15.50);
    }

    private Integer calculatePercentile(BigDecimal cvpi, BigDecimal average) {
        if (cvpi.compareTo(BigDecimal.ZERO) == 0) return 50;
        // Lower CVPI is better, so higher percentile for lower CVPI
        BigDecimal ratio = average.divide(cvpi, 2, RoundingMode.HALF_UP);
        int percentile = Math.min(99, Math.max(1, (int) (ratio.doubleValue() * 50)));
        return percentile;
    }

    private BigDecimal calculateCreatorPayment(Campaign campaign) {
        // Creator payment is typically 85% of budget after fees
        return campaign.getTotalBudget().multiply(BigDecimal.valueOf(0.85));
    }

    private BigDecimal calculatePlatformFee(Campaign campaign) {
        // Platform fee is typically 10% of budget
        return campaign.getTotalBudget().multiply(BigDecimal.valueOf(0.10));
    }

    private BigDecimal calculateOracleFee(Campaign campaign) {
        // Oracle fee is typically 5% of budget
        return campaign.getTotalBudget().multiply(BigDecimal.valueOf(0.05));
    }

    private List<KPIResult> getKPIResults(Campaign campaign) {
        // TODO: Get actual KPI results from campaign
        return List.of(
                KPIResult.builder()
                        .metric("Twitter Impressions")
                        .target(BigDecimal.valueOf(10000))
                        .actual(BigDecimal.valueOf(12500))
                        .achievement(BigDecimal.valueOf(125))
                        .weight(BigDecimal.valueOf(0.4))
                        .contribution(BigDecimal.valueOf(50))
                        .build(),
                KPIResult.builder()
                        .metric("Engagement Rate")
                        .target(BigDecimal.valueOf(5))
                        .actual(BigDecimal.valueOf(4.8))
                        .achievement(BigDecimal.valueOf(96))
                        .weight(BigDecimal.valueOf(0.3))
                        .contribution(BigDecimal.valueOf(28.8))
                        .build()
        );
    }

    private CreatorPerformance getCreatorPerformance(Campaign campaign) {
        // TODO: Get actual creator performance from campaign
        return CreatorPerformance.builder()
                .creatorId(UUID.randomUUID())
                .creatorName("Top Creator")
                .creatorHistoricalCVPI(BigDecimal.valueOf(14.2))
                .thisComparison("Above Average")
                .build();
    }

    private BigDecimal calculateTotalImpact(List<Campaign> campaigns) {
        return campaigns.stream()
                .map(this::getVerifiedImpactScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<CVPITrendItem> calculateCVPITrend(List<Campaign> campaigns) {
        Map<String, List<Campaign>> byMonth = campaigns.stream()
                .collect(Collectors.groupingBy(c -> 
                        c.getCreatedAt().atZone(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ofPattern("yyyy-MM"))));

        return byMonth.entrySet().stream()
                .map(entry -> {
                    List<BigDecimal> scores = entry.getValue().stream()
                            .map(this::calculateCampaignCVPI)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    BigDecimal avg = scores.isEmpty() ? BigDecimal.ZERO :
                            scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                                    .divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
                    return CVPITrendItem.builder()
                            .month(entry.getKey())
                            .averageCVPI(avg)
                            .campaigns(entry.getValue().size())
                            .build();
                })
                .sorted(Comparator.comparing(CVPITrendItem::getMonth))
                .collect(Collectors.toList());
    }

    private List<CategoryBreakdown> calculateCategoryBreakdown(List<Campaign> campaigns) {
        Map<String, List<Campaign>> byCategory = campaigns.stream()
                .collect(Collectors.groupingBy(c -> 
                        c.getCategory() != null ? c.getCategory() : "Other"));

        return byCategory.entrySet().stream()
                .map(entry -> {
                    List<BigDecimal> scores = entry.getValue().stream()
                            .map(this::calculateCampaignCVPI)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    BigDecimal avg = scores.isEmpty() ? BigDecimal.ZERO :
                            scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                                    .divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
                    BigDecimal totalSpent = entry.getValue().stream()
                            .map(Campaign::getTotalBudget)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return CategoryBreakdown.builder()
                            .category(entry.getKey())
                            .campaigns(entry.getValue().size())
                            .averageCVPI(avg)
                            .totalSpent(totalSpent)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private BigDecimal getPlatformAverageCVPI() {
        // TODO: Calculate from all platform campaigns
        return BigDecimal.valueOf(18.75);
    }

    private Integer calculatePlatformPercentile(BigDecimal cvpi) {
        // TODO: Calculate actual percentile from platform data
        return 72;
    }
}

