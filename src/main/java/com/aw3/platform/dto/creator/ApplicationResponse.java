package com.aw3.platform.dto.creator;

import com.aw3.platform.entity.enums.ApplicationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO for application response in Creator Portal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {

    private UUID applicationId;
    private UUID campaignId;
    private String campaignTitle;
    private UUID creatorId;
    private BigDecimal proposedRate;
    private String proposal;
    private ApplicationStatus status;
    private List<String> portfolioLinks;
    private String relevantExperience;
    private Integer estimatedCompletionDays;
    private BigDecimal matchScore;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant appliedAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant reviewedAt;
    
    private String rejectionReason;
}

