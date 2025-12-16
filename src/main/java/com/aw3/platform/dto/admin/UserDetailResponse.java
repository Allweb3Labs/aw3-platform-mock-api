package com.aw3.platform.dto.admin;

import com.aw3.platform.entity.enums.UserRole;
import com.aw3.platform.entity.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for detailed user information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponse {
    private String userId;
    private String walletAddress;
    private UserRole role;
    private UserStatus status;
    private ProfileInfo profile;
    private ReputationInfo reputation;
    private StatisticsInfo statistics;
    private Instant joinedAt;
    private Instant lastActive;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileInfo {
        private String displayName;
        private String bio;
        private List<SocialAccount> socialAccounts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SocialAccount {
        private String platform;
        private String handle;
        private Integer followers;
        private Boolean verified;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReputationInfo {
        private Double score;
        private String tier;
        private Integer totalCampaigns;
        private Double completionRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatisticsInfo {
        private Double totalEarnings;
        private Double avgCVPI;
        private Integer spcNftsEarned;
    }
}

