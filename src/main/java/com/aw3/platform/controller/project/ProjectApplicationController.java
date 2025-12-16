package com.aw3.platform.controller.project;

import com.aw3.platform.dto.common.ApiResponse;
import com.aw3.platform.dto.creator.ApplicationResponse;
import com.aw3.platform.entity.Application;
import com.aw3.platform.entity.Campaign;
import com.aw3.platform.entity.enums.ApplicationStatus;
import com.aw3.platform.exception.BadRequestException;
import com.aw3.platform.exception.ForbiddenException;
import com.aw3.platform.exception.NotFoundException;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Project Portal - Application Review Endpoints
 * 
 * Business Rules:
 * - Projects can only view applications for their own campaigns
 * - Only pending applications can be approved/rejected
 * - Approved applications automatically allocate campaign spots
 */
@RestController
@RequestMapping("/project")
@PreAuthorize("hasRole('PROJECT')")
@RequiredArgsConstructor
@Slf4j
public class ProjectApplicationController {

    private final ApplicationRepository applicationRepository;
    private final CampaignRepository campaignRepository;
    private final ObjectMapper objectMapper;

    /**
     * GET /api/project/campaigns/{id}/applications
     * View applications for own campaign
     */
    @GetMapping("/campaigns/{id}/applications")
    public ApiResponse<ApplicationListResponse> getCampaignApplications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Project {} fetching applications for campaign {}", userDetails.getUserId(), id);

        // Verify campaign ownership
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Campaign not found"));

        if (!campaign.getProjectId().equals(userDetails.getUserId())) {
            throw new ForbiddenException("Not authorized to view applications for this campaign");
        }

        Page<Application> applications;
        PageRequest pageRequest = PageRequest.of(page, size);

        if (status != null) {
            applications = applicationRepository.findByCampaignIdAndStatus(
                    id, ApplicationStatus.valueOf(status.toUpperCase()), pageRequest);
        } else {
            applications = applicationRepository.findByCampaignId(id, pageRequest);
        }

        List<ApplicationResponse> responses = applications.getContent().stream()
                .map(this::toApplicationResponse)
                .collect(Collectors.toList());

        return ApiResponse.success(ApplicationListResponse.builder()
                .applications(responses)
                .pagination(PaginationInfo.builder()
                        .total(applications.getTotalElements())
                        .page(applications.getNumber())
                        .size(applications.getSize())
                        .totalPages(applications.getTotalPages())
                        .build())
                .build());
    }

    /**
     * PUT /api/project/applications/{id}/approve
     * Approve an application
     */
    @PutMapping("/applications/{id}/approve")
    public ApiResponse<ApplicationResponse> approveApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody ApprovalRequest request) {
        
        log.info("Project {} approving application {}", userDetails.getUserId(), id);

        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        // Verify campaign ownership
        Campaign campaign = campaignRepository.findById(application.getCampaignId())
                .orElseThrow(() -> new NotFoundException("Campaign not found"));

        if (!campaign.getProjectId().equals(userDetails.getUserId())) {
            throw new ForbiddenException("Not authorized to approve applications for this campaign");
        }

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BadRequestException("Can only approve pending applications");
        }

        // Check if campaign has available spots
        long approvedCount = applicationRepository.countByCampaignIdAndStatus(
                application.getCampaignId(), ApplicationStatus.ACCEPTED);
        if (approvedCount >= campaign.getNumberOfCreators()) {
            throw new BadRequestException("Campaign has reached maximum number of creators");
        }

        // Approve application
        application.setStatus(ApplicationStatus.ACCEPTED);
        application.setReviewedAt(Instant.now());
        application.setReviewNotes(request.getNotes());
        application = applicationRepository.save(application);

        log.info("Application {} approved for campaign {}", id, application.getCampaignId());

        return ApiResponse.success(toApplicationResponse(application));
    }

    /**
     * PUT /api/project/applications/{id}/reject
     * Reject an application
     */
    @PutMapping("/applications/{id}/reject")
    public ApiResponse<ApplicationResponse> rejectApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody RejectionRequest request) {
        
        log.info("Project {} rejecting application {}", userDetails.getUserId(), id);

        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        // Verify campaign ownership
        Campaign campaign = campaignRepository.findById(application.getCampaignId())
                .orElseThrow(() -> new NotFoundException("Campaign not found"));

        if (!campaign.getProjectId().equals(userDetails.getUserId())) {
            throw new ForbiddenException("Not authorized to reject applications for this campaign");
        }

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BadRequestException("Can only reject pending applications");
        }

        // Reject application
        application.setStatus(ApplicationStatus.REJECTED);
        application.setReviewedAt(Instant.now());
        application.setRejectionReason(request.getReason());
        application = applicationRepository.save(application);

        log.info("Application {} rejected for campaign {}", id, application.getCampaignId());

        return ApiResponse.success(toApplicationResponse(application));
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

    // Request/Response DTOs

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ApplicationListResponse {
        private List<ApplicationResponse> applications;
        private PaginationInfo pagination;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PaginationInfo {
        private Long total;
        private Integer page;
        private Integer size;
        private Integer totalPages;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ApprovalRequest {
        private String notes;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RejectionRequest {
        private String reason;
    }
}

