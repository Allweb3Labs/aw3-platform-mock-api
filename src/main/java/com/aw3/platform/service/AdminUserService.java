package com.aw3.platform.service;

import com.aw3.platform.dto.admin.*;
import com.aw3.platform.entity.AuditLog;
import com.aw3.platform.entity.User;
import com.aw3.platform.entity.enums.UserRole;
import com.aw3.platform.entity.enums.UserStatus;
import com.aw3.platform.exception.NotFoundException;
import com.aw3.platform.repository.AuditLogRepository;
import com.aw3.platform.repository.CampaignRepository;
import com.aw3.platform.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for admin user management operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final CampaignRepository campaignRepository;
    private final ObjectMapper objectMapper;

    /**
     * List all users with filtering
     */
    public UserListResponse listUsers(UserListRequest request) {
        Pageable pageable = PageRequest.of(
            Math.max(0, request.getPage() - 1),
            Math.min(request.getLimit(), 200)
        );

        Page<User> userPage;
        
        // Apply filters
        if (request.getRole() != null && request.getStatus() != null) {
            userPage = userRepository.findByUserRoleAndStatus(request.getRole(), request.getStatus(), pageable);
        } else if (request.getRole() != null) {
            userPage = userRepository.findByUserRole(request.getRole(), pageable);
        } else if (request.getStatus() != null) {
            userPage = userRepository.findByStatus(request.getStatus(), pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        List<UserListResponse.UserSummary> users = userPage.getContent().stream()
            .map(user -> UserListResponse.UserSummary.builder()
                .userId(user.getUserId().toString())
                .walletAddress(user.getWalletAddress())
                .role(user.getUserRole())
                .status(user.getStatus())
                .reputationScore(user.getReputationScore() != null ? user.getReputationScore().doubleValue() : 0.0)
                .joinedAt(user.getCreatedAt())
                .lastActive(user.getLastLoginAt())
                .build())
            .collect(Collectors.toList());

        UserListResponse.PaginationInfo pagination = UserListResponse.PaginationInfo.builder()
            .total(userPage.getTotalElements())
            .page(request.getPage())
            .limit(request.getLimit())
            .totalPages(userPage.getTotalPages())
            .build();

        return UserListResponse.builder()
            .users(users)
            .pagination(pagination)
            .build();
    }

    /**
     * Get detailed user information
     */
    public UserDetailResponse getUserDetails(String userId) {
        User user = userRepository.findById(java.util.UUID.fromString(userId))
            .orElseThrow(() -> new NotFoundException("User not found"));

        // Calculate statistics
        Long totalCampaigns = campaignRepository.countByCreatedBy(userId);
        
        Double reputationScore = user.getReputationScore() != null ? user.getReputationScore().doubleValue() : 0.0;
        
        return UserDetailResponse.builder()
            .userId(user.getUserId().toString())
            .walletAddress(user.getWalletAddress())
            .role(user.getUserRole())
            .status(user.getStatus())
            .profile(UserDetailResponse.ProfileInfo.builder()
                .displayName(user.getDisplayName())
                .bio(user.getBio())
                .socialAccounts(List.of()) // TODO: Implement social accounts
                .build())
            .reputation(UserDetailResponse.ReputationInfo.builder()
                .score(reputationScore)
                .tier(calculateTier(reputationScore))
                .totalCampaigns(totalCampaigns.intValue())
                .completionRate(95.0) // TODO: Calculate from actual data
                .build())
            .statistics(UserDetailResponse.StatisticsInfo.builder()
                .totalEarnings(0.0) // TODO: Calculate from payments
                .avgCVPI(0.0) // TODO: Calculate from CVPI scores
                .spcNftsEarned(0) // TODO: Count SPC NFTs
                .build())
            .joinedAt(user.getCreatedAt())
            .lastActive(user.getLastLoginAt())
            .build();
    }

    /**
     * Update user status (ban, suspend, activate)
     */
    @Transactional
    public UpdateUserStatusResponse updateUserStatus(
            String userId,
            UpdateUserStatusRequest request,
            String adminUserId,
            String adminWalletAddress,
            String ipAddress
    ) {
        User user = userRepository.findById(java.util.UUID.fromString(userId))
            .orElseThrow(() -> new NotFoundException("User not found"));

        UserStatus previousStatus = user.getStatus();
        user.setStatus(request.getStatus());
        
        Instant effectiveAt = Instant.now();
        Instant expiresAt = null;
        
        if (request.getStatus() == UserStatus.SUSPENDED && request.getDuration() != null) {
            expiresAt = effectiveAt.plusSeconds(request.getDuration() * 86400L);
        }

        userRepository.save(user);

        // Create audit log
        String auditLogId = createAuditLog(
            adminUserId,
            adminWalletAddress,
            "USER_STATUS_CHANGE",
            "USER",
            userId,
            request,
            ipAddress,
            request.getReason()
        );

        log.info("User status updated: {} from {} to {}", userId, previousStatus, request.getStatus());

        return UpdateUserStatusResponse.builder()
            .userId(userId)
            .previousStatus(previousStatus)
            .newStatus(request.getStatus())
            .reason(request.getReason())
            .effectiveAt(effectiveAt)
            .expiresAt(expiresAt)
            .auditLogId(auditLogId)
            .build();
    }

    /**
     * Change user role
     */
    @Transactional
    public UpdateUserRoleResponse updateUserRole(
            String userId,
            UpdateUserRoleRequest request,
            String adminUserId,
            String adminWalletAddress,
            String ipAddress
    ) {
        User user = userRepository.findById(java.util.UUID.fromString(userId))
            .orElseThrow(() -> new NotFoundException("User not found"));

        UserRole previousRole = user.getUserRole();
        user.setUserRole(request.getNewRole());
        userRepository.save(user);

        // Create audit log
        String auditLogId = createAuditLog(
            adminUserId,
            adminWalletAddress,
            "USER_ROLE_CHANGE",
            "USER",
            userId,
            request,
            ipAddress,
            request.getReason()
        );

        log.info("User role updated: {} from {} to {}", userId, previousRole, request.getNewRole());

        return UpdateUserRoleResponse.builder()
            .userId(userId)
            .previousRole(previousRole)
            .newRole(request.getNewRole())
            .effectiveAt(Instant.now())
            .auditLogId(auditLogId)
            .build();
    }

    private String createAuditLog(
            String adminUserId,
            String adminWalletAddress,
            String actionType,
            String entityType,
            String entityId,
            Object requestData,
            String ipAddress,
            String reason
    ) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .adminUserId(adminUserId)
                .adminWalletAddress(adminWalletAddress)
                .actionType(actionType)
                .targetEntityType(entityType)
                .targetEntityId(entityId)
                .requestData(objectMapper.writeValueAsString(requestData))
                .ipAddress(ipAddress)
                .reason(reason)
                .build();

            auditLog = auditLogRepository.save(auditLog);
            return auditLog.getAuditLogId();
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
            return "error";
        }
    }

    private String calculateTier(Double score) {
        if (score == null) return "Newcomer";
        if (score >= 900) return "S";
        if (score >= 800) return "A";
        if (score >= 700) return "B";
        if (score >= 600) return "C";
        return "Newcomer";
    }
}

