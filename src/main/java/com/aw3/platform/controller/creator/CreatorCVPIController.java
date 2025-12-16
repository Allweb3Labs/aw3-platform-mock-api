package com.aw3.platform.controller.creator;

import com.aw3.platform.dto.common.ApiResponse;
import com.aw3.platform.dto.creator.CVPIProjectionResponse;
import com.aw3.platform.dto.creator.CVPIRecommendationsResponse;
import com.aw3.platform.dto.cvpi.CVPIHistoryResponse;
import com.aw3.platform.dto.cvpi.CVPIScoreResponse;
import com.aw3.platform.security.CustomUserDetails;
import com.aw3.platform.service.CVPICalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Creator Portal - CVPI Endpoints
 * 
 * CVPI (Creator Value Performance Index) = Total Campaign Cost / Verified Impact Score
 * Lower CVPI = Better cost efficiency
 * 
 * CVPI Interpretation:
 * - Excellent: 0-50 (High efficiency)
 * - Good: 50-100 (Above average)
 * - Average: 100-200 (Market standard)
 * - Below Average: 200-500 (Low efficiency)
 * - Poor: 500+ (Very low efficiency)
 */
@RestController
@RequestMapping("/creator/cvpi")
@PreAuthorize("hasRole('CREATOR')")
@RequiredArgsConstructor
@Slf4j
public class CreatorCVPIController {

    private final CVPICalculationService cvpiCalculationService;

    /**
     * GET /api/creator/cvpi/score
     * Retrieve the creator's current CVPI score and ranking
     */
    @GetMapping("/score")
    public ApiResponse<CVPIScoreResponse> getCVPIScore(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Creator {} fetching CVPI score", userDetails.getUserId());

        CVPIScoreResponse score = cvpiCalculationService.getCreatorCVPIScore(userDetails.getUserId());
        
        return ApiResponse.success(score);
    }

    /**
     * GET /api/creator/cvpi/history
     * Retrieve historical CVPI trend data for the creator
     */
    @GetMapping("/history")
    public ApiResponse<CVPIHistoryResponse> getCVPIHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "30d") String period,
            @RequestParam(defaultValue = "daily") String granularity) {
        
        log.info("Creator {} fetching CVPI history for period: {}", 
                userDetails.getUserId(), period);

        CVPIHistoryResponse history = cvpiCalculationService.getCreatorCVPIHistory(
                userDetails.getUserId(), 
                period
        );
        
        return ApiResponse.success(history);
    }

    /**
     * GET /api/creator/cvpi/recommendations
     * Get AI-matched campaigns optimized for the creator's CVPI profile
     * 
     * Recommendation Algorithm Factors:
     * - Historical CVPI (35%): Past performance in similar categories
     * - Audience Match (25%): Alignment between creator's audience and campaign target
     * - Budget Fit (20%): Campaign budget within creator's optimal performance range
     * - Category Expertise (15%): Track record in the campaign category
     * - Reputation Match (5%): Creator's reputation score vs. campaign requirements
     */
    @GetMapping("/recommendations")
    public ApiResponse<CVPIRecommendationsResponse> getCVPIRecommendations(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minBudget,
            @RequestParam(required = false) BigDecimal maxBudget) {
        
        log.info("Creator {} fetching CVPI recommendations with limit: {}", 
                userDetails.getUserId(), limit);

        CVPIRecommendationsResponse recommendations = cvpiCalculationService.getCVPIRecommendations(
                userDetails.getUserId(),
                limit,
                category,
                minBudget,
                maxBudget
        );
        
        return ApiResponse.success(recommendations);
    }

    /**
     * GET /api/creator/cvpi/projection/{campaignId}
     * Get projected CVPI score for a specific campaign before applying
     * 
     * Projection includes:
     * - Estimated cost breakdown (base payment + platform fee + oracle fee)
     * - Projected impact based on historical performance
     * - Confidence score based on data availability
     * - Comparison to platform average
     */
    @GetMapping("/projection/{campaignId}")
    public ApiResponse<CVPIProjectionResponse> getCVPIProjection(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID campaignId) {
        
        log.info("Creator {} fetching CVPI projection for campaign {}", 
                userDetails.getUserId(), campaignId);

        CVPIProjectionResponse projection = cvpiCalculationService.getCVPIProjection(
                userDetails.getUserId(),
                campaignId
        );
        
        return ApiResponse.success(projection);
    }
}

