package com.aw3.platform.dto.creator;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Token Balance Response DTO
 * 
 * GET /api/creator/tokens/balance response format
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenBalanceResponse {

    private UUID userId;
    private TokenBalanceInfo tokenBalance;
    private DiscountEligibilityInfo discountEligibility;
    private StakingInfo stakingInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenBalanceInfo {
        private BigDecimal aw3;
        private BigDecimal usdEquivalent;
        private String walletAddress;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiscountEligibilityInfo {
        private Boolean eligible;
        private BigDecimal discountRate;
        private String description;
        private BigDecimal minimumRequired;
        private BigDecimal yourBalance;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StakingInfo {
        private BigDecimal staked;
        private BigDecimal availableForPayment;
        private BigDecimal stakingRewards;
    }
}

