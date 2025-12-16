package com.aw3.platform.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for reputation adjustment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReputationAdjustmentResponse {
    private String userId;
    private Double previousScore;
    private Double adjustment;
    private Double newScore;
    private TierChange tierChange;
    private String auditLogId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TierChange {
        private String from;
        private String to;
    }
}

