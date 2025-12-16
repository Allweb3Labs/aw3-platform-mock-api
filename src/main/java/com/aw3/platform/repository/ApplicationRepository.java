package com.aw3.platform.repository;

import com.aw3.platform.entity.Application;
import com.aw3.platform.entity.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Application entity with ownership filtering
 * 
 * Ownership Rules:
 * - Creators can only see their own applications
 * - Projects can only see applications for campaigns they own
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    // Creator Portal: List own applications
    Page<Application> findByCreatorId(UUID creatorId, Pageable pageable);

    // Creator Portal: Get specific application (ownership check needed)
    Optional<Application> findByApplicationIdAndCreatorId(UUID applicationId, UUID creatorId);

    // Project Portal: List applications for campaign (ownership of campaign must be verified)
    Page<Application> findByCampaignId(UUID campaignId, Pageable pageable);

    // Project Portal: List applications for campaigns owned by project
    @Query("SELECT a FROM Application a JOIN Campaign c ON a.campaignId = c.campaignId " +
           "WHERE c.projectId = :projectId")
    Page<Application> findByProjectId(@Param("projectId") UUID projectId, Pageable pageable);

    // Check if creator already applied to campaign
    boolean existsByCampaignIdAndCreatorId(UUID campaignId, UUID creatorId);

    // Get application by campaign and creator
    Optional<Application> findByCampaignIdAndCreatorId(UUID campaignId, UUID creatorId);

    // Filter applications by status for a campaign
    Page<Application> findByCampaignIdAndStatus(UUID campaignId, ApplicationStatus status, Pageable pageable);

    // Count applications for a campaign
    long countByCampaignId(UUID campaignId);

    // Count applications by creator
    long countByCreatorId(UUID creatorId);

    // Find accepted applications for creator
    List<Application> findByCreatorIdAndStatus(UUID creatorId, ApplicationStatus status);
}

