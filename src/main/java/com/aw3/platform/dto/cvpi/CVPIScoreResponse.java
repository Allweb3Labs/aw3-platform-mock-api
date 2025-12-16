package com.aw3.platform.dto.cvpi;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for CVPI score response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVPIScoreResponse {

    private UUID creatorId;
    private BigDecimal currentCVPI;
    private RankingInfo ranking;
    private String trend; // improving, declining, stable
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant lastUpdated;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RankingInfo {
        private Integer overall;
        private Integer category;
        private Long totalCreators;
    }
}

