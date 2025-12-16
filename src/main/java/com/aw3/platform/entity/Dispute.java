package com.aw3.platform.entity;

import com.aw3.platform.entity.enums.DisputeStatus;
import com.aw3.platform.entity.enums.DisputeResolution;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Entity for dispute management
 */
@Entity
@Table(name = "disputes")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dispute {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String disputeId;
    
    @Column(nullable = false)
    private String campaignId;
    
    @Column(nullable = false)
    private String initiatorUserId;  // User who raised the dispute
    
    @Column(nullable = false)
    private String respondentUserId;  // Other party
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisputeStatus status;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;
    
    @Column(columnDefinition = "TEXT")
    private String evidence;  // JSON array of evidence URLs
    
    @Enumerated(EnumType.STRING)
    private DisputeResolution resolution;
    
    @Column(columnDefinition = "TEXT")
    private String resolutionNotes;
    
    private String resolvedByAdminId;
    
    private Instant resolvedAt;
    
    @Builder.Default
    private Boolean escalated = false;
    
    private String daoProposalId;  // If escalated to DAO
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    
    @LastModifiedDate
    private Instant updatedAt;
}

