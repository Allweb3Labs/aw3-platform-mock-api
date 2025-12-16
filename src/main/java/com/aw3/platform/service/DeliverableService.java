package com.aw3.platform.service;

import com.aw3.platform.controller.creator.CreatorDeliverableController;
import com.aw3.platform.dto.creator.DeliverableRequest;
import com.aw3.platform.dto.creator.DeliverableResponse;
import com.aw3.platform.entity.Application;
import com.aw3.platform.entity.Campaign;
import com.aw3.platform.entity.Deliverable;
import com.aw3.platform.entity.enums.ApplicationStatus;
import com.aw3.platform.entity.enums.DeliverableStatus;
import com.aw3.platform.exception.BadRequestException;
import com.aw3.platform.exception.ForbiddenException;
import com.aw3.platform.exception.NotFoundException;
import com.aw3.platform.repository.ApplicationRepository;
import com.aw3.platform.repository.CampaignRepository;
import com.aw3.platform.repository.DeliverableRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Deliverable Service
 * 
 * Business Rules:
 * - Must have approved application for campaign
 * - Content must be publicly accessible
 * - Oracle will verify metrics after submission
 * - Deliverables trigger CVPI calculation and SPC NFT minting upon approval
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DeliverableService {

    private final DeliverableRepository deliverableRepository;
    private final ApplicationRepository applicationRepository;
    private final CampaignRepository campaignRepository;
    private final OracleService oracleService;
    private final ObjectMapper objectMapper;

    /**
     * List creator's deliverables with filtering
     */
    public CreatorDeliverableController.DeliverableListResponse listCreatorDeliverables(
            UUID creatorId, UUID campaignId, String status, int limit, int offset) {
        
        Page<Deliverable> deliverables;
        PageRequest pageRequest = PageRequest.of(offset / limit, limit);

        if (campaignId != null && status != null) {
            deliverables = deliverableRepository.findByCreatorIdAndCampaignIdAndStatus(
                    creatorId, campaignId, DeliverableStatus.valueOf(status.toUpperCase()), pageRequest);
        } else if (campaignId != null) {
            deliverables = deliverableRepository.findByCreatorIdAndCampaignId(creatorId, campaignId, pageRequest);
        } else if (status != null) {
            deliverables = deliverableRepository.findByCreatorIdAndStatus(
                    creatorId, DeliverableStatus.valueOf(status.toUpperCase()), pageRequest);
        } else {
            deliverables = deliverableRepository.findByCreatorId(creatorId, pageRequest);
        }

        List<DeliverableResponse> responses = deliverables.getContent().stream()
                .map(this::toDeliverableResponse)
                .collect(Collectors.toList());

        return CreatorDeliverableController.DeliverableListResponse.builder()
                .deliverables(responses)
                .pagination(CreatorDeliverableController.PaginationInfo.builder()
                        .total(deliverables.getTotalElements())
                        .limit(limit)
                        .offset(offset)
                        .hasMore(deliverables.hasNext())
                        .build())
                .build();
    }

    /**
     * Submit a new deliverable
     */
    @Transactional
    public DeliverableResponse submitDeliverable(UUID creatorId, DeliverableRequest request) {
        // Verify application exists and is approved
        Application application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new NotFoundException("Application not found"));

        if (!application.getCreatorId().equals(creatorId)) {
            throw new ForbiddenException("Not authorized to submit deliverable for this application");
        }

        if (application.getStatus() != ApplicationStatus.ACCEPTED) {
            throw new BadRequestException("Application must be approved to submit deliverables");
        }

        // Verify campaign matches
        if (!application.getCampaignId().equals(request.getCampaignId())) {
            throw new BadRequestException("Campaign ID does not match application");
        }

        // Get campaign details
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new NotFoundException("Campaign not found"));

        try {
            // Create deliverable
            Deliverable deliverable = Deliverable.builder()
                    .creatorId(creatorId)
                    .campaignId(request.getCampaignId())
                    .applicationId(request.getApplicationId())
                    .contentType(request.getType())
                    .contentUrl(request.getContentUrl())
                    .platform(request.getPlatform())
                    .description(request.getDescription())
                    .publishedAt(request.getPublishedAt())
                    .initialMetrics(request.getInitialMetrics() != null 
                            ? objectMapper.writeValueAsString(request.getInitialMetrics()) 
                            : null)
                    .proofData(request.getProof() != null 
                            ? objectMapper.writeValueAsString(request.getProof()) 
                            : null)
                    .status(DeliverableStatus.PENDING)
                    .build();

            deliverable = deliverableRepository.save(deliverable);

            // Queue oracle verification (async)
            queueOracleVerification(deliverable);

            log.info("Deliverable {} submitted for campaign {} by creator {}", 
                    deliverable.getDeliverableId(), request.getCampaignId(), creatorId);

            return toDeliverableResponse(deliverable);

        } catch (JsonProcessingException e) {
            log.error("Error processing deliverable data", e);
            throw new BadRequestException("Error processing deliverable data");
        }
    }

    /**
     * Get detailed deliverable information
     */
    public DeliverableResponse getDeliverableDetails(UUID creatorId, UUID deliverableId) {
        Deliverable deliverable = deliverableRepository.findById(deliverableId)
                .orElseThrow(() -> new NotFoundException("Deliverable not found"));

        if (!deliverable.getCreatorId().equals(creatorId)) {
            throw new ForbiddenException("Not authorized to view this deliverable");
        }

        return toDeliverableResponse(deliverable);
    }

    /**
     * Queue oracle verification for deliverable
     */
    @Async
    public void queueOracleVerification(Deliverable deliverable) {
        try {
            // Simulate oracle verification queueing
            log.info("Queuing oracle verification for deliverable {}", deliverable.getDeliverableId());
            // In production, this would call the oracle service
        } catch (Exception e) {
            log.error("Error queueing oracle verification", e);
        }
    }

    private DeliverableResponse toDeliverableResponse(Deliverable deliverable) {
        Campaign campaign = campaignRepository.findById(deliverable.getCampaignId())
                .orElse(null);

        return DeliverableResponse.builder()
                .deliverableId(deliverable.getDeliverableId())
                .campaignId(deliverable.getCampaignId())
                .campaignTitle(campaign != null ? campaign.getTitle() : "Unknown Campaign")
                .applicationId(deliverable.getApplicationId())
                .type(deliverable.getContentType())
                .status(deliverable.getStatus().name().toLowerCase())
                .submittedAt(deliverable.getSubmittedAt())
                .reviewedAt(deliverable.getReviewedAt())
                .approvedAt(deliverable.getApprovedAt())
                .contentUrl(deliverable.getContentUrl())
                .platform(deliverable.getPlatform())
                .description(deliverable.getDescription())
                .metrics(buildMetricsInfo(deliverable))
                .oracleVerification(buildOracleVerificationInfo(deliverable))
                .payment(buildPaymentInfo(deliverable))
                .spcNFT(buildSPCNFTInfo(deliverable))
                .reviewNotes(deliverable.getReviewNotes())
                .build();
    }

    private DeliverableResponse.MetricsInfo buildMetricsInfo(Deliverable deliverable) {
        // Mock metrics for MVP
        return DeliverableResponse.MetricsInfo.builder()
                .current(DeliverableResponse.CurrentMetrics.builder()
                        .views(52000)
                        .likes(4200)
                        .comments(580)
                        .shares(320)
                        .engagement(new BigDecimal("8.7"))
                        .conversions(420)
                        .lastUpdated(Instant.now())
                        .build())
                .target(DeliverableResponse.TargetMetrics.builder()
                        .engagement(new BigDecimal("7.5"))
                        .reach(50000)
                        .conversions(350)
                        .build())
                .achievement(DeliverableResponse.AchievementMetrics.builder()
                        .engagement(new BigDecimal("116.0"))
                        .reach(new BigDecimal("104.0"))
                        .conversions(new BigDecimal("120.0"))
                        .overall(new BigDecimal("113.3"))
                        .build())
                .build();
    }

    private DeliverableResponse.OracleVerificationInfo buildOracleVerificationInfo(Deliverable deliverable) {
        if (deliverable.getVerifiedAt() == null) {
            return DeliverableResponse.OracleVerificationInfo.builder()
                    .status("queued")
                    .build();
        }

        return DeliverableResponse.OracleVerificationInfo.builder()
                .status("verified")
                .verifiedBy(deliverable.getVerifiedBy())
                .verifiedAt(deliverable.getVerifiedAt())
                .confidenceScore(new BigDecimal("0.95"))
                .build();
    }

    private DeliverableResponse.PaymentInfo buildPaymentInfo(Deliverable deliverable) {
        if (deliverable.getPaymentAmount() == null) {
            return null;
        }

        return DeliverableResponse.PaymentInfo.builder()
                .baseAmount(deliverable.getPaymentAmount())
                .performanceBonus(deliverable.getPerformanceBonus())
                .totalEarned(deliverable.getPaymentAmount().add(
                        deliverable.getPerformanceBonus() != null 
                                ? deliverable.getPerformanceBonus() 
                                : BigDecimal.ZERO))
                .status(deliverable.getPaymentStatus())
                .txHash(deliverable.getPaymentTxHash())
                .paidAt(deliverable.getPaidAt())
                .build();
    }

    private DeliverableResponse.SPCNFTInfo buildSPCNFTInfo(Deliverable deliverable) {
        if (deliverable.getSpcTokenId() == null) {
            return null;
        }

        return DeliverableResponse.SPCNFTInfo.builder()
                .minted(true)
                .tokenId(deliverable.getSpcTokenId())
                .contractAddress(deliverable.getSpcContractAddress())
                .mintedAt(deliverable.getSpcMintedAt())
                .build();
    }
}

