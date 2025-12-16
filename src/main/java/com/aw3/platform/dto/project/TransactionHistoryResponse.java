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
 * Transaction History Response DTO
 * 
 * GET /api/project/wallet/transactions response format
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionHistoryResponse {

    private List<TransactionItem> transactions;
    private PaginationInfo pagination;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionItem {
        private UUID transactionId;
        private String type; // DEPOSIT, ESCROW_LOCK, PAYMENT_RELEASE, REFUND, FEE
        private BigDecimal amount;
        private String token;
        private String status; // PENDING, CONFIRMED, FAILED
        private String txHash;
        private UUID campaignId;
        private String description;
        private Instant timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        private Long total;
        private Integer page;
        private Integer size;
        private Integer totalPages;
    }
}

