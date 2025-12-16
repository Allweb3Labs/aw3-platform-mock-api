package com.aw3.platform.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for CVPI algorithm configuration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVPIAlgorithmConfigResponse {
    private String version;
    private Instant effectiveDate;
    private String formula;
    private Map<String, ComponentWeight> impactScoreComponents;
    private Map<String, Object> totalCostCalculation;
    private Map<String, Object> normalizationFactors;
    private Map<String, CVPIInterpretation> cvpiInterpretation;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentWeight {
        private Double weight;
        private List<String> metrics;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CVPIInterpretation {
        private Range range;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Range {
        private Double min;
        private Double max;
    }
}

