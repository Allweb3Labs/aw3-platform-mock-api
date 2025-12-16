package com.aw3.platform.dto.project;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Project Profile Update Request DTO
 * 
 * PUT /api/project/profile/me request format
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectProfileUpdateRequest {

    @Size(max = 100, message = "Project name cannot exceed 100 characters")
    private String projectName;

    @Size(max = 100, message = "Display name cannot exceed 100 characters")
    private String displayName;

    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    private String bio;

    private String logo;

    private String website;

    private Map<String, String> socialLinks;

    private String category; // DeFi, NFT, Gaming, Infrastructure, DAO, Other
}

