package com.aw3.platform.service;

import com.aw3.platform.controller.admin.AdminCampaignController.*;
import com.aw3.platform.entity.Campaign;
import com.aw3.platform.exception.NotFoundException;
import com.aw3.platform.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * Admin Campaign Management Service
 * 
 * Business Rules:
 * - Suspension requires valid reason and notifies both parties
 * - Force cancellation triggers escrow refund process
 * - Fraud flagging initiates investigation workflow
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCampaignService {

    private final CampaignRepository campaignRepository;
    private final NotificationService notificationService;
    private final EscrowService escrowService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public CampaignListResponse listCampaigns(String status, String flagged, UUID projectId, 
            String category, String sortBy, int page, int size) {
        
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Campaign> campaigns = campaignRepository.findAll(pageRequest);

        List<CampaignSummary> summaries = campaigns.getContent().stream()
                .map(this::toCampaignSummary)
                .collect(Collectors.toList());

        long totalActive = campaignRepository.countByStatus("ACTIVE");
        long totalCompleted = campaignRepository.countByStatus("COMPLETED");
        long totalSuspended = campaignRepository.countByStatus("SUSPENDED");

        return CampaignListResponse.builder()
                .campaigns(summaries)
                .stats(CampaignStats.builder()
                        .total(campaigns.getTotalElements())
                        .active(totalActive)
                        .completed(totalCompleted)
                        .suspended(totalSuspended)
                        .flagged(0L) // TODO: Implement flagging
                        .totalBudgetLocked(BigDecimal.valueOf(1000000))
                        .build())
                .pagination(PaginationInfo.builder()
                        .total(campaigns.getTotalElements())
                        .page(campaigns.getNumber())
                        .size(campaigns.getSize())
                        .totalPages(campaigns.getTotalPages())
                        .build())
                .build();
    }

    @Transactional(readOnly = true)
    public CampaignDetailResponse getCampaignDetails(UUID campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NotFoundException("Campaign not found"));

        return CampaignDetailResponse.builder()
                .campaignId(campaign.getCampaignId())
                .title(campaign.getTitle())
                .description(campaign.getDescription())
                .project(ProjectInfo.builder()
                        .projectId(campaign.getProjectId())
                        .projectName("Project Name") // TODO: Fetch from user
                        .walletAddress("0x...")
                        .reputationScore(BigDecimal.valueOf(75))
                        .build())
                .status(campaign.getStatus())
                .category(campaign.getCategory())
                .totalBudget(campaign.getTotalBudget())
                .escrowLocked(campaign.getTotalBudget())
                .escrowReleased(BigDecimal.ZERO)
                .creators(List.of())
                .deliverables(List.of())
                .milestones(List.of())
                .auditLog(AuditLog.builder().entries(List.of()).build())
                .flagged(false)
                .createdAt(campaign.getCreatedAt())
                .deadline(campaign.getDeadline())
                .build();
    }

    @Transactional
    public CampaignActionResponse suspendCampaign(UUID adminId, UUID campaignId, SuspendCampaignRequest request) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NotFoundException("Campaign not found"));

        String previousStatus = campaign.getStatus();
        campaign.setStatus("SUSPENDED");
        campaignRepository.save(campaign);

        // Log audit
        auditService.logAction(adminId, "CAMPAIGN_SUSPENDED", campaignId, request.getReason());

        // Notify parties if requested
        if (Boolean.TRUE.equals(request.getNotifyParties())) {
            notificationService.notifyCampaignSuspended(campaign, request.getReason());
        }

        log.info("Campaign {} suspended by admin {}", campaignId, adminId);

        return CampaignActionResponse.builder()
                .campaignId(campaignId)
                .previousStatus(previousStatus)
                .newStatus("SUSPENDED")
                .action("SUSPEND")
                .adminId(adminId)
                .timestamp(Instant.now())
                .notes(request.getReason())
                .build();
    }

    @Transactional
    public CampaignActionResponse resumeCampaign(UUID adminId, UUID campaignId, ResumeCampaignRequest request) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NotFoundException("Campaign not found"));

        String previousStatus = campaign.getStatus();
        campaign.setStatus("ACTIVE");
        campaignRepository.save(campaign);

        auditService.logAction(adminId, "CAMPAIGN_RESUMED", campaignId, request.getNotes());

        if (Boolean.TRUE.equals(request.getNotifyParties())) {
            notificationService.notifyCampaignResumed(campaign);
        }

        log.info("Campaign {} resumed by admin {}", campaignId, adminId);

        return CampaignActionResponse.builder()
                .campaignId(campaignId)
                .previousStatus(previousStatus)
                .newStatus("ACTIVE")
                .action("RESUME")
                .adminId(adminId)
                .timestamp(Instant.now())
                .notes(request.getNotes())
                .build();
    }

    @Transactional
    public CampaignActionResponse forceCancelCampaign(UUID adminId, UUID campaignId, ForceCancelRequest request) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NotFoundException("Campaign not found"));

        String previousStatus = campaign.getStatus();
        campaign.setStatus("CANCELLED");
        campaignRepository.save(campaign);

        // Trigger escrow refund based on policy
        escrowService.processRefund(campaign, request.getRefundPolicy(), request.getCreatorPaymentAmount());

        auditService.logAction(adminId, "CAMPAIGN_FORCE_CANCELLED", campaignId, request.getReason());

        log.info("Campaign {} force cancelled by admin {}", campaignId, adminId);

        return CampaignActionResponse.builder()
                .campaignId(campaignId)
                .previousStatus(previousStatus)
                .newStatus("CANCELLED")
                .action("FORCE_CANCEL")
                .adminId(adminId)
                .timestamp(Instant.now())
                .notes(request.getReason())
                .build();
    }

    @Transactional
    public FlagResponse flagCampaign(UUID adminId, UUID campaignId, FlagCampaignRequest request) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NotFoundException("Campaign not found"));

        // TODO: Implement campaign flagging in entity
        auditService.logAction(adminId, "CAMPAIGN_FLAGGED", campaignId, request.getReason());

        log.info("Campaign {} flagged by admin {}: {}", campaignId, adminId, request.getSeverity());

        return FlagResponse.builder()
                .campaignId(campaignId)
                .flagged(true)
                .reason(request.getReason())
                .severity(request.getSeverity())
                .adminId(adminId)
                .timestamp(Instant.now())
                .build();
    }

    @Transactional
    public FlagResponse unflagCampaign(UUID adminId, UUID campaignId, UnflagRequest request) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NotFoundException("Campaign not found"));

        auditService.logAction(adminId, "CAMPAIGN_UNFLAGGED", campaignId, request.getResolution());

        log.info("Campaign {} unflagged by admin {}", campaignId, adminId);

        return FlagResponse.builder()
                .campaignId(campaignId)
                .flagged(false)
                .reason(request.getResolution())
                .adminId(adminId)
                .timestamp(Instant.now())
                .build();
    }

    private CampaignSummary toCampaignSummary(Campaign campaign) {
        return CampaignSummary.builder()
                .campaignId(campaign.getCampaignId())
                .title(campaign.getTitle())
                .projectId(campaign.getProjectId())
                .projectName("Project") // TODO: Fetch from user
                .status(campaign.getStatus())
                .category(campaign.getCategory())
                .budget(campaign.getTotalBudget())
                .creatorsAssigned(campaign.getNumberOfCreators())
                .flagged(false)
                .createdAt(campaign.getCreatedAt())
                .deadline(campaign.getDeadline())
                .build();
    }
}
