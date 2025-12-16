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
 * Response DTO for user list
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {
    private List<UserSummary> users;
    private PaginationInfo pagination;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummary {
        private String userId;
        private String walletAddress;
        private UserRole role;
        private UserStatus status;
        private Double reputationScore;
        private Instant joinedAt;
        private Instant lastActive;
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

