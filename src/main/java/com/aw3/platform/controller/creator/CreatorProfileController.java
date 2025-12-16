package com.aw3.platform.controller.creator;

import com.aw3.platform.dto.common.ApiResponse;
import com.aw3.platform.dto.creator.*;
import com.aw3.platform.security.CustomUserDetails;
import com.aw3.platform.service.CreatorProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Creator Portal - Profile Management Endpoints
 * 
 * Business Rules:
 * - Creators can only view/update their own profiles
 * - Social verification expires after 90 days
 * - Each social account can only be verified by one creator
 */
@RestController
@RequestMapping("/creator/profile")
@PreAuthorize("hasRole('CREATOR')")
@RequiredArgsConstructor
@Slf4j
public class CreatorProfileController {

    private final CreatorProfileService creatorProfileService;

    /**
     * GET /api/creator/profile/me
     * Retrieve the authenticated creator's profile information
     */
    @GetMapping("/me")
    public ApiResponse<CreatorProfileResponse> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Creator {} fetching profile", userDetails.getUserId());
        
        CreatorProfileResponse profile = creatorProfileService.getCreatorProfile(userDetails.getUserId());
        
        return ApiResponse.success(profile);
    }

    /**
     * PUT /api/creator/profile/me
     * Update the authenticated creator's profile information
     */
    @PutMapping("/me")
    public ApiResponse<CreatorProfileResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreatorProfileUpdateRequest request) {
        
        log.info("Creator {} updating profile", userDetails.getUserId());
        
        CreatorProfileResponse profile = creatorProfileService.updateCreatorProfile(
                userDetails.getUserId(), 
                request
        );
        
        return ApiResponse.success(profile);
    }

    /**
     * POST /api/creator/profile/social-verification
     * Verify ownership of a social media account
     * 
     * Supports: Twitter, Discord, Instagram, TikTok, YouTube
     */
    @PostMapping("/social-verification")
    public ApiResponse<SocialVerificationResponse> verifySocialAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SocialVerificationRequest request) {
        
        log.info("Creator {} verifying social account: {} on {}", 
                userDetails.getUserId(), request.getHandle(), request.getPlatform());
        
        SocialVerificationResponse result = creatorProfileService.verifySocialAccount(
                userDetails.getUserId(), 
                request
        );
        
        return ApiResponse.success(result);
    }
}

