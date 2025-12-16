package com.aw3.platform.dto.project;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Project Profile Response DTO
 * 
 * GET /api/project/profile/me response format
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectProfileResponse {

    private UUID userId;
    private String walletAddress;
    private String role;
    private ProfileInfo profile;
    private ReputationInfo reputation;
    private SubscriptionInfo subscription;
    private Instant createdAt;
    private Instant lastLogin;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileInfo {
        private String projectName;
        private String displayName;
        private String bio;
        private String logo;
        private String website;
        private Map<String, String> socialLinks;
        private String category;
        private Boolean verified;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReputationInfo {
        private BigDecimal score;
        private String tier;
        private Integer totalCampaigns;
        private Integer completedCampaigns;
        private BigDecimal averageCreatorRating;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscriptionInfo {
        private String tier;
        private String status;
    }
}

