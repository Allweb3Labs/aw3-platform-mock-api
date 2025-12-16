package com.aw3.platform.controller.admin;

import com.aw3.platform.dto.common.ApiResponse;
import com.aw3.platform.security.CustomUserDetails;
import com.aw3.platform.service.AdminDisputeService;
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
 * Admin Portal - Dispute Resolution Endpoints
 * 
 * Business Rules:
 * - Disputes must be resolved within 14 days
 * - Resolution affects both party reputation scores
 * - Payment splits executed via smart contract
 * - Both parties notified at each stage
 */
@RestController
@RequestMapping("/admin/disputes")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminDisputeController {

    private final AdminDisputeService disputeService;

    /**
     * GET /api/admin/disputes
     * List all disputes with filters
     */
    @GetMapping
    public ApiResponse<DisputeListResponse> listDisputes(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) UUID assignedTo,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Admin {} listing disputes", userDetails.getUserId());
        
        DisputeListResponse disputes = disputeService.listDisputes(
                status, priority, assignedTo, category, page, size);
        
        return ApiResponse.success(disputes);
    }

    /**
     * GET /api/admin/disputes/{id}
     * Get detailed dispute information
     */
    @GetMapping("/{id}")
    public ApiResponse<DisputeDetailResponse> getDisputeDetails(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {
        
        log.info("Admin {} fetching dispute {}", userDetails.getUserId(), id);
        
        DisputeDetailResponse dispute = disputeService.getDisputeDetails(id);
        
        return ApiResponse.success(dispute);
    }

    /**
     * PUT /api/admin/disputes/{id}/assign
     * Assign dispute to admin for handling
     */
    @PutMapping("/{id}/assign")
    public ApiResponse<DisputeAssignmentResponse> assignDispute(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody AssignDisputeRequest request) {
        
        log.info("Admin {} assigning dispute {} to {}", 
                userDetails.getUserId(), id, request.getAssigneeId());
        
        DisputeAssignmentResponse response = disputeService.assignDispute(
                userDetails.getUserId(), id, request);
        
        return ApiResponse.success(response);
    }

    /**
     * POST /api/admin/disputes/{id}/escalate
     * Escalate to validator arbitration
     */
    @PostMapping("/{id}/escalate")
    public ApiResponse<EscalationResponse> escalateDispute(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody EscalateRequest request) {
        
        log.info("Admin {} escalating dispute {}", userDetails.getUserId(), id);
        
        EscalationResponse response = disputeService.escalateDispute(
                userDetails.getUserId(), id, request);
        
        return ApiResponse.success(response);
    }

    /**
     * PUT /api/admin/disputes/{id}/resolve
     * Resolve dispute with decision
     */
    @PutMapping("/{id}/resolve")
    public ApiResponse<ResolutionResponse> resolveDispute(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody ResolveDisputeRequest request) {
        
        log.info("Admin {} resolving dispute {}", userDetails.getUserId(), id);
        
        ResolutionResponse response = disputeService.resolveDispute(
                userDetails.getUserId(), id, request);
        
        return ApiResponse.success(response);
    }

    /**
     * POST /api/admin/disputes/{id}/message
     * Send message to dispute parties
     */
    @PostMapping("/{id}/message")
    public ApiResponse<MessageResponse> sendMessage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody SendMessageRequest request) {
        
        log.info("Admin {} sending message in dispute {}", userDetails.getUserId(), id);
        
        MessageResponse response = disputeService.sendMessage(
                userDetails.getUserId(), id, request);
        
        return ApiResponse.success(response);
    }

    /**
     * GET /api/admin/disputes/analytics
     * Get dispute analytics and trends
     */
    @GetMapping("/analytics")
    public ApiResponse<DisputeAnalyticsResponse> getDisputeAnalytics(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String period) {
        
        log.info("Admin {} fetching dispute analytics", userDetails.getUserId());
        
        DisputeAnalyticsResponse analytics = disputeService.getDisputeAnalytics(period);
        
        return ApiResponse.success(analytics);
    }

    // DTOs

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DisputeListResponse {
        private List<DisputeSummary> disputes;
        private DisputeStats stats;
        private PaginationInfo pagination;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DisputeSummary {
        private UUID disputeId;
        private UUID campaignId;
        private String campaignTitle;
        private UUID initiatorId;
        private String initiatorName;
        private String initiatorRole;
        private UUID respondentId;
        private String respondentName;
        private String status;
        private String priority;
        private String category;
        private BigDecimal amountInDispute;
        private UUID assignedTo;
        private String assignedToName;
        private Instant createdAt;
        private Instant dueDate;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DisputeStats {
        private Long total;
        private Long pending;
        private Long inProgress;
        private Long escalated;
        private Long resolved;
        private BigDecimal totalAmountInDispute;
        private Long overdue;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DisputeDetailResponse {
        private UUID disputeId;
        private CampaignInfo campaign;
        private PartyInfo initiator;
        private PartyInfo respondent;
        private String status;
        private String priority;
        private String category;
        private String description;
        private BigDecimal amountInDispute;
        private List<EvidenceItem> evidence;
        private List<MessageItem> messages;
        private List<TimelineEvent> timeline;
        private AssignmentInfo assignment;
        private ResolutionInfo resolution;
        private Instant createdAt;
        private Instant dueDate;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CampaignInfo {
        private UUID campaignId;
        private String title;
        private String status;
        private BigDecimal budget;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PartyInfo {
        private UUID userId;
        private String name;
        private String role;
        private String walletAddress;
        private BigDecimal reputationScore;
        private Integer previousDisputes;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EvidenceItem {
        private UUID evidenceId;
        private String type; // DOCUMENT, IMAGE, LINK, TEXT
        private String description;
        private String url;
        private UUID submittedBy;
        private Instant submittedAt;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MessageItem {
        private UUID messageId;
        private UUID senderId;
        private String senderName;
        private String senderRole;
        private String content;
        private Boolean isInternal;
        private Instant sentAt;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TimelineEvent {
        private Instant timestamp;
        private String event;
        private String description;
        private UUID actorId;
        private String actorRole;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AssignmentInfo {
        private UUID assignedTo;
        private String assignedToName;
        private Instant assignedAt;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResolutionInfo {
        private String decision;
        private String reasoning;
        private PaymentSplit paymentSplit;
        private ReputationAdjustment reputationAdjustment;
        private UUID resolvedBy;
        private Instant resolvedAt;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PaymentSplit {
        private BigDecimal toCreator;
        private BigDecimal toProject;
        private BigDecimal toPlatform;
        private String txHash;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReputationAdjustment {
        private BigDecimal initiatorChange;
        private BigDecimal respondentChange;
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
    public static class AssignDisputeRequest {
        private UUID assigneeId;
        private String notes;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EscalateRequest {
        private String reason;
        private Integer arbitratorsRequired;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResolveDisputeRequest {
        private String decision; // FAVOR_INITIATOR, FAVOR_RESPONDENT, SPLIT, DISMISS
        private String reasoning;
        private BigDecimal creatorPayment;
        private BigDecimal projectRefund;
        private BigDecimal initiatorReputationChange;
        private BigDecimal respondentReputationChange;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SendMessageRequest {
        private String content;
        private Boolean isInternal;
        private List<UUID> recipients; // null = all parties
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DisputeAssignmentResponse {
        private UUID disputeId;
        private UUID assignedTo;
        private String assignedToName;
        private UUID assignedBy;
        private Instant assignedAt;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EscalationResponse {
        private UUID disputeId;
        private String previousStatus;
        private String newStatus;
        private Integer arbitratorsAssigned;
        private List<UUID> arbitratorIds;
        private Instant escalatedAt;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResolutionResponse {
        private UUID disputeId;
        private String decision;
        private String reasoning;
        private PaymentSplit paymentSplit;
        private ReputationAdjustment reputationAdjustment;
        private UUID resolvedBy;
        private Instant resolvedAt;
        private String txHash;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MessageResponse {
        private UUID messageId;
        private UUID disputeId;
        private UUID senderId;
        private Instant sentAt;
        private Boolean delivered;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DisputeAnalyticsResponse {
        private OverviewStats overview;
        private List<TrendItem> resolutionTrend;
        private List<CategoryBreakdown> byCategory;
        private PerformanceMetrics performance;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class OverviewStats {
        private Long totalDisputes;
        private Long resolvedDisputes;
        private BigDecimal resolutionRate;
        private BigDecimal averageResolutionTime;
        private BigDecimal totalValueDisputed;
        private BigDecimal totalValueResolved;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TrendItem {
        private String month;
        private Long disputes;
        private Long resolved;
        private BigDecimal avgResolutionDays;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CategoryBreakdown {
        private String category;
        private Long count;
        private BigDecimal percentage;
        private BigDecimal avgResolutionTime;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PerformanceMetrics {
        private BigDecimal initiatorFavorRate;
        private BigDecimal respondentFavorRate;
        private BigDecimal splitRate;
        private BigDecimal dismissRate;
        private BigDecimal escalationRate;
    }
}
