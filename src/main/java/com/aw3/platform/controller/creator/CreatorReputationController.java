package com.aw3.platform.controller.creator;

import com.aw3.platform.dto.common.ApiResponse;
import com.aw3.platform.dto.creator.EarningsSummaryResponse;
import com.aw3.platform.dto.creator.ReputationResponse;
import com.aw3.platform.security.CustomUserDetails;
import com.aw3.platform.service.ReputationService;
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
 * Creator Portal - Reputation and Earnings Endpoints
 * 
 * Reputation Tiers:
 * - S: 900+ (Elite, 40% fee discount)
 * - A: 800-899 (30% fee discount)
 * - B: 700-799 (20% fee discount)
 * - C: 600-699 (10% fee discount)
 * - Newcomer: <600 (No discount)
 */
@RestController
@RequestMapping("/creator/reputation")
@PreAuthorize("hasRole('CREATOR')")
@RequiredArgsConstructor
@Slf4j
public class CreatorReputationController {

    private final ReputationService reputationService;

    /**
     * GET /api/creator/reputation/me
     * View current reputation score and tier
     */
    @GetMapping("/me")
    public ApiResponse<ReputationResponse> getReputation(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Creator {} fetching reputation", userDetails.getUserId());
        
        ReputationResponse response = reputationService.getCreatorReputation(userDetails.getUserId());
        
        return ApiResponse.success(response);
    }

    /**
     * GET /api/creator/reputation/history
     * View reputation score changes over time
     */
    @GetMapping("/history")
    public ApiResponse<ReputationHistoryResponse> getReputationHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "90d") String period,
            @RequestParam(defaultValue = "50") int limit) {
        
        log.info("Creator {} fetching reputation history for period: {}", 
                userDetails.getUserId(), period);
        
        ReputationHistoryResponse response = reputationService.getReputationHistory(
                userDetails.getUserId(), 
                period,
                limit
        );
        
        return ApiResponse.success(response);
    }

    /**
     * GET /api/creator/reputation/spc-nfts
     * View all earned SPC (Success Proof Certificate) NFTs
     */
    @GetMapping("/spc-nfts")
    public ApiResponse<SPCListResponse> getSPCNFTs(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        
        log.info("Creator {} fetching SPC NFTs", userDetails.getUserId());
        
        SPCListResponse response = reputationService.getCreatorSPCNFTs(
                userDetails.getUserId(),
                status,
                limit,
                offset
        );
        
        return ApiResponse.success(response);
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReputationHistoryResponse {
        private BigDecimal currentScore;
        private Integer scoreChange30d;
        private Integer scoreChange90d;
        private List<HistoryItem> history;
        private List<TimelineItem> timeline;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class HistoryItem {
        private Instant date;
        private String action;
        private UUID campaignId;
        private Integer scoreChange;
        private BigDecimal newScore;
        private String reason;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TimelineItem {
        private String month;
        private BigDecimal score;
        private Integer campaignsCompleted;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SPCListResponse {
        private Integer totalSPCs;
        private Integer ownedSPCs;
        private Integer listedSPCs;
        private Integer soldSPCs;
        private BigDecimal totalRoyaltiesEarned;
        private List<SPCItem> spcs;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SPCItem {
        private UUID spcId;
        private String tokenId;
        private UUID campaignId;
        private String campaignTitle;
        private String contractAddress;
        private Integer chainId;
        private Instant mintedAt;
        private SPCMetadata metadata;
        private String status;
        private BigDecimal marketValue;
        private BigDecimal royaltyPercentage;
        private BigDecimal royaltiesEarned;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SPCMetadata {
        private String image;
        private BigDecimal cvpi;
        private BigDecimal performanceScore;
        private String category;
    }
}

