package com.aw3.platform.dto.creator;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO for submitting application in Creator Portal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRequest {

    @NotNull(message = "Campaign ID is required")
    private UUID campaignId;

    @Positive(message = "Proposed rate must be positive")
    private BigDecimal proposedRate;

    @NotBlank(message = "Proposal is required")
    private String proposal;

    private List<String> portfolioLinks;

    private String relevantExperience;

    private Integer estimatedCompletionDays;
}

