package com.aw3.platform.service;

import com.aw3.platform.controller.creator.CreatorTokenController;
import com.aw3.platform.dto.creator.TokenBalanceResponse;
import com.aw3.platform.entity.User;
import com.aw3.platform.exception.NotFoundException;
import com.aw3.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

/**
 * AW3 Token Service
 * 
 * Business Rules:
 * - 20% discount on platform fees when paying with AW3 tokens
 * - Minimum 1000 AW3 tokens required for discount eligibility
 * - Token payment estimates valid for 15 minutes
 * - Current AW3 token price: $0.20 (mock for MVP)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TokenService {

    private final UserRepository userRepository;
    private final Web3Service web3Service;

    private static final BigDecimal AW3_TOKEN_PRICE = new BigDecimal("0.20");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.20");
    private static final BigDecimal MINIMUM_TOKENS_FOR_DISCOUNT = new BigDecimal("1000");

    /**
     * Get creator's AW3 token balance and discount eligibility
     */
    public TokenBalanceResponse getCreatorTokenBalance(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Fetch on-chain balance (mock for MVP)
        BigDecimal aw3Balance = fetchOnChainBalance(user.getWalletAddress());
        BigDecimal stakedAmount = fetchStakedAmount(user.getWalletAddress());
        BigDecimal stakingRewards = fetchStakingRewards(user.getWalletAddress());
        
        BigDecimal usdEquivalent = aw3Balance.multiply(AW3_TOKEN_PRICE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal availableForPayment = aw3Balance.subtract(stakedAmount);
        
        boolean isEligible = aw3Balance.compareTo(MINIMUM_TOKENS_FOR_DISCOUNT) >= 0;

        return TokenBalanceResponse.builder()
                .userId(userId)
                .tokenBalance(TokenBalanceResponse.TokenBalanceInfo.builder()
                        .aw3(aw3Balance)
                        .usdEquivalent(usdEquivalent)
                        .walletAddress(user.getWalletAddress())
                        .build())
                .discountEligibility(TokenBalanceResponse.DiscountEligibilityInfo.builder()
                        .eligible(isEligible)
                        .discountRate(DISCOUNT_RATE)
                        .description("20% discount on all platform fees when paying with AW3 tokens")
                        .minimumRequired(MINIMUM_TOKENS_FOR_DISCOUNT)
                        .yourBalance(aw3Balance)
                        .build())
                .stakingInfo(TokenBalanceResponse.StakingInfo.builder()
                        .staked(stakedAmount)
                        .availableForPayment(availableForPayment)
                        .stakingRewards(stakingRewards)
                        .build())
                .build();
    }

    /**
     * Get project's AW3 token balance for campaign payments
     */
    public TokenBalanceResponse getProjectTokenBalance(UUID projectId) {
        User user = userRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        BigDecimal aw3Balance = fetchOnChainBalance(user.getWalletAddress());
        BigDecimal usdEquivalent = aw3Balance.multiply(AW3_TOKEN_PRICE).setScale(2, RoundingMode.HALF_UP);
        
        boolean isEligible = aw3Balance.compareTo(new BigDecimal("5000")) >= 0;

        return TokenBalanceResponse.builder()
                .userId(projectId)
                .tokenBalance(TokenBalanceResponse.TokenBalanceInfo.builder()
                        .aw3(aw3Balance)
                        .usdEquivalent(usdEquivalent)
                        .walletAddress(user.getWalletAddress())
                        .build())
                .discountEligibility(TokenBalanceResponse.DiscountEligibilityInfo.builder()
                        .eligible(isEligible)
                        .discountRate(DISCOUNT_RATE)
                        .description("20% discount on campaign fees when paying with AW3 tokens")
                        .minimumRequired(new BigDecimal("5000"))
                        .yourBalance(aw3Balance)
                        .build())
                .build();
    }

    /**
     * Estimate token payment for a specific fee amount
     */
    public CreatorTokenController.TokenPaymentEstimate estimateTokenPayment(
            UUID userId, UUID campaignId, BigDecimal serviceFee) {
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        BigDecimal aw3Balance = fetchOnChainBalance(user.getWalletAddress());
        BigDecimal feeWithDiscount = serviceFee.multiply(BigDecimal.ONE.subtract(DISCOUNT_RATE))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal savings = serviceFee.subtract(feeWithDiscount);
        BigDecimal tokensRequired = feeWithDiscount.divide(AW3_TOKEN_PRICE, 0, RoundingMode.CEILING);
        
        boolean sufficient = aw3Balance.compareTo(tokensRequired) >= 0;

        return CreatorTokenController.TokenPaymentEstimate.builder()
                .campaignId(campaignId)
                .feeInUSD(serviceFee)
                .feeWithDiscount(feeWithDiscount)
                .savings(savings)
                .aw3TokensRequired(tokensRequired)
                .currentAW3Price(AW3_TOKEN_PRICE)
                .yourBalance(aw3Balance)
                .sufficient(sufficient)
                .estimateValidUntil(Instant.now().plusSeconds(900)) // 15 minutes
                .build();
    }

    /**
     * Fetch on-chain token balance (mock for MVP)
     */
    private BigDecimal fetchOnChainBalance(String walletAddress) {
        // Mock implementation - in production, use Web3 to query token contract
        return new BigDecimal("15000");
    }

    /**
     * Fetch staked token amount (mock for MVP)
     */
    private BigDecimal fetchStakedAmount(String walletAddress) {
        return new BigDecimal("5000");
    }

    /**
     * Fetch pending staking rewards (mock for MVP)
     */
    private BigDecimal fetchStakingRewards(String walletAddress) {
        return new BigDecimal("125");
    }
}

