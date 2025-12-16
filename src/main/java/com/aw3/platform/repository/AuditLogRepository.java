package com.aw3.platform.repository;

import com.aw3.platform.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

/**
 * Repository for AuditLog entity
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
    
    Page<AuditLog> findByAdminUserId(String adminUserId, Pageable pageable);
    
    Page<AuditLog> findByActionType(String actionType, Pageable pageable);
    
    Page<AuditLog> findByTargetEntityId(String targetEntityId, Pageable pageable);
    
    Page<AuditLog> findByTimestampBetween(Instant start, Instant end, Pageable pageable);
}

