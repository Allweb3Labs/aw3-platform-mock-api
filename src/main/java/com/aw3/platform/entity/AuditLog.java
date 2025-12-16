package com.aw3.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Entity for audit trail logging
 * All admin actions are recorded immutably
 */
@Entity
@Table(name = "audit_logs")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String auditLogId;
    
    @Column(nullable = false)
    private String adminUserId;
    
    @Column(nullable = false)
    private String adminWalletAddress;
    
    @Column(nullable = false)
    private String actionType;  // e.g., "USER_STATUS_CHANGE", "ROLE_UPDATE", "FEE_CONFIG_UPDATE"
    
    private String targetEntityType;  // e.g., "USER", "CAMPAIGN", "FEE_CONFIG"
    
    private String targetEntityId;
    
    @Column(columnDefinition = "TEXT")
    private String requestData;  // JSON of request parameters
    
    @Column(columnDefinition = "TEXT")
    private String responseData;  // JSON of response
    
    @Column(nullable = false)
    private String ipAddress;
    
    @Column(columnDefinition = "TEXT")
    private String reason;  // Business justification
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant timestamp;
}

