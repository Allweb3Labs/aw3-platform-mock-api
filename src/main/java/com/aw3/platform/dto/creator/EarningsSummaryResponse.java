package com.aw3.platform.dto.creator;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Earnings Summary Response DTO
 * 
 * GET /api/creator/earnings/summary response format
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EarningsSummaryResponse {

    private UUID userId;
    private SummaryInfo summary;
    private Period30dInfo period30d;
    private BreakdownInfo breakdown;
    private PendingInfo pending;
    private WalletInfo wallet;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryInfo {
        private BigDecimal totalEarnings;
        private BigDecimal campaignPayments;
        private BigDecimal performanceBonuses;
        private BigDecimal spcRoyalties;
        private BigDecimal referralBonuses;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Period30dInfo {
        private BigDecimal totalEarnings;
        private Integer campaignsCompleted;
        private BigDecimal avgEarningsPerCampaign;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BreakdownInfo {
        private List<CategoryBreakdown> byCategory;
        private PaymentTypeBreakdown byPaymentType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryBreakdown {
        private String category;
        private BigDecimal earnings;
        private Integer campaigns;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentTypeBreakdown {
        private BigDecimal fixed;
        private BigDecimal performanceBased;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingInfo {
        private BigDecimal amount;
        private Integer campaigns;
        private Instant expectedReleaseDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WalletInfo {
        private BigDecimal availableBalance;
        private BigDecimal lockedBalance;
    }
}

