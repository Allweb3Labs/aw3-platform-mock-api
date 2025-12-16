package com.aw3.platform.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for revenue dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueDashboardResponse {
    private String period;
    private Summary summary;
    private Distribution distribution;
    private List<TrendData> trends;
    private BlockchainInfo blockchain;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Double totalRevenue;
        private Integer totalCampaigns;
        private Double averageRevenuePerCampaign;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Distribution {
        private AllocationInfo treasury;
        private AllocationInfo validatorIncentives;
        private AllocationInfo aiEcosystem;
        private AllocationInfo daoTreasury;
        private AllocationInfo buybackAndBurn;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllocationInfo {
        private Double amount;
        private Double percentage;
        private String description;
        private Map<String, Double> subAllocations;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData {
        private LocalDate date;
        private Double totalRevenue;
        private Double treasury;
        private Double validators;
        private Double ai;
        private Double dao;
        private Double buyback;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlockchainInfo {
        private Integer chainId;
        private String chainName;
        private String feeDistributorContract;
        private Long lastDistributionBlock;
        private Instant lastDistributionTime;
    }
}

