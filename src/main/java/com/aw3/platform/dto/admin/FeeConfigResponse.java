package com.aw3.platform.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for fee configuration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeConfigResponse {
    private String version;
    private Instant effectiveDate;
    private List<ConfigItem> configs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfigItem {
        private String key;
        private Double value;
        private String description;
        private Instant lastUpdated;
    }
}

