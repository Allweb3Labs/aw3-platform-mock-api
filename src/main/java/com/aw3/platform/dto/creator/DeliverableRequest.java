package com.aw3.platform.dto.creator;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Deliverable Submission Request DTO
 * 
 * POST /api/creator/deliverables request format
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliverableRequest {

    @NotNull(message = "Campaign ID is required")
    private UUID campaignId;

    @NotNull(message = "Application ID is required")
    private UUID applicationId;

    @NotBlank(message = "Type is required")
    private String type; // video, thread, post, article, livestream

    @NotBlank(message = "Content URL is required")
    private String contentUrl;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Platform is required")
    private String platform; // twitter, youtube, instagram, tiktok, etc.

    @NotNull(message = "Published date is required")
    private Instant publishedAt;

    private InitialMetrics initialMetrics;

    private List<ProofItem> proof;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InitialMetrics {
        private Integer views;
        private Integer likes;
        private Integer comments;
        private Integer shares;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProofItem {
        private String type;
        private String url;
        private String description;
    }
}

