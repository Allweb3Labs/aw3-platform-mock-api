package com.aw3.platform.repository;

import com.aw3.platform.entity.Campaign;
import com.aw3.platform.entity.enums.CampaignStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Campaign entity with ownership filtering
 */
@Repository
public interface CampaignRepository extends JpaRepository<Campaign, UUID> {

    // Project Portal: List own campaigns
    Page<Campaign> findByProjectId(UUID projectId, Pageable pageable);

    // Creator Portal: List active campaigns only
    Page<Campaign> findByStatus(CampaignStatus status, Pageable pageable);

    // Creator Portal: Search campaigns with filters
    @Query("SELECT c FROM Campaign c WHERE c.status = :status " +
           "AND (:category IS NULL OR c.category = :category) " +
           "AND (:minBudget IS NULL OR c.budgetAmount >= :minBudget) " +
           "AND (:maxBudget IS NULL OR c.budgetAmount <= :maxBudget)")
    Page<Campaign> findActiveCampaignsWithFilters(
        @Param("status") CampaignStatus status,
        @Param("category") String category,
        @Param("minBudget") java.math.BigDecimal minBudget,
        @Param("maxBudget") java.math.BigDecimal maxBudget,
        Pageable pageable
    );

    // Project Portal: Get campaigns by project and status
    Page<Campaign> findByProjectIdAndStatus(UUID projectId, CampaignStatus status, Pageable pageable);

    // Admin Portal: Search all campaigns
    @Query("SELECT c FROM Campaign c WHERE " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Campaign> searchCampaigns(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Find campaigns by deadline range
    List<Campaign> findByDeadlineBetween(Instant startDate, Instant endDate);

    // Count campaigns by project
    long countByProjectId(UUID projectId);

    // Count campaigns by creator
    Long countByCreatedBy(String createdBy);

    // Count campaigns by status
    Long countByStatus(CampaignStatus status);

    // Verify ownership
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Campaign c " +
           "WHERE c.campaignId = :campaignId AND c.projectId = :projectId")
    boolean existsByCampaignIdAndProjectId(@Param("campaignId") UUID campaignId, 
                                          @Param("projectId") UUID projectId);
}

