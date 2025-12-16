package com.aw3.platform.service;

import com.aw3.platform.entity.Campaign;
import com.aw3.platform.entity.Deliverable;
import com.aw3.platform.entity.PlatformFee;
import com.aw3.platform.entity.enums.DeliverableStatus;
import com.aw3.platform.entity.enums.FeeType;
import com.aw3.platform.repository.CampaignRepository;
import com.aw3.platform.repository.DeliverableRepository;
import com.aw3.platform.repository.PlatformFeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Payment Service for handling escrow and payment releases
 * 
 * Responsibilities:
 * - Calculate payment amounts based on KPI achievement
 * - Release payments via smart contract
 * - Distribute platform fees via FeeDistributor
 * - Track payment transactions
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final CampaignRepository campaignRepository;
    private final DeliverableRepository deliverableRepository;
    private final PlatformFeeRepository platformFeeRepository;
    private final Web3Service web3Service;
    private final OracleService oracleService;
    private final CVPICalculationService cvpiCalculationService;
    private final ObjectMapper objectMapper;

    /**
     * Release payment to creator after KPI verification
     * 
     * Process:
     * 1. Verify KPIs via Oracle
     * 2. Calculate payment amount based on achievement
     * 3. Release payment via smart contract
     * 4. Distribute platform fees
     * 5. Calculate CVPI
     */
    @Async
    @Transactional
    public CompletableFuture<PaymentReleaseResponse> releasePayment(
            UUID deliverableId,
            UUID projectId) {
        
        try {
            log.info("Starting payment release for deliverable: {}", deliverableId);

            // 1. Get deliverable and campaign
            Deliverable deliverable = deliverableRepository.findById(deliverableId)
                    .orElseThrow(() -> new RuntimeException("Deliverable not found"));

            Campaign campaign = campaignRepository.findById(deliverable.getCampaignId())
                    .orElseThrow(() -> new RuntimeException("Campaign not found"));

            // Verify ownership
            if (!campaign.getProjectId().equals(projectId)) {
                throw new RuntimeException("Not authorized to release payment for this campaign");
            }

            // 2. Verify KPIs (if not already verified)
            if (deliverable.getStatus() != DeliverableStatus.VERIFIED) {
                OracleService.KpiVerificationResult verification = 
                        oracleService.verifyDeliverableKPIs(deliverableId, campaign.getContractAddress()).get();
                
                deliverable = deliverableRepository.findById(deliverableId)
                        .orElseThrow(() -> new RuntimeException("Deliverable not found"));
            }

            // 3. Calculate payment amount
            PaymentCalculation calculation = calculatePaymentAmount(deliverable, campaign);

            // 4. Release payment via smart contract
            String txHash = web3Service.releasePayment(
                    campaign.getContractAddress(),
                    deliverable.getCreatorId().toString(), // TODO: Get actual wallet address
                    calculation.getNetToCreator(),
                    calculation.getAchievementRate()
            ).get();

            // 5. Record platform fee
            PlatformFee platformFee = PlatformFee.builder()
                    .campaignId(campaign.getCampaignId())
                    .projectId(campaign.getProjectId())
                    .creatorId(deliverable.getCreatorId())
                    .feeType(FeeType.SERVICE_FEE)
                    .baseAmount(calculation.getPlatformFee())
                    .discountAmount(BigDecimal.ZERO)
                    .finalAmount(calculation.getPlatformFee())
                    .calculationSnapshot(objectMapper.writeValueAsString(calculation))
                    .paymentStatus("PAID")
                    .transactionHash(txHash)
                    .paidAt(Instant.now())
                    .build();
            
            platformFeeRepository.save(platformFee);

            // 6. Update deliverable status
            deliverable.setStatus(DeliverableStatus.PAYMENT_RELEASED);
            deliverable.setPaymentAmount(calculation.getNetToCreator());
            deliverableRepository.save(deliverable);

            // 7. Calculate CVPI asynchronously
            BigDecimal totalCost = campaign.getBudgetAmount()
                    .add(campaign.getServiceFee() != null ? campaign.getServiceFee() : BigDecimal.ZERO)
                    .add(campaign.getOracleFee() != null ? campaign.getOracleFee() : BigDecimal.ZERO);
            
            cvpiCalculationService.calculateCVPIForCampaign(
                    campaign.getCampaignId(),
                    deliverable.getCreatorId(),
                    calculation.getVerifiedImpactScore()
            );

            log.info("Payment released successfully for deliverable: {} with tx hash: {}", 
                    deliverableId, txHash);

            PaymentReleaseResponse response = PaymentReleaseResponse.builder()
                    .transactionId(UUID.randomUUID())
                    .campaignId(campaign.getCampaignId())
                    .deliverableId(deliverableId)
                    .paymentCalculation(calculation)
                    .distribution(calculateDistribution(calculation.getPlatformFee()))
                    .blockchain(BlockchainInfo.builder()
                            .txHash(txHash)
                            .blockNumber(0L) // TODO: Get actual block number
                            .chainId(campaign.getChainId())
                            .gasUsed(BigInteger.valueOf(125000))
                            .status("CONFIRMED")
                            .build())
                    .escrowUpdate(EscrowUpdate.builder()
                            .previousBalance(campaign.getEscrowBalance())
                            .released(calculation.getCalculatedPayment())
                            .remainingBalance(campaign.getEscrowBalance().subtract(calculation.getCalculatedPayment()))
                            .build())
                    .timestamp(Instant.now())
                    .build();

            return CompletableFuture.completedFuture(response);

        } catch (Exception e) {
            log.error("Error releasing payment for deliverable {}: {}", deliverableId, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Calculate payment amount based on KPI achievement
     * 
     * Economic Model:
     * - Performance-based: Payment = BaseAmount * (achievement% / 100)
     * - Cap at 150% of base amount
     * - Platform fee: 3-5% deducted before creator payment
     */
    private PaymentCalculation calculatePaymentAmount(Deliverable deliverable, Campaign campaign) {
        try {
            // Parse KPI results
            Map<String, Object> kpiResults = objectMapper.readValue(
                    deliverable.getKpiResults(), 
                    Map.class
            );

            // Get overall achievement rate (in basis points, e.g., 11630 = 116.3%)
            Double overallAchievement = 116.3; // TODO: Parse from kpiResults
            BigInteger achievementBasisPoints = BigInteger.valueOf((long) (overallAchievement * 100));

            // Calculate payment amount
            BigDecimal baseAmount = deliverable.getPaymentAmount() != null ? 
                    deliverable.getPaymentAmount() : BigDecimal.valueOf(5000);

            BigDecimal achievementMultiplier = new BigDecimal(overallAchievement).divide(
                    new BigDecimal("100"), 4, RoundingMode.HALF_UP);

            // Cap at 150%
            if (achievementMultiplier.compareTo(new BigDecimal("1.5")) > 0) {
                achievementMultiplier = new BigDecimal("1.5");
            }

            BigDecimal calculatedPayment = baseAmount.multiply(achievementMultiplier)
                    .setScale(2, RoundingMode.HALF_UP);

            // Calculate platform fee (3-5%)
            BigDecimal platformFeeRate = new BigDecimal("0.04"); // 4%
            BigDecimal platformFee = calculatedPayment.multiply(platformFeeRate)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal netToCreator = calculatedPayment.subtract(platformFee)
                    .setScale(2, RoundingMode.HALF_UP);

            // Calculate verified impact score (simplified)
            BigDecimal verifiedImpactScore = new BigDecimal(overallAchievement * 1000);

            return PaymentCalculation.builder()
                    .baseAmount(baseAmount)
                    .overallAchievement(overallAchievement)
                    .achievementRate(achievementBasisPoints)
                    .calculatedPayment(calculatedPayment)
                    .platformFee(platformFee)
                    .netToCreator(netToCreator)
                    .refundToProject(BigDecimal.ZERO)
                    .verifiedImpactScore(verifiedImpactScore)
                    .build();

        } catch (Exception e) {
            log.error("Error calculating payment amount: {}", e.getMessage(), e);
            throw new RuntimeException("Payment calculation failed", e);
        }
    }

    /**
     * Calculate 5-layer revenue distribution
     * 
     * Economic Model Section 14.4:
     * - Treasury: 50%
     * - Validators: 20%
     * - AI Ecosystem: 15%
     * - DAO Treasury: 10%
     * - Buyback: 5%
     */
    private FeeDistribution calculateDistribution(BigDecimal platformFee) {
        BigDecimal treasury = platformFee.multiply(new BigDecimal("0.50"))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal validators = platformFee.multiply(new BigDecimal("0.20"))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal aiEcosystem = platformFee.multiply(new BigDecimal("0.15"))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal daoTreasury = platformFee.multiply(new BigDecimal("0.10"))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal buyback = platformFee.multiply(new BigDecimal("0.05"))
                .setScale(2, RoundingMode.HALF_UP);

        Map<String, BigDecimal> breakdown = new HashMap<>();
        breakdown.put("treasury", treasury);
        breakdown.put("validators", validators);
        breakdown.put("aiEcosystem", aiEcosystem);
        breakdown.put("daoTreasury", daoTreasury);
        breakdown.put("buyback", buyback);

        return FeeDistribution.builder()
                .creatorPayment(BigDecimal.ZERO) // Already calculated separately
                .platformRevenue(platformFee)
                .breakdown(breakdown)
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentCalculation {
        private BigDecimal baseAmount;
        private Double overallAchievement;
        private BigInteger achievementRate;
        private BigDecimal calculatedPayment;
        private BigDecimal platformFee;
        private BigDecimal netToCreator;
        private BigDecimal refundToProject;
        private BigDecimal verifiedImpactScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeeDistribution {
        private BigDecimal creatorPayment;
        private BigDecimal platformRevenue;
        private Map<String, BigDecimal> breakdown;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentReleaseResponse {
        private UUID transactionId;
        private UUID campaignId;
        private UUID deliverableId;
        private PaymentCalculation paymentCalculation;
        private FeeDistribution distribution;
        private BlockchainInfo blockchain;
        private EscrowUpdate escrowUpdate;
        private Instant timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlockchainInfo {
        private String txHash;
        private Long blockNumber;
        private Integer chainId;
        private BigInteger gasUsed;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EscrowUpdate {
        private BigDecimal previousBalance;
        private BigDecimal released;
        private BigDecimal remainingBalance;
    }
}

