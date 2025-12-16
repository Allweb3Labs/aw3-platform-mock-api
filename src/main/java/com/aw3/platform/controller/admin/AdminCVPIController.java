package com.aw3.platform.controller.admin;

import com.aw3.platform.dto.admin.CVPIAlgorithmConfigResponse;
import com.aw3.platform.dto.common.ApiResponse;
import com.aw3.platform.security.CustomUserDetails;
import com.aw3.platform.service.AdminCVPIService;
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
 * Admin CVPI Configuration Controller
 * 
 * Endpoints for managing CVPI algorithm configuration and platform-wide statistics
 * Base path: /api/admin/cvpi
 * 
 * Business Rules:
 * - Algorithm weight changes require DAO approval
 * - Recalculation is resource-intensive, batched asynchronously
 * - All configuration changes are audit logged
 */
@RestController
@RequestMapping("/admin/cvpi")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminCVPIController {

    private final AdminCVPIService adminCVPIService;

    /**
     * GET /api/admin/cvpi/algorithm-config
     * Get CVPI algorithm configuration
     */
    @GetMapping("/algorithm-config")
    public ApiResponse<CVPIAlgorithmConfigResponse> getAlgorithmConfig(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Admin {} fetching CVPI algorithm configuration", userDetails.getUserId());
        CVPIAlgorithmConfigResponse response = adminCVPIService.getAlgorithmConfig();
        return ApiResponse.success(response);
    }

    /**
     * PUT /api/admin/cvpi/algorithm-config
     * Update CVPI algorithm configuration (requires DAO approval for major changes)
     */
    @PutMapping("/algorithm-config")
    public ApiResponse<CVPIAlgorithmConfigResponse> updateAlgorithmConfig(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AlgorithmConfigUpdateRequest request) {
        log.info("Admin {} updating CVPI algorithm configuration", userDetails.getUserId());
        CVPIAlgorithmConfigResponse response = adminCVPIService.updateAlgorithmConfig(
                userDetails.getUserId(), request);
        return ApiResponse.success(response);
    }

    /**
     * GET /api/admin/cvpi/platform-stats
     * Get platform-wide CVPI statistics
     */
    @GetMapping("/platform-stats")
    public ApiResponse<PlatformCVPIStatsResponse> getPlatformStats(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String period) {
        log.info("Admin {} fetching platform CVPI stats", userDetails.getUserId());
        PlatformCVPIStatsResponse stats = adminCVPIService.getPlatformStats(period);
        return ApiResponse.success(stats);
    }

    /**
     * POST /api/admin/cvpi/recalculate
     * Trigger CVPI recalculation for specific campaigns or all
     */
    @PostMapping("/recalculate")
    public ApiResponse<RecalculationResponse> triggerRecalculation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody RecalculationRequest request) {
        log.info("Admin {} triggering CVPI recalculation", userDetails.getUserId());
        RecalculationResponse response = adminCVPIService.triggerRecalculation(
                userDetails.getUserId(), request);
        return ApiResponse.success(response);
    }

    /**
     * GET /api/admin/cvpi/recalculate/{jobId}
     * Check recalculation job status
     */
    @GetMapping("/recalculate/{jobId}")
    public ApiResponse<RecalculationJobStatus> getRecalculationStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID jobId) {
        log.info("Admin {} checking recalculation job {}", userDetails.getUserId(), jobId);
        RecalculationJobStatus status = adminCVPIService.getRecalculationStatus(jobId);
        return ApiResponse.success(status);
    }

    /**
     * GET /api/admin/cvpi/outliers
     * Get campaigns with abnormal CVPI values for review
     */
    @GetMapping("/outliers")
    public ApiResponse<CVPIOutliersResponse> getOutliers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "2.0") BigDecimal threshold) {
        log.info("Admin {} fetching CVPI outliers with threshold {}", userDetails.getUserId(), threshold);
        CVPIOutliersResponse outliers = adminCVPIService.getOutliers(threshold);
        return ApiResponse.success(outliers);
    }

    // DTOs

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AlgorithmConfigUpdateRequest {
        private WeightConfig weights;
        private NormalizationConfig normalization;
        private BenchmarkConfig benchmarks;
        private String daoProposalId; // Required for major changes
        private String changeReason;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class WeightConfig {
        private BigDecimal engagementWeight;
        private BigDecimal conversionWeight;
        private BigDecimal reachWeight;
        private BigDecimal qualityWeight;
        private BigDecimal timelinessWeight;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class NormalizationConfig {
        private String method; // MIN_MAX, Z_SCORE, LOG
        private BigDecimal minValue;
        private BigDecimal maxValue;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BenchmarkConfig {
        private BigDecimal excellent;
        private BigDecimal good;
        private BigDecimal average;
        private BigDecimal poor;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PlatformCVPIStatsResponse {
        private CVPIOverview overview;
        private List<CVPITrendItem> trend;
        private DistributionStats distribution;
        private List<CategoryPerformance> byCategory;
        private List<TopPerformer> topPerformers;
        private Instant generatedAt;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CVPIOverview {
        private BigDecimal platformAverage;
        private BigDecimal platformMedian;
        private BigDecimal standardDeviation;
        private Long totalCampaignsScored;
        private BigDecimal improvementRate;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CVPITrendItem {
        private String period;
        private BigDecimal averageCVPI;
        private Long campaignsCompleted;
        private BigDecimal totalValueProcessed;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DistributionStats {
        private List<DistributionBucket> buckets;
        private BigDecimal p25;
        private BigDecimal p50;
        private BigDecimal p75;
        private BigDecimal p90;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DistributionBucket {
        private BigDecimal rangeStart;
        private BigDecimal rangeEnd;
        private Long count;
        private BigDecimal percentage;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CategoryPerformance {
        private String category;
        private BigDecimal averageCVPI;
        private Long campaigns;
        private BigDecimal improvement;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TopPerformer {
        private UUID entityId;
        private String name;
        private String type; // CREATOR, PROJECT
        private BigDecimal cvpi;
        private Integer campaignsCompleted;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RecalculationRequest {
        private String scope; // ALL, CATEGORY, CAMPAIGN_IDS
        private String category;
        private List<UUID> campaignIds;
        private Boolean forceRecalculate;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RecalculationResponse {
        private UUID jobId;
        private String scope;
        private Integer campaignsQueued;
        private String status;
        private Instant startedAt;
        private Instant estimatedCompletion;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RecalculationJobStatus {
        private UUID jobId;
        private String status; // QUEUED, IN_PROGRESS, COMPLETED, FAILED
        private Integer totalCampaigns;
        private Integer processedCampaigns;
        private Integer failedCampaigns;
        private BigDecimal progressPercent;
        private Instant startedAt;
        private Instant completedAt;
        private String errorMessage;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CVPIOutliersResponse {
        private List<OutlierCampaign> outliers;
        private BigDecimal thresholdUsed;
        private Long totalOutliers;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class OutlierCampaign {
        private UUID campaignId;
        private String title;
        private UUID projectId;
        private String projectName;
        private BigDecimal cvpi;
        private BigDecimal deviation;
        private String outlierType; // HIGH, LOW
        private Boolean flagged;
        private Instant completedAt;
    }
}
