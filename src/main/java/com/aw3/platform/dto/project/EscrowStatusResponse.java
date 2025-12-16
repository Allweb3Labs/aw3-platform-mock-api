package com.aw3.platform.dto.project;

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
 * Escrow Status Response DTO
 * 
 * GET /api/project/wallet/escrow response format
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EscrowStatusResponse {

    private UUID userId;
    private BigDecimal totalLocked;
    private BigDecimal totalReleased;
    private BigDecimal totalPending;
    private List<CampaignEscrowItem> campaigns;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampaignEscrowItem {
        private UUID campaignId;
        private String campaignTitle;
        private String status;
        private BigDecimal lockedAmount;
        private BigDecimal releasedAmount;
        private BigDecimal pendingAmount;
        private String smartContractAddress;
        private List<MilestoneEscrow> milestones;
        private Instant lockedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MilestoneEscrow {
        private Integer milestoneNumber;
        private String description;
        private BigDecimal amount;
        private String status; // LOCKED, RELEASED, REFUNDED
        private Instant releaseDate;
    }
}

