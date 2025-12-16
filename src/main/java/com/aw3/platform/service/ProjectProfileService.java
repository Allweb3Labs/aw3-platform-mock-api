package com.aw3.platform.service;

import com.aw3.platform.dto.project.ProjectProfileResponse;
import com.aw3.platform.dto.project.ProjectProfileUpdateRequest;
import com.aw3.platform.entity.User;
import com.aw3.platform.exception.NotFoundException;
import com.aw3.platform.repository.UserRepository;
import com.aw3.platform.repository.CampaignRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Service for Project Profile Management
 * 
 * Business Rules:
 * - Projects can only view/update their own profiles
 * - Verified status can only be changed by admin
 * - Logo must be a valid image URL (HTTPS required)
 * - Profile updates do not affect existing campaigns
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectProfileService {

    private final UserRepository userRepository;
    private final CampaignRepository campaignRepository;
    private final ReputationService reputationService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public ProjectProfileResponse getProjectProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Map<String, Object> profileData = parseJson(user.getProfileData());
        Map<String, String> socialLinks = extractSocialLinks(profileData);

        // Get campaign stats
        long totalCampaigns = campaignRepository.countByProjectId(userId);
        long completedCampaigns = campaignRepository.countByProjectIdAndStatus(userId, "COMPLETED");

        return ProjectProfileResponse.builder()
                .userId(user.getUserId())
                .walletAddress(user.getWalletAddress())
                .role(user.getRole().name())
                .profile(ProjectProfileResponse.ProfileInfo.builder()
                        .projectName(getStringValue(profileData, "projectName"))
                        .displayName(user.getDisplayName())
                        .bio(getStringValue(profileData, "bio"))
                        .logo(getStringValue(profileData, "logo"))
                        .website(getStringValue(profileData, "website"))
                        .socialLinks(socialLinks)
                        .category(getStringValue(profileData, "category"))
                        .verified(getBooleanValue(profileData, "verified"))
                        .build())
                .reputation(ProjectProfileResponse.ReputationInfo.builder()
                        .score(user.getReputationScore() != null ? user.getReputationScore() : BigDecimal.ZERO)
                        .tier(calculateReputationTier(user.getReputationScore()))
                        .totalCampaigns((int) totalCampaigns)
                        .completedCampaigns((int) completedCampaigns)
                        .averageCreatorRating(getAverageCreatorRating(userId))
                        .build())
                .subscription(ProjectProfileResponse.SubscriptionInfo.builder()
                        .tier(user.getSubscriptionTier())
                        .status("ACTIVE")
                        .build())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }

    @Transactional
    public ProjectProfileResponse updateProjectProfile(UUID userId, ProjectProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Map<String, Object> profileData = parseJson(user.getProfileData());

        // Update profile fields
        if (request.getProjectName() != null) {
            profileData.put("projectName", request.getProjectName());
        }
        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }
        if (request.getBio() != null) {
            profileData.put("bio", request.getBio());
        }
        if (request.getLogo() != null) {
            // Validate HTTPS URL
            if (!request.getLogo().startsWith("https://")) {
                throw new IllegalArgumentException("Logo URL must use HTTPS");
            }
            profileData.put("logo", request.getLogo());
        }
        if (request.getWebsite() != null) {
            profileData.put("website", request.getWebsite());
        }
        if (request.getSocialLinks() != null) {
            profileData.put("socialLinks", request.getSocialLinks());
        }
        if (request.getCategory() != null) {
            profileData.put("category", request.getCategory());
        }

        user.setProfileData(toJson(profileData));
        userRepository.save(user);

        log.info("Project {} profile updated", userId);

        return getProjectProfile(userId);
    }

    private Map<String, Object> parseJson(String json) {
        if (json == null || json.isEmpty()) {
            return new java.util.HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error parsing profile data JSON", e);
            return new java.util.HashMap<>();
        }
    }

    private String toJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing profile data", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> extractSocialLinks(Map<String, Object> profileData) {
        Object socialLinks = profileData.get("socialLinks");
        if (socialLinks instanceof Map) {
            return (Map<String, String>) socialLinks;
        }
        return new java.util.HashMap<>();
    }

    private String getStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }

    private Boolean getBooleanValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return false;
    }

    private String calculateReputationTier(BigDecimal score) {
        if (score == null) return "NEWCOMER";
        int scoreInt = score.intValue();
        if (scoreInt >= 90) return "DIAMOND";
        if (scoreInt >= 75) return "PLATINUM";
        if (scoreInt >= 60) return "GOLD";
        if (scoreInt >= 40) return "SILVER";
        if (scoreInt >= 20) return "BRONZE";
        return "NEWCOMER";
    }

    private BigDecimal getAverageCreatorRating(UUID projectId) {
        // TODO: Implement average creator rating calculation
        return BigDecimal.valueOf(4.5);
    }
}

