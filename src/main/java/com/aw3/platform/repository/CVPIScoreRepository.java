package com.aw3.platform.repository;

import com.aw3.platform.entity.CVPIScore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for CVPIScore entity
 */
@Repository
public interface CVPIScoreRepository extends JpaRepository<CVPIScore, UUID> {

    // Get CVPI score for a specific campaign
    Optional<CVPIScore> findByCampaignId(UUID campaignId);

    // Creator Portal: Get creator's CVPI scores
    Page<CVPIScore> findByCreatorId(UUID creatorId, Pageable pageable);

    // Creator Portal: Get creator's average CVPI
    @Query("SELECT AVG(c.cvpiScore) FROM CVPIScore c WHERE c.creatorId = :creatorId")
    BigDecimal getAverageCvpiByCreatorId(@Param("creatorId") UUID creatorId);

    // Get creator's CVPI history within date range
    @Query("SELECT c FROM CVPIScore c WHERE c.creatorId = :creatorId " +
           "AND c.calculatedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY c.calculatedAt DESC")
    List<CVPIScore> findByCreatorIdAndDateRange(
        @Param("creatorId") UUID creatorId,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );

    // Project Portal: Get CVPI scores for campaigns
    @Query("SELECT c FROM CVPIScore c JOIN Campaign camp ON c.campaignId = camp.campaignId " +
           "WHERE camp.projectId = :projectId")
    Page<CVPIScore> findByProjectId(@Param("projectId") UUID projectId, Pageable pageable);

    // Project Portal: Get top performing creators by CVPI
    @Query("SELECT c FROM CVPIScore c WHERE " +
           "(:category IS NULL OR c.category = :category) " +
           "ORDER BY c.cvpiScore ASC")
    Page<CVPIScore> findTopPerformersByCategory(@Param("category") String category, Pageable pageable);

    // Admin Portal: Calculate platform average CVPI
    @Query("SELECT AVG(c.cvpiScore) FROM CVPIScore c WHERE " +
           "c.calculatedAt BETWEEN :startDate AND :endDate")
    BigDecimal getPlatformAverageCvpi(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );

    // Get CVPI distribution statistics
    @Query("SELECT MIN(c.cvpiScore), MAX(c.cvpiScore), AVG(c.cvpiScore), " +
           "STDDEV(c.cvpiScore) FROM CVPIScore c WHERE " +
           "c.calculatedAt BETWEEN :startDate AND :endDate")
    Object[] getCvpiStatistics(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );

    // Count CVPI records by category
    @Query("SELECT c.category, COUNT(c) FROM CVPIScore c " +
           "WHERE c.calculatedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY c.category")
    List<Object[]> countByCategoryAndPeriod(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );
}

