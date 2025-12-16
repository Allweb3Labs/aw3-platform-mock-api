package com.aw3.platform.service;

import com.aw3.platform.dto.admin.CVPIAlgorithmConfigResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

/**
 * Service for admin CVPI configuration management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCVPIService {

    /**
     * Get CVPI algorithm configuration
     */
    public CVPIAlgorithmConfigResponse getAlgorithmConfig() {
        // Build impact score components
        Map<String, CVPIAlgorithmConfigResponse.ComponentWeight> components = new HashMap<>();
        
        components.put("engagement", CVPIAlgorithmConfigResponse.ComponentWeight.builder()
            .weight(0.35)
            .metrics(Arrays.asList("likes", "comments", "shares", "saves"))
            .description("User interaction quality")
            .build());
            
        components.put("reach", CVPIAlgorithmConfigResponse.ComponentWeight.builder()
            .weight(0.25)
            .metrics(Arrays.asList("impressions", "unique_views", "follower_growth"))
            .description("Content visibility and audience expansion")
            .build());
            
        components.put("conversion", CVPIAlgorithmConfigResponse.ComponentWeight.builder()
            .weight(0.30)
            .metrics(Arrays.asList("clicks", "sign_ups", "purchases", "wallet_connects"))
            .description("Direct business outcomes")
            .build());
            
        components.put("sentiment", CVPIAlgorithmConfigResponse.ComponentWeight.builder()
            .weight(0.10)
            .metrics(Arrays.asList("positive_mentions", "brand_sentiment_score"))
            .description("Brand perception and community sentiment")
            .build());

        // Build total cost calculation
        Map<String, Object> totalCost = new HashMap<>();
        totalCost.put("components", Arrays.asList("creator_payment", "platform_service_fee", "oracle_verification_fee"));
        totalCost.put("formula", "Total Cost = Creator Payment + Service Fee + Oracle Fee");

        // Build normalization factors
        Map<String, Object> normalization = new HashMap<>();
        Map<String, Object> engagementRate = new HashMap<>();
        engagementRate.put("min", 0);
        engagementRate.put("max", 15);
        engagementRate.put("description", "Engagement rate normalized to 0-100 scale");
        normalization.put("engagementRate", engagementRate);

        // Build CVPI interpretation
        Map<String, CVPIAlgorithmConfigResponse.CVPIInterpretation> interpretation = new HashMap<>();
        
        interpretation.put("excellent", CVPIAlgorithmConfigResponse.CVPIInterpretation.builder()
            .range(CVPIAlgorithmConfigResponse.Range.builder().min(0.0).max(50.0).build())
            .description("High efficiency - Low cost per impact unit")
            .build());
            
        interpretation.put("good", CVPIAlgorithmConfigResponse.CVPIInterpretation.builder()
            .range(CVPIAlgorithmConfigResponse.Range.builder().min(50.0).max(100.0).build())
            .description("Above average efficiency")
            .build());
            
        interpretation.put("average", CVPIAlgorithmConfigResponse.CVPIInterpretation.builder()
            .range(CVPIAlgorithmConfigResponse.Range.builder().min(100.0).max(200.0).build())
            .description("Market standard performance")
            .build());

        return CVPIAlgorithmConfigResponse.builder()
            .version("v1.2")
            .effectiveDate(Instant.parse("2025-11-01T00:00:00Z"))
            .formula("CVPI = Total Campaign Cost / Verified Impact Score")
            .impactScoreComponents(components)
            .totalCostCalculation(totalCost)
            .normalizationFactors(normalization)
            .cvpiInterpretation(interpretation)
            .build();
    }
}

