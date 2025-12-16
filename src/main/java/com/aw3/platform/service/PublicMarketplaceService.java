package com.aw3.platform.service;

import com.aw3.platform.controller.pub.PublicMarketplaceController.*;
import com.aw3.platform.entity.Campaign;
import com.aw3.platform.repository.CampaignRepository;
import com.aw3.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Public Marketplace Service
 * 
 * Provides public access to marketplace data without authentication
 * All data is cached for performance
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PublicMarketplaceService {

    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "publicCampaigns", key = "#category + '-' + #status + '-' + #page + '-' + #size")
    public PublicCampaignsResponse getCampaigns(String category, String status, BigDecimal minBudget,
            BigDecimal maxBudget, String sortBy, int page, int size) {
        
        PageRequest pageRequest = PageRequest.of(page, size);
        
        // Filter by status (default to ACTIVE for public view)
        Page<Campaign> campaigns = campaignRepository.findByStatus(
                status != null ? status : "ACTIVE", pageRequest);

        List<PublicCampaign> publicCampaigns = campaigns.getContent().stream()
                .map(this::toPublicCampaign)
                .collect(Collectors.toList());

        return PublicCampaignsResponse.builder()
                .campaigns(publicCampaigns)
                .total(campaigns.getTotalElements())
                .page(campaigns.getNumber())
                .size(campaigns.getSize())
                .totalPages(campaigns.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "publicCampaignDetail", key = "#campaignId")
    public PublicCampaignDetail getCampaignDetail(UUID campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        return PublicCampaignDetail.builder()
                .campaignId(campaign.getCampaignId())
                .title(campaign.getTitle())
                .description(campaign.getDescription())
                .category(campaign.getCategory())
                .status(campaign.getStatus())
                .budget(campaign.getTotalBudget())
                .project(ProjectInfo.builder()
                        .projectId(campaign.getProjectId())
                        .name("Project Name") // TODO: Fetch from user
                        .logo(null)
                        .website(null)
                        .category(campaign.getCategory())
                        .verified(false)
                        .rating(BigDecimal.valueOf(4.5))
                        .completedCampaigns(10)
                        .build())
                .creatorsNeeded(campaign.getNumberOfCreators())
                .creatorsApplied(0) // TODO: Count applications
                .requirements(List.of())
                .deliverables(List.of())
                .kpis(List.of())
                .deadline(campaign.getDeadline())
                .createdAt(campaign.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "publicCreators", key = "#category + '-' + #minReputation + '-' + #page + '-' + #size")
    public PublicCreatorsResponse getCreators(String category, Integer minReputation, 
            String sortBy, int page, int size) {
        // TODO: Implement from database
        return PublicCreatorsResponse.builder()
                .creators(List.of())
                .total(0L)
                .page(page)
                .size(size)
                .totalPages(0)
                .build();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "publicCreatorProfile", key = "#creatorId")
    public PublicCreatorProfile getCreatorProfile(UUID creatorId) {
        // TODO: Implement from database
        return PublicCreatorProfile.builder()
                .creatorId(creatorId)
                .displayName("Creator Name")
                .bio("Creator bio")
                .categories(List.of("DeFi", "NFT"))
                .socialLinks(SocialLinks.builder().build())
                .reputation(ReputationInfo.builder()
                        .score(BigDecimal.valueOf(75))
                        .tier("GOLD")
                        .totalReviews(25)
                        .averageRating(BigDecimal.valueOf(4.8))
                        .build())
                .performance(PerformanceStats.builder()
                        .completedCampaigns(15)
                        .cvpiScore(BigDecimal.valueOf(12.5))
                        .successRate(BigDecimal.valueOf(95))
                        .onTimeDeliveryRate(BigDecimal.valueOf(90))
                        .build())
                .portfolio(List.of())
                .joinedAt(Instant.now().minusSeconds(86400 * 365))
                .build();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "publicProjects", key = "#category + '-' + #verified + '-' + #page + '-' + #size")
    public PublicProjectsResponse getProjects(String category, Boolean verified, 
            String sortBy, int page, int size) {
        // TODO: Implement from database
        return PublicProjectsResponse.builder()
                .projects(List.of())
                .total(0L)
                .page(page)
                .size(size)
                .totalPages(0)
                .build();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "publicProjectProfile", key = "#projectId")
    public PublicProjectProfile getProjectProfile(UUID projectId) {
        // TODO: Implement from database
        return PublicProjectProfile.builder()
                .projectId(projectId)
                .name("Project Name")
                .bio("Project description")
                .category("DeFi")
                .verified(true)
                .socialLinks(SocialLinks.builder().build())
                .stats(ProjectStats.builder()
                        .totalCampaigns(25)
                        .completedCampaigns(20)
                        .averageRating(BigDecimal.valueOf(4.7))
                        .totalCreatorsWorkedWith(50)
                        .averageCVPI(BigDecimal.valueOf(15.2))
                        .build())
                .recentCampaigns(List.of())
                .joinedAt(Instant.now().minusSeconds(86400 * 180))
                .build();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "platformStats", unless = "#result == null")
    public PlatformStatsResponse getPlatformStats() {
        long totalCampaigns = campaignRepository.count();
        long activeCampaigns = campaignRepository.countByStatus("ACTIVE");

        return PlatformStatsResponse.builder()
                .totalCreators(2500L)
                .totalProjects(800L)
                .totalCampaigns(totalCampaigns)
                .activeCampaigns(activeCampaigns)
                .totalValueProcessed(BigDecimal.valueOf(5000000))
                .averageCVPI(BigDecimal.valueOf(14.5))
                .topCategories(List.of(
                        CategoryStat.builder().category("DeFi").campaigns(500L).creators(800L).build(),
                        CategoryStat.builder().category("NFT").campaigns(400L).creators(600L).build(),
                        CategoryStat.builder().category("Gaming").campaigns(300L).creators(400L).build()
                ))
                .lastUpdated(Instant.now())
                .build();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "categories")
    public CategoriesResponse getCategories() {
        return CategoriesResponse.builder()
                .categories(List.of(
                        CategoryInfo.builder()
                                .id("defi")
                                .name("DeFi")
                                .description("Decentralized Finance projects")
                                .icon("chart-line")
                                .campaignCount(500L)
                                .creatorCount(800L)
                                .build(),
                        CategoryInfo.builder()
                                .id("nft")
                                .name("NFT")
                                .description("NFT and Digital Art projects")
                                .icon("image")
                                .campaignCount(400L)
                                .creatorCount(600L)
                                .build(),
                        CategoryInfo.builder()
                                .id("gaming")
                                .name("Gaming")
                                .description("Web3 Gaming projects")
                                .icon("gamepad")
                                .campaignCount(300L)
                                .creatorCount(400L)
                                .build(),
                        CategoryInfo.builder()
                                .id("infrastructure")
                                .name("Infrastructure")
                                .description("Blockchain infrastructure projects")
                                .icon("server")
                                .campaignCount(200L)
                                .creatorCount(300L)
                                .build(),
                        CategoryInfo.builder()
                                .id("dao")
                                .name("DAO")
                                .description("Decentralized Autonomous Organizations")
                                .icon("users")
                                .campaignCount(150L)
                                .creatorCount(250L)
                                .build(),
                        CategoryInfo.builder()
                                .id("other")
                                .name("Other")
                                .description("Other Web3 projects")
                                .icon("more")
                                .campaignCount(100L)
                                .creatorCount(150L)
                                .build()
                ))
                .build();
    }

    private PublicCampaign toPublicCampaign(Campaign campaign) {
        return PublicCampaign.builder()
                .campaignId(campaign.getCampaignId())
                .title(campaign.getTitle())
                .description(campaign.getDescription())
                .category(campaign.getCategory())
                .status(campaign.getStatus())
                .budget(campaign.getTotalBudget())
                .budgetRange(getBudgetRange(campaign.getTotalBudget()))
                .project(ProjectPreview.builder()
                        .projectId(campaign.getProjectId())
                        .name("Project Name") // TODO: Fetch
                        .verified(false)
                        .rating(BigDecimal.valueOf(4.5))
                        .build())
                .creatorsNeeded(campaign.getNumberOfCreators())
                .creatorsApplied(0) // TODO: Count
                .requirements(List.of())
                .deadline(campaign.getDeadline())
                .createdAt(campaign.getCreatedAt())
                .build();
    }

    private String getBudgetRange(BigDecimal budget) {
        if (budget == null) return "Unknown";
        int value = budget.intValue();
        if (value < 1000) return "$0 - $1K";
        if (value < 5000) return "$1K - $5K";
        if (value < 10000) return "$5K - $10K";
        if (value < 50000) return "$10K - $50K";
        return "$50K+";
    }
}

