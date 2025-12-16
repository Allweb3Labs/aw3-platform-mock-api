package com.aw3.platform.dto.creator;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Social Account Verification Response DTO
 * 
 * POST /api/creator/profile/social-verification response format
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SocialVerificationResponse {

    private String platform;
    private String handle;
    private Boolean verified;
    private Integer followers;
    private BigDecimal engagementRate;
    private Instant verifiedAt;
    private MetricsInfo metrics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricsInfo {
        private Integer posts;
        private Integer avgLikes;
        private Integer avgRetweets;
        private Integer avgComments;
    }
}

