package com.aw3.platform.service;

import com.aw3.platform.dto.creator.*;
import com.aw3.platform.entity.User;
import com.aw3.platform.exception.BadRequestException;
import com.aw3.platform.exception.NotFoundException;
import com.aw3.platform.repository.ApplicationRepository;
import com.aw3.platform.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Subscription Service
 * 
 * SaaS Subscription Tiers:
 * - FREE: Default tier, basic features
 * - PRO: $29/month, enhanced features
 * - ELITE: $99/month, premium features
 * 
 * Business Rules:
 * - Annual billing provides 20% discount
 * - Grandfathering for existing subscribers on plan changes
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionService {

    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final ObjectMapper objectMapper;

    /**
     * Get current subscription status and benefits
     */
    public SubscriptionResponse getCurrentSubscription(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Map<String, Object> subscriptionData = getSubscriptionData(user);
        String tier = (String) subscriptionData.getOrDefault("tier", "FREE");
        
        // Get usage statistics
        long activeCampaigns = applicationRepository.countActiveByCreatorId(userId);
        long completedCampaigns = applicationRepository.countCompletedByCreatorId(userId);
        long monthlyApplications = applicationRepository.countMonthlyByCreatorId(userId);

        return SubscriptionResponse.builder()
                .userId(userId)
                .subscription(SubscriptionResponse.SubscriptionInfo.builder()
                        .tier(tier)
                        .status("active")
                        .startDate(subscriptionData.get("startDate") != null 
                                ? Instant.parse((String) subscriptionData.get("startDate")) 
                                : user.getCreatedAt())
                        .renewalDate(subscriptionData.get("renewalDate") != null 
                                ? Instant.parse((String) subscriptionData.get("renewalDate")) 
                                : null)
                        .autoRenew((Boolean) subscriptionData.getOrDefault("autoRenew", false))
                        .build())
                .benefits(getBenefitsForTier(tier))
                .usage(SubscriptionResponse.UsageInfo.builder()
                        .activeCampaigns((int) activeCampaigns)
                        .completedCampaigns((int) completedCampaigns)
                        .monthlyApplications((int) monthlyApplications)
                        .build())
                .build();
    }

    /**
     * Get all available subscription plans
     */
    public SubscriptionPlansResponse getAvailablePlans() {
        List<SubscriptionPlansResponse.PlanInfo> plans = Arrays.asList(
                SubscriptionPlansResponse.PlanInfo.builder()
                        .tier("FREE")
                        .name("Free Tier")
                        .price(BigDecimal.ZERO)
                        .billingCycle(null)
                        .benefits(SubscriptionPlansResponse.BenefitsInfo.builder()
                                .maxActiveCampaigns(3)
                                .prioritySupport(false)
                                .advancedAnalytics(false)
                                .cvpiInsights("basic")
                                .earlyAccessFeatures(false)
                                .customBranding(false)
                                .portfolioShowcase(false)
                                .dedicatedAccountManager(false)
                                .customContractTerms(false)
                                .build())
                        .recommended(false)
                        .build(),
                SubscriptionPlansResponse.PlanInfo.builder()
                        .tier("PRO")
                        .name("Pro Creator")
                        .price(new BigDecimal("29"))
                        .billingCycle("monthly")
                        .benefits(SubscriptionPlansResponse.BenefitsInfo.builder()
                                .maxActiveCampaigns(15)
                                .prioritySupport(true)
                                .advancedAnalytics(true)
                                .cvpiInsights("advanced")
                                .earlyAccessFeatures(true)
                                .customBranding(true)
                                .portfolioShowcase(true)
                                .dedicatedAccountManager(false)
                                .customContractTerms(false)
                                .build())
                        .recommended(true)
                        .build(),
                SubscriptionPlansResponse.PlanInfo.builder()
                        .tier("ELITE")
                        .name("Elite Creator")
                        .price(new BigDecimal("99"))
                        .billingCycle("monthly")
                        .benefits(SubscriptionPlansResponse.BenefitsInfo.builder()
                                .maxActiveCampaigns(-1) // Unlimited
                                .prioritySupport(true)
                                .advancedAnalytics(true)
                                .cvpiInsights("premium")
                                .earlyAccessFeatures(true)
                                .customBranding(true)
                                .portfolioShowcase(true)
                                .dedicatedAccountManager(true)
                                .customContractTerms(true)
                                .build())
                        .recommended(false)
                        .build()
        );

        return SubscriptionPlansResponse.builder()
                .plans(plans)
                .build();
    }

    /**
     * Upgrade subscription tier
     */
    @Transactional
    public SubscriptionResponse upgradeSubscription(UUID userId, SubscriptionUpgradeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Validate target tier
        if (!Arrays.asList("PRO", "ELITE").contains(request.getTargetTier())) {
            throw new BadRequestException("Invalid target tier: " + request.getTargetTier());
        }

        // Validate billing cycle
        if (!Arrays.asList("monthly", "annual").contains(request.getBillingCycle())) {
            throw new BadRequestException("Invalid billing cycle: " + request.getBillingCycle());
        }

        // Calculate renewal date
        Instant renewalDate = "annual".equals(request.getBillingCycle())
                ? Instant.now().plus(365, ChronoUnit.DAYS)
                : Instant.now().plus(30, ChronoUnit.DAYS);

        // Update subscription data
        try {
            Map<String, Object> subscriptionData = new HashMap<>();
            subscriptionData.put("tier", request.getTargetTier());
            subscriptionData.put("billingCycle", request.getBillingCycle());
            subscriptionData.put("startDate", Instant.now().toString());
            subscriptionData.put("renewalDate", renewalDate.toString());
            subscriptionData.put("autoRenew", true);
            subscriptionData.put("paymentMethod", request.getPaymentMethod());

            Map<String, Object> profileData = user.getProfileData() != null
                    ? objectMapper.readValue(user.getProfileData(), new TypeReference<Map<String, Object>>() {})
                    : new HashMap<>();
            
            profileData.put("subscription", subscriptionData);
            user.setProfileData(objectMapper.writeValueAsString(profileData));
            userRepository.save(user);

        } catch (JsonProcessingException e) {
            log.error("Error updating subscription", e);
            throw new BadRequestException("Error updating subscription");
        }

        log.info("User {} upgraded to {} tier", userId, request.getTargetTier());

        return getCurrentSubscription(userId);
    }

    /**
     * Get billing history
     */
    public InvoiceListResponse getBillingHistory(UUID userId, int limit, int offset) {
        // Mock invoice data for MVP
        List<InvoiceListResponse.InvoiceItem> invoices = new ArrayList<>();
        
        for (int i = 0; i < Math.min(limit, 5); i++) {
            invoices.add(InvoiceListResponse.InvoiceItem.builder()
                    .invoiceId("inv-" + UUID.randomUUID().toString().substring(0, 8))
                    .date(Instant.now().minus(30 * (i + 1), ChronoUnit.DAYS))
                    .amount(new BigDecimal("29"))
                    .currency("USD")
                    .status("paid")
                    .description("Pro Creator - Monthly")
                    .receiptUrl("https://receipts.aw3.io/inv-" + (i + 1))
                    .build());
        }

        return InvoiceListResponse.builder()
                .invoices(invoices)
                .pagination(InvoiceListResponse.PaginationInfo.builder()
                        .total((long) invoices.size())
                        .limit(limit)
                        .offset(offset)
                        .hasMore(false)
                        .build())
                .build();
    }

    private Map<String, Object> getSubscriptionData(User user) {
        if (user.getProfileData() == null) {
            return new HashMap<>();
        }

        try {
            Map<String, Object> profileData = objectMapper.readValue(
                    user.getProfileData(), 
                    new TypeReference<Map<String, Object>>() {}
            );
            Object subscription = profileData.get("subscription");
            if (subscription instanceof Map) {
                return (Map<String, Object>) subscription;
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing profile data", e);
        }
        
        return new HashMap<>();
    }

    private SubscriptionResponse.BenefitsInfo getBenefitsForTier(String tier) {
        return switch (tier.toUpperCase()) {
            case "PRO" -> SubscriptionResponse.BenefitsInfo.builder()
                    .maxActiveCampaigns(15)
                    .prioritySupport(true)
                    .advancedAnalytics(true)
                    .cvpiInsights("advanced")
                    .earlyAccessFeatures(true)
                    .customBranding(true)
                    .portfolioShowcase(true)
                    .dedicatedAccountManager(false)
                    .customContractTerms(false)
                    .build();
            case "ELITE" -> SubscriptionResponse.BenefitsInfo.builder()
                    .maxActiveCampaigns(-1)
                    .prioritySupport(true)
                    .advancedAnalytics(true)
                    .cvpiInsights("premium")
                    .earlyAccessFeatures(true)
                    .customBranding(true)
                    .portfolioShowcase(true)
                    .dedicatedAccountManager(true)
                    .customContractTerms(true)
                    .build();
            default -> SubscriptionResponse.BenefitsInfo.builder()
                    .maxActiveCampaigns(3)
                    .prioritySupport(false)
                    .advancedAnalytics(false)
                    .cvpiInsights("basic")
                    .earlyAccessFeatures(false)
                    .customBranding(false)
                    .portfolioShowcase(false)
                    .dedicatedAccountManager(false)
                    .customContractTerms(false)
                    .build();
        };
    }
}

