package com.aw3.platform.dto.admin;

import com.aw3.platform.entity.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for changing user role
 */
@Data
public class UpdateUserRoleRequest {
    
    @NotNull(message = "New role is required")
    private UserRole newRole;
    
    @NotNull(message = "Reason is required")
    @Size(min = 10, message = "Reason must be at least 10 characters")
    private String reason;
}

