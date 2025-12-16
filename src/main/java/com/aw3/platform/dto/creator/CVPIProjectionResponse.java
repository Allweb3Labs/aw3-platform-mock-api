package com.aw3.platform.dto.creator;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * CVPI Projection Response DTO
 * 
 * GET /api/creator/cvpi/projection/{campaignId} response format
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CVPIProjectionResponse {

    private UUID campaignId;
    private BigDecimal projectedCVPI;
    private BigDecimal confidence;
    private BreakdownInfo breakdown;
    private ComparisonInfo comparisonToAverage;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BreakdownInfo {
        private BigDecimal estimatedCost;
        private BigDecimal basePayment;
        private BigDecimal platformFee;
        private BigDecimal oracleFee;
        private BigDecimal projectedImpact;
        private List<ImpactFactorInfo> impactFactors;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImpactFactorInfo {
        private String metric;
        private BigDecimal weight;
        private BigDecimal expectedValue;
        private BigDecimal contributionToImpact;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonInfo {
        private BigDecimal yourProjectedCVPI;
        private BigDecimal platformAverageCVPI;
        private BigDecimal percentageBetter;
    }
}

