package com.aw3.platform.repository;

import com.aw3.platform.entity.PlatformFee;
import com.aw3.platform.entity.enums.FeeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for PlatformFee entity
 */
@Repository
public interface PlatformFeeRepository extends JpaRepository<PlatformFee, UUID> {

    // Find fees by campaign
    List<PlatformFee> findByCampaignId(UUID campaignId);

    // Find fees by project
    Page<PlatformFee> findByProjectId(UUID projectId, Pageable pageable);

    // Find fees by type
    Page<PlatformFee> findByFeeType(FeeType feeType, Pageable pageable);

    // Get fees within date range
    @Query("SELECT f FROM PlatformFee f WHERE f.chargedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY f.chargedAt DESC")
    List<PlatformFee> findByDateRange(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );

    // Calculate total fees for a project
    @Query("SELECT SUM(f.finalAmount) FROM PlatformFee f WHERE f.projectId = :projectId")
    BigDecimal sumTotalFeesByProjectId(@Param("projectId") UUID projectId);

    // Calculate total revenue for a period
    @Query("SELECT SUM(f.finalAmount) FROM PlatformFee f WHERE " +
           "f.chargedAt BETWEEN :startDate AND :endDate AND f.paymentStatus = 'PAID'")
    BigDecimal sumTotalRevenueByPeriod(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );

    // Get fee breakdown by type for analytics
    @Query("SELECT f.feeType, SUM(f.finalAmount), COUNT(f) FROM PlatformFee f " +
           "WHERE f.chargedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY f.feeType")
    List<Object[]> getFeeBreakdownByType(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );
}

