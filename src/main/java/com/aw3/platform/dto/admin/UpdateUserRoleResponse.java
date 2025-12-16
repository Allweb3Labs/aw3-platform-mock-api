package com.aw3.platform.dto.admin;

import com.aw3.platform.entity.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for user role update
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRoleResponse {
    private String userId;
    private UserRole previousRole;
    private UserRole newRole;
    private Instant effectiveAt;
    private String auditLogId;
}

