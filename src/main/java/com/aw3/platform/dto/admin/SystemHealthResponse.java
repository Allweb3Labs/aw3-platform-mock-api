package com.aw3.platform.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Response DTO for system health check
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemHealthResponse {
    private String status;
    private Map<String, ServiceHealth> services;
    private Double uptime;
    private Instant lastIncident;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceHealth {
        private String status;
        private Integer latency;      // in milliseconds
        private Integer connections;   // for database
        private Long lastBlock;        // for blockchain
        private Integer activeNodes;   // for oracle
        private String memory;         // for redis
    }
}

