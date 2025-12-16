package com.aw3.platform.controller.creator;

import com.aw3.platform.dto.common.ApiResponse;
import com.aw3.platform.dto.creator.*;
import com.aw3.platform.security.CustomUserDetails;
import com.aw3.platform.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Creator Portal - Subscription Management Endpoints
 * 
 * Business Rules:
 * - FREE tier is default for all creators
 * - PRO tier: $29/month with enhanced features
 * - ELITE tier: $99/month with premium features
 * - Annual billing provides 20% discount
 */
@RestController
@RequestMapping("/creator/subscriptions")
@PreAuthorize("hasRole('CREATOR')")
@RequiredArgsConstructor
@Slf4j
public class CreatorSubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * GET /api/creator/subscriptions/current
     * View active subscription tier and benefits
     */
    @GetMapping("/current")
    public ApiResponse<SubscriptionResponse> getCurrentSubscription(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Creator {} fetching current subscription", userDetails.getUserId());
        
        SubscriptionResponse subscription = subscriptionService.getCurrentSubscription(userDetails.getUserId());
        
        return ApiResponse.success(subscription);
    }

    /**
     * GET /api/creator/subscriptions/plans
     * View all available subscription plans and pricing
     */
    @GetMapping("/plans")
    public ApiResponse<SubscriptionPlansResponse> getAvailablePlans(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Creator {} fetching available plans", userDetails.getUserId());
        
        SubscriptionPlansResponse plans = subscriptionService.getAvailablePlans();
        
        return ApiResponse.success(plans);
    }

    /**
     * POST /api/creator/subscriptions/upgrade
     * Upgrade to a higher subscription tier
     */
    @PostMapping("/upgrade")
    public ApiResponse<SubscriptionResponse> upgradeSubscription(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SubscriptionUpgradeRequest request) {
        
        log.info("Creator {} upgrading to tier: {}", userDetails.getUserId(), request.getTargetTier());
        
        SubscriptionResponse subscription = subscriptionService.upgradeSubscription(
                userDetails.getUserId(), 
                request
        );
        
        return ApiResponse.success(subscription);
    }

    /**
     * GET /api/creator/subscriptions/invoices
     * View subscription billing history and invoices
     */
    @GetMapping("/invoices")
    public ApiResponse<InvoiceListResponse> getBillingHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        
        log.info("Creator {} fetching billing history", userDetails.getUserId());
        
        InvoiceListResponse invoices = subscriptionService.getBillingHistory(
                userDetails.getUserId(), 
                limit, 
                offset
        );
        
        return ApiResponse.success(invoices);
    }
}

