package com.aw3.platform.dto.admin;

import com.aw3.platform.entity.enums.CampaignStatus;
import lombok.Data;

/**
 * Request DTO for listing campaigns with filters
 */
@Data
public class CampaignListRequest {
    private CampaignStatus status;     // Filter by status
    private String vertical;           // Filter by vertical
    private Double minBudget;          // Minimum campaign budget
    private Double maxBudget;          // Maximum campaign budget
    private Integer page = 1;          // Page number (default: 1)
    private Integer limit = 50;        // Results per page (default: 50)
}

