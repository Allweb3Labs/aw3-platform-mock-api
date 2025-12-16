package com.aw3.platform.service;

import com.aw3.platform.dto.admin.FeeConfigResponse;
import com.aw3.platform.dto.admin.RevenueDashboardResponse;
import com.aw3.platform.entity.PlatformFee;
import com.aw3.platform.repository.CampaignRepository;
import com.aw3.platform.repository.PlatformFeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for admin financial configuration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminFinanceService {

    private final PlatformFeeRepository platformFeeRepository;
    private final CampaignRepository campaignRepository;

    /**
     * Get all fee configuration rules
     */
    public FeeConfigResponse getFeeConfigs() {
        // TODO: Create a separate FeeConfig entity for configuration management
        // For now, return default configuration
        List<FeeConfigResponse.ConfigItem> configs = List.of(
            FeeConfigResponse.ConfigItem.builder()
                .key("base_rate_tier_1")
                .value(0.10)
                .description("Base rate for campaigns $0-$5k")
                .lastUpdated(Instant.now())
                .build(),
            FeeConfigResponse.ConfigItem.builder()
                .key("base_rate_tier_2")
                .value(0.08)
                .description("Base rate for campaigns $5k-$20k")
                .lastUpdated(Instant.now())
                .build(),
            FeeConfigResponse.ConfigItem.builder()
                .key("reputation_discount_tier_s")
                .value(0.40)
                .description("Discount for reputation 900+")
                .lastUpdated(Instant.now())
                .build(),
            FeeConfigResponse.ConfigItem.builder()
                .key("aw3_token_discount")
                .value(0.20)
                .description("Discount when paying with AW3 tokens")
                .lastUpdated(Instant.now())
                .build()
        );

        return FeeConfigResponse.builder()
            .version("v2.1")
            .effectiveDate(Instant.now())
            .configs(configs)
            .build();
    }

    /**
     * Get revenue dashboard data
     */
    public RevenueDashboardResponse getRevenueDashboard(String period) {
        // Calculate period range
        Instant startDate = calculateStartDate(period);
        
        // TODO: Calculate actual revenue from completed campaigns
        Double totalRevenue = 125000.0; // Mock data
        Long totalCampaigns = campaignRepository.count();
        
        // Build summary
        RevenueDashboardResponse.Summary summary = RevenueDashboardResponse.Summary.builder()
            .totalRevenue(totalRevenue)
            .totalCampaigns(totalCampaigns.intValue())
            .averageRevenuePerCampaign(totalRevenue / totalCampaigns)
            .build();

        // Build distribution (5-layer model)
        RevenueDashboardResponse.Distribution distribution = buildDistribution(totalRevenue);

        // Build trends
        List<RevenueDashboardResponse.TrendData> trends = buildTrends(period);

        // Build blockchain info
        RevenueDashboardResponse.BlockchainInfo blockchain = RevenueDashboardResponse.BlockchainInfo.builder()
            .chainId(8453)
            .chainName("Base")
            .feeDistributorContract("0xabcd...1234")
            .lastDistributionBlock(12345678L)
            .lastDistributionTime(Instant.now().minus(1, ChronoUnit.DAYS))
            .build();

        return RevenueDashboardResponse.builder()
            .period(period)
            .summary(summary)
            .distribution(distribution)
            .trends(trends)
            .blockchain(blockchain)
            .build();
    }

    private RevenueDashboardResponse.Distribution buildDistribution(Double totalRevenue) {
        Map<String, Double> treasurySubAlloc = new HashMap<>();
        treasurySubAlloc.put("operations", totalRevenue * 0.30);
        treasurySubAlloc.put("development", totalRevenue * 0.15);
        treasurySubAlloc.put("reserves", totalRevenue * 0.05);

        Map<String, Double> aiSubAlloc = new HashMap<>();
        aiSubAlloc.put("chainlink", totalRevenue * 0.075);
        aiSubAlloc.put("openai", totalRevenue * 0.045);
        aiSubAlloc.put("infrastructure", totalRevenue * 0.03);

        return RevenueDashboardResponse.Distribution.builder()
            .treasury(RevenueDashboardResponse.AllocationInfo.builder()
                .amount(totalRevenue * 0.50)
                .percentage(50.0)
                .description("Operational expenses, development, liquidity reserves")
                .subAllocations(treasurySubAlloc)
                .build())
            .validatorIncentives(RevenueDashboardResponse.AllocationInfo.builder()
                .amount(totalRevenue * 0.20)
                .percentage(20.0)
                .description("Staking rewards, oracle operations, dispute resolution")
                .build())
            .aiEcosystem(RevenueDashboardResponse.AllocationInfo.builder()
                .amount(totalRevenue * 0.15)
                .percentage(15.0)
                .description("Chainlink subscriptions, OpenAI API, infrastructure scaling")
                .subAllocations(aiSubAlloc)
                .build())
            .daoTreasury(RevenueDashboardResponse.AllocationInfo.builder()
                .amount(totalRevenue * 0.10)
                .percentage(10.0)
                .description("Community grants, governance incentives")
                .build())
            .buybackAndBurn(RevenueDashboardResponse.AllocationInfo.builder()
                .amount(totalRevenue * 0.05)
                .percentage(5.0)
                .description("Token buyback and burn mechanism")
                .build())
            .build();
    }

    private List<RevenueDashboardResponse.TrendData> buildTrends(String period) {
        // TODO: Calculate from actual data
        List<RevenueDashboardResponse.TrendData> trends = new ArrayList<>();
        
        for (int i = 0; i < 30; i++) {
            LocalDate date = LocalDate.now().minusDays(29 - i);
            Double dailyRevenue = 4500.0;
            
            trends.add(RevenueDashboardResponse.TrendData.builder()
                .date(date)
                .totalRevenue(dailyRevenue)
                .treasury(dailyRevenue * 0.50)
                .validators(dailyRevenue * 0.20)
                .ai(dailyRevenue * 0.15)
                .dao(dailyRevenue * 0.10)
                .buyback(dailyRevenue * 0.05)
                .build());
        }
        
        return trends;
    }

    private Instant calculateStartDate(String period) {
        return switch (period) {
            case "7d" -> Instant.now().minus(7, ChronoUnit.DAYS);
            case "30d" -> Instant.now().minus(30, ChronoUnit.DAYS);
            case "90d" -> Instant.now().minus(90, ChronoUnit.DAYS);
            case "1y" -> Instant.now().minus(365, ChronoUnit.DAYS);
            default -> Instant.now().minus(30, ChronoUnit.DAYS);
        };
    }
}

