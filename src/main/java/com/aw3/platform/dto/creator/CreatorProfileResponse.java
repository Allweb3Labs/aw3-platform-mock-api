package com.aw3.platform.dto.creator;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Creator Profile Response DTO
 * 
 * GET /api/creator/profile/me response format
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreatorProfileResponse {

    private UUID userId;
    private String walletAddress;
    private String role;
    private ProfileInfo profile;
    private List<SocialAccountInfo> socialAccounts;
    private ReputationInfo reputation;
    private SubscriptionInfo subscription;
    private Instant createdAt;
    private Instant lastLoginAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileInfo {
        private String displayName;
        private String bio;
        private String avatar;
        private String location;
        private String timezone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SocialAccountInfo {
        private String platform;
        private String handle;
        private Boolean verified;
        private Integer followers;
        private Instant verifiedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReputationInfo {
        private BigDecimal score;
        private String tier;
        private Integer totalCampaigns;
        private BigDecimal completionRate;
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

