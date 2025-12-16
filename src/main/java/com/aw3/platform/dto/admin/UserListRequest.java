package com.aw3.platform.dto.admin;

import com.aw3.platform.entity.enums.UserRole;
import com.aw3.platform.entity.enums.UserStatus;
import lombok.Data;

/**
 * Request DTO for listing users with filters
 */
@Data
public class UserListRequest {
    private UserRole role;           // Filter by role: CREATOR, PROJECT, VALIDATOR, ADMIN
    private UserStatus status;       // Filter by status: active, banned, suspended
    private Integer page = 1;        // Page number (default: 1)
    private Integer limit = 50;      // Results per page (default: 50, max: 200)
}

