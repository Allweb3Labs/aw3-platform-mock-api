package com.aw3.platform.controller.creator;

import com.aw3.platform.dto.common.ApiResponse;
import com.aw3.platform.dto.creator.ApplicationRequest;
import com.aw3.platform.dto.creator.ApplicationResponse;
import com.aw3.platform.entity.Application;
import com.aw3.platform.entity.Campaign;
import com.aw3.platform.entity.enums.ApplicationStatus;
import com.aw3.platform.entity.enums.CampaignStatus;
import com.aw3.platform.repository.ApplicationRepository;
import com.aw3.platform.repository.CampaignRepository;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Creator Portal - Application Management Endpoints
 * 
 * Business Rules:
 * - Creators can only view/edit their own applications
 * - Can only apply to ACTIVE campaigns
 * - Cannot apply twice to the same campaign
 */
@RestController
@RequestMapping("/creator/applications")
@PreAuthorize("hasRole('CREATOR')")
@RequiredArgsConstructor
@Slf4j
public class CreatorApplicationController {

    private final ApplicationRepository applicationRepository;
    private final CampaignRepository campaignRepository;
    private final ObjectMapper objectMapper;

    /**
     * GET /api/creator/applications
     * List own applications
     */
    @GetMapping
    public ApiResponse<List<ApplicationResponse>> listApplications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Creator {} listing applications", userDetails.getUserId());

        Pageable pageable = PageRequest.of(page, size);
        Page<Application> applications = applicationRepository.findByCreatorId(
                userDetails.getUserId(), 
                pageable
        );

        List<ApplicationResponse> responses = applications.getContent().stream()
                .map(this::toApplicationResponse)
                .collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    /**
     * POST /api/creator/applications
     * Submit new application
     */
    @PostMapping
    public ApiResponse<ApplicationResponse> submitApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ApplicationRequest request) {
        
        log.info("Creator {} submitting application for campaign {}", 
                userDetails.getUserId(), request.getCampaignId());

        // 1. Verify campaign exists and is active
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        if (campaign.getStatus() != CampaignStatus.ACTIVE) {
            throw new RuntimeException("Campaign is not active");
        }

        // 2. Check if creator already applied
        if (applicationRepository.existsByCampaignIdAndCreatorId(
                request.getCampaignId(), userDetails.getUserId())) {
            throw new RuntimeException("Already applied to this campaign");
        }

        // 3. Create application
        try {
            Application application = Application.builder()
                    .campaignId(request.getCampaignId())
                    .creatorId(userDetails.getUserId())
                    .proposedRate(request.getProposedRate())
                    .proposal(request.getProposal())
                    .portfolioLinks(objectMapper.writeValueAsString(request.getPortfolioLinks()))
                    .relevantExperience(request.getRelevantExperience())
                    .estimatedCompletionDays(request.getEstimatedCompletionDays())
                    .status(ApplicationStatus.PENDING)
                    .build();

            application = applicationRepository.save(application);

            // 4. TODO: Notify project owner (async)

            return ApiResponse.success(toApplicationResponse(application));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing application data", e);
        }
    }

    /**
     * GET /api/creator/applications/{id}
     * View application details
     */
    @GetMapping("/{id}")
    public ApiResponse<ApplicationResponse> getApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String id) {
        
        log.info("Creator {} viewing application {}", userDetails.getUserId(), id);

        Application application = applicationRepository.findByApplicationIdAndCreatorId(
                UUID.fromString(id), userDetails.getUserId())
                .orElseThrow(() -> new RuntimeException("Application not found or not authorized"));

        return ApiResponse.success(toApplicationResponse(application));
    }

    /**
     * PUT /api/creator/applications/{id}
     * Edit pending application
     */
    @PutMapping("/{id}")
    public ApiResponse<ApplicationResponse> updateApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String id,
            @Valid @RequestBody ApplicationRequest request) {
        
        log.info("Creator {} updating application {}", userDetails.getUserId(), id);

        Application application = applicationRepository.findByApplicationIdAndCreatorId(
                UUID.fromString(id), userDetails.getUserId())
                .orElseThrow(() -> new RuntimeException("Application not found or not authorized"));

        // Only allow editing pending applications
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new RuntimeException("Can only edit pending applications");
        }

        // Update application
        try {
            application.setProposedRate(request.getProposedRate());
            application.setProposal(request.getProposal());
            application.setPortfolioLinks(objectMapper.writeValueAsString(request.getPortfolioLinks()));
            application.setRelevantExperience(request.getRelevantExperience());
            application.setEstimatedCompletionDays(request.getEstimatedCompletionDays());

            application = applicationRepository.save(application);

            return ApiResponse.success(toApplicationResponse(application));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing application data", e);
        }
    }

    private ApplicationResponse toApplicationResponse(Application application) {
        Campaign campaign = campaignRepository.findById(application.getCampaignId())
                .orElse(null);

        try {
            return ApplicationResponse.builder()
                    .applicationId(application.getApplicationId())
                    .campaignId(application.getCampaignId())
                    .campaignTitle(campaign != null ? campaign.getTitle() : "Unknown Campaign")
                    .creatorId(application.getCreatorId())
                    .proposedRate(application.getProposedRate())
                    .proposal(application.getProposal())
                    .status(application.getStatus())
                    .portfolioLinks(application.getPortfolioLinks() != null ? 
                            objectMapper.readValue(application.getPortfolioLinks(), List.class) : null)
                    .relevantExperience(application.getRelevantExperience())
                    .estimatedCompletionDays(application.getEstimatedCompletionDays())
                    .matchScore(application.getMatchScore())
                    .appliedAt(application.getAppliedAt())
                    .reviewedAt(application.getReviewedAt())
                    .rejectionReason(application.getRejectionReason())
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing application data", e);
        }
    }
}

