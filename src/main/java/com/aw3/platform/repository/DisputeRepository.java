package com.aw3.platform.repository;

import com.aw3.platform.entity.Dispute;
import com.aw3.platform.entity.enums.DisputeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Dispute entity
 */
@Repository
public interface DisputeRepository extends JpaRepository<Dispute, String> {
    
    Page<Dispute> findByStatus(DisputeStatus status, Pageable pageable);
    
    Page<Dispute> findByEscalated(Boolean escalated, Pageable pageable);
    
    Page<Dispute> findByCampaignId(String campaignId, Pageable pageable);
    
    Long countByStatus(DisputeStatus status);
}

