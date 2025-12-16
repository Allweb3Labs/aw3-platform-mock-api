package com.aw3.platform.dto.admin;

import com.aw3.platform.entity.enums.DisputeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for dispute list
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisputeListResponse {
    private List<DisputeSummary> disputes;
    private PaginationInfo pagination;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DisputeSummary {
        private String disputeId;
        private String campaignId;
        private String campaignTitle;
        private String initiatorUserId;
        private String respondentUserId;
        private DisputeStatus status;
        private String reason;
        private Boolean escalated;
        private Instant createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        private Long total;
        private Integer page;
        private Integer limit;
        private Integer totalPages;
    }
}

