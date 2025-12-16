package com.aw3.platform.controller.auth;

import com.aw3.platform.dto.common.ApiResponse;
import com.aw3.platform.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Authentication Controller
 * 
 * Handles wallet-based authentication using DID (Decentralized Identifiers)
 * 
 * Flow:
 * 1. Client requests nonce for wallet address
 * 2. Client signs message with wallet
 * 3. Server verifies signature and issues JWT
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/wallet-connect
     * Initiate wallet connection - generate nonce for signing
     */
    @PostMapping("/wallet-connect")
    public ApiResponse<WalletConnectResponse> walletConnect(
            @Valid @RequestBody WalletConnectRequest request) {
        
        log.info("Wallet connect request for address: {}", request.getWalletAddress());
        
        WalletConnectResponse response = authService.initiateWalletConnect(request);
        
        return ApiResponse.success(response);
    }

    /**
     * POST /api/auth/verify-signature
     * Verify wallet signature and issue JWT
     */
    @PostMapping("/verify-signature")
    public ApiResponse<AuthResponse> verifySignature(
            @Valid @RequestBody VerifySignatureRequest request) {
        
        log.info("Verifying signature for wallet: {}", request.getWalletAddress());
        
        AuthResponse response = authService.verifySignature(request);
        
        return ApiResponse.success(response);
    }

    /**
     * POST /api/auth/refresh
     * Refresh JWT token
     */
    @PostMapping("/refresh")
    public ApiResponse<TokenRefreshResponse> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request) {
        
        log.info("Token refresh request");
        
        TokenRefreshResponse response = authService.refreshToken(request);
        
        return ApiResponse.success(response);
    }

    /**
     * POST /api/auth/logout
     * Logout and invalidate tokens
     */
    @PostMapping("/logout")
    public ApiResponse<LogoutResponse> logout(
            @RequestHeader("Authorization") String authHeader) {
        
        log.info("Logout request");
        
        LogoutResponse response = authService.logout(authHeader);
        
        return ApiResponse.success(response);
    }

    /**
     * POST /api/auth/register
     * Register new user with role selection
     */
    @PostMapping("/register")
    public ApiResponse<RegistrationResponse> register(
            @Valid @RequestBody RegistrationRequest request) {
        
        log.info("Registration request for wallet: {} as {}", 
                request.getWalletAddress(), request.getRole());
        
        RegistrationResponse response = authService.register(request);
        
        return ApiResponse.success(response);
    }

    /**
     * GET /api/auth/nonce/{walletAddress}
     * Get nonce for wallet (alternative to wallet-connect)
     */
    @GetMapping("/nonce/{walletAddress}")
    public ApiResponse<NonceResponse> getNonce(@PathVariable String walletAddress) {
        log.info("Nonce request for wallet: {}", walletAddress);
        
        NonceResponse response = authService.getNonce(walletAddress);
        
        return ApiResponse.success(response);
    }

    // DTOs

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class WalletConnectRequest {
        private String walletAddress;
        private String chainId;
        private String walletType; // MetaMask, WalletConnect, etc.
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class WalletConnectResponse {
        private String walletAddress;
        private String nonce;
        private String message; // Message to sign
        private Long expiresAt;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class VerifySignatureRequest {
        private String walletAddress;
        private String signature;
        private String nonce;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private Long expiresIn;
        private UserInfo user;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserInfo {
        private UUID userId;
        private String walletAddress;
        private String role;
        private String displayName;
        private Boolean profileComplete;
        private String subscriptionTier;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TokenRefreshRequest {
        private String refreshToken;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TokenRefreshResponse {
        private String accessToken;
        private String refreshToken;
        private Long expiresIn;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class LogoutResponse {
        private Boolean success;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RegistrationRequest {
        private String walletAddress;
        private String signature;
        private String nonce;
        private String role; // CREATOR, PROJECT, VALIDATOR
        private String displayName;
        private String email;
        private Boolean termsAccepted;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RegistrationResponse {
        private UUID userId;
        private String walletAddress;
        private String role;
        private String accessToken;
        private String refreshToken;
        private Long expiresIn;
        private Boolean profileComplete;
        private List<String> nextSteps;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class NonceResponse {
        private String walletAddress;
        private String nonce;
        private String message;
        private Long expiresAt;
    }
}
