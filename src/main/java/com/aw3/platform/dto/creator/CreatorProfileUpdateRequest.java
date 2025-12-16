package com.aw3.platform.dto.creator;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Creator Profile Update Request DTO
 * 
 * PUT /api/creator/profile/me request format
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatorProfileUpdateRequest {

    @Size(max = 100, message = "Display name cannot exceed 100 characters")
    private String displayName;

    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    private String bio;

    private String avatar;

    @Size(max = 100, message = "Location cannot exceed 100 characters")
    private String location;

    private String timezone;

    private PreferencesInfo preferences;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreferencesInfo {
        private Boolean emailNotifications;
        private List<String> campaignCategories;
        private BigDecimal minBudget;
    }
}

