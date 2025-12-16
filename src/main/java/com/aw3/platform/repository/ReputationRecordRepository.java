package com.aw3.platform.repository;

import com.aw3.platform.entity.ReputationRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for ReputationRecord entity
 * 
 * Ownership Rules:
 * - Creators can view their own records (read-only)
 * - Projects can view records for their campaigns
 * - Admins can view and manually adjust all records
 */
@Repository
public interface ReputationRecordRepository extends JpaRepository<ReputationRecord, UUID> {

    // Creator Portal: View own reputation history
    Page<ReputationRecord> findByUserId(UUID userId, Pageable pageable);

    // Project Portal: View reputation records for campaign
    Page<ReputationRecord> findByCampaignId(UUID campaignId, Pageable pageable);

    // Get reputation history within date range
    @Query("SELECT r FROM ReputationRecord r WHERE r.userId = :userId " +
           "AND r.recordedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY r.recordedAt DESC")
    List<ReputationRecord> findByUserIdAndDateRange(
        @Param("userId") UUID userId,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );

    // Get records by type
    List<ReputationRecord> findByUserIdAndRecordType(UUID userId, String recordType);

    // Admin Portal: Get manually adjusted records
    @Query("SELECT r FROM ReputationRecord r WHERE r.adjustedBy IS NOT NULL " +
           "ORDER BY r.recordedAt DESC")
    Page<ReputationRecord> findManualAdjustments(Pageable pageable);

    // Count records for user
    long countByUserId(UUID userId);
}

