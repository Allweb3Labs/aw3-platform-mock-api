package com.aw3.platform.controller.admin;

import com.aw3.platform.dto.admin.*;
import com.aw3.platform.dto.common.ApiResponse;
import com.aw3.platform.security.CustomUserDetails;
import com.aw3.platform.service.AdminUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Admin User Management Controller
 * 
 * Endpoints for managing platform users
 * Base path: /api/admin/users
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * GET /api/admin/users
     * List all users with filtering
     */
    @GetMapping
    public ApiResponse<UserListResponse> listUsers(
            @ModelAttribute UserListRequest request
    ) {
        log.info("Admin listing users with filters: role={}, status={}", request.getRole(), request.getStatus());
        UserListResponse response = adminUserService.listUsers(request);
        return ApiResponse.success(response);
    }

    /**
     * GET /api/admin/users/{id}
     * Get detailed user information
     */
    @GetMapping("/{id}")
    public ApiResponse<UserDetailResponse> getUserDetails(@PathVariable String id) {
        log.info("Admin fetching user details: {}", id);
        UserDetailResponse response = adminUserService.getUserDetails(id);
        return ApiResponse.success(response);
    }

    /**
     * PUT /api/admin/users/{id}/status
     * Update user status (ban, suspend, activate)
     */
    @PutMapping("/{id}/status")
    public ApiResponse<UpdateUserStatusResponse> updateUserStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails adminUser,
            HttpServletRequest httpRequest
    ) {
        log.info("Admin updating user status: {}, new status: {}", id, request.getStatus());
        
        UpdateUserStatusResponse response = adminUserService.updateUserStatus(
            id,
            request,
            adminUser.getUserId().toString(),
            adminUser.getWalletAddress(),
            httpRequest.getRemoteAddr()
        );
        
        return ApiResponse.success(response);
    }

    /**
     * PUT /api/admin/users/{id}/role
     * Change user role (requires super admin privileges)
     */
    @PutMapping("/{id}/role")
    public ApiResponse<UpdateUserRoleResponse> updateUserRole(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRoleRequest request,
            @AuthenticationPrincipal CustomUserDetails adminUser,
            HttpServletRequest httpRequest
    ) {
        log.info("Admin updating user role: {}, new role: {}", id, request.getNewRole());
        
        UpdateUserRoleResponse response = adminUserService.updateUserRole(
            id,
            request,
            adminUser.getUserId().toString(),
            adminUser.getWalletAddress(),
            httpRequest.getRemoteAddr()
        );
        
        return ApiResponse.success(response);
    }
}

