package com.aw3.platform.dto.creator;

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
 * DTO for campaign list in Creator Portal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignListResponse {

    private List<CampaignItem> campaigns;
    private PageInfo pagination;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampaignItem {
        private UUID campaignId;
        private String title;
        private String description;
        private String category;
        private BigDecimal budgetAmount;
        private String budgetToken;
        private BigDecimal requiredReputation;
        private Integer numberOfCreators;
        
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        private Instant deadline;
        
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        private Instant createdAt;

        private Boolean hasApplied;
        private BigDecimal matchScore;
        private String projectName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageInfo {
        private Integer currentPage;
        private Integer totalPages;
        private Long totalElements;
        private Integer pageSize;
    }
}

