package com.aw3.platform.controller.project;

import com.aw3.platform.dto.common.ApiResponse;
import com.aw3.platform.dto.project.CampaignCreateRequest;
import com.aw3.platform.dto.project.CampaignResponse;
import com.aw3.platform.entity.Campaign;
import com.aw3.platform.entity.enums.CampaignStatus;
import com.aw3.platform.repository.ApplicationRepository;
import com.aw3.platform.repository.CampaignRepository;
import com.aw3.platform.repository.DeliverableRepository;
import com.aw3.platform.security.CustomUserDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Project Portal - Campaign Management Endpoints
 * 
 * Business Rules:
 * - Projects can only manage campaigns they own
 * - Must lock Budget + Estimated Fees + Buffer in escrow when creating campaign
 * - Fee estimate is valid for 15 minutes
 */
@RestController
@RequestMapping("/project/campaigns")
@PreAuthorize("hasRole('PROJECT')")
@RequiredArgsConstructor
@Slf4j
public class ProjectCampaignController {

    private final CampaignRepository campaignRepository;
    private final ApplicationRepository applicationRepository;
    private final DeliverableRepository deliverableRepository;
    private final ObjectMapper objectMapper;

    /**
     * GET /api/project/campaigns
     * List own campaigns
     */
    @GetMapping
    public ApiResponse<List<CampaignResponse>> listCampaigns(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) CampaignStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Project {} listing campaigns", userDetails.getUserId());

        Pageable pageable = PageRequest.of(page, size);
        Page<Campaign> campaigns;

        if (status != null) {
            campaigns = campaignRepository.findByProjectIdAndStatus(
                    userDetails.getUserId(), status, pageable);
        } else {
            campaigns = campaignRepository.findByProjectId(
                    userDetails.getUserId(), pageable);
        }

        List<CampaignResponse> responses = campaigns.getContent().stream()
                .map(this::toCampaignResponse)
                .collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    /**
     * POST /api/project/campaigns
     * Create new campaign
     */
    @PostMapping
    public ApiResponse<CampaignResponse> createCampaign(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CampaignCreateRequest request) {
        
        log.info("Project {} creating campaign", userDetails.getUserId());

        // TODO: Verify fee estimate is valid
        // TODO: Lock funds in escrow

        try {
            Campaign campaign = Campaign.builder()
                    .projectId(userDetails.getUserId())
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .category(request.getCategory())
                    .kpiTargets(objectMapper.writeValueAsString(request.getKpiTargets()))
                    .budgetAmount(request.getBudgetAmount())
                    .budgetToken(request.getBudgetToken())
                    .deadline(request.getDeadline())
                    .requiredReputation(request.getRequiredReputation())
                    .numberOfCreators(request.getNumberOfCreators())
                    .complexity(request.getComplexity())
                    .feeEstimateId(request.getFeeEstimateId())
                    .aw3TokenPaymentEnabled(request.getAw3TokenPaymentEnabled())
                    .campaignMetadata(request.getCampaignMetadata() != null ? 
                            objectMapper.writeValueAsString(request.getCampaignMetadata()) : null)
                    .status(CampaignStatus.DRAFT)
                    .build();

            campaign = campaignRepository.save(campaign);

            // TODO: Deploy smart contract (async)

            return ApiResponse.success(toCampaignResponse(campaign));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing campaign data", e);
        }
    }

    /**
     * GET /api/project/campaigns/{id}
     * View campaign details
     */
    @GetMapping("/{id}")
    public ApiResponse<CampaignResponse> getCampaign(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String id) {
        
        log.info("Project {} viewing campaign {}", userDetails.getUserId(), id);

        Campaign campaign = campaignRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        // Verify ownership
        if (!campaign.getProjectId().equals(userDetails.getUserId())) {
            throw new RuntimeException("Not authorized to view this campaign");
        }

        return ApiResponse.success(toCampaignResponse(campaign));
    }

    /**
     * PUT /api/project/campaigns/{id}
     * Update campaign
     */
    @PutMapping("/{id}")
    public ApiResponse<CampaignResponse> updateCampaign(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String id,
            @Valid @RequestBody CampaignCreateRequest request) {
        
        log.info("Project {} updating campaign {}", userDetails.getUserId(), id);

        Campaign campaign = campaignRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        // Verify ownership
        if (!campaign.getProjectId().equals(userDetails.getUserId())) {
            throw new RuntimeException("Not authorized to update this campaign");
        }

        // Only allow updates for DRAFT campaigns
        if (campaign.getStatus() != CampaignStatus.DRAFT) {
            throw new RuntimeException("Can only update draft campaigns");
        }

        // Update campaign
        try {
            campaign.setTitle(request.getTitle());
            campaign.setDescription(request.getDescription());
            campaign.setCategory(request.getCategory());
            campaign.setKpiTargets(objectMapper.writeValueAsString(request.getKpiTargets()));
            campaign.setBudgetAmount(request.getBudgetAmount());
            campaign.setDeadline(request.getDeadline());
            campaign.setRequiredReputation(request.getRequiredReputation());
            campaign.setNumberOfCreators(request.getNumberOfCreators());
            campaign.setComplexity(request.getComplexity());

            campaign = campaignRepository.save(campaign);

            return ApiResponse.success(toCampaignResponse(campaign));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing campaign data", e);
        }
    }

    /**
     * DELETE /api/project/campaigns/{id}
     * Cancel campaign
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> cancelCampaign(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String id) {
        
        log.info("Project {} cancelling campaign {}", userDetails.getUserId(), id);

        Campaign campaign = campaignRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        // Verify ownership
        if (!campaign.getProjectId().equals(userDetails.getUserId())) {
            throw new RuntimeException("Not authorized to cancel this campaign");
        }

        campaign.setStatus(CampaignStatus.CANCELLED);
        campaignRepository.save(campaign);

        // TODO: Refund escrow

        return ApiResponse.success(null);
    }

    @SuppressWarnings("unchecked")
    private CampaignResponse toCampaignResponse(Campaign campaign) {
        // Get statistics
        long totalApplications = applicationRepository.countByCampaignId(campaign.getCampaignId());
        long acceptedApplications = applicationRepository.findByCampaignIdAndStatus(
                campaign.getCampaignId(), 
                com.aw3.platform.entity.enums.ApplicationStatus.ACCEPTED,
                PageRequest.of(0, 1)
        ).getTotalElements();
        long pendingApplications = applicationRepository.findByCampaignIdAndStatus(
                campaign.getCampaignId(),
                com.aw3.platform.entity.enums.ApplicationStatus.PENDING,
                PageRequest.of(0, 1)
        ).getTotalElements();
        long totalDeliverables = deliverableRepository.countByCampaignId(campaign.getCampaignId());

        try {
            return CampaignResponse.builder()
                    .campaignId(campaign.getCampaignId())
                    .projectId(campaign.getProjectId())
                    .title(campaign.getTitle())
                    .description(campaign.getDescription())
                    .category(campaign.getCategory())
                    .kpiTargets(campaign.getKpiTargets() != null ? 
                            objectMapper.readValue(campaign.getKpiTargets(), java.util.Map.class) : null)
                    .budgetAmount(campaign.getBudgetAmount())
                    .budgetToken(campaign.getBudgetToken())
                    .status(campaign.getStatus())
                    .deadline(campaign.getDeadline())
                    .contractAddress(campaign.getContractAddress())
                    .chainId(campaign.getChainId())
                    .escrowBalance(campaign.getEscrowBalance())
                    .serviceFee(campaign.getServiceFee())
                    .oracleFee(campaign.getOracleFee())
                    .totalFee(campaign.getTotalFee())
                    .feeEstimateId(campaign.getFeeEstimateId())
                    .aw3TokenPaymentEnabled(campaign.getAw3TokenPaymentEnabled())
                    .requiredReputation(campaign.getRequiredReputation())
                    .numberOfCreators(campaign.getNumberOfCreators())
                    .complexity(campaign.getComplexity())
                    .campaignMetadata(campaign.getCampaignMetadata() != null ?
                            objectMapper.readValue(campaign.getCampaignMetadata(), java.util.Map.class) : null)
                    .createdAt(campaign.getCreatedAt())
                    .updatedAt(campaign.getUpdatedAt())
                    .startDate(campaign.getStartDate())
                    .completionDate(campaign.getCompletionDate())
                    .statistics(CampaignResponse.CampaignStatistics.builder()
                            .totalApplications(totalApplications)
                            .acceptedApplications(acceptedApplications)
                            .pendingApplications(pendingApplications)
                            .totalDeliverables(totalDeliverables)
                            .completedDeliverables(0L) // TODO: Count completed
                            .build())
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing campaign data", e);
        }
    }
}

