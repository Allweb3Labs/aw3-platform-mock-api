package com.aw3.platform.controller.creator;

import com.aw3.platform.dto.common.ApiResponse;
import com.aw3.platform.dto.creator.EarningsSummaryResponse;
import com.aw3.platform.security.CustomUserDetails;
import com.aw3.platform.service.EarningsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Creator Portal - Earnings Management Endpoints
 * 
 * Business Rules:
 * - Earnings include campaign payments, performance bonuses, SPC royalties
 * - Payments are released after oracle verification
 * - SPC NFT royalties are 5% on secondary sales
 */
@RestController
@RequestMapping("/creator/earnings")
@PreAuthorize("hasRole('CREATOR')")
@RequiredArgsConstructor
@Slf4j
public class CreatorEarningsController {

    private final EarningsService earningsService;

    /**
     * GET /api/creator/earnings/summary
     * View earnings overview and statistics
     */
    @GetMapping("/summary")
    public ApiResponse<EarningsSummaryResponse> getEarningsSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "all") String period) {
        
        log.info("Creator {} fetching earnings summary for period: {}", 
                userDetails.getUserId(), period);
        
        EarningsSummaryResponse response = earningsService.getEarningsSummary(
                userDetails.getUserId(), 
                period
        );
        
        return ApiResponse.success(response);
    }

    /**
     * GET /api/creator/earnings/transactions
     * View detailed payment transaction history
     */
    @GetMapping("/transactions")
    public ApiResponse<TransactionListResponse> getTransactionHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        
        log.info("Creator {} fetching transaction history", userDetails.getUserId());
        
        TransactionListResponse response = earningsService.getTransactionHistory(
                userDetails.getUserId(),
                type,
                startDate,
                endDate,
                limit,
                offset
        );
        
        return ApiResponse.success(response);
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TransactionListResponse {
        private List<TransactionItem> transactions;
        private PaginationInfo pagination;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TransactionItem {
        private String transactionId;
        private String type; // campaign_payment, spc_royalty, referral_bonus
        private BigDecimal amount;
        private String currency;
        private UUID campaignId;
        private String campaignTitle;
        private UUID spcId;
        private Instant date;
        private String status;
        private BlockchainInfo blockchain;
        private TransactionBreakdown breakdown;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BlockchainInfo {
        private String txHash;
        private Integer chainId;
        private Long blockNumber;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TransactionBreakdown {
        private BigDecimal basePayment;
        private BigDecimal performanceBonus;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PaginationInfo {
        private Long total;
        private Integer limit;
        private Integer offset;
        private Boolean hasMore;
    }
}

