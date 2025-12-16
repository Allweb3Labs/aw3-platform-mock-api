package com.aw3.platform.dto.cvpi;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO for CVPI history response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVPIHistoryResponse {

    private UUID creatorId;
    private String period; // 7d, 30d, 90d, 1y
    private List<DataPoint> dataPoints;
    private Summary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPoint {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        private Instant date;
        
        private BigDecimal cvpi;
        private Integer campaignsCompleted;
        private BigDecimal totalImpact;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private BigDecimal averageCVPI;
        private BigDecimal bestCVPI;
        private BigDecimal worstCVPI;
        private Integer totalCampaigns;
    }
}

