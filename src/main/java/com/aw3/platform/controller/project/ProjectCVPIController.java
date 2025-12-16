package com.aw3.platform.controller.project;

import com.aw3.platform.dto.common.ApiResponse;
import com.aw3.platform.security.CustomUserDetails;
import com.aw3.platform.service.ProjectCVPIService;
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
 * Project Portal - CVPI Analytics Endpoints
 * 
 * Business Rules:
 * - Project can only view CVPI for their own campaigns
 * - CVPI data is available only after campaign completion and oracle verification
 */
@RestController
@RequestMapping("/project")
@PreAuthorize("hasRole('PROJECT')")
@RequiredArgsConstructor
@Slf4j
public class ProjectCVPIController {

    private final ProjectCVPIService projectCVPIService;

    /**
     * GET /api/project/campaigns/{id}/cvpi
     * Retrieve detailed CVPI analytics for a specific campaign
     */
    @GetMapping("/campaigns/{id}/cvpi")
    public ApiResponse<CampaignCVPIResponse> getCampaignCVPI(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {
        
        log.info("Project {} fetching CVPI for campaign {}", userDetails.getUserId(), id);
        
        CampaignCVPIResponse cvpi = projectCVPIService.getCampaignCVPI(userDetails.getUserId(), id);
        
        return ApiResponse.success(cvpi);
    }

    /**
     * GET /api/project/cvpi/portfolio
     * Get CVPI performance summary across all campaigns
     */
    @GetMapping("/cvpi/portfolio")
    public ApiResponse<PortfolioCVPIResponse> getPortfolioCVPI(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        log.info("Project {} fetching portfolio CVPI", userDetails.getUserId());
        
        PortfolioCVPIResponse portfolio = projectCVPIService.getPortfolioCVPI(
                userDetails.getUserId(), status, startDate, endDate);
        
        return ApiResponse.success(portfolio);
    }

    /**
     * GET /api/project/cvpi/creator-rankings
     * View top-performing creators ranked by CVPI for campaign planning
     */
    @GetMapping("/cvpi/creator-rankings")
    public ApiResponse<CreatorRankingsResponse> getCreatorRankings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer minReputation,
            @RequestParam(defaultValue = "20") int limit) {
        
        log.info("Project {} fetching creator rankings", userDetails.getUserId());
        
        CreatorRankingsResponse rankings = projectCVPIService.getCreatorRankings(
                category, minReputation, limit);
        
        return ApiResponse.success(rankings);
    }

    // Response DTOs

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CampaignCVPIResponse {
        private UUID campaignId;
        private String campaignName;
        private String status;
        private CVPIInfo cvpi;
        private CostBreakdown costBreakdown;
        private ImpactMetrics impactMetrics;
        private CreatorPerformance creatorPerformance;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CVPIInfo {
        private BigDecimal overall;
        private BigDecimal industryAverage;
        private Integer percentile;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CostBreakdown {
        private BigDecimal totalCost;
        private BigDecimal creatorPayment;
        private BigDecimal platformFee;
        private BigDecimal oracleFee;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ImpactMetrics {
        private BigDecimal totalImpactScore;
        private String verifiedBy;
        private Instant verificationDate;
        private List<KPIResult> kpiResults;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class KPIResult {
        private String metric;
        private BigDecimal target;
        private BigDecimal actual;
        private BigDecimal achievement;
        private BigDecimal weight;
        private BigDecimal contribution;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CreatorPerformance {
        private UUID creatorId;
        private String creatorName;
        private BigDecimal creatorHistoricalCVPI;
        private String thisComparison;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PortfolioCVPIResponse {
        private UUID projectId;
        private SummaryInfo summary;
        private List<CVPITrendItem> cvpiTrend;
        private List<CategoryBreakdown> categoryBreakdown;
        private BenchmarkingInfo benchmarking;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SummaryInfo {
        private Integer totalCampaigns;
        private Integer completedCampaigns;
        private BigDecimal averageCVPI;
        private BigDecimal bestCVPI;
        private BigDecimal worstCVPI;
        private BigDecimal totalSpent;
        private BigDecimal totalVerifiedImpact;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CVPITrendItem {
        private String month;
        private BigDecimal averageCVPI;
        private Integer campaigns;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CategoryBreakdown {
        private String category;
        private Integer campaigns;
        private BigDecimal averageCVPI;
        private BigDecimal totalSpent;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BenchmarkingInfo {
        private BigDecimal yourAverageCVPI;
        private BigDecimal platformAverage;
        private Integer yourPercentile;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CreatorRankingsResponse {
        private List<CreatorRankItem> rankings;
        private Long totalCreators;
        private FilterApplied filterApplied;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CreatorRankItem {
        private Integer rank;
        private UUID creatorId;
        private String creatorName;
        private BigDecimal cvpi;
        private Integer reputation;
        private Integer completedCampaigns;
        private List<String> categories;
        private BigDecimal averageImpact;
        private BigDecimal responseRate;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FilterApplied {
        private String category;
        private Integer minReputation;
    }
}

