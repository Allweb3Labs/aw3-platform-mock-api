package com.aw3.platform.controller.project;

import com.aw3.platform.dto.common.ApiResponse;
import com.aw3.platform.dto.project.ProjectTokenBalanceResponse;
import com.aw3.platform.dto.project.DepositRequest;
import com.aw3.platform.dto.project.DepositResponse;
import com.aw3.platform.dto.project.EscrowStatusResponse;
import com.aw3.platform.dto.project.TransactionHistoryResponse;
import com.aw3.platform.security.CustomUserDetails;
import com.aw3.platform.service.ProjectTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Project Portal - Token/Wallet Management Endpoints
 * 
 * Business Rules:
 * - Projects can only view their own wallet/escrow
 * - Deposits require on-chain confirmation
 * - Escrow funds locked until campaign conditions met
 */
@RestController
@RequestMapping("/project")
@PreAuthorize("hasRole('PROJECT')")
@RequiredArgsConstructor
@Slf4j
public class ProjectTokenController {

    private final ProjectTokenService projectTokenService;

    /**
     * GET /api/project/wallet/balance
     * Get project wallet balance for campaign funding
     */
    @GetMapping("/wallet/balance")
    public ApiResponse<ProjectTokenBalanceResponse> getWalletBalance(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Project {} fetching wallet balance", userDetails.getUserId());
        
        ProjectTokenBalanceResponse balance = projectTokenService.getWalletBalance(userDetails.getUserId());
        
        return ApiResponse.success(balance);
    }

    /**
     * POST /api/project/wallet/deposit
     * Initiate deposit to fund campaigns
     */
    @PostMapping("/wallet/deposit")
    public ApiResponse<DepositResponse> initiateDeposit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DepositRequest request) {
        
        log.info("Project {} initiating deposit of {} {}", 
                userDetails.getUserId(), request.getAmount(), request.getToken());
        
        DepositResponse response = projectTokenService.initiateDeposit(userDetails.getUserId(), request);
        
        return ApiResponse.success(response);
    }

    /**
     * GET /api/project/wallet/escrow
     * View escrow details for active campaigns
     */
    @GetMapping("/wallet/escrow")
    public ApiResponse<EscrowStatusResponse> getEscrowStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Project {} fetching escrow status", userDetails.getUserId());
        
        EscrowStatusResponse escrow = projectTokenService.getEscrowStatus(userDetails.getUserId());
        
        return ApiResponse.success(escrow);
    }

    /**
     * GET /api/project/wallet/escrow/{campaignId}
     * View escrow details for a specific campaign
     */
    @GetMapping("/wallet/escrow/{campaignId}")
    public ApiResponse<EscrowStatusResponse.CampaignEscrowItem> getCampaignEscrow(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID campaignId) {
        
        log.info("Project {} fetching escrow for campaign {}", userDetails.getUserId(), campaignId);
        
        EscrowStatusResponse.CampaignEscrowItem escrow = projectTokenService.getCampaignEscrow(
                userDetails.getUserId(), campaignId);
        
        return ApiResponse.success(escrow);
    }

    /**
     * GET /api/project/wallet/transactions
     * View transaction history
     */
    @GetMapping("/wallet/transactions")
    public ApiResponse<TransactionHistoryResponse> getTransactionHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Project {} fetching transaction history", userDetails.getUserId());
        
        TransactionHistoryResponse history = projectTokenService.getTransactionHistory(
                userDetails.getUserId(), type, startDate, endDate, page, size);
        
        return ApiResponse.success(history);
    }
}

