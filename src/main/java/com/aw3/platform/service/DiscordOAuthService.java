package com.aw3.platform.service;

import com.aw3.platform.config.OAuthConfig;
import com.aw3.platform.dto.auth.OAuthAuthenticationResponse;
import com.aw3.platform.dto.auth.OAuthCallbackRequest;
import com.aw3.platform.dto.auth.OAuthInitiateResponse;
import com.aw3.platform.entity.OAuthAccount;
import com.aw3.platform.entity.OAuthState;
import com.aw3.platform.exception.BadRequestException;
import com.aw3.platform.repository.OAuthAccountRepository;
import com.aw3.platform.repository.OAuthStateRepository;
import com.aw3.platform.security.JwtService;
import com.aw3.platform.util.OAuthCryptoUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;

/**
 * Service for Discord OAuth 2.0 authentication
 * Implements OAuth 2.0 Authorization Code Flow
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DiscordOAuthService {
    
    private final OAuthConfig oauthConfig;
    private final OAuthStateRepository stateRepository;
    private final OAuthAccountRepository accountRepository;
    private final JwtService jwtService;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    
    private static final String PROVIDER = "discord";
    
    /**
     * Initiate Discord OAuth flow
     */
    @Transactional
    public OAuthInitiateResponse initiateOAuth(String ipAddress, String userAgent) {
        log.info("Initiating Discord OAuth flow from IP: {}", ipAddress);
        
        // Generate state parameter
        String state = OAuthCryptoUtil.generateState();
        
        // Store state
        OAuthState oauthState = OAuthState.builder()
                .state(state)
                .provider(PROVIDER)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        
        stateRepository.save(oauthState);
        
        // Build authorization URL
        String authUrl = String.format(
                "%s?client_id=%s&redirect_uri=%s&response_type=code&scope=%s&state=%s&prompt=consent",
                oauthConfig.getDiscord().getAuthorizationUrl(),
                oauthConfig.getDiscord().getClientId(),
                oauthConfig.getDiscord().getRedirectUri(),
                oauthConfig.getDiscord().getScope().replace(" ", "%20"),
                state
        );
        
        log.info("Generated Discord OAuth URL with state: {}", state);
        
        return OAuthInitiateResponse.builder()
                .authorizationUrl(authUrl)
                .state(state)
                .provider(PROVIDER)
                .expiresAt(oauthState.getExpiresAt().toString())
                .build();
    }
    
    /**
     * Handle OAuth callback and exchange code for tokens
     */
    @Transactional
    public OAuthAuthenticationResponse handleCallback(OAuthCallbackRequest request) {
        log.info("Handling Discord OAuth callback with state: {}", request.getState());
        
        // Check for error
        if (request.getError() != null) {
            log.error("Discord OAuth error: {} - {}", request.getError(), request.getErrorDescription());
            throw new BadRequestException("OAuth authorization failed: " + request.getError());
        }
        
        // Validate state
        OAuthState oauthState = stateRepository.findByStateAndProvider(request.getState(), PROVIDER)
                .orElseThrow(() -> new BadRequestException("Invalid state parameter"));
        
        if (oauthState.isExpired()) {
            throw new BadRequestException("State parameter has expired");
        }
        
        if (oauthState.getUsed()) {
            throw new BadRequestException("State parameter has already been used");
        }
        
        // Mark state as used
        oauthState.setUsed(true);
        stateRepository.save(oauthState);
        
        // Exchange code for tokens
        TokenResponse tokenResponse = exchangeCodeForToken(request.getCode());
        
        // Get user information
        DiscordUser discordUser = getUserInfo(
                tokenResponse.getAccessToken(),
                tokenResponse.getTokenType()
        );
        
        // Generate anonymous ID
        String anonymousId = OAuthCryptoUtil.generateAnonymousId(
                PROVIDER,
                discordUser.getId(),
                oauthConfig.getAnonymousIdSalt()
        );
        
        // Build avatar URL
        String avatarUrl = buildAvatarUrl(discordUser);
        
        // Store or update OAuth account
        OAuthAccount account = accountRepository
                .findByProviderAndProviderUserId(PROVIDER, discordUser.getId())
                .orElse(new OAuthAccount());
        
        account.setAnonymousId(anonymousId);
        account.setProvider(PROVIDER);
        account.setProviderUserId(discordUser.getId());
        account.setUsername(discordUser.getUsername());
        account.setDisplayName(discordUser.getGlobalName());
        account.setEmail(discordUser.getEmail());
        account.setAvatarUrl(avatarUrl);
        account.setAccessToken(tokenResponse.getAccessToken());
        account.setRefreshToken(tokenResponse.getRefreshToken());
        account.setScopes(tokenResponse.getScope());
        account.setVerified(discordUser.getVerified() != null && discordUser.getVerified());
        account.setTokenExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn()));
        
        accountRepository.save(account);
        
        // Generate JWT for session
        String jwt = jwtService.generateToken(anonymousId);
        
        log.info("Successfully authenticated Discord user with anonymous ID: {}", anonymousId);
        
        return OAuthAuthenticationResponse.builder()
                .anonymousId(anonymousId)
                .provider(PROVIDER)
                .username(discordUser.getUsername())
                .displayName(discordUser.getGlobalName())
                .avatarUrl(avatarUrl)
                .accessToken(jwt)
                .verified(discordUser.getVerified() != null && discordUser.getVerified())
                .expiresAt(account.getTokenExpiresAt().toString())
                .build();
    }
    
    /**
     * Exchange authorization code for access token
     */
    private TokenResponse exchangeCodeForToken(String code) {
        log.info("Exchanging authorization code for access token");
        
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", oauthConfig.getDiscord().getClientId());
        formData.add("client_secret", oauthConfig.getDiscord().getClientSecret());
        formData.add("grant_type", "authorization_code");
        formData.add("code", code);
        formData.add("redirect_uri", oauthConfig.getDiscord().getRedirectUri());
        
        try {
            String response = webClientBuilder.build()
                    .post()
                    .uri(oauthConfig.getDiscord().getTokenUrl())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            JsonNode json = objectMapper.readTree(response);
            
            return TokenResponse.builder()
                    .accessToken(json.get("access_token").asText())
                    .refreshToken(json.get("refresh_token").asText())
                    .expiresIn(json.get("expires_in").asLong())
                    .tokenType(json.get("token_type").asText())
                    .scope(json.get("scope").asText())
                    .build();
        } catch (Exception e) {
            log.error("Error exchanging code for token", e);
            throw new BadRequestException("Failed to exchange authorization code: " + e.getMessage());
        }
    }
    
    /**
     * Get user information from Discord API
     */
    private DiscordUser getUserInfo(String accessToken, String tokenType) {
        log.info("Fetching Discord user information");
        
        try {
            String response = webClientBuilder.build()
                    .get()
                    .uri(oauthConfig.getDiscord().getUserInfoUrl())
                    .header(HttpHeaders.AUTHORIZATION, tokenType + " " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            JsonNode json = objectMapper.readTree(response);
            
            return DiscordUser.builder()
                    .id(json.get("id").asText())
                    .username(json.get("username").asText())
                    .discriminator(json.has("discriminator") ? json.get("discriminator").asText() : "0")
                    .globalName(json.has("global_name") && !json.get("global_name").isNull() 
                            ? json.get("global_name").asText() 
                            : json.get("username").asText())
                    .email(json.has("email") && !json.get("email").isNull() ? json.get("email").asText() : null)
                    .verified(json.has("verified") ? json.get("verified").asBoolean() : null)
                    .avatar(json.has("avatar") && !json.get("avatar").isNull() ? json.get("avatar").asText() : null)
                    .build();
        } catch (Exception e) {
            log.error("Error fetching user info", e);
            throw new BadRequestException("Failed to fetch user information: " + e.getMessage());
        }
    }
    
    /**
     * Build Discord avatar URL
     */
    private String buildAvatarUrl(DiscordUser user) {
        if (user.getAvatar() == null) {
            // Default avatar
            int defaultAvatarIndex = Integer.parseInt(user.getDiscriminator()) % 5;
            return String.format("https://cdn.discordapp.com/embed/avatars/%d.png", defaultAvatarIndex);
        }
        
        // Check if animated
        String format = user.getAvatar().startsWith("a_") ? "gif" : "png";
        return String.format("https://cdn.discordapp.com/avatars/%s/%s.%s", 
                user.getId(), user.getAvatar(), format);
    }
    
    /**
     * Refresh access token using refresh token
     */
    @Transactional
    public void refreshToken(String anonymousId) {
        log.info("Refreshing Discord token for anonymous ID: {}", anonymousId);
        
        OAuthAccount account = accountRepository.findByAnonymousId(anonymousId)
                .orElseThrow(() -> new BadRequestException("OAuth account not found"));
        
        if (account.getRefreshToken() == null) {
            throw new BadRequestException("No refresh token available");
        }
        
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", oauthConfig.getDiscord().getClientId());
        formData.add("client_secret", oauthConfig.getDiscord().getClientSecret());
        formData.add("grant_type", "refresh_token");
        formData.add("refresh_token", account.getRefreshToken());
        
        try {
            String response = webClientBuilder.build()
                    .post()
                    .uri(oauthConfig.getDiscord().getTokenUrl())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            JsonNode json = objectMapper.readTree(response);
            
            account.setAccessToken(json.get("access_token").asText());
            account.setRefreshToken(json.get("refresh_token").asText());
            account.setTokenExpiresAt(LocalDateTime.now().plusSeconds(json.get("expires_in").asLong()));
            account.setScopes(json.get("scope").asText());
            
            accountRepository.save(account);
            
            log.info("Successfully refreshed Discord token for anonymous ID: {}", anonymousId);
        } catch (Exception e) {
            log.error("Error refreshing token", e);
            throw new BadRequestException("Failed to refresh token: " + e.getMessage());
        }
    }
    
    /**
     * Revoke OAuth token
     */
    @Transactional
    public void revokeToken(String anonymousId) {
        log.info("Revoking Discord token for anonymous ID: {}", anonymousId);
        
        OAuthAccount account = accountRepository.findByAnonymousId(anonymousId)
                .orElseThrow(() -> new BadRequestException("OAuth account not found"));
        
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", oauthConfig.getDiscord().getClientId());
        formData.add("client_secret", oauthConfig.getDiscord().getClientSecret());
        formData.add("token", account.getAccessToken());
        
        try {
            webClientBuilder.build()
                    .post()
                    .uri(oauthConfig.getDiscord().getTokenRevocationUrl())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            // Delete account from database
            accountRepository.delete(account);
            
            log.info("Successfully revoked Discord token for anonymous ID: {}", anonymousId);
        } catch (Exception e) {
            log.error("Error revoking token", e);
            // Still delete from database even if revocation fails
            accountRepository.delete(account);
        }
    }
    
    // Inner classes for response mapping
    @lombok.Data
    @lombok.Builder
    private static class TokenResponse {
        private String accessToken;
        private String refreshToken;
        private Long expiresIn;
        private String tokenType;
        private String scope;
    }
    
    @lombok.Data
    @lombok.Builder
    private static class DiscordUser {
        private String id;
        private String username;
        private String discriminator;
        private String globalName;
        private String email;
        private Boolean verified;
        private String avatar;
    }
}

