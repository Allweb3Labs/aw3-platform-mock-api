package com.aw3.platform.service;

import com.aw3.platform.dto.creator.*;
import com.aw3.platform.entity.User;
import com.aw3.platform.entity.enums.UserRole;
import com.aw3.platform.exception.BadRequestException;
import com.aw3.platform.exception.NotFoundException;
import com.aw3.platform.repository.ApplicationRepository;
import com.aw3.platform.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Creator Profile Service
 * 
 * Business Rules:
 * - Each social account can only be verified by one creator
 * - Verification expires after 90 days and must be refreshed
 * - Minimum follower requirements may apply for certain campaigns
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CreatorProfileService {

    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final ObjectMapper objectMapper;

    /**
     * Get creator profile with full details
     */
    public CreatorProfileResponse getCreatorProfile(UUID creatorId) {
        User user = userRepository.findById(creatorId)
                .orElseThrow(() -> new NotFoundException("Creator not found"));

        // Parse social verifications from JSON
        List<CreatorProfileResponse.SocialAccountInfo> socialAccounts = parseSocialVerifications(user.getSocialVerifications());

        // Get campaign statistics
        long totalCampaigns = applicationRepository.countByCreatorId(creatorId);
        long completedCampaigns = applicationRepository.countCompletedByCreatorId(creatorId);
        BigDecimal completionRate = totalCampaigns > 0 
                ? BigDecimal.valueOf(completedCampaigns * 100.0 / totalCampaigns)
                : BigDecimal.ZERO;

        // Determine reputation tier
        String tier = calculateReputationTier(user.getReputationScore());

        return CreatorProfileResponse.builder()
                .userId(user.getUserId())
                .walletAddress(user.getWalletAddress())
                .role(user.getUserRole().name())
                .profile(CreatorProfileResponse.ProfileInfo.builder()
                        .displayName(user.getDisplayName())
                        .bio(user.getBio())
                        .avatar(user.getAvatarUrl())
                        .location(extractProfileField(user.getProfileData(), "location"))
                        .timezone(extractProfileField(user.getProfileData(), "timezone"))
                        .build())
                .socialAccounts(socialAccounts)
                .reputation(CreatorProfileResponse.ReputationInfo.builder()
                        .score(user.getReputationScore())
                        .tier(tier)
                        .totalCampaigns((int) totalCampaigns)
                        .completionRate(completionRate)
                        .build())
                .subscription(CreatorProfileResponse.SubscriptionInfo.builder()
                        .tier(extractProfileField(user.getProfileData(), "subscriptionTier", "FREE"))
                        .status("active")
                        .build())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    /**
     * Update creator profile
     */
    @Transactional
    public CreatorProfileResponse updateCreatorProfile(UUID creatorId, CreatorProfileUpdateRequest request) {
        User user = userRepository.findById(creatorId)
                .orElseThrow(() -> new NotFoundException("Creator not found"));

        // Update basic fields
        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getAvatar() != null) {
            user.setAvatarUrl(request.getAvatar());
        }

        // Update profile data JSON
        try {
            Map<String, Object> profileData = user.getProfileData() != null
                    ? objectMapper.readValue(user.getProfileData(), new TypeReference<Map<String, Object>>() {})
                    : new HashMap<>();

            if (request.getLocation() != null) {
                profileData.put("location", request.getLocation());
            }
            if (request.getTimezone() != null) {
                profileData.put("timezone", request.getTimezone());
            }
            if (request.getPreferences() != null) {
                profileData.put("preferences", request.getPreferences());
            }

            user.setProfileData(objectMapper.writeValueAsString(profileData));
        } catch (JsonProcessingException e) {
            log.error("Error updating profile data", e);
            throw new BadRequestException("Error updating profile data");
        }

        userRepository.save(user);

        return getCreatorProfile(creatorId);
    }

    /**
     * Verify social account ownership
     * 
     * Supports: Twitter, Discord, Instagram, TikTok, YouTube
     */
    @Transactional
    public SocialVerificationResponse verifySocialAccount(UUID creatorId, SocialVerificationRequest request) {
        User user = userRepository.findById(creatorId)
                .orElseThrow(() -> new NotFoundException("Creator not found"));

        // Validate platform
        List<String> validPlatforms = Arrays.asList("twitter", "discord", "instagram", "tiktok", "youtube");
        if (!validPlatforms.contains(request.getPlatform().toLowerCase())) {
            throw new BadRequestException("Invalid platform: " + request.getPlatform());
        }

        // Validate verification method
        if ("oauth".equals(request.getVerificationMethod()) && request.getOauthCode() == null) {
            throw new BadRequestException("OAuth code is required for OAuth verification");
        }
        if ("signature".equals(request.getVerificationMethod()) && request.getSignature() == null) {
            throw new BadRequestException("Signature is required for signature verification");
        }

        // Check if social account is already verified by another creator
        if (isSocialAccountVerifiedByAnother(request.getPlatform(), request.getHandle(), creatorId)) {
            throw new BadRequestException("This social account is already verified by another creator");
        }

        // Perform verification (mock for MVP)
        SocialVerificationResponse.MetricsInfo metrics = fetchSocialMetrics(request.getPlatform(), request.getHandle());
        int followers = estimateFollowers(request.getPlatform());
        BigDecimal engagementRate = BigDecimal.valueOf(Math.random() * 10 + 2);

        // Store verification in user's social verifications
        try {
            List<Map<String, Object>> socialVerifications = user.getSocialVerifications() != null
                    ? objectMapper.readValue(user.getSocialVerifications(), new TypeReference<List<Map<String, Object>>>() {})
                    : new ArrayList<>();

            // Remove existing verification for this platform if any
            socialVerifications.removeIf(v -> request.getPlatform().equalsIgnoreCase((String) v.get("platform")));

            Map<String, Object> newVerification = new HashMap<>();
            newVerification.put("platform", request.getPlatform().toLowerCase());
            newVerification.put("handle", request.getHandle());
            newVerification.put("verified", true);
            newVerification.put("followers", followers);
            newVerification.put("engagementRate", engagementRate);
            newVerification.put("verifiedAt", Instant.now().toString());
            newVerification.put("metrics", metrics);

            socialVerifications.add(newVerification);
            user.setSocialVerifications(objectMapper.writeValueAsString(socialVerifications));
            userRepository.save(user);

        } catch (JsonProcessingException e) {
            log.error("Error storing social verification", e);
            throw new BadRequestException("Error storing social verification");
        }

        return SocialVerificationResponse.builder()
                .platform(request.getPlatform())
                .handle(request.getHandle())
                .verified(true)
                .followers(followers)
                .engagementRate(engagementRate)
                .verifiedAt(Instant.now())
                .metrics(metrics)
                .build();
    }

    private List<CreatorProfileResponse.SocialAccountInfo> parseSocialVerifications(String socialVerificationsJson) {
        if (socialVerificationsJson == null) {
            return new ArrayList<>();
        }

        try {
            List<Map<String, Object>> verifications = objectMapper.readValue(
                    socialVerificationsJson, 
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            return verifications.stream()
                    .map(v -> CreatorProfileResponse.SocialAccountInfo.builder()
                            .platform((String) v.get("platform"))
                            .handle((String) v.get("handle"))
                            .verified((Boolean) v.getOrDefault("verified", false))
                            .followers(v.get("followers") != null ? ((Number) v.get("followers")).intValue() : null)
                            .verifiedAt(v.get("verifiedAt") != null ? Instant.parse((String) v.get("verifiedAt")) : null)
                            .build())
                    .toList();
        } catch (JsonProcessingException e) {
            log.error("Error parsing social verifications", e);
            return new ArrayList<>();
        }
    }

    private String calculateReputationTier(BigDecimal score) {
        if (score == null) return "Newcomer";
        
        int scoreInt = score.intValue();
        if (scoreInt >= 900) return "S";
        if (scoreInt >= 800) return "A";
        if (scoreInt >= 700) return "B";
        if (scoreInt >= 600) return "C";
        return "Newcomer";
    }

    private String extractProfileField(String profileDataJson, String field) {
        return extractProfileField(profileDataJson, field, null);
    }

    private String extractProfileField(String profileDataJson, String field, String defaultValue) {
        if (profileDataJson == null) return defaultValue;
        
        try {
            Map<String, Object> profileData = objectMapper.readValue(
                    profileDataJson, 
                    new TypeReference<Map<String, Object>>() {}
            );
            return (String) profileData.getOrDefault(field, defaultValue);
        } catch (JsonProcessingException e) {
            return defaultValue;
        }
    }

    private boolean isSocialAccountVerifiedByAnother(String platform, String handle, UUID currentCreatorId) {
        // Check all users for this social verification
        // This is a simplified check - in production, use a dedicated index or table
        return false; // Simplified for MVP
    }

    private SocialVerificationResponse.MetricsInfo fetchSocialMetrics(String platform, String handle) {
        // Mock metrics for MVP
        return SocialVerificationResponse.MetricsInfo.builder()
                .posts((int) (Math.random() * 5000 + 500))
                .avgLikes((int) (Math.random() * 3000 + 200))
                .avgRetweets((int) (Math.random() * 500 + 50))
                .avgComments((int) (Math.random() * 200 + 20))
                .build();
    }

    private int estimateFollowers(String platform) {
        // Mock follower counts for MVP
        return (int) (Math.random() * 200000 + 10000);
    }
}

