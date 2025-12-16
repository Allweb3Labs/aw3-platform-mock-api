package com.aw3.platform.service;

import com.aw3.platform.dto.project.FeeEstimateRequest;
import com.aw3.platform.dto.project.FeeEstimateResponse;
import com.aw3.platform.entity.User;
import com.aw3.platform.entity.enums.UserRole;
import com.aw3.platform.entity.enums.UserStatus;
import com.aw3.platform.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for FeeCalculationService
 * 
 * Tests cover:
 * - Budget-tiered fee calculation (4-10%)
 * - Reputation discounts (0-40%)
 * - Complexity multipliers (0.8x-1.5x)
 * - Oracle fee calculation
 * - AW3 token discount (20%)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Fee Calculation Service Tests")
class FeeCalculationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private FeeCalculationService feeCalculationService;

    private UUID projectId;
    private User projectUser;

    @BeforeEach
    void setUp() {
        // Set configuration properties
        ReflectionTestUtils.setField(feeCalculationService, "tier1Max", new BigDecimal("5000"));
        ReflectionTestUtils.setField(feeCalculationService, "tier1Rate", new BigDecimal("0.10"));
        ReflectionTestUtils.setField(feeCalculationService, "tier2Max", new BigDecimal("20000"));
        ReflectionTestUtils.setField(feeCalculationService, "tier2Rate", new BigDecimal("0.08"));
        ReflectionTestUtils.setField(feeCalculationService, "tier3Max", new BigDecimal("50000"));
        ReflectionTestUtils.setField(feeCalculationService, "tier3Rate", new BigDecimal("0.06"));
        ReflectionTestUtils.setField(feeCalculationService, "tier4Rate", new BigDecimal("0.04"));

        projectId = UUID.randomUUID();
        projectUser = User.builder()
                .userId(projectId)
                .walletAddress("0x1234567890abcdef")
                .didIdentifier("did:aw3:0x1234567890abcdef")
                .userRole(UserRole.PROJECT)
                .status(UserStatus.ACTIVE)
                .reputationScore(BigDecimal.valueOf(750))
                .cumulativeSpend(BigDecimal.valueOf(25000))
                .build();
    }

    @Test
    @DisplayName("Should calculate fees for small budget (tier 1: 10%)")
    void testSmallBudgetFeeCalculation() {
        // Given
        when(userRepository.findById(projectId)).thenReturn(Optional.of(projectUser));

        FeeEstimateRequest request = FeeEstimateRequest.builder()
                .campaignBudget(new BigDecimal("3000"))
                .category("DeFi")
                .complexity("standard")
                .requestedCreators(2)
                .kpiMetrics(new ArrayList<>())
                .useAW3Token(false)
                .build();

        // When
        FeeEstimateResponse response = feeCalculationService.calculateCampaignFees(projectId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getFeeBreakdown().getBaseRate()).isEqualByComparingTo(new BigDecimal("0.10"));
        assertThat(response.getCampaignBudget()).isEqualByComparingTo(new BigDecimal("3000"));
    }

    @Test
    @DisplayName("Should calculate fees for medium budget (tier 2: 8%)")
    void testMediumBudgetFeeCalculation() {
        // Given
        when(userRepository.findById(projectId)).thenReturn(Optional.of(projectUser));

        FeeEstimateRequest request = FeeEstimateRequest.builder()
                .campaignBudget(new BigDecimal("10000"))
                .category("NFT")
                .complexity("standard")
                .requestedCreators(3)
                .kpiMetrics(new ArrayList<>())
                .useAW3Token(false)
                .build();

        // When
        FeeEstimateResponse response = feeCalculationService.calculateCampaignFees(projectId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getFeeBreakdown().getBaseRate()).isEqualByComparingTo(new BigDecimal("0.08"));
        assertThat(response.getTotalFees()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should apply reputation discount for high spend users")
    void testReputationDiscount() {
        // Given
        User highSpendUser = User.builder()
                .userId(projectId)
                .walletAddress("0x1234567890abcdef")
                .didIdentifier("did:aw3:0x1234567890abcdef")
                .userRole(UserRole.PROJECT)
                .status(UserStatus.ACTIVE)
                .reputationScore(BigDecimal.valueOf(850))
                .cumulativeSpend(BigDecimal.valueOf(60000)) // Gold tier
                .build();

        when(userRepository.findById(projectId)).thenReturn(Optional.of(highSpendUser));

        FeeEstimateRequest request = FeeEstimateRequest.builder()
                .campaignBudget(new BigDecimal("10000"))
                .category("DeFi")
                .complexity("standard")
                .requestedCreators(2)
                .kpiMetrics(new ArrayList<>())
                .useAW3Token(false)
                .build();

        // When
        FeeEstimateResponse response = feeCalculationService.calculateCampaignFees(projectId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getFeeBreakdown().getReputationDiscount()).isGreaterThan(BigDecimal.ZERO);
        assertThat(response.getFeeBreakdown().getDiscountAmount()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should apply 20% AW3 token discount")
    void testAW3TokenDiscount() {
        // Given
        when(userRepository.findById(projectId)).thenReturn(Optional.of(projectUser));

        FeeEstimateRequest request = FeeEstimateRequest.builder()
                .campaignBudget(new BigDecimal("10000"))
                .category("DeFi")
                .complexity("standard")
                .requestedCreators(2)
                .kpiMetrics(new ArrayList<>())
                .useAW3Token(true) // Enable AW3 token payment
                .build();

        // When
        FeeEstimateResponse response = feeCalculationService.calculateCampaignFees(projectId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getFeeBreakdown().getAw3TokenDiscount()).isEqualByComparingTo(new BigDecimal("0.20"));
        assertThat(response.getFeeBreakdown().getAw3DiscountAmount()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should apply complexity multipliers correctly")
    void testComplexityMultipliers() {
        // Given
        when(userRepository.findById(projectId)).thenReturn(Optional.of(projectUser));

        // Test simple complexity
        FeeEstimateRequest simpleRequest = FeeEstimateRequest.builder()
                .campaignBudget(new BigDecimal("10000"))
                .category("DeFi")
                .complexity("simple")
                .requestedCreators(1)
                .kpiMetrics(new ArrayList<>())
                .useAW3Token(false)
                .build();

        // When
        FeeEstimateResponse simpleResponse = feeCalculationService.calculateCampaignFees(projectId, simpleRequest);

        // Then
        assertThat(simpleResponse.getFeeBreakdown().getComplexityMultiplier())
                .isEqualByComparingTo(new BigDecimal("0.8"));

        // Test complex complexity
        FeeEstimateRequest complexRequest = FeeEstimateRequest.builder()
                .campaignBudget(new BigDecimal("10000"))
                .category("DeFi")
                .complexity("complex")
                .requestedCreators(25)
                .kpiMetrics(new ArrayList<>())
                .useAW3Token(false)
                .build();

        // When
        FeeEstimateResponse complexResponse = feeCalculationService.calculateCampaignFees(projectId, complexRequest);

        // Then
        assertThat(complexResponse.getFeeBreakdown().getComplexityMultiplier())
                .isEqualByComparingTo(new BigDecimal("1.5"));
    }

    @Test
    @DisplayName("Should calculate escrow requirement with 10% buffer")
    void testEscrowRequirement() {
        // Given
        when(userRepository.findById(projectId)).thenReturn(Optional.of(projectUser));

        FeeEstimateRequest request = FeeEstimateRequest.builder()
                .campaignBudget(new BigDecimal("10000"))
                .category("DeFi")
                .complexity("standard")
                .requestedCreators(2)
                .kpiMetrics(new ArrayList<>())
                .useAW3Token(false)
                .build();

        // When
        FeeEstimateResponse response = feeCalculationService.calculateCampaignFees(projectId, request);

        // Then
        assertThat(response.getEscrowRequirement()).isNotNull();
        assertThat(response.getEscrowRequirement().getTotalRequired())
                .isGreaterThan(response.getCampaignBudget());
        assertThat(response.getEscrowRequirement().getBufferPercentage())
                .isEqualByComparingTo(new BigDecimal("10"));
    }

    @Test
    @DisplayName("Should generate unique fee estimate ID")
    void testFeeEstimateId() {
        // Given
        when(userRepository.findById(projectId)).thenReturn(Optional.of(projectUser));

        FeeEstimateRequest request = FeeEstimateRequest.builder()
                .campaignBudget(new BigDecimal("10000"))
                .category("DeFi")
                .complexity("standard")
                .requestedCreators(2)
                .kpiMetrics(new ArrayList<>())
                .useAW3Token(false)
                .build();

        // When
        FeeEstimateResponse response = feeCalculationService.calculateCampaignFees(projectId, request);

        // Then
        assertThat(response.getFeeEstimateId()).isNotNull();
        assertThat(response.getFeeEstimateId()).startsWith("est-");
        assertThat(response.getValidUntil()).isNotNull();
        assertThat(response.getSignature()).isNotNull();
    }
}

