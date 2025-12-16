package com.aw3.platform.repository;

import com.aw3.platform.entity.Deliverable;
import com.aw3.platform.entity.enums.DeliverableStatus;
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
 * Repository for Deliverable entity with ownership filtering
 */
@Repository
public interface DeliverableRepository extends JpaRepository<Deliverable, UUID> {

    // Creator Portal: List own deliverables
    Page<Deliverable> findByCreatorId(UUID creatorId, Pageable pageable);

    // Creator Portal: Get specific deliverable (ownership check)
    Optional<Deliverable> findByDeliverableIdAndCreatorId(UUID deliverableId, UUID creatorId);

    // Project Portal: List deliverables for campaign
    Page<Deliverable> findByCampaignId(UUID campaignId, Pageable pageable);

    // Project Portal: List deliverables for campaigns owned by project
    @Query("SELECT d FROM Deliverable d JOIN Campaign c ON d.campaignId = c.campaignId " +
           "WHERE c.projectId = :projectId")
    Page<Deliverable> findByProjectId(@Param("projectId") UUID projectId, Pageable pageable);

    // Find deliverables by application
    List<Deliverable> findByApplicationId(UUID applicationId);

    // Filter deliverables by status for a campaign
    Page<Deliverable> findByCampaignIdAndStatus(UUID campaignId, DeliverableStatus status, Pageable pageable);

    // Count deliverables for a campaign
    long countByCampaignId(UUID campaignId);

    // Count deliverables by creator
    long countByCreatorId(UUID creatorId);
}

