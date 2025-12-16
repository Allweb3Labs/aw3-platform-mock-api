package com.aw3.platform.controller.admin;

import com.aw3.platform.dto.admin.FeeConfigResponse;
import com.aw3.platform.dto.admin.RevenueDashboardResponse;
import com.aw3.platform.dto.common.ApiResponse;
import com.aw3.platform.security.CustomUserDetails;
import com.aw3.platform.service.AdminFinanceService;
import jakarta.validation.Valid;
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
 * Admin Financial Configuration Controller
 * 
 * Endpoints for managing platform fees and revenue distribution
 * Base path: /api/admin/finance
 * 
 * Business Rules:
 * - Fee changes require DAO approval for significant modifications
 * - Revenue distribution follows 5-layer model
 * - All financial configuration changes are audit logged
 * - Historical fee structures maintained for billing accuracy
 */
@RestController
@RequestMapping("/admin/finance")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminFinanceController {

    private final AdminFinanceService adminFinanceService;

    /**
     * GET /api/admin/finance/fee-configs
     * Get all fee configuration rules
     */
    @GetMapping("/fee-configs")
    public ApiResponse<FeeConfigResponse> getFeeConfigs(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Admin {} fetching fee configurations", userDetails.getUserId());
        FeeConfigResponse response = adminFinanceService.getFeeConfigs();
        return ApiResponse.success(response);
    }

    /**
     * PUT /api/admin/finance/fee-configs
     * Update fee configuration (requires DAO approval for major changes)
     */
    @PutMapping("/fee-configs")
    public ApiResponse<FeeConfigResponse> updateFeeConfigs(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody FeeConfigUpdateRequest request) {
        log.info("Admin {} updating fee configurations", userDetails.getUserId());
        FeeConfigResponse response = adminFinanceService.updateFeeConfigs(
                userDetails.getUserId(), request);
        return ApiResponse.success(response);
    }

    /**
     * GET /api/admin/finance/revenue-dashboard
     * Get revenue dashboard with 5-layer distribution
     */
    @GetMapping("/revenue-dashboard")
    public ApiResponse<RevenueDashboardResponse> getRevenueDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "30d") String period,
            @RequestParam(defaultValue = "daily") String breakdown) {
        log.info("Admin {} fetching revenue dashboard: period={}, breakdown={}", 
                userDetails.getUserId(), period, breakdown);
        RevenueDashboardResponse response = adminFinanceService.getRevenueDashboard(period);
        return ApiResponse.success(response);
    }

    /**
     * GET /api/admin/finance/revenue-distribution
     * Get detailed 5-layer revenue distribution configuration
     */
    @GetMapping("/revenue-distribution")
    public ApiResponse<RevenueDistributionResponse> getRevenueDistribution(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Admin {} fetching revenue distribution config", userDetails.getUserId());
        RevenueDistributionResponse response = adminFinanceService.getRevenueDistribution();
        return ApiResponse.success(response);
    }

    /**
     * PUT /api/admin/finance/revenue-distribution
     * Update revenue distribution (requires DAO approval)
     */
    @PutMapping("/revenue-distribution")
    public ApiResponse<RevenueDistributionResponse> updateRevenueDistribution(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody RevenueDistributionUpdateRequest request) {
        log.info("Admin {} updating revenue distribution", userDetails.getUserId());
        RevenueDistributionResponse response = adminFinanceService.updateRevenueDistribution(
                userDetails.getUserId(), request);
        return ApiResponse.success(response);
    }

    /**
     * GET /api/admin/finance/escrow-overview
     * Get platform-wide escrow status
     */
    @GetMapping("/escrow-overview")
    public ApiResponse<EscrowOverviewResponse> getEscrowOverview(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Admin {} fetching escrow overview", userDetails.getUserId());
        EscrowOverviewResponse response = adminFinanceService.getEscrowOverview();
        return ApiResponse.success(response);
    }

    /**
     * GET /api/admin/finance/treasury
     * Get treasury balance and health
     */
    @GetMapping("/treasury")
    public ApiResponse<TreasuryResponse> getTreasury(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Admin {} fetching treasury info", userDetails.getUserId());
        TreasuryResponse response = adminFinanceService.getTreasury();
        return ApiResponse.success(response);
    }

    /**
     * POST /api/admin/finance/treasury/withdraw
     * Initiate treasury withdrawal (requires DAO approval and multi-sig)
     */
    @PostMapping("/treasury/withdraw")
    public ApiResponse<TreasuryWithdrawalResponse> withdrawFromTreasury(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TreasuryWithdrawalRequest request) {
        log.info("Admin {} initiating treasury withdrawal", userDetails.getUserId());
        TreasuryWithdrawalResponse response = adminFinanceService.withdrawFromTreasury(
                userDetails.getUserId(), request);
        return ApiResponse.success(response);
    }

    /**
     * GET /api/admin/finance/transactions
     * Get platform transaction history
     */
    @GetMapping("/transactions")
    public ApiResponse<TransactionListResponse> getTransactions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        log.info("Admin {} fetching transactions", userDetails.getUserId());
        TransactionListResponse response = adminFinanceService.getTransactions(
                type, status, startDate, endDate, page, size);
        return ApiResponse.success(response);
    }

    // DTOs

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FeeConfigUpdateRequest {
        private TieredFeeConfig tieredFees;
        private ComplexityMultipliers complexityMultipliers;
        private DiscountConfig discounts;
        private OracleFeeConfig oracleFees;
        private String daoProposalId;
        private String changeReason;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TieredFeeConfig {
        private BigDecimal tier1Rate; // < $1,000
        private BigDecimal tier2Rate; // $1,000 - $10,000
        private BigDecimal tier3Rate; // $10,000 - $50,000
        private BigDecimal tier4Rate; // > $50,000
        private BigDecimal tier1Threshold;
        private BigDecimal tier2Threshold;
        private BigDecimal tier3Threshold;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ComplexityMultipliers {
        private BigDecimal simpleMultiplier;
        private BigDecimal mediumMultiplier;
        private BigDecimal complexMultiplier;
        private BigDecimal enterpriseMultiplier;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DiscountConfig {
        private BigDecimal aw3PaymentDiscount;
        private BigDecimal reputationMaxDiscount;
        private BigDecimal bulkCampaignDiscount;
        private BigDecimal earlyPaymentDiscount;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class OracleFeeConfig {
        private BigDecimal baseOracleFee;
        private BigDecimal perKPIFee;
        private BigDecimal complexVerificationFee;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RevenueDistributionResponse {
        private Layer1Config layer1; // Creator Payment
        private Layer2Config layer2; // Platform Fees
        private Layer3Config layer3; // Oracle/Validator
        private Layer4Config layer4; // Token Incentives
        private Layer5Config layer5; // DAO Treasury
        private Instant lastUpdated;
        private String daoApprovalId;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class Layer1Config {
        private String name;
        private BigDecimal percentage;
        private String description;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class Layer2Config {
        private String name;
        private BigDecimal percentage;
        private String description;
        private BigDecimal operationalShare;
        private BigDecimal developmentShare;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class Layer3Config {
        private String name;
        private BigDecimal percentage;
        private String description;
        private BigDecimal oracleShare;
        private BigDecimal validatorShare;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class Layer4Config {
        private String name;
        private BigDecimal percentage;
        private String description;
        private BigDecimal stakingRewards;
        private BigDecimal liquidityIncentives;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class Layer5Config {
        private String name;
        private BigDecimal percentage;
        private String description;
        private BigDecimal reserveFund;
        private BigDecimal growthFund;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RevenueDistributionUpdateRequest {
        private BigDecimal layer1Percentage;
        private BigDecimal layer2Percentage;
        private BigDecimal layer3Percentage;
        private BigDecimal layer4Percentage;
        private BigDecimal layer5Percentage;
        private String daoProposalId; // Required
        private String changeReason;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EscrowOverviewResponse {
        private BigDecimal totalLocked;
        private BigDecimal totalReleased;
        private BigDecimal pendingRelease;
        private Long activeCampaigns;
        private List<EscrowByStatus> byStatus;
        private List<LargestEscrow> largestEscrows;
        private EscrowHealth health;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EscrowByStatus {
        private String status;
        private BigDecimal amount;
        private Long count;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class LargestEscrow {
        private UUID campaignId;
        private String campaignTitle;
        private UUID projectId;
        private BigDecimal amount;
        private String status;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EscrowHealth {
        private String status;
        private BigDecimal contractBalance;
        private BigDecimal expectedBalance;
        private BigDecimal discrepancy;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TreasuryResponse {
        private BigDecimal totalBalance;
        private TokenBalances tokenBalances;
        private List<TreasuryTransaction> recentTransactions;
        private AllocationBreakdown allocation;
        private TreasuryHealth health;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TokenBalances {
        private BigDecimal aw3Balance;
        private BigDecimal usdcBalance;
        private BigDecimal ethBalance;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TreasuryTransaction {
        private UUID transactionId;
        private String type;
        private BigDecimal amount;
        private String token;
        private String description;
        private Instant timestamp;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AllocationBreakdown {
        private BigDecimal operationalReserve;
        private BigDecimal developmentFund;
        private BigDecimal insuranceFund;
        private BigDecimal growthFund;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TreasuryHealth {
        private String status;
        private BigDecimal runwayMonths;
        private BigDecimal monthlyBurnRate;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TreasuryWithdrawalRequest {
        private BigDecimal amount;
        private String token;
        private String destinationAddress;
        private String purpose;
        private String daoProposalId; // Required
        private List<String> multiSigApprovals;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TreasuryWithdrawalResponse {
        private UUID withdrawalId;
        private BigDecimal amount;
        private String token;
        private String status; // PENDING_APPROVAL, APPROVED, EXECUTED, REJECTED
        private Integer approvalsReceived;
        private Integer approvalsRequired;
        private String txHash;
        private Instant initiatedAt;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TransactionListResponse {
        private List<TransactionItem> transactions;
        private TransactionStats stats;
        private PaginationInfo pagination;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TransactionItem {
        private UUID transactionId;
        private String type;
        private BigDecimal amount;
        private String token;
        private String status;
        private UUID fromUserId;
        private UUID toUserId;
        private UUID campaignId;
        private String txHash;
        private Instant timestamp;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TransactionStats {
        private BigDecimal totalVolume;
        private Long totalTransactions;
        private BigDecimal averageValue;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PaginationInfo {
        private Long total;
        private Integer page;
        private Integer size;
        private Integer totalPages;
    }
}
