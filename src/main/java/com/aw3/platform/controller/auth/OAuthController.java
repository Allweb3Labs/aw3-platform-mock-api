package com.aw3.platform.controller.auth;

import com.aw3.platform.dto.auth.*;
import com.aw3.platform.service.DiscordOAuthService;
import com.aw3.platform.service.TelegramOAuthService;
import com.aw3.platform.service.TwitterOAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * OAuth 2.0 Authentication Controller
 * Handles Twitter, Discord, and Telegram OAuth flows
 * Implements privacy-protected anonymous mapping
 */
@RestController
@RequestMapping("/api/auth/oauth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "OAuth Authentication", description = "OAuth 2.0 authentication endpoints for Twitter, Discord, and Telegram")
public class OAuthController {
    
    private final TwitterOAuthService twitterOAuthService;
    private final DiscordOAuthService discordOAuthService;
    private final TelegramOAuthService telegramOAuthService;
    
    // ========== Twitter OAuth Endpoints ==========
    
    @GetMapping("/twitter/initiate")
    @Operation(
        summary = "Initiate Twitter OAuth flow",
        description = "Generates authorization URL with PKCE challenge for Twitter OAuth 2.0"
    )
    public ResponseEntity<OAuthInitiateResponse> initiateTwitterOAuth(HttpServletRequest request) {
        log.info("Initiating Twitter OAuth flow");
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        
        OAuthInitiateResponse response = twitterOAuthService.initiateOAuth(ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/twitter/callback")
    @Operation(
        summary = "Handle Twitter OAuth callback",
        description = "Exchanges authorization code for tokens and creates user session"
    )
    public ResponseEntity<OAuthAuthenticationResponse> handleTwitterCallback(
            @Valid @RequestBody OAuthCallbackRequest request) {
        log.info("Handling Twitter OAuth callback");
        
        OAuthAuthenticationResponse response = twitterOAuthService.handleCallback(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/twitter/refresh")
    @Operation(
        summary = "Refresh Twitter OAuth token",
        description = "Refreshes expired Twitter access token using refresh token"
    )
    public ResponseEntity<Void> refreshTwitterToken(
            @Parameter(description = "Anonymous user ID") @RequestParam String anonymousId) {
        log.info("Refreshing Twitter token for anonymous ID: {}", anonymousId);
        
        twitterOAuthService.refreshToken(anonymousId);
        return ResponseEntity.ok().build();
    }
    
    // ========== Discord OAuth Endpoints ==========
    
    @GetMapping("/discord/initiate")
    @Operation(
        summary = "Initiate Discord OAuth flow",
        description = "Generates authorization URL for Discord OAuth 2.0"
    )
    public ResponseEntity<OAuthInitiateResponse> initiateDiscordOAuth(HttpServletRequest request) {
        log.info("Initiating Discord OAuth flow");
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        
        OAuthInitiateResponse response = discordOAuthService.initiateOAuth(ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/discord/callback")
    @Operation(
        summary = "Handle Discord OAuth callback",
        description = "Exchanges authorization code for tokens and creates user session"
    )
    public ResponseEntity<OAuthAuthenticationResponse> handleDiscordCallback(
            @Valid @RequestBody OAuthCallbackRequest request) {
        log.info("Handling Discord OAuth callback");
        
        OAuthAuthenticationResponse response = discordOAuthService.handleCallback(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/discord/refresh")
    @Operation(
        summary = "Refresh Discord OAuth token",
        description = "Refreshes expired Discord access token using refresh token"
    )
    public ResponseEntity<Void> refreshDiscordToken(
            @Parameter(description = "Anonymous user ID") @RequestParam String anonymousId) {
        log.info("Refreshing Discord token for anonymous ID: {}", anonymousId);
        
        discordOAuthService.refreshToken(anonymousId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/discord/revoke")
    @Operation(
        summary = "Revoke Discord OAuth token",
        description = "Revokes Discord access token and deletes account connection"
    )
    public ResponseEntity<Void> revokeDiscordToken(
            @Parameter(description = "Anonymous user ID") @RequestParam String anonymousId) {
        log.info("Revoking Discord token for anonymous ID: {}", anonymousId);
        
        discordOAuthService.revokeToken(anonymousId);
        return ResponseEntity.ok().build();
    }
    
    // ========== Telegram OAuth Endpoints ==========
    
    @PostMapping("/telegram/callback")
    @Operation(
        summary = "Handle Telegram authentication",
        description = "Verifies Telegram login widget data and creates user session"
    )
    public ResponseEntity<OAuthAuthenticationResponse> handleTelegramAuth(
            @Valid @RequestBody TelegramAuthRequest request) {
        log.info("Handling Telegram authentication for user ID: {}", request.getId());
        
        OAuthAuthenticationResponse response = telegramOAuthService.authenticate(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/telegram/config")
    @Operation(
        summary = "Get Telegram widget configuration",
        description = "Returns configuration for Telegram login widget"
    )
    public ResponseEntity<Map<String, String>> getTelegramConfig() {
        log.info("Fetching Telegram widget configuration");
        
        Map<String, String> config = telegramOAuthService.getWidgetConfig();
        return ResponseEntity.ok(config);
    }
    
    @DeleteMapping("/telegram/disconnect")
    @Operation(
        summary = "Disconnect Telegram account",
        description = "Removes Telegram account connection"
    )
    public ResponseEntity<Void> disconnectTelegram(
            @Parameter(description = "Anonymous user ID") @RequestParam String anonymousId) {
        log.info("Disconnecting Telegram account for anonymous ID: {}", anonymousId);
        
        telegramOAuthService.disconnect(anonymousId);
        return ResponseEntity.ok().build();
    }
    
    // ========== Utility Methods ==========
    
    /**
     * Get client IP address from request
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}

