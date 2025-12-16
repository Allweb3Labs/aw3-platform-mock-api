package com.aw3.platform.dto.admin;

import com.aw3.platform.entity.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for updating user status
 */
@Data
public class UpdateUserStatusRequest {
    
    @NotNull(message = "Status is required")
    private UserStatus status;  // active, banned, suspended
    
    @NotNull(message = "Reason is required")
    @Size(min = 10, message = "Reason must be at least 10 characters")
    private String reason;      // Justification for status change
    
    private Integer duration;   // Suspension duration in days (null for permanent ban)
    
    private Boolean notifyUser = true;  // Send notification to user (default: true)
}

