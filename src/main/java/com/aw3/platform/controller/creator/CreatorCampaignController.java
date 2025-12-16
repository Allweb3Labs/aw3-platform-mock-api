package com.aw3.platform.controller.creator;

import com.aw3.platform.dto.common.ApiResponse;
import com.aw3.platform.dto.creator.CampaignListResponse;
import com.aw3.platform.entity.Campaign;
import com.aw3.platform.entity.enums.CampaignStatus;
import com.aw3.platform.repository.CampaignRepository;
import com.aw3.platform.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Creator Portal - Campaign Discovery Endpoints
 * 
 * Business Rules:
 * - Creators can only view campaigns with status = ACTIVE
 * - Cannot see other creators' applications
 * - Filtering by: category, budget range, reputation requirement
 */
@RestController
@RequestMapping("/creator/campaigns")
@PreAuthorize("hasRole('CREATOR')")
@RequiredArgsConstructor
@Slf4j
public class CreatorCampaignController {

    private final CampaignRepository campaignRepository;

    /**
     * GET /api/creator/campaigns
     * Browse active campaigns with filtering
     */
    @GetMapping
    public ApiResponse<CampaignListResponse> browseCampaigns(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minBudget,
            @RequestParam(required = false) BigDecimal maxBudget,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Creator {} browsing campaigns", userDetails.getUserId());

        Pageable pageable = PageRequest.of(page, size);
        Page<Campaign> campaigns = campaignRepository.findActiveCampaignsWithFilters(
                CampaignStatus.ACTIVE,
                category,
                minBudget,
                maxBudget,
                pageable
        );

        List<CampaignListResponse.CampaignItem> campaignItems = campaigns.getContent().stream()
                .map(campaign -> CampaignListResponse.CampaignItem.builder()
                        .campaignId(campaign.getCampaignId())
                        .title(campaign.getTitle())
                        .description(campaign.getDescription())
                        .category(campaign.getCategory())
                        .budgetAmount(campaign.getBudgetAmount())
                        .budgetToken(campaign.getBudgetToken())
                        .requiredReputation(campaign.getRequiredReputation())
                        .numberOfCreators(campaign.getNumberOfCreators())
                        .deadline(campaign.getDeadline())
                        .createdAt(campaign.getCreatedAt())
                        .hasApplied(false) // TODO: Check if creator has applied
                        .matchScore(null) // TODO: Calculate match score
                        .projectName("Project Name") // TODO: Get project name
                        .build())
                .collect(Collectors.toList());

        CampaignListResponse response = CampaignListResponse.builder()
                .campaigns(campaignItems)
                .pagination(CampaignListResponse.PageInfo.builder()
                        .currentPage(campaigns.getNumber())
                        .totalPages(campaigns.getTotalPages())
                        .totalElements(campaigns.getTotalElements())
                        .pageSize(campaigns.getSize())
                        .build())
                .build();

        return ApiResponse.success(response);
    }

    /**
     * GET /api/creator/campaigns/{id}
     * View campaign details
     */
    @GetMapping("/{id}")
    public ApiResponse<Campaign> getCampaignDetails(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String id) {
        
        log.info("Creator {} viewing campaign {}", userDetails.getUserId(), id);

        Campaign campaign = campaignRepository.findById(java.util.UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        // Verify campaign is active
        if (campaign.getStatus() != CampaignStatus.ACTIVE) {
            throw new RuntimeException("Campaign is not active");
        }

        return ApiResponse.success(campaign);
    }

    /**
     * GET /api/creator/campaigns/recommended
     * AI-matched campaigns (Phase 2 feature)
     */
    @GetMapping("/recommended")
    public ApiResponse<CampaignListResponse> getRecommendedCampaigns(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Creator {} fetching recommended campaigns", userDetails.getUserId());

        // TODO: Implement AI matching algorithm
        // For MVP, return empty list or top campaigns
        
        CampaignListResponse response = CampaignListResponse.builder()
                .campaigns(List.of())
                .pagination(CampaignListResponse.PageInfo.builder()
                        .currentPage(page)
                        .totalPages(0)
                        .totalElements(0L)
                        .pageSize(size)
                        .build())
                .build();

        return ApiResponse.success(response);
    }
}

