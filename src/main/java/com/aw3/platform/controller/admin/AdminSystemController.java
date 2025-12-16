package com.aw3.platform.controller.admin;

import com.aw3.platform.dto.common.ApiResponse;
import com.aw3.platform.security.CustomUserDetails;
import com.aw3.platform.service.AdminSystemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Admin Portal - System Control Endpoints
 * 
 * Business Rules:
 * - System-wide operations require 2FA confirmation
 * - Maintenance mode notifications sent to all users
 * - Feature flags can be rolled out gradually
 * - Cache operations logged for audit
 */
@RestController
@RequestMapping("/admin/system")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminSystemController {

    private final AdminSystemService systemService;

    /**
     * GET /api/admin/system/status
     * Get overall system status
     */
    @GetMapping("/status")
    public ApiResponse<SystemStatusResponse> getSystemStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Admin {} fetching system status", userDetails.getUserId());
        
        SystemStatusResponse status = systemService.getSystemStatus();
        
        return ApiResponse.success(status);
    }

    /**
     * PUT /api/admin/system/maintenance
     * Toggle maintenance mode
     */
    @PutMapping("/maintenance")
    public ApiResponse<MaintenanceResponse> setMaintenanceMode(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MaintenanceModeRequest request) {
        
        log.info("Admin {} setting maintenance mode: {}", userDetails.getUserId(), request.getEnabled());
        
        MaintenanceResponse response = systemService.setMaintenanceMode(
                userDetails.getUserId(), request);
        
        return ApiResponse.success(response);
    }

    /**
     * GET /api/admin/system/feature-flags
     * List all feature flags
     */
    @GetMapping("/feature-flags")
    public ApiResponse<FeatureFlagsResponse> getFeatureFlags(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Admin {} fetching feature flags", userDetails.getUserId());
        
        FeatureFlagsResponse flags = systemService.getFeatureFlags();
        
        return ApiResponse.success(flags);
    }

    /**
     * PUT /api/admin/system/feature-flags/{flagName}
     * Update a feature flag
     */
    @PutMapping("/feature-flags/{flagName}")
    public ApiResponse<FeatureFlag> updateFeatureFlag(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String flagName,
            @Valid @RequestBody FeatureFlagUpdateRequest request) {
        
        log.info("Admin {} updating feature flag: {}", userDetails.getUserId(), flagName);
        
        FeatureFlag flag = systemService.updateFeatureFlag(
                userDetails.getUserId(), flagName, request);
        
        return ApiResponse.success(flag);
    }

    /**
     * POST /api/admin/system/cache/clear
     * Clear specific cache or all caches
     */
    @PostMapping("/cache/clear")
    public ApiResponse<CacheOperationResponse> clearCache(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CacheClearRequest request) {
        
        log.info("Admin {} clearing cache: {}", userDetails.getUserId(), request.getCacheName());
        
        CacheOperationResponse response = systemService.clearCache(
                userDetails.getUserId(), request);
        
        return ApiResponse.success(response);
    }

    /**
     * GET /api/admin/system/config
     * Get system configuration
     */
    @GetMapping("/config")
    public ApiResponse<SystemConfigResponse> getSystemConfig(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Admin {} fetching system config", userDetails.getUserId());
        
        SystemConfigResponse config = systemService.getSystemConfig();
        
        return ApiResponse.success(config);
    }

    /**
     * PUT /api/admin/system/config
     * Update system configuration
     */
    @PutMapping("/config")
    public ApiResponse<SystemConfigResponse> updateSystemConfig(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SystemConfigUpdateRequest request) {
        
        log.info("Admin {} updating system config", userDetails.getUserId());
        
        SystemConfigResponse config = systemService.updateSystemConfig(
                userDetails.getUserId(), request);
        
        return ApiResponse.success(config);
    }

    /**
     * GET /api/admin/system/audit-log
     * Get system audit log
     */
    @GetMapping("/audit-log")
    public ApiResponse<AuditLogResponse> getAuditLog(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) UUID actorId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        log.info("Admin {} fetching audit log", userDetails.getUserId());
        
        AuditLogResponse auditLog = systemService.getAuditLog(
                action, actorId, startDate, endDate, page, size);
        
        return ApiResponse.success(auditLog);
    }

    /**
     * POST /api/admin/system/broadcast
     * Send broadcast message to all users
     */
    @PostMapping("/broadcast")
    public ApiResponse<BroadcastResponse> sendBroadcast(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody BroadcastRequest request) {
        
        log.info("Admin {} sending broadcast", userDetails.getUserId());
        
        BroadcastResponse response = systemService.sendBroadcast(
                userDetails.getUserId(), request);
        
        return ApiResponse.success(response);
    }

    /**
     * POST /api/admin/system/smart-contracts/deploy
     * Deploy or upgrade smart contracts (requires DAO approval)
     */
    @PostMapping("/smart-contracts/deploy")
    public ApiResponse<ContractDeploymentResponse> deployContract(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ContractDeploymentRequest request) {
        
        log.info("Admin {} deploying smart contract: {}", userDetails.getUserId(), request.getContractType());
        
        ContractDeploymentResponse response = systemService.deployContract(
                userDetails.getUserId(), request);
        
        return ApiResponse.success(response);
    }

    /**
     * GET /api/admin/system/smart-contracts
     * Get deployed smart contracts
     */
    @GetMapping("/smart-contracts")
    public ApiResponse<DeployedContractsResponse> getDeployedContracts(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Admin {} fetching deployed contracts", userDetails.getUserId());
        
        DeployedContractsResponse contracts = systemService.getDeployedContracts();
        
        return ApiResponse.success(contracts);
    }

    // DTOs

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SystemStatusResponse {
        private String overallHealth;
        private Boolean maintenanceMode;
        private String maintenanceMessage;
        private Instant maintenanceEndTime;
        private List<ServiceHealth> services;
        private DatabaseStatus database;
        private CacheStatus cache;
        private BlockchainStatus blockchain;
        private Instant lastUpdated;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ServiceHealth {
        private String name;
        private String status; // HEALTHY, DEGRADED, DOWN
        private BigDecimal uptime;
        private String version;
        private Instant lastCheck;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DatabaseStatus {
        private String status;
        private Long connectionPoolActive;
        private Long connectionPoolIdle;
        private BigDecimal queryLatency;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CacheStatus {
        private String status;
        private BigDecimal hitRate;
        private Long memoryUsed;
        private Long keys;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BlockchainStatus {
        private String network;
        private String status;
        private Long currentBlock;
        private BigDecimal gasPrice;
        private List<ContractStatus> contracts;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ContractStatus {
        private String name;
        private String address;
        private String status;
        private BigDecimal balance;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MaintenanceModeRequest {
        private Boolean enabled;
        private String message;
        private Integer durationMinutes;
        private Boolean notifyUsers;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MaintenanceResponse {
        private Boolean maintenanceMode;
        private String message;
        private Instant startTime;
        private Instant endTime;
        private UUID enabledBy;
        private Integer usersNotified;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FeatureFlagsResponse {
        private List<FeatureFlag> flags;
        private Instant lastUpdated;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FeatureFlag {
        private String name;
        private String description;
        private Boolean enabled;
        private String rolloutStrategy; // ALL, PERCENTAGE, USER_LIST, BETA
        private Integer rolloutPercentage;
        private List<UUID> allowedUsers;
        private Instant updatedAt;
        private UUID updatedBy;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FeatureFlagUpdateRequest {
        private Boolean enabled;
        private String rolloutStrategy;
        private Integer rolloutPercentage;
        private List<UUID> allowedUsers;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CacheClearRequest {
        private String cacheName; // null for all caches
        private String pattern; // Optional key pattern
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CacheOperationResponse {
        private String operation;
        private String cacheName;
        private Long keysAffected;
        private UUID performedBy;
        private Instant timestamp;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SystemConfigResponse {
        private RateLimitConfig rateLimits;
        private SecurityConfig security;
        private NotificationConfig notifications;
        private IntegrationConfig integrations;
        private Instant lastUpdated;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RateLimitConfig {
        private Integer defaultRequestsPerMinute;
        private Integer maxRequestsPerMinute;
        private Integer burstLimit;
        private Map<String, Integer> endpointOverrides;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SecurityConfig {
        private Boolean twoFactorRequired;
        private Integer sessionTimeoutMinutes;
        private Integer maxLoginAttempts;
        private Integer lockoutDurationMinutes;
        private List<String> allowedOrigins;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class NotificationConfig {
        private Boolean emailEnabled;
        private Boolean pushEnabled;
        private Boolean smsEnabled;
        private Boolean webhookEnabled;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class IntegrationConfig {
        private ChainlinkConfig chainlink;
        private IPFSConfig ipfs;
        private AnalyticsConfig analytics;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ChainlinkConfig {
        private String nodeUrl;
        private Boolean enabled;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class IPFSConfig {
        private String gateway;
        private Boolean enabled;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AnalyticsConfig {
        private Boolean enabled;
        private String provider;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SystemConfigUpdateRequest {
        private RateLimitConfig rateLimits;
        private SecurityConfig security;
        private NotificationConfig notifications;
        private IntegrationConfig integrations;
        private String twoFactorCode; // Required for sensitive changes
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AuditLogResponse {
        private List<AuditEntry> entries;
        private PaginationInfo pagination;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AuditEntry {
        private UUID entryId;
        private Instant timestamp;
        private String action;
        private UUID actorId;
        private String actorName;
        private String actorRole;
        private String resourceType;
        private UUID resourceId;
        private String details;
        private String ipAddress;
        private String userAgent;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PaginationInfo {
        private Long total;
        private Integer page;
        private Integer size;
        private Integer totalPages;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BroadcastRequest {
        private String title;
        private String message;
        private String type; // INFO, WARNING, CRITICAL
        private List<String> targetRoles; // null = all
        private Boolean email;
        private Boolean push;
        private Boolean inApp;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BroadcastResponse {
        private UUID broadcastId;
        private String title;
        private Integer recipientCount;
        private Instant sentAt;
        private UUID sentBy;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ContractDeploymentRequest {
        private String contractType; // ESCROW, TOKEN, STAKING, GOVERNANCE
        private String version;
        private Map<String, Object> parameters;
        private String daoProposalId; // Required
        private String twoFactorCode;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ContractDeploymentResponse {
        private UUID deploymentId;
        private String contractType;
        private String status; // PENDING, DEPLOYED, FAILED
        private String address;
        private String txHash;
        private Instant deployedAt;
        private UUID deployedBy;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DeployedContractsResponse {
        private List<DeployedContract> contracts;
        private String network;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DeployedContract {
        private String type;
        private String name;
        private String address;
        private String version;
        private String status;
        private BigDecimal balance;
        private Instant deployedAt;
        private String txHash;
    }
}
