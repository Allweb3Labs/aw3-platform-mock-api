package com.aw3.platform.dto.admin;

import com.aw3.platform.entity.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for user status update
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStatusResponse {
    private String userId;
    private UserStatus previousStatus;
    private UserStatus newStatus;
    private String reason;
    private Instant effectiveAt;
    private Instant expiresAt;
    private String auditLogId;
}

