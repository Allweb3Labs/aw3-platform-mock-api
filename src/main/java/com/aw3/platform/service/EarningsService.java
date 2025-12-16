package com.aw3.platform.service;

import com.aw3.platform.controller.creator.CreatorEarningsController;
import com.aw3.platform.dto.creator.EarningsSummaryResponse;
import com.aw3.platform.entity.Deliverable;
import com.aw3.platform.entity.User;
import com.aw3.platform.exception.NotFoundException;
import com.aw3.platform.repository.DeliverableRepository;
import com.aw3.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Earnings Service
 * 
 * Business Rules:
 * - Earnings include campaign payments, performance bonuses, SPC royalties
 * - Payments are released after oracle verification
 * - SPC NFT royalties are 5% on secondary sales
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EarningsService {

    private final UserRepository userRepository;
    private final DeliverableRepository deliverableRepository;

    /**
     * Get earnings summary for creator
     */
    public EarningsSummaryResponse getEarningsSummary(UUID creatorId, String period) {
        User user = userRepository.findById(creatorId)
                .orElseThrow(() -> new NotFoundException("Creator not found"));

        Instant startDate = calculateStartDate(period);
        
        // Get deliverables with payments
        List<Deliverable> paidDeliverables = deliverableRepository.findPaidDeliverablesByCreatorId(
                creatorId, startDate, Instant.now());

        // Calculate totals
        BigDecimal campaignPayments = paidDeliverables.stream()
                .map(d -> d.getPaymentAmount() != null ? d.getPaymentAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal performanceBonuses = paidDeliverables.stream()
                .map(d -> d.getPerformanceBonus() != null ? d.getPerformanceBonus() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal spcRoyalties = new BigDecimal("1250"); // Mock for MVP
        BigDecimal referralBonuses = new BigDecimal("250"); // Mock for MVP

        BigDecimal totalEarnings = campaignPayments.add(performanceBonuses).add(spcRoyalties).add(referralBonuses);

        // Calculate 30-day period
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        List<Deliverable> last30DaysDeliverables = paidDeliverables.stream()
                .filter(d -> d.getPaidAt() != null && d.getPaidAt().isAfter(thirtyDaysAgo))
                .collect(Collectors.toList());

        BigDecimal earnings30d = last30DaysDeliverables.stream()
                .map(d -> {
                    BigDecimal payment = d.getPaymentAmount() != null ? d.getPaymentAmount() : BigDecimal.ZERO;
                    BigDecimal bonus = d.getPerformanceBonus() != null ? d.getPerformanceBonus() : BigDecimal.ZERO;
                    return payment.add(bonus);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgEarningsPerCampaign = last30DaysDeliverables.isEmpty() 
                ? BigDecimal.ZERO 
                : earnings30d.divide(new BigDecimal(last30DaysDeliverables.size()), 2, RoundingMode.HALF_UP);

        // Calculate category breakdown
        Map<String, List<Deliverable>> byCategory = paidDeliverables.stream()
                .collect(Collectors.groupingBy(d -> getCategoryForDeliverable(d)));

        List<EarningsSummaryResponse.CategoryBreakdown> categoryBreakdown = byCategory.entrySet().stream()
                .map(entry -> EarningsSummaryResponse.CategoryBreakdown.builder()
                        .category(entry.getKey())
                        .earnings(entry.getValue().stream()
                                .map(d -> d.getPaymentAmount() != null ? d.getPaymentAmount() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add))
                        .campaigns(entry.getValue().size())
                        .build())
                .sorted((a, b) -> b.getEarnings().compareTo(a.getEarnings()))
                .collect(Collectors.toList());

        // Get pending payments
        List<Deliverable> pendingDeliverables = deliverableRepository.findPendingPaymentsByCreatorId(creatorId);
        BigDecimal pendingAmount = pendingDeliverables.stream()
                .map(d -> d.getPaymentAmount() != null ? d.getPaymentAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return EarningsSummaryResponse.builder()
                .userId(creatorId)
                .summary(EarningsSummaryResponse.SummaryInfo.builder()
                        .totalEarnings(totalEarnings)
                        .campaignPayments(campaignPayments)
                        .performanceBonuses(performanceBonuses)
                        .spcRoyalties(spcRoyalties)
                        .referralBonuses(referralBonuses)
                        .build())
                .period30d(EarningsSummaryResponse.Period30dInfo.builder()
                        .totalEarnings(earnings30d)
                        .campaignsCompleted(last30DaysDeliverables.size())
                        .avgEarningsPerCampaign(avgEarningsPerCampaign)
                        .build())
                .breakdown(EarningsSummaryResponse.BreakdownInfo.builder()
                        .byCategory(categoryBreakdown)
                        .byPaymentType(EarningsSummaryResponse.PaymentTypeBreakdown.builder()
                                .fixed(campaignPayments.multiply(new BigDecimal("0.33")))
                                .performanceBased(campaignPayments.multiply(new BigDecimal("0.67")))
                                .build())
                        .build())
                .pending(EarningsSummaryResponse.PendingInfo.builder()
                        .amount(pendingAmount)
                        .campaigns(pendingDeliverables.size())
                        .expectedReleaseDate(Instant.now().plus(7, ChronoUnit.DAYS))
                        .build())
                .wallet(EarningsSummaryResponse.WalletInfo.builder()
                        .availableBalance(new BigDecimal("5000"))
                        .lockedBalance(pendingAmount)
                        .build())
                .build();
    }

    /**
     * Get transaction history
     */
    public CreatorEarningsController.TransactionListResponse getTransactionHistory(
            UUID creatorId, String type, String startDate, String endDate, int limit, int offset) {
        
        // Mock transaction data for MVP
        List<CreatorEarningsController.TransactionItem> transactions = new ArrayList<>();

        for (int i = 0; i < Math.min(limit, 10); i++) {
            transactions.add(CreatorEarningsController.TransactionItem.builder()
                    .transactionId("tx-" + UUID.randomUUID().toString().substring(0, 8))
                    .type(i % 3 == 0 ? "spc_royalty" : "campaign_payment")
                    .amount(new BigDecimal(String.valueOf(500 + i * 100)))
                    .currency("USDC")
                    .campaignId(UUID.randomUUID())
                    .campaignTitle("Campaign " + (i + 1))
                    .date(Instant.now().minus(i, ChronoUnit.DAYS))
                    .status("completed")
                    .blockchain(CreatorEarningsController.BlockchainInfo.builder()
                            .txHash("0x" + UUID.randomUUID().toString().replace("-", ""))
                            .chainId(8453)
                            .blockNumber(12345678L + i)
                            .build())
                    .breakdown(CreatorEarningsController.TransactionBreakdown.builder()
                            .basePayment(new BigDecimal(String.valueOf(400 + i * 80)))
                            .performanceBonus(new BigDecimal(String.valueOf(100 + i * 20)))
                            .build())
                    .build());
        }

        return CreatorEarningsController.TransactionListResponse.builder()
                .transactions(transactions)
                .pagination(CreatorEarningsController.PaginationInfo.builder()
                        .total(87L)
                        .limit(limit)
                        .offset(offset)
                        .hasMore(true)
                        .build())
                .build();
    }

    private Instant calculateStartDate(String period) {
        return switch (period) {
            case "30d" -> Instant.now().minus(30, ChronoUnit.DAYS);
            case "90d" -> Instant.now().minus(90, ChronoUnit.DAYS);
            case "1y" -> Instant.now().minus(365, ChronoUnit.DAYS);
            default -> Instant.EPOCH;
        };
    }

    private String getCategoryForDeliverable(Deliverable deliverable) {
        // In production, get from campaign
        List<String> categories = Arrays.asList("DeFi", "NFT", "Gaming", "Infrastructure");
        return categories.get(Math.abs(deliverable.getDeliverableId().hashCode()) % categories.size());
    }
}

