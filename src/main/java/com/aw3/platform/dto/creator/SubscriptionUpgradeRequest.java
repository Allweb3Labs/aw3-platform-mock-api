package com.aw3.platform.dto.creator;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Subscription Upgrade Request DTO
 * 
 * POST /api/creator/subscriptions/upgrade request format
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionUpgradeRequest {

    @NotBlank(message = "Target tier is required")
    private String targetTier; // PRO, ELITE

    @NotBlank(message = "Billing cycle is required")
    private String billingCycle; // monthly, annual

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // card, crypto

    @NotBlank(message = "Payment token is required")
    private String paymentToken; // Stripe token or wallet signature
}

