package com.aw3.platform.dto.project;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for fee estimation request in Project Portal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeEstimateRequest {

    @NotNull(message = "Campaign budget is required")
    @Positive(message = "Budget must be positive")
    private BigDecimal campaignBudget;

    @NotNull(message = "Category is required")
    private String category;

    private String complexity; // low, medium, high

    private Integer estimatedDuration; // days

    private Integer kpiCount;

    private Integer requestedCreators;

    @Builder.Default
    private Boolean useAW3Token = false;

    private List<KpiMetricInfo> kpiMetrics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KpiMetricInfo {
        private String metric;
        private String source; // TWITTER, DISCORD, ONCHAIN, etc.
        private Double target;
        private Double weight;
    }
}

