package com.aw3.platform.dto.project;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * DTO for creating campaign in Project Portal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignCreateRequest {

    @NotBlank(message = "Campaign title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Budget amount is required")
    @Positive(message = "Budget must be positive")
    private BigDecimal budgetAmount;

    @NotBlank(message = "Budget token is required")
    private String budgetToken;

    @NotNull(message = "Deadline is required")
    @Future(message = "Deadline must be in the future")
    private Instant deadline;

    @NotNull(message = "KPI targets are required")
    private Map<String, Object> kpiTargets;

    private BigDecimal requiredReputation;

    @Positive(message = "Number of creators must be positive")
    private Integer numberOfCreators;

    @Pattern(regexp = "simple|standard|complex|enterprise", message = "Invalid complexity level")
    private String complexity;

    @NotBlank(message = "Fee estimate ID is required")
    private String feeEstimateId;

    private Boolean aw3TokenPaymentEnabled;

    private Map<String, Object> campaignMetadata;
}

