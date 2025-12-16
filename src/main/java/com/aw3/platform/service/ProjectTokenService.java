package com.aw3.platform.service;

import com.aw3.platform.dto.project.*;
import com.aw3.platform.entity.Campaign;
import com.aw3.platform.entity.User;
import com.aw3.platform.exception.ForbiddenException;
import com.aw3.platform.exception.NotFoundException;
import com.aw3.platform.repository.CampaignRepository;
import com.aw3.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Service for Project Token/Wallet Management
 * 
 * Business Rules:
 * - Projects can only view their own wallet/escrow
 * - Deposits require on-chain confirmation
 * - Escrow funds locked until campaign conditions met
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectTokenService {

    private final UserRepository userRepository;
    private final CampaignRepository campaignRepository;
    private final Web3Service web3Service;

    @Transactional(readOnly = true)
    public ProjectTokenBalanceResponse getWalletBalance(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Get on-chain balances
        BigDecimal aw3Balance = web3Service.getAW3Balance(user.getWalletAddress());
        BigDecimal usdcBalance = web3Service.getUSDCBalance(user.getWalletAddress());

        // Calculate escrow locked amount
        BigDecimal escrowLocked = calculateTotalEscrowLocked(userId);

        return ProjectTokenBalanceResponse.builder()
                .userId(userId)
                .walletAddress(user.getWalletAddress())
                .aw3Balance(aw3Balance)
                .usdcBalance(usdcBalance)
                .totalEscrowLocked(escrowLocked)
                .availableForCampaigns(aw3Balance.add(usdcBalance).subtract(escrowLocked))
                .build();
    }

    @Transactional
    public DepositResponse initiateDeposit(UUID userId, DepositRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Generate deposit address and transaction
        String depositAddress = web3Service.getDepositAddress(request.getToken());
        String transactionId = UUID.randomUUID().toString();

        log.info("Project {} initiated deposit of {} {} to {}", 
                userId, request.getAmount(), request.getToken(), depositAddress);

        return DepositResponse.builder()
                .depositAddress(depositAddress)
                .amount(request.getAmount())
                .token(request.getToken())
                .transactionId(transactionId)
                .status("PENDING")
                .expiresAt(Instant.now().plusSeconds(3600).toEpochMilli())
                .build();
    }

    @Transactional(readOnly = true)
    public EscrowStatusResponse getEscrowStatus(UUID userId) {
        List<Campaign> campaigns = campaignRepository.findByProjectId(userId);

        List<EscrowStatusResponse.CampaignEscrowItem> escrowItems = new ArrayList<>();
        BigDecimal totalLocked = BigDecimal.ZERO;
        BigDecimal totalReleased = BigDecimal.ZERO;
        BigDecimal totalPending = BigDecimal.ZERO;

        for (Campaign campaign : campaigns) {
            if (!"DRAFT".equals(campaign.getStatus())) {
                EscrowStatusResponse.CampaignEscrowItem item = buildCampaignEscrowItem(campaign);
                escrowItems.add(item);
                totalLocked = totalLocked.add(item.getLockedAmount());
                totalReleased = totalReleased.add(item.getReleasedAmount());
                totalPending = totalPending.add(item.getPendingAmount());
            }
        }

        return EscrowStatusResponse.builder()
                .userId(userId)
                .totalLocked(totalLocked)
                .totalReleased(totalReleased)
                .totalPending(totalPending)
                .campaigns(escrowItems)
                .build();
    }

    @Transactional(readOnly = true)
    public EscrowStatusResponse.CampaignEscrowItem getCampaignEscrow(UUID userId, UUID campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NotFoundException("Campaign not found"));

        if (!campaign.getProjectId().equals(userId)) {
            throw new ForbiddenException("Not authorized to view escrow for this campaign");
        }

        return buildCampaignEscrowItem(campaign);
    }

    @Transactional(readOnly = true)
    public TransactionHistoryResponse getTransactionHistory(
            UUID userId, String type, String startDate, String endDate, int page, int size) {
        
        // TODO: Implement actual transaction history from database
        List<TransactionHistoryResponse.TransactionItem> transactions = new ArrayList<>();

        // Mock transaction data
        transactions.add(TransactionHistoryResponse.TransactionItem.builder()
                .transactionId(UUID.randomUUID())
                .type("DEPOSIT")
                .amount(BigDecimal.valueOf(10000))
                .token("USDC")
                .status("CONFIRMED")
                .txHash("0x1234567890abcdef...")
                .description("Wallet deposit")
                .timestamp(Instant.now().minusSeconds(86400))
                .build());

        transactions.add(TransactionHistoryResponse.TransactionItem.builder()
                .transactionId(UUID.randomUUID())
                .type("ESCROW_LOCK")
                .amount(BigDecimal.valueOf(5000))
                .token("USDC")
                .status("CONFIRMED")
                .txHash("0xabcdef1234567890...")
                .campaignId(UUID.randomUUID())
                .description("Campaign funding locked")
                .timestamp(Instant.now().minusSeconds(43200))
                .build());

        return TransactionHistoryResponse.builder()
                .transactions(transactions)
                .pagination(TransactionHistoryResponse.PaginationInfo.builder()
                        .total((long) transactions.size())
                        .page(page)
                        .size(size)
                        .totalPages(1)
                        .build())
                .build();
    }

    // Helper methods

    private BigDecimal calculateTotalEscrowLocked(UUID userId) {
        List<Campaign> campaigns = campaignRepository.findByProjectId(userId);
        return campaigns.stream()
                .filter(c -> "ACTIVE".equals(c.getStatus()) || "PENDING".equals(c.getStatus()))
                .map(Campaign::getTotalBudget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private EscrowStatusResponse.CampaignEscrowItem buildCampaignEscrowItem(Campaign campaign) {
        BigDecimal lockedAmount = BigDecimal.ZERO;
        BigDecimal releasedAmount = BigDecimal.ZERO;
        BigDecimal pendingAmount = BigDecimal.ZERO;

        switch (campaign.getStatus().toUpperCase()) {
            case "ACTIVE":
            case "PENDING":
                lockedAmount = campaign.getTotalBudget();
                break;
            case "COMPLETED":
                releasedAmount = campaign.getTotalBudget();
                break;
            case "IN_PROGRESS":
                // Partial release based on milestones
                lockedAmount = campaign.getTotalBudget().multiply(BigDecimal.valueOf(0.5));
                releasedAmount = campaign.getTotalBudget().multiply(BigDecimal.valueOf(0.5));
                break;
        }

        return EscrowStatusResponse.CampaignEscrowItem.builder()
                .campaignId(campaign.getCampaignId())
                .campaignTitle(campaign.getTitle())
                .status(campaign.getStatus())
                .lockedAmount(lockedAmount)
                .releasedAmount(releasedAmount)
                .pendingAmount(pendingAmount)
                .smartContractAddress(campaign.getSmartContractAddress())
                .milestones(getMilestoneEscrow(campaign))
                .lockedAt(campaign.getCreatedAt())
                .build();
    }

    private List<EscrowStatusResponse.MilestoneEscrow> getMilestoneEscrow(Campaign campaign) {
        // TODO: Get actual milestones from campaign
        return List.of(
                EscrowStatusResponse.MilestoneEscrow.builder()
                        .milestoneNumber(1)
                        .description("Content Creation")
                        .amount(campaign.getTotalBudget().multiply(BigDecimal.valueOf(0.3)))
                        .status("RELEASED")
                        .releaseDate(Instant.now().minusSeconds(86400))
                        .build(),
                EscrowStatusResponse.MilestoneEscrow.builder()
                        .milestoneNumber(2)
                        .description("Publication")
                        .amount(campaign.getTotalBudget().multiply(BigDecimal.valueOf(0.4)))
                        .status("LOCKED")
                        .build(),
                EscrowStatusResponse.MilestoneEscrow.builder()
                        .milestoneNumber(3)
                        .description("KPI Achievement")
                        .amount(campaign.getTotalBudget().multiply(BigDecimal.valueOf(0.3)))
                        .status("LOCKED")
                        .build()
        );
    }
}

