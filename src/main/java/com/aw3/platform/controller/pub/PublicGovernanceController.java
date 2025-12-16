package com.aw3.platform.controller.pub;

import com.aw3.platform.dto.common.ApiResponse;
import com.aw3.platform.service.PublicGovernanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Public Governance Controller
 * 
 * Provides public access to governance data for transparency
 * Allows viewing proposals, votes, and governance statistics
 */
@RestController
@RequestMapping("/public/governance")
@RequiredArgsConstructor
@Slf4j
public class PublicGovernanceController {

    private final PublicGovernanceService governanceService;

    /**
     * GET /api/public/governance/proposals
     * Get public governance proposals
     */
    @GetMapping("/proposals")
    public ApiResponse<PublicProposalsResponse> getProposals(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Public governance proposals request: status={}", status);
        
        PublicProposalsResponse proposals = governanceService.getProposals(status, category, page, size);
        
        return ApiResponse.success(proposals);
    }

    /**
     * GET /api/public/governance/proposals/{id}
     * Get public proposal details
     */
    @GetMapping("/proposals/{id}")
    public ApiResponse<PublicProposalDetail> getProposalDetail(@PathVariable UUID id) {
        log.info("Public proposal detail request: {}", id);
        
        PublicProposalDetail proposal = governanceService.getProposalDetail(id);
        
        return ApiResponse.success(proposal);
    }

    /**
     * GET /api/public/governance/stats
     * Get governance statistics
     */
    @GetMapping("/stats")
    public ApiResponse<GovernanceStatsResponse> getGovernanceStats() {
        log.info("Public governance stats request");
        
        GovernanceStatsResponse stats = governanceService.getGovernanceStats();
        
        return ApiResponse.success(stats);
    }

    /**
     * GET /api/public/governance/delegates
     * Get top delegates by voting power
     */
    @GetMapping("/delegates")
    public ApiResponse<DelegatesResponse> getDelegates(
            @RequestParam(defaultValue = "20") int limit) {
        
        log.info("Public delegates request");
        
        DelegatesResponse delegates = governanceService.getDelegates(limit);
        
        return ApiResponse.success(delegates);
    }

    /**
     * GET /api/public/governance/token-info
     * Get governance token information
     */
    @GetMapping("/token-info")
    public ApiResponse<TokenInfoResponse> getTokenInfo() {
        log.info("Public token info request");
        
        TokenInfoResponse tokenInfo = governanceService.getTokenInfo();
        
        return ApiResponse.success(tokenInfo);
    }

    // DTOs

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PublicProposalsResponse {
        private List<PublicProposal> proposals;
        private ProposalStats stats;
        private Long total;
        private Integer page;
        private Integer size;
        private Integer totalPages;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PublicProposal {
        private UUID proposalId;
        private String title;
        private String category;
        private String status;
        private ProposerPreview proposer;
        private VoteSummary votes;
        private BigDecimal quorum;
        private BigDecimal quorumProgress;
        private Instant votingEndsAt;
        private Instant createdAt;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProposerPreview {
        private UUID userId;
        private String name;
        private String avatar;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class VoteSummary {
        private BigDecimal forVotes;
        private BigDecimal againstVotes;
        private BigDecimal abstainVotes;
        private Long voterCount;
        private BigDecimal forPercentage;
        private BigDecimal againstPercentage;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProposalStats {
        private Long total;
        private Long active;
        private Long passed;
        private Long rejected;
        private Long executed;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PublicProposalDetail {
        private UUID proposalId;
        private String title;
        private String description;
        private String category;
        private String status;
        private ProposerInfo proposer;
        private VoteSummary votes;
        private VotingInfo voting;
        private List<ProposalAction> actions;
        private List<TopVoter> topVoters;
        private ExecutionInfo execution;
        private Instant createdAt;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProposerInfo {
        private UUID userId;
        private String name;
        private String avatar;
        private BigDecimal votingPower;
        private Integer proposalsCreated;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class VotingInfo {
        private BigDecimal quorum;
        private BigDecimal quorumProgress;
        private BigDecimal threshold;
        private Instant votingStartsAt;
        private Instant votingEndsAt;
        private Integer timelockDays;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProposalAction {
        private Integer order;
        private String type;
        private String description;
        private String target;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TopVoter {
        private UUID userId;
        private String name;
        private String vote;
        private BigDecimal votingPower;
        private Instant votedAt;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ExecutionInfo {
        private Boolean executed;
        private String txHash;
        private Instant executedAt;
        private String result;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class GovernanceStatsResponse {
        private OverallStats overall;
        private ParticipationStats participation;
        private List<CategoryBreakdown> byCategory;
        private List<MonthlyTrend> trend;
        private Instant lastUpdated;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class OverallStats {
        private Long totalProposals;
        private Long passedProposals;
        private Long rejectedProposals;
        private Long executedProposals;
        private BigDecimal passRate;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ParticipationStats {
        private Long totalVoters;
        private Long activeVoters;
        private BigDecimal averageParticipation;
        private BigDecimal totalVotingPower;
        private BigDecimal averageVotingPower;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CategoryBreakdown {
        private String category;
        private Long proposalCount;
        private BigDecimal passRate;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MonthlyTrend {
        private String month;
        private Long proposals;
        private Long passed;
        private BigDecimal participation;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DelegatesResponse {
        private List<DelegateInfo> delegates;
        private Long totalDelegates;
        private BigDecimal totalDelegatedPower;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DelegateInfo {
        private UUID userId;
        private String name;
        private String avatar;
        private BigDecimal votingPower;
        private BigDecimal delegatedPower;
        private Integer delegators;
        private Integer proposalsVoted;
        private BigDecimal participationRate;
        private String statement;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TokenInfoResponse {
        private String tokenName;
        private String tokenSymbol;
        private String contractAddress;
        private BigDecimal totalSupply;
        private BigDecimal circulatingSupply;
        private BigDecimal stakedSupply;
        private BigDecimal price;
        private BigDecimal marketCap;
        private TokenDistribution distribution;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TokenDistribution {
        private BigDecimal communityPercentage;
        private BigDecimal teamPercentage;
        private BigDecimal treasuryPercentage;
        private BigDecimal investorsPercentage;
    }
}

