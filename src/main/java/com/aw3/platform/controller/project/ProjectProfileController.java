package com.aw3.platform.controller.project;

import com.aw3.platform.dto.common.ApiResponse;
import com.aw3.platform.dto.project.ProjectProfileResponse;
import com.aw3.platform.dto.project.ProjectProfileUpdateRequest;
import com.aw3.platform.security.CustomUserDetails;
import com.aw3.platform.service.ProjectProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Project Portal - Profile Management Endpoints
 * 
 * Business Rules:
 * - Projects can only view/update their own profiles
 * - Verified status can only be changed by admin
 * - Logo must be a valid image URL (HTTPS required)
 * - Profile updates do not affect existing campaigns
 */
@RestController
@RequestMapping("/project/profile")
@PreAuthorize("hasRole('PROJECT')")
@RequiredArgsConstructor
@Slf4j
public class ProjectProfileController {

    private final ProjectProfileService projectProfileService;

    /**
     * GET /api/project/profile/me
     * Retrieve the authenticated project owner's profile information
     */
    @GetMapping("/me")
    public ApiResponse<ProjectProfileResponse> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Project {} fetching profile", userDetails.getUserId());
        
        ProjectProfileResponse profile = projectProfileService.getProjectProfile(userDetails.getUserId());
        
        return ApiResponse.success(profile);
    }

    /**
     * PUT /api/project/profile/me
     * Update the authenticated project owner's profile information
     */
    @PutMapping("/me")
    public ApiResponse<ProjectProfileResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProjectProfileUpdateRequest request) {
        
        log.info("Project {} updating profile", userDetails.getUserId());
        
        ProjectProfileResponse profile = projectProfileService.updateProjectProfile(
                userDetails.getUserId(), 
                request
        );
        
        return ApiResponse.success(profile);
    }
}

