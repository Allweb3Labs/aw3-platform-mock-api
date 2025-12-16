package com.aw3.platform.service;

import com.aw3.platform.controller.admin.AdminDisputeController.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Admin Dispute Resolution Service
 * 
 * Business Rules:
 * - Disputes must be resolved within 14 days
 * - Resolution affects both party reputation scores
 * - Payment splits executed via smart contract
 * - Both parties notified at each stage
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDisputeService {

    private final NotificationService notificationService;
    private final EscrowService escrowService;
    private final ReputationService reputationService;
    private final AuditService auditService;

    public DisputeListResponse listDisputes(String status, String priority, UUID assignedTo,
            String category, int page, int size) {
        // TODO: Implement from database
        return DisputeListResponse.builder()
                .disputes(List.of())
                .stats(DisputeStats.builder()
                        .total(50L)
                        .pending(10L)
                        .inProgress(15L)
                        .escalated(5L)
                        .resolved(20L)
                        .totalAmountInDispute(BigDecimal.valueOf(100000))
                        .overdue(2L)
                        .build())
                .pagination(PaginationInfo.builder()
                        .total(50L)
                        .page(page)
                        .size(size)
                        .totalPages(3)
                        .build())
                .build();
    }

    public DisputeDetailResponse getDisputeDetails(UUID disputeId) {
        // TODO: Implement from database
        return DisputeDetailResponse.builder()
                .disputeId(disputeId)
                .campaign(CampaignInfo.builder()
                        .campaignId(UUID.randomUUID())
                        .title("Sample Campaign")
                        .status("DISPUTED")
                        .budget(BigDecimal.valueOf(5000))
                        .build())
                .initiator(PartyInfo.builder()
                        .userId(UUID.randomUUID())
                        .name("Creator Name")
                        .role("CREATOR")
                        .reputationScore(BigDecimal.valueOf(72))
                        .previousDisputes(1)
                        .build())
                .respondent(PartyInfo.builder()
                        .userId(UUID.randomUUID())
                        .name("Project Name")
                        .role("PROJECT")
                        .reputationScore(BigDecimal.valueOf(85))
                        .previousDisputes(0)
                        .build())
                .status("IN_PROGRESS")
                .priority("HIGH")
                .category("PAYMENT")
                .description("Dispute description")
                .amountInDispute(BigDecimal.valueOf(2500))
                .evidence(List.of())
                .messages(List.of())
                .timeline(List.of())
                .createdAt(Instant.now().minusSeconds(86400))
                .dueDate(Instant.now().plusSeconds(86400 * 13))
                .build();
    }

    public DisputeAssignmentResponse assignDispute(UUID adminId, UUID disputeId, AssignDisputeRequest request) {
        log.info("Admin {} assigning dispute {} to {}", adminId, disputeId, request.getAssigneeId());
        
        auditService.logAction(adminId, "DISPUTE_ASSIGNED", disputeId, 
                "Assigned to: " + request.getAssigneeId());

        return DisputeAssignmentResponse.builder()
                .disputeId(disputeId)
                .assignedTo(request.getAssigneeId())
                .assignedToName("Admin Name")
                .assignedBy(adminId)
                .assignedAt(Instant.now())
                .build();
    }

    public EscalationResponse escalateDispute(UUID adminId, UUID disputeId, EscalateRequest request) {
        log.info("Admin {} escalating dispute {} to arbitration", adminId, disputeId);

        auditService.logAction(adminId, "DISPUTE_ESCALATED", disputeId, request.getReason());

        // TODO: Select random arbitrators from qualified pool
        List<UUID> arbitratorIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        return EscalationResponse.builder()
                .disputeId(disputeId)
                .previousStatus("IN_PROGRESS")
                .newStatus("ESCALATED")
                .arbitratorsAssigned(request.getArbitratorsRequired())
                .arbitratorIds(arbitratorIds)
                .escalatedAt(Instant.now())
                .build();
    }

    public ResolutionResponse resolveDispute(UUID adminId, UUID disputeId, ResolveDisputeRequest request) {
        log.info("Admin {} resolving dispute {} with decision: {}", adminId, disputeId, request.getDecision());

        // Execute payment split via smart contract
        PaymentSplit paymentSplit = PaymentSplit.builder()
                .toCreator(request.getCreatorPayment())
                .toProject(request.getProjectRefund())
                .toPlatform(BigDecimal.ZERO)
                .txHash("0x...") // TODO: Get from escrow service
                .build();

        // Adjust reputation
        ReputationAdjustment reputationAdjustment = ReputationAdjustment.builder()
                .initiatorChange(request.getInitiatorReputationChange())
                .respondentChange(request.getRespondentReputationChange())
                .build();

        // TODO: Apply reputation changes
        // TODO: Execute escrow release

        auditService.logAction(adminId, "DISPUTE_RESOLVED", disputeId, 
                "Decision: " + request.getDecision() + ", Reason: " + request.getReasoning());

        // Notify parties
        // notificationService.notifyDisputeResolved(disputeId, request.getDecision());

        return ResolutionResponse.builder()
                .disputeId(disputeId)
                .decision(request.getDecision())
                .reasoning(request.getReasoning())
                .paymentSplit(paymentSplit)
                .reputationAdjustment(reputationAdjustment)
                .resolvedBy(adminId)
                .resolvedAt(Instant.now())
                .txHash(paymentSplit.getTxHash())
                .build();
    }

    public MessageResponse sendMessage(UUID adminId, UUID disputeId, SendMessageRequest request) {
        log.info("Admin {} sending message in dispute {}", adminId, disputeId);

        return MessageResponse.builder()
                .messageId(UUID.randomUUID())
                .disputeId(disputeId)
                .senderId(adminId)
                .sentAt(Instant.now())
                .delivered(true)
                .build();
    }

    public DisputeAnalyticsResponse getDisputeAnalytics(String period) {
        return DisputeAnalyticsResponse.builder()
                .overview(OverviewStats.builder()
                        .totalDisputes(150L)
                        .resolvedDisputes(120L)
                        .resolutionRate(BigDecimal.valueOf(80))
                        .averageResolutionTime(BigDecimal.valueOf(7.5))
                        .totalValueDisputed(BigDecimal.valueOf(500000))
                        .totalValueResolved(BigDecimal.valueOf(400000))
                        .build())
                .resolutionTrend(List.of())
                .byCategory(List.of())
                .performance(PerformanceMetrics.builder()
                        .initiatorFavorRate(BigDecimal.valueOf(35))
                        .respondentFavorRate(BigDecimal.valueOf(40))
                        .splitRate(BigDecimal.valueOf(20))
                        .dismissRate(BigDecimal.valueOf(5))
                        .escalationRate(BigDecimal.valueOf(15))
                        .build())
                .build();
    }
}
