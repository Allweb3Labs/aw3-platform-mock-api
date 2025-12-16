package com.aw3.platform.controller.creator;

import com.aw3.platform.dto.common.ApiResponse;
import com.aw3.platform.dto.creator.TokenBalanceResponse;
import com.aw3.platform.security.CustomUserDetails;
import com.aw3.platform.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Creator Portal - AW3 Token Management Endpoints
 * 
 * Business Rules:
 * - 20% discount on platform fees when paying with AW3 tokens
 * - Minimum 1000 AW3 tokens required for discount eligibility
 * - Token payment estimates valid for 15 minutes
 */
@RestController
@RequestMapping("/creator/tokens")
@PreAuthorize("hasRole('CREATOR')")
@RequiredArgsConstructor
@Slf4j
public class CreatorTokenController {

    private final TokenService tokenService;

    /**
     * GET /api/creator/tokens/balance
     * View AW3 token balance and payment discount eligibility
     */
    @GetMapping("/balance")
    public ApiResponse<TokenBalanceResponse> getTokenBalance(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Creator {} fetching token balance", userDetails.getUserId());
        
        TokenBalanceResponse balance = tokenService.getCreatorTokenBalance(userDetails.getUserId());
        
        return ApiResponse.success(balance);
    }

    /**
     * POST /api/creator/tokens/estimate-payment
     * Calculate how many AW3 tokens needed for a specific campaign application
     */
    @PostMapping("/estimate-payment")
    public ApiResponse<TokenPaymentEstimate> estimateTokenPayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody TokenPaymentRequest request) {
        
        log.info("Creator {} estimating token payment for campaign {}", 
                userDetails.getUserId(), request.getCampaignId());
        
        TokenPaymentEstimate estimate = tokenService.estimateTokenPayment(
                userDetails.getUserId(), 
                request.getCampaignId(),
                request.getServiceFee()
        );
        
        return ApiResponse.success(estimate);
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TokenPaymentRequest {
        private UUID campaignId;
        private BigDecimal serviceFee;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TokenPaymentEstimate {
        private UUID campaignId;
        private BigDecimal feeInUSD;
        private BigDecimal feeWithDiscount;
        private BigDecimal savings;
        private BigDecimal aw3TokensRequired;
        private BigDecimal currentAW3Price;
        private BigDecimal yourBalance;
        private Boolean sufficient;
        private Instant estimateValidUntil;
    }
}

