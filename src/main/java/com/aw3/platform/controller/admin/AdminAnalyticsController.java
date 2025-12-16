package com.aw3.platform.controller.admin;

import com.aw3.platform.dto.common.ApiResponse;
import com.aw3.platform.security.CustomUserDetails;
import com.aw3.platform.service.AdminAnalyticsService;
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
 * Admin Portal - Platform Analytics Endpoints
 * 
 * Provides comprehensive platform-wide analytics including:
 * - User growth metrics
 * - Campaign performance
 * - Financial health
 * - CVPI trends
 * - System health
 */
@RestController
@RequestMapping("/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminAnalyticsController {

    private final AdminAnalyticsService analyticsService;

    /**
     * GET /api/admin/analytics/dashboard
     * Get comprehensive platform dashboard
     */
    @GetMapping("/dashboard")
    public ApiResponse<DashboardResponse> getDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String period) {
        
        log.info("Admin {} fetching dashboard", userDetails.getUserId());
        
        DashboardResponse dashboard = analyticsService.getDashboard(period);
        
        return ApiResponse.success(dashboard);
    }

    /**
     * GET /api/admin/analytics/users
     * Get user growth and engagement analytics
     */
    @GetMapping("/users")
    public ApiResponse<UserAnalyticsResponse> getUserAnalytics(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String role) {
        
        log.info("Admin {} fetching user analytics", userDetails.getUserId());
        
        UserAnalyticsResponse analytics = analyticsService.getUserAnalytics(period, role);
        
        return ApiResponse.success(analytics);
    }

    /**
     * GET /api/admin/analytics/campaigns
     * Get campaign performance analytics
     */
    @GetMapping("/campaigns")
    public ApiResponse<CampaignAnalyticsResponse> getCampaignAnalytics(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String category) {
        
        log.info("Admin {} fetching campaign analytics", userDetails.getUserId());
        
        CampaignAnalyticsResponse analytics = analyticsService.getCampaignAnalytics(period, category);
        
        return ApiResponse.success(analytics);
    }

    /**
     * GET /api/admin/analytics/financial
     * Get financial health metrics
     */
    @GetMapping("/financial")
    public ApiResponse<FinancialAnalyticsResponse> getFinancialAnalytics(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String period) {
        
        log.info("Admin {} fetching financial analytics", userDetails.getUserId());
        
        FinancialAnalyticsResponse analytics = analyticsService.getFinancialAnalytics(period);
        
        return ApiResponse.success(analytics);
    }

    /**
     * GET /api/admin/analytics/cvpi
     * Get platform-wide CVPI statistics
     */
    @GetMapping("/cvpi")
    public ApiResponse<PlatformCVPIAnalyticsResponse> getCVPIAnalytics(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String period) {
        
        log.info("Admin {} fetching CVPI analytics", userDetails.getUserId());
        
        PlatformCVPIAnalyticsResponse analytics = analyticsService.getCVPIAnalytics(period);
        
        return ApiResponse.success(analytics);
    }

    /**
     * GET /api/admin/analytics/system
     * Get system health and performance metrics
     */
    @GetMapping("/system")
    public ApiResponse<SystemAnalyticsResponse> getSystemAnalytics(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Admin {} fetching system analytics", userDetails.getUserId());
        
        SystemAnalyticsResponse analytics = analyticsService.getSystemAnalytics();
        
        return ApiResponse.success(analytics);
    }

    /**
     * GET /api/admin/analytics/export
     * Export analytics data
     */
    @GetMapping("/export")
    public ApiResponse<ExportResponse> exportAnalytics(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String type,
            @RequestParam String format,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        log.info("Admin {} exporting {} analytics as {}", userDetails.getUserId(), type, format);
        
        ExportResponse export = analyticsService.exportAnalytics(type, format, startDate, endDate);
        
        return ApiResponse.success(export);
    }

    // DTOs

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DashboardResponse {
        private OverviewMetrics overview;
        private UserMetrics users;
        private CampaignMetrics campaigns;
        private FinancialMetrics financial;
        private CVPIMetrics cvpi;
        private List<AlertItem> alerts;
        private Instant generatedAt;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class OverviewMetrics {
        private Long totalUsers;
        private Long activeUsers;
        private Long totalCampaigns;
        private Long activeCampaigns;
        private BigDecimal totalValueLocked;
        private BigDecimal platformRevenue;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserMetrics {
        private Long totalCreators;
        private Long totalProjects;
        private Long totalValidators;
        private Long newUsersToday;
        private Long newUsersThisWeek;
        private BigDecimal userGrowthRate;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CampaignMetrics {
        private Long activeCampaigns;
        private Long completedThisMonth;
        private BigDecimal successRate;
        private BigDecimal averageBudget;
        private BigDecimal averageCVPI;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FinancialMetrics {
        private BigDecimal totalVolume;
        private BigDecimal feesCollected;
        private BigDecimal escrowBalance;
        private BigDecimal averageTransactionValue;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CVPIMetrics {
        private BigDecimal platformAverage;
        private BigDecimal platformMedian;
        private BigDecimal bestCVPI;
        private BigDecimal worstCVPI;
        private BigDecimal improvementRate;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AlertItem {
        private String type;
        private String severity;
        private String message;
        private Instant timestamp;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserAnalyticsResponse {
        private GrowthStats growth;
        private List<GrowthTrendItem> growthTrend;
        private RoleDistribution roleDistribution;
        private EngagementStats engagement;
        private RetentionStats retention;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class GrowthStats {
        private Long totalUsers;
        private Long newUsers;
        private BigDecimal growthRate;
        private Long activeUsers;
        private BigDecimal activeRate;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class GrowthTrendItem {
        private String date;
        private Long totalUsers;
        private Long newUsers;
        private Long activeUsers;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RoleDistribution {
        private Long creators;
        private Long projects;
        private Long validators;
        private Long admins;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EngagementStats {
        private BigDecimal dailyActiveUsers;
        private BigDecimal weeklyActiveUsers;
        private BigDecimal monthlyActiveUsers;
        private BigDecimal averageSessionDuration;
        private BigDecimal actionsPerSession;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RetentionStats {
        private BigDecimal day1Retention;
        private BigDecimal day7Retention;
        private BigDecimal day30Retention;
        private BigDecimal churnRate;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CampaignAnalyticsResponse {
        private CampaignOverview overview;
        private List<CampaignTrendItem> trend;
        private List<CategoryStats> byCategory;
        private PerformanceStats performance;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CampaignOverview {
        private Long totalCampaigns;
        private Long activeCampaigns;
        private Long completedCampaigns;
        private Long cancelledCampaigns;
        private BigDecimal totalValue;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CampaignTrendItem {
        private String date;
        private Long created;
        private Long completed;
        private BigDecimal totalValue;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CategoryStats {
        private String category;
        private Long count;
        private BigDecimal totalValue;
        private BigDecimal successRate;
        private BigDecimal averageCVPI;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PerformanceStats {
        private BigDecimal successRate;
        private BigDecimal averageCompletionTime;
        private BigDecimal onTimeDeliveryRate;
        private BigDecimal disputeRate;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FinancialAnalyticsResponse {
        private RevenueOverview revenue;
        private List<RevenueTrendItem> revenueTrend;
        private VolumeStats volume;
        private FeeBreakdown feeBreakdown;
        private EscrowStats escrow;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RevenueOverview {
        private BigDecimal totalRevenue;
        private BigDecimal periodRevenue;
        private BigDecimal growthRate;
        private BigDecimal projectedAnnual;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RevenueTrendItem {
        private String date;
        private BigDecimal revenue;
        private BigDecimal volume;
        private Long transactions;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class VolumeStats {
        private BigDecimal totalVolume;
        private BigDecimal periodVolume;
        private Long totalTransactions;
        private BigDecimal averageTransactionValue;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FeeBreakdown {
        private BigDecimal platformFees;
        private BigDecimal oracleFees;
        private BigDecimal subscriptionFees;
        private BigDecimal otherFees;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EscrowStats {
        private BigDecimal totalLocked;
        private BigDecimal totalReleased;
        private BigDecimal pendingRelease;
        private Long activeEscrows;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PlatformCVPIAnalyticsResponse {
        private CVPIOverview overview;
        private List<CVPITrendItem> trend;
        private List<CVPICategoryBreakdown> byCategory;
        private CVPIDistribution distribution;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CVPIOverview {
        private BigDecimal platformAverage;
        private BigDecimal platformMedian;
        private BigDecimal bestPerformer;
        private BigDecimal improvement;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CVPITrendItem {
        private String date;
        private BigDecimal averageCVPI;
        private Long campaignsCompleted;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CVPICategoryBreakdown {
        private String category;
        private BigDecimal averageCVPI;
        private Long campaigns;
        private Integer percentile;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CVPIDistribution {
        private List<DistributionBucket> buckets;
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
    public static class SystemAnalyticsResponse {
        private ServiceHealth health;
        private PerformanceMetrics performance;
        private ResourceUsage resources;
        private List<ErrorItem> recentErrors;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ServiceHealth {
        private String overallStatus;
        private List<ServiceStatus> services;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ServiceStatus {
        private String name;
        private String status;
        private BigDecimal uptime;
        private Instant lastCheck;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PerformanceMetrics {
        private BigDecimal averageResponseTime;
        private BigDecimal p95ResponseTime;
        private BigDecimal p99ResponseTime;
        private Long requestsPerSecond;
        private BigDecimal errorRate;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResourceUsage {
        private BigDecimal cpuUsage;
        private BigDecimal memoryUsage;
        private BigDecimal diskUsage;
        private BigDecimal networkBandwidth;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ErrorItem {
        private Instant timestamp;
        private String service;
        private String message;
        private Integer count;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ExportResponse {
        private String exportId;
        private String type;
        private String format;
        private String downloadUrl;
        private Long fileSizeBytes;
        private Instant expiresAt;
    }
}
