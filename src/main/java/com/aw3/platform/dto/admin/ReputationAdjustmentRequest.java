package com.aw3.platform.dto.admin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for manual reputation adjustment
 */
@Data
public class ReputationAdjustmentRequest {
    
    @NotNull(message = "User ID is required")
    private String userId;
    
    @NotNull(message = "Adjustment type is required")
    private String adjustmentType;  // "add" or "subtract"
    
    @NotNull(message = "Amount is required")
    private Double amount;
    
    @NotNull(message = "Reason is required")
    @Size(min = 10, message = "Reason must be at least 10 characters")
    private String reason;
    
    @NotNull(message = "Category is required")
    private String category;  // e.g., "platform_error_compensation", "violation_penalty"
}

