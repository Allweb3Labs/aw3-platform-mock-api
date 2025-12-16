package com.aw3.platform.dto.creator;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Social Account Verification Request DTO
 * 
 * POST /api/creator/profile/social-verification request format
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialVerificationRequest {

    @NotBlank(message = "Platform is required")
    private String platform; // twitter, discord, instagram, tiktok, youtube

    @NotBlank(message = "Handle is required")
    private String handle;

    @NotBlank(message = "Verification method is required")
    private String verificationMethod; // oauth, signature

    private String oauthCode; // Required if verificationMethod = oauth

    private String signature; // Required if verificationMethod = signature
}

