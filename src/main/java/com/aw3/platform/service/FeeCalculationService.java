package com.aw3.platform.service;

import com.aw3.platform.dto.project.FeeEstimateRequest;
import com.aw3.platform.dto.project.FeeEstimateResponse;
import com.aw3.platform.entity.User;
import com.aw3.platform.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Fee Calculation Service implementing dynamic pricing from economic model
 * 
 * Economic Model Reference:
 * - Base service fee: 4-10% (budget-tiered)
 * - Reputation discount: 0-40%
 * - Complexity multiplier: 0.8x-1.5x
 * - Oracle verification fee: $15-25 per verification
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FeeCalculationService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Value("${fee.base-rates.tier1-max}")
    private BigDecimal tier1Max;
    
    @Value("${fee.base-rates.tier1-rate}")
    private BigDecimal tier1Rate;
    
    @Value("${fee.base-rates.tier2-max}")
    private BigDecimal tier2Max;
    
    @Value("${fee.base-rates.tier2-rate}")
    private BigDecimal tier2Rate;
    
    @Value("${fee.base-rates.tier3-max}")
    private BigDecimal tier3Max;
    
    @Value("${fee.base-rates.tier3-rate}")
    private BigDecimal tier3Rate;
    
    @Value("${fee.base-rates.tier4-rate}")
    private BigDecimal tier4Rate;

    /**
     * Calculate total platform fees for a campaign based on economic model rules
     */
    public FeeEstimateResponse calculateCampaignFees(UUID projectId, FeeEstimateRequest request) {
        log.info("Calculating fees for project: {} with budget: {}", projectId, request.getCampaignBudget());

        // 1. Determine base rate from budget tier
        BigDecimal baseRate = determineBaseFeeRate(request.getCampaignBudget());
        
        // 2. Calculate base fee
        BigDecimal baseFee = request.getCampaignBudget()
                .multiply(baseRate)
                .setScale(2, RoundingMode.HALF_UP);

        // 3. Apply complexity multiplier
        BigDecimal complexityMultiplier = calculateComplexityMultiplier(
                request.getRequestedCreators(), 
                request.getComplexity()
        );
        
        BigDecimal complexityAdjustedFee = baseFee
                .multiply(complexityMultiplier)
                .setScale(2, RoundingMode.HALF_UP);

        // 4. Calculate reputation-based discount
        User projectUser = userRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project user not found"));
        
        BigDecimal reputationDiscount = calculateReputationDiscount(
                projectUser.getCumulativeSpend(),
                projectUser.getReputationScore()
        );
        
        BigDecimal discountAmount = complexityAdjustedFee
                .multiply(reputationDiscount)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal serviceFeeBeforeToken = complexityAdjustedFee
                .subtract(discountAmount)
                .setScale(2, RoundingMode.HALF_UP);

        // 5. Apply AW3 token discount if enabled
        BigDecimal aw3TokenDiscount = request.getUseAW3Token() ? new BigDecimal("0.20") : BigDecimal.ZERO;
        BigDecimal aw3DiscountAmount = serviceFeeBeforeToken
                .multiply(aw3TokenDiscount)
                .setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal finalServiceFee = serviceFeeBeforeToken
                .subtract(aw3DiscountAmount)
                .setScale(2, RoundingMode.HALF_UP);

        // 6. Calculate oracle fee
        BigDecimal oracleFee = calculateOracleFee(request.getKpiMetrics(), request.getRequestedCreators());
        FeeEstimateResponse.OracleFeeBreakdown oracleBreakdown = buildOracleFeeBreakdown(request);

        // 7. Calculate total fees
        BigDecimal totalFees = finalServiceFee.add(oracleFee);

        // 8. Calculate escrow requirement (120% buffer)
        BigDecimal buffer = request.getCampaignBudget()
                .add(totalFees)
                .multiply(new BigDecimal("0.10"))
                .setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal escrowTotal = request.getCampaignBudget()
                .add(totalFees)
                .add(buffer)
                .setScale(2, RoundingMode.HALF_UP);

        // 9. Build response
        String estimateId = "est-" + UUID.randomUUID().toString().substring(0, 8);
        Instant validUntil = Instant.now().plusSeconds(900); // 15 minutes

        return FeeEstimateResponse.builder()
                .feeEstimateId(estimateId)
                .campaignBudget(request.getCampaignBudget())
                .feeBreakdown(FeeEstimateResponse.FeeBreakdown.builder()
                        .baseRate(baseRate)
                        .baseFee(baseFee)
                        .complexityMultiplier(complexityMultiplier)
                        .complexityAdjustedFee(complexityAdjustedFee)
                        .reputationDiscount(reputationDiscount)
                        .discountAmount(discountAmount)
                        .serviceFeeBeforeToken(serviceFeeBeforeToken)
                        .aw3TokenDiscount(aw3TokenDiscount)
                        .aw3DiscountAmount(aw3DiscountAmount)
                        .finalServiceFee(finalServiceFee)
                        .oracleFee(oracleFee)
                        .oracleFeeBreakdown(oracleBreakdown)
                        .build())
                .totalFees(totalFees)
                .escrowRequirement(FeeEstimateResponse.EscrowRequirement.builder()
                        .campaignBudget(request.getCampaignBudget())
                        .serviceFee(finalServiceFee)
                        .oracleFee(oracleFee)
                        .buffer(buffer)
                        .totalRequired(escrowTotal)
                        .bufferPercentage(new BigDecimal("10"))
                        .build())
                .validUntil(validUntil)
                .signature("0x" + UUID.randomUUID().toString().replace("-", ""))
                .calculationSnapshot(buildCalculationSnapshot(request, projectUser))
                .build();
    }

    /**
     * Determine base fee rate based on campaign budget tiers
     */
    private BigDecimal determineBaseFeeRate(BigDecimal budgetAmount) {
        if (budgetAmount.compareTo(tier1Max) < 0) {
            return tier1Rate; // 10%
        } else if (budgetAmount.compareTo(tier2Max) < 0) {
            return tier2Rate; // 8%
        } else if (budgetAmount.compareTo(tier3Max) < 0) {
            return tier3Rate; // 6%
        } else {
            return tier4Rate; // 4%
        }
    }

    /**
     * Calculate reputation-based discount (0-40%)
     */
    private BigDecimal calculateReputationDiscount(BigDecimal cumulativeSpend, BigDecimal reputationScore) {
        // Spend-based discount component (up to 20%)
        BigDecimal spendDiscount = cumulativeSpend
                .divide(new BigDecimal("100000"), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("0.20"))
                .min(new BigDecimal("0.20"));

        // Loyalty tier bonus
        BigDecimal tierBonus = BigDecimal.ZERO;
        if (cumulativeSpend.compareTo(new BigDecimal("100000")) >= 0) {
            tierBonus = new BigDecimal("0.20"); // Platinum: +20%
        } else if (cumulativeSpend.compareTo(new BigDecimal("50000")) >= 0) {
            tierBonus = new BigDecimal("0.10"); // Gold: +10%
        } else if (cumulativeSpend.compareTo(new BigDecimal("10000")) >= 0) {
            tierBonus = new BigDecimal("0.05"); // Silver: +5%
        }

        // Total discount capped at 40%
        return spendDiscount.add(tierBonus).min(new BigDecimal("0.40"));
    }

    /**
     * Calculate complexity multiplier (0.8x - 1.5x)
     */
    private BigDecimal calculateComplexityMultiplier(Integer numberOfKOLs, String complexity) {
        if (numberOfKOLs == null) numberOfKOLs = 1;
        
        if ("simple".equals(complexity) || numberOfKOLs == 1) {
            return new BigDecimal("0.8");
        } else if ("complex".equals(complexity) || numberOfKOLs > 20) {
            return new BigDecimal("1.5");
        } else if ("enterprise".equals(complexity)) {
            return new BigDecimal("1.5");
        } else if (numberOfKOLs <= 5) {
            return new BigDecimal("1.0");
        } else {
            return new BigDecimal("1.2");
        }
    }

    /**
     * Calculate oracle verification fee ($15-25 per verification)
     */
    private BigDecimal calculateOracleFee(java.util.List<FeeEstimateRequest.KpiMetricInfo> metrics, Integer numberOfKOLs) {
        if (metrics == null || metrics.isEmpty()) {
            return new BigDecimal("50.00"); // Base fee
        }

        if (numberOfKOLs == null) numberOfKOLs = 1;

        BigDecimal baseOracleCost = new BigDecimal("5.00");
        BigDecimal dataSourceCost = BigDecimal.ZERO;

        for (FeeEstimateRequest.KpiMetricInfo metric : metrics) {
            switch (metric.getSource().toUpperCase()) {
                case "TWITTER":
                    dataSourceCost = dataSourceCost.add(new BigDecimal("2.00"));
                    break;
                case "DISCORD":
                case "TELEGRAM":
                    dataSourceCost = dataSourceCost.add(new BigDecimal("1.50"));
                    break;
                case "ONCHAIN":
                    dataSourceCost = dataSourceCost.add(new BigDecimal("3.00"));
                    break;
                case "MEDIA_PUBLICATION":
                    dataSourceCost = dataSourceCost.add(new BigDecimal("4.00"));
                    break;
                default:
                    dataSourceCost = dataSourceCost.add(new BigDecimal("2.00"));
            }
        }

        // Complexity based on number of metrics
        BigDecimal complexityMultiplier;
        if (metrics.size() == 1) {
            complexityMultiplier = new BigDecimal("1.0");
        } else if (metrics.size() <= 3) {
            complexityMultiplier = new BigDecimal("1.3");
        } else {
            complexityMultiplier = new BigDecimal("1.8");
        }

        BigDecimal perKOLFee = baseOracleCost
                .add(dataSourceCost)
                .multiply(complexityMultiplier);

        return perKOLFee.multiply(new BigDecimal(numberOfKOLs))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private FeeEstimateResponse.OracleFeeBreakdown buildOracleFeeBreakdown(FeeEstimateRequest request) {
        int kpiCount = request.getKpiMetrics() != null ? request.getKpiMetrics().size() : 1;
        int additionalKPIs = Math.max(0, kpiCount - 3);
        
        return FeeEstimateResponse.OracleFeeBreakdown.builder()
                .baseFee(new BigDecimal("50.00"))
                .additionalKPIs(additionalKPIs)
                .kpiFee(new BigDecimal(additionalKPIs * 10))
                .complexityPremium(new BigDecimal("20.00"))
                .build();
    }

    private Map<String, Object> buildCalculationSnapshot(FeeEstimateRequest request, User projectUser) {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("timestamp", Instant.now());
        snapshot.put("budgetAmount", request.getCampaignBudget());
        snapshot.put("projectCumulativeSpend", projectUser.getCumulativeSpend());
        snapshot.put("projectReputationScore", projectUser.getReputationScore());
        snapshot.put("numberOfKOLs", request.getRequestedCreators());
        snapshot.put("complexity", request.getComplexity());
        snapshot.put("kpiCount", request.getKpiCount());
        snapshot.put("useAW3Token", request.getUseAW3Token());
        return snapshot;
    }
}

