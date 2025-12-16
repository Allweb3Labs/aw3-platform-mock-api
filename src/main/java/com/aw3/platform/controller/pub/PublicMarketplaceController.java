package com.aw3.platform.controller.pub;

import com.aw3.platform.dto.common.ApiResponse;
import com.aw3.platform.service.PublicMarketplaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Public Marketplace Controller
 * 
 * Provides public access to marketplace data without authentication
 * Used for campaign discovery, creator exploration, and platform stats
 */
@RestController
@RequestMapping("/public/marketplace")
@RequiredArgsConstructor
@Slf4j
public class PublicMarketplaceController {

    private final PublicMarketplaceService marketplaceService;

    /**
     * GET /api/public/marketplace/campaigns
     * Browse public campaigns
     */
    @GetMapping("/campaigns")
    public ApiResponse<PublicCampaignsResponse> getCampaigns(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal minBudget,
            @RequestParam(required = false) BigDecimal maxBudget,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Public campaigns request: category={}, status={}", category, status);
        
        PublicCampaignsResponse campaigns = marketplaceService.getCampaigns(
                category, status, minBudget, maxBudget, sortBy, page, size);
        
        return ApiResponse.success(campaigns);
    }

    /**
     * GET /api/public/marketplace/campaigns/{id}
     * Get public campaign details
     */
    @GetMapping("/campaigns/{id}")
    public ApiResponse<PublicCampaignDetail> getCampaignDetail(@PathVariable UUID id) {
        log.info("Public campaign detail request: {}", id);
        
        PublicCampaignDetail campaign = marketplaceService.getCampaignDetail(id);
        
        return ApiResponse.success(campaign);
    }

    /**
     * GET /api/public/marketplace/creators
     * Browse public creator profiles
     */
    @GetMapping("/creators")
    public ApiResponse<PublicCreatorsResponse> getCreators(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer minReputation,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Public creators request: category={}", category);
        
        PublicCreatorsResponse creators = marketplaceService.getCreators(
                category, minReputation, sortBy, page, size);
        
        return ApiResponse.success(creators);
    }

    /**
     * GET /api/public/marketplace/creators/{id}
     * Get public creator profile
     */
    @GetMapping("/creators/{id}")
    public ApiResponse<PublicCreatorProfile> getCreatorProfile(@PathVariable UUID id) {
        log.info("Public creator profile request: {}", id);
        
        PublicCreatorProfile creator = marketplaceService.getCreatorProfile(id);
        
        return ApiResponse.success(creator);
    }

    /**
     * GET /api/public/marketplace/projects
     * Browse public project profiles
     */
    @GetMapping("/projects")
    public ApiResponse<PublicProjectsResponse> getProjects(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean verified,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Public projects request: category={}", category);
        
        PublicProjectsResponse projects = marketplaceService.getProjects(
                category, verified, sortBy, page, size);
        
        return ApiResponse.success(projects);
    }

    /**
     * GET /api/public/marketplace/projects/{id}
     * Get public project profile
     */
    @GetMapping("/projects/{id}")
    public ApiResponse<PublicProjectProfile> getProjectProfile(@PathVariable UUID id) {
        log.info("Public project profile request: {}", id);
        
        PublicProjectProfile project = marketplaceService.getProjectProfile(id);
        
        return ApiResponse.success(project);
    }

    /**
     * GET /api/public/marketplace/stats
     * Get platform statistics
     */
    @GetMapping("/stats")
    public ApiResponse<PlatformStatsResponse> getPlatformStats() {
        log.info("Public platform stats request");
        
        PlatformStatsResponse stats = marketplaceService.getPlatformStats();
        
        return ApiResponse.success(stats);
    }

    /**
     * GET /api/public/marketplace/categories
     * Get available categories
     */
    @GetMapping("/categories")
    public ApiResponse<CategoriesResponse> getCategories() {
        log.info("Public categories request");
        
        CategoriesResponse categories = marketplaceService.getCategories();
        
        return ApiResponse.success(categories);
    }

    // DTOs

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PublicCampaignsResponse {
        private List<PublicCampaign> campaigns;
        private Long total;
        private Integer page;
        private Integer size;
        private Integer totalPages;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PublicCampaign {
        private UUID campaignId;
        private String title;
        private String description;
        private String category;
        private String status;
        private BigDecimal budget;
        private String budgetRange;
        private ProjectPreview project;
        private Integer creatorsNeeded;
        private Integer creatorsApplied;
        private List<String> requirements;
        private Instant deadline;
        private Instant createdAt;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProjectPreview {
        private UUID projectId;
        private String name;
        private String logo;
        private Boolean verified;
        private BigDecimal rating;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PublicCampaignDetail {
        private UUID campaignId;
        private String title;
        private String description;
        private String category;
        private String status;
        private BigDecimal budget;
        private ProjectInfo project;
        private Integer creatorsNeeded;
        private Integer creatorsApplied;
        private List<String> requirements;
        private List<String> deliverables;
        private List<KPIInfo> kpis;
        private Instant deadline;
        private Instant createdAt;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProjectInfo {
        private UUID projectId;
        private String name;
        private String logo;
        private String website;
        private String category;
        private Boolean verified;
        private BigDecimal rating;
        private Integer completedCampaigns;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class KPIInfo {
        private String metric;
        private String target;
        private String description;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PublicCreatorsResponse {
        private List<PublicCreator> creators;
        private Long total;
        private Integer page;
        private Integer size;
        private Integer totalPages;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PublicCreator {
        private UUID creatorId;
        private String displayName;
        private String avatar;
        private String bio;
        private List<String> categories;
        private BigDecimal reputationScore;
        private String tier;
        private Integer completedCampaigns;
        private BigDecimal cvpiScore;
        private List<String> platforms;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PublicCreatorProfile {
        private UUID creatorId;
        private String displayName;
        private String avatar;
        private String bio;
        private List<String> categories;
        private SocialLinks socialLinks;
        private ReputationInfo reputation;
        private PerformanceStats performance;
        private List<PortfolioItem> portfolio;
        private Instant joinedAt;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SocialLinks {
        private String twitter;
        private String youtube;
        private String instagram;
        private String tiktok;
        private String discord;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReputationInfo {
        private BigDecimal score;
        private String tier;
        private Integer totalReviews;
        private BigDecimal averageRating;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PerformanceStats {
        private Integer completedCampaigns;
        private BigDecimal cvpiScore;
        private BigDecimal successRate;
        private BigDecimal onTimeDeliveryRate;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PortfolioItem {
        private String title;
        private String description;
        private String type;
        private String url;
        private String thumbnail;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PublicProjectsResponse {
        private List<PublicProject> projects;
        private Long total;
        private Integer page;
        private Integer size;
        private Integer totalPages;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PublicProject {
        private UUID projectId;
        private String name;
        private String logo;
        private String category;
        private Boolean verified;
        private BigDecimal rating;
        private Integer totalCampaigns;
        private Integer activeCampaigns;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PublicProjectProfile {
        private UUID projectId;
        private String name;
        private String logo;
        private String bio;
        private String website;
        private String category;
        private Boolean verified;
        private SocialLinks socialLinks;
        private ProjectStats stats;
        private List<PublicCampaign> recentCampaigns;
        private Instant joinedAt;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProjectStats {
        private Integer totalCampaigns;
        private Integer completedCampaigns;
        private BigDecimal averageRating;
        private Integer totalCreatorsWorkedWith;
        private BigDecimal averageCVPI;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PlatformStatsResponse {
        private Long totalCreators;
        private Long totalProjects;
        private Long totalCampaigns;
        private Long activeCampaigns;
        private BigDecimal totalValueProcessed;
        private BigDecimal averageCVPI;
        private List<CategoryStat> topCategories;
        private Instant lastUpdated;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CategoryStat {
        private String category;
        private Long campaigns;
        private Long creators;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CategoriesResponse {
        private List<CategoryInfo> categories;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CategoryInfo {
        private String id;
        private String name;
        private String description;
        private String icon;
        private Long campaignCount;
        private Long creatorCount;
    }
}

