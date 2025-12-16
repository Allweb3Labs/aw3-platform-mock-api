package com.aw3.platform.controller.creator;

import com.aw3.platform.dto.common.ApiResponse;
import com.aw3.platform.dto.creator.DeliverableRequest;
import com.aw3.platform.dto.creator.DeliverableResponse;
import com.aw3.platform.security.CustomUserDetails;
import com.aw3.platform.service.DeliverableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Creator Portal - Deliverable Submission Endpoints
 * 
 * Business Rules:
 * - Must have approved application for campaign
 * - Content must be publicly accessible
 * - Oracle will verify metrics after submission
 * - Deliverables trigger CVPI calculation and SPC NFT minting
 */
@RestController
@RequestMapping("/creator/deliverables")
@PreAuthorize("hasRole('CREATOR')")
@RequiredArgsConstructor
@Slf4j
public class CreatorDeliverableController {

    private final DeliverableService deliverableService;

    /**
     * GET /api/creator/deliverables
     * List all deliverables submitted by the authenticated creator
     */
    @GetMapping
    public ApiResponse<DeliverableListResponse> listDeliverables(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) UUID campaignId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        
        log.info("Creator {} listing deliverables", userDetails.getUserId());
        
        DeliverableListResponse response = deliverableService.listCreatorDeliverables(
                userDetails.getUserId(),
                campaignId,
                status,
                limit,
                offset
        );
        
        return ApiResponse.success(response);
    }

    /**
     * POST /api/creator/deliverables
     * Submit a deliverable for campaign approval
     */
    @PostMapping
    public ApiResponse<DeliverableResponse> submitDeliverable(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DeliverableRequest request) {
        
        log.info("Creator {} submitting deliverable for campaign {}", 
                userDetails.getUserId(), request.getCampaignId());
        
        DeliverableResponse response = deliverableService.submitDeliverable(
                userDetails.getUserId(), 
                request
        );
        
        return ApiResponse.success(response);
    }

    /**
     * GET /api/creator/deliverables/{id}
     * View detailed status and metrics for a specific deliverable
     */
    @GetMapping("/{id}")
    public ApiResponse<DeliverableResponse> getDeliverable(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {
        
        log.info("Creator {} viewing deliverable {}", userDetails.getUserId(), id);
        
        DeliverableResponse response = deliverableService.getDeliverableDetails(
                userDetails.getUserId(), 
                id
        );
        
        return ApiResponse.success(response);
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DeliverableListResponse {
        private List<DeliverableResponse> deliverables;
        private PaginationInfo pagination;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PaginationInfo {
        private Long total;
        private Integer limit;
        private Integer offset;
        private Boolean hasMore;
    }
}

