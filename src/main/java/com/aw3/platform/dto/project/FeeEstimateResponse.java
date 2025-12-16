package com.aw3.platform.dto.project;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * DTO for fee estimation response in Project Portal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeEstimateResponse {

    private String feeEstimateId;
    private BigDecimal campaignBudget;
    private FeeBreakdown feeBreakdown;
    private BigDecimal totalFees;
    private EscrowRequirement escrowRequirement;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant validUntil;

    private String signature;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeeBreakdown {
        private BigDecimal baseRate;
        private BigDecimal baseFee;
        private BigDecimal complexityMultiplier;
        private BigDecimal complexityAdjustedFee;
        private BigDecimal reputationDiscount;
        private BigDecimal discountAmount;
        private BigDecimal serviceFeeBeforeToken;
        private BigDecimal aw3TokenDiscount;
        private BigDecimal aw3DiscountAmount;
        private BigDecimal finalServiceFee;
        private BigDecimal oracleFee;
        private OracleFeeBreakdown oracleFeeBreakdown;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OracleFeeBreakdown {
        private BigDecimal baseFee;
        private Integer additionalKPIs;
        private BigDecimal kpiFee;
        private BigDecimal complexityPremium;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EscrowRequirement {
        private BigDecimal campaignBudget;
        private BigDecimal serviceFee;
        private BigDecimal oracleFee;
        private BigDecimal buffer;
        private BigDecimal totalRequired;
        private BigDecimal bufferPercentage;
    }

    private Map<String, Object> calculationSnapshot;
}

