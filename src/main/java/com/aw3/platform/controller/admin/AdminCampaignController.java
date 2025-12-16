package com.aw3.platform.controller.admin;

import com.aw3.platform.dto.common.ApiResponse;
import com.aw3.platform.security.CustomUserDetails;
import com.aw3.platform.service.AdminCampaignService;
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
 * Admin Portal - Campaign Management Endpoints
 * 
 * Business Rules:
 * - Suspension requires valid reason and notifies both parties
 * - Force cancellation triggers escrow refund process
 * - Fraud flagging initiates investigation workflow
 */
@RestController
@RequestMapping("/admin/campaigns")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminCampaignController {

    private final AdminCampaignService campaignService;

    /**
     * GET /api/admin/campaigns
     * List all campaigns with filters
     */
    @GetMapping
    public ApiResponse<CampaignListResponse> listCampaigns(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String flagged,
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Admin {} listing campaigns", userDetails.getUserId());
        
        CampaignListResponse campaigns = campaignService.listCampaigns(
                status, flagged, projectId, category, sortBy, page, size);
        
        return ApiResponse.success(campaigns);
    }

    /**
     * GET /api/admin/campaigns/{id}
     * Get detailed campaign information
     */
    @GetMapping("/{id}")
    public ApiResponse<CampaignDetailResponse> getCampaignDetails(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {
        
        log.info("Admin {} fetching campaign {}", userDetails.getUserId(), id);
        
        CampaignDetailResponse campaign = campaignService.getCampaignDetails(id);
        
        return ApiResponse.success(campaign);
    }

    /**
     * PUT /api/admin/campaigns/{id}/suspend
     * Suspend a campaign
     */
    @PutMapping("/{id}/suspend")
    public ApiResponse<CampaignActionResponse> suspendCampaign(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody SuspendCampaignRequest request) {
        
        log.info("Admin {} suspending campaign {}", userDetails.getUserId(), id);
        
        CampaignActionResponse response = campaignService.suspendCampaign(
                userDetails.getUserId(), id, request);
        
        return ApiResponse.success(response);
    }

    /**
     * PUT /api/admin/campaigns/{id}/resume
     * Resume a suspended campaign
     */
    @PutMapping("/{id}/resume")
    public ApiResponse<CampaignActionResponse> resumeCampaign(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody ResumeCampaignRequest request) {
        
        log.info("Admin {} resuming campaign {}", userDetails.getUserId(), id);
        
        CampaignActionResponse response = campaignService.resumeCampaign(
                userDetails.getUserId(), id, request);
        
        return ApiResponse.success(response);
    }

    /**
     * PUT /api/admin/campaigns/{id}/cancel
     * Force cancel a campaign (triggers escrow refund)
     */
    @PutMapping("/{id}/cancel")
    public ApiResponse<CampaignActionResponse> forceCancelCampaign(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody ForceCancelRequest request) {
        
        log.info("Admin {} force canceling campaign {}", userDetails.getUserId(), id);
        
        CampaignActionResponse response = campaignService.forceCancelCampaign(
                userDetails.getUserId(), id, request);
        
        return ApiResponse.success(response);
    }

    /**
     * POST /api/admin/campaigns/{id}/flag
     * Flag a campaign for investigation
     */
    @PostMapping("/{id}/flag")
    public ApiResponse<FlagResponse> flagCampaign(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody FlagCampaignRequest request) {
        
        log.info("Admin {} flagging campaign {}", userDetails.getUserId(), id);
        
        FlagResponse response = campaignService.flagCampaign(
                userDetails.getUserId(), id, request);
        
        return ApiResponse.success(response);
    }

    /**
     * DELETE /api/admin/campaigns/{id}/flag
     * Remove flag from campaign
     */
    @DeleteMapping("/{id}/flag")
    public ApiResponse<FlagResponse> unflagCampaign(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody UnflagRequest request) {
        
        log.info("Admin {} unflagging campaign {}", userDetails.getUserId(), id);
        
        FlagResponse response = campaignService.unflagCampaign(
                userDetails.getUserId(), id, request);
        
        return ApiResponse.success(response);
    }

    // DTOs

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CampaignListResponse {
        private List<CampaignSummary> campaigns;
        private CampaignStats stats;
        private PaginationInfo pagination;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CampaignSummary {
        private UUID campaignId;
        private String title;
        private UUID projectId;
        private String projectName;
        private String status;
        private String category;
        private BigDecimal budget;
        private Integer creatorsAssigned;
        private Boolean flagged;
        private String flagReason;
        private Instant createdAt;
        private Instant deadline;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CampaignStats {
        private Long total;
        private Long active;
        private Long completed;
        private Long suspended;
        private Long flagged;
        private BigDecimal totalBudgetLocked;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CampaignDetailResponse {
        private UUID campaignId;
        private String title;
        private String description;
        private ProjectInfo project;
        private String status;
        private String category;
        private BigDecimal totalBudget;
        private BigDecimal escrowLocked;
        private BigDecimal escrowReleased;
        private List<CreatorAssignment> creators;
        private List<DeliverableInfo> deliverables;
        private List<MilestoneInfo> milestones;
        private AuditLog auditLog;
        private Boolean flagged;
        private FlagInfo flagInfo;
        private Instant createdAt;
        private Instant deadline;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProjectInfo {
        private UUID projectId;
        private String projectName;
        private String walletAddress;
        private BigDecimal reputationScore;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CreatorAssignment {
        private UUID creatorId;
        private String creatorName;
        private String status;
        private BigDecimal payment;
        private Integer deliverablesCompleted;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DeliverableInfo {
        private UUID deliverableId;
        private String type;
        private String status;
        private UUID creatorId;
        private Instant submittedAt;
        private Instant verifiedAt;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MilestoneInfo {
        private Integer number;
        private String description;
        private BigDecimal amount;
        private String status;
        private Instant completedAt;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AuditLog {
        private List<AuditEntry> entries;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AuditEntry {
        private Instant timestamp;
        private String action;
        private UUID actorId;
        private String actorRole;
        private String details;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FlagInfo {
        private String reason;
        private String severity;
        private UUID flaggedBy;
        private Instant flaggedAt;
        private String investigationStatus;
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
    public static class SuspendCampaignRequest {
        private String reason;
        private Boolean notifyParties;
        private Integer suspensionDays;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResumeCampaignRequest {
        private String notes;
        private Boolean notifyParties;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ForceCancelRequest {
        private String reason;
        private String refundPolicy; // FULL_REFUND, PARTIAL_REFUND, SPLIT
        private BigDecimal creatorPaymentAmount;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FlagCampaignRequest {
        private String reason;
        private String severity; // LOW, MEDIUM, HIGH, CRITICAL
        private String category; // FRAUD, POLICY_VIOLATION, QUALITY, OTHER
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UnflagRequest {
        private String resolution;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CampaignActionResponse {
        private UUID campaignId;
        private String previousStatus;
        private String newStatus;
        private String action;
        private UUID adminId;
        private Instant timestamp;
        private String notes;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FlagResponse {
        private UUID campaignId;
        private Boolean flagged;
        private String reason;
        private String severity;
        private UUID adminId;
        private Instant timestamp;
    }
}
