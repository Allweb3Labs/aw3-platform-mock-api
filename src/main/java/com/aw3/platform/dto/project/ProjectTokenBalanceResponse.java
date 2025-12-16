package com.aw3.platform.dto.project;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Project Token Balance Response DTO
 * 
 * GET /api/project/wallet/balance response format
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectTokenBalanceResponse {

    private UUID userId;
    private String walletAddress;
    private BigDecimal aw3Balance;
    private BigDecimal usdcBalance;
    private BigDecimal totalEscrowLocked;
    private BigDecimal availableForCampaigns;
}

