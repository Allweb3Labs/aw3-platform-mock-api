package com.aw3.platform.dto.project;

import com.aw3.platform.entity.enums.CampaignStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for campaign response in Project Portal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignResponse {

    private UUID campaignId;
    private UUID projectId;
    private String title;
    private String description;
    private String category;
    private Map<String, Object> kpiTargets;
    private BigDecimal budgetAmount;
    private String budgetToken;
    private CampaignStatus status;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant deadline;

    private String contractAddress;
    private Integer chainId;
    private BigDecimal escrowBalance;
    private BigDecimal serviceFee;
    private BigDecimal oracleFee;
    private BigDecimal totalFee;
    private String feeEstimateId;
    private Boolean aw3TokenPaymentEnabled;
    private BigDecimal requiredReputation;
    private Integer numberOfCreators;
    private String complexity;
    private Map<String, Object> campaignMetadata;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant updatedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant completionDate;

    private CampaignStatistics statistics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampaignStatistics {
        private Long totalApplications;
        private Long acceptedApplications;
        private Long pendingApplications;
        private Long totalDeliverables;
        private Long completedDeliverables;
    }
}

