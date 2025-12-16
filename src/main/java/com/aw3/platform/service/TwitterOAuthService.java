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

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Service for Twitter OAuth 2.0 authentication with PKCE
 * Implements OAuth 2.0 Authorization Code Flow with PKCE
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TwitterOAuthService {
    
    private final OAuthConfig oauthConfig;
    private final OAuthStateRepository stateRepository;
    private final OAuthAccountRepository accountRepository;
    private final JwtService jwtService;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    
    private static final String PROVIDER = "twitter";
    
    /**
     * Initiate Twitter OAuth flow
     * Generates PKCE challenge and redirects to Twitter
     */
    @Transactional
    public OAuthInitiateResponse initiateOAuth(String ipAddress, String userAgent) {
        log.info("Initiating Twitter OAuth flow from IP: {}", ipAddress);
        
        // Generate PKCE parameters
        String codeVerifier = OAuthCryptoUtil.generateCodeVerifier();
        String codeChallenge = OAuthCryptoUtil.generateCodeChallenge(codeVerifier);
        String state = OAuthCryptoUtil.generateState();
        
        // Store state and verifier
        OAuthState oauthState = OAuthState.builder()
                .state(state)
                .provider(PROVIDER)
                .codeVerifier(codeVerifier)
                .codeChallenge(codeChallenge)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        
        stateRepository.save(oauthState);
        
        // Build authorization URL
        String authUrl = String.format(
                "%s?response_type=code&client_id=%s&redirect_uri=%s&scope=%s&state=%s&code_challenge=%s&code_challenge_method=S256",
                oauthConfig.getTwitter().getAuthorizationUrl(),
                oauthConfig.getTwitter().getClientId(),
                oauthConfig.getTwitter().getRedirectUri(),
                oauthConfig.getTwitter().getScope().replace(" ", "%20"),
                state,
                codeChallenge
        );
        
        log.info("Generated Twitter OAuth URL with state: {}", state);
        
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
        log.info("Handling Twitter OAuth callback with state: {}", request.getState());
        
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
        TokenResponse tokenResponse = exchangeCodeForToken(
                request.getCode(),
                oauthState.getCodeVerifier()
        );
        
        // Get user information
        TwitterUser twitterUser = getUserInfo(tokenResponse.getAccessToken());
        
        // Generate anonymous ID
        String anonymousId = OAuthCryptoUtil.generateAnonymousId(
                PROVIDER,
                twitterUser.getId(),
                oauthConfig.getAnonymousIdSalt()
        );
        
        // Store or update OAuth account
        OAuthAccount account = accountRepository
                .findByProviderAndProviderUserId(PROVIDER, twitterUser.getId())
                .orElse(new OAuthAccount());
        
        account.setAnonymousId(anonymousId);
        account.setProvider(PROVIDER);
        account.setProviderUserId(twitterUser.getId());
        account.setUsername(twitterUser.getUsername());
        account.setDisplayName(twitterUser.getName());
        account.setAccessToken(tokenResponse.getAccessToken());
        account.setRefreshToken(tokenResponse.getRefreshToken());
        account.setScopes(oauthConfig.getTwitter().getScope());
        account.setVerified(twitterUser.getVerified());
        
        if (tokenResponse.getExpiresIn() != null) {
            account.setTokenExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn()));
        }
        
        accountRepository.save(account);
        
        // Generate JWT for session
        String jwt = jwtService.generateToken(anonymousId);
        
        log.info("Successfully authenticated Twitter user with anonymous ID: {}", anonymousId);
        
        return OAuthAuthenticationResponse.builder()
                .anonymousId(anonymousId)
                .provider(PROVIDER)
                .username(twitterUser.getUsername())
                .displayName(twitterUser.getName())
                .avatarUrl(twitterUser.getProfileImageUrl())
                .accessToken(jwt)
                .verified(twitterUser.getVerified())
                .expiresAt(account.getTokenExpiresAt() != null ? account.getTokenExpiresAt().toString() : null)
                .build();
    }
    
    /**
     * Exchange authorization code for access token
     */
    private TokenResponse exchangeCodeForToken(String code, String codeVerifier) {
        log.info("Exchanging authorization code for access token");
        
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", oauthConfig.getTwitter().getClientId());
        formData.add("redirect_uri", oauthConfig.getTwitter().getRedirectUri());
        formData.add("code_verifier", codeVerifier);
        
        // Create Basic Auth header
        String auth = oauthConfig.getTwitter().getClientId() + ":" + 
                      oauthConfig.getTwitter().getClientSecret();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        
        try {
            String response = webClientBuilder.build()
                    .post()
                    .uri(oauthConfig.getTwitter().getTokenUrl())
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            JsonNode json = objectMapper.readTree(response);
            
            return TokenResponse.builder()
                    .accessToken(json.get("access_token").asText())
                    .refreshToken(json.has("refresh_token") ? json.get("refresh_token").asText() : null)
                    .expiresIn(json.has("expires_in") ? json.get("expires_in").asLong() : null)
                    .tokenType(json.get("token_type").asText())
                    .build();
        } catch (Exception e) {
            log.error("Error exchanging code for token", e);
            throw new BadRequestException("Failed to exchange authorization code: " + e.getMessage());
        }
    }
    
    /**
     * Get user information from Twitter API
     */
    private TwitterUser getUserInfo(String accessToken) {
        log.info("Fetching Twitter user information");
        
        try {
            String response = webClientBuilder.build()
                    .get()
                    .uri(oauthConfig.getTwitter().getUserInfoUrl())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            JsonNode json = objectMapper.readTree(response);
            JsonNode data = json.get("data");
            
            return TwitterUser.builder()
                    .id(data.get("id").asText())
                    .username(data.get("username").asText())
                    .name(data.get("name").asText())
                    .verified(data.has("verified") && data.get("verified").asBoolean())
                    .profileImageUrl(data.has("profile_image_url") ? data.get("profile_image_url").asText() : null)
                    .build();
        } catch (Exception e) {
            log.error("Error fetching user info", e);
            throw new BadRequestException("Failed to fetch user information: " + e.getMessage());
        }
    }
    
    /**
     * Refresh access token using refresh token
     */
    @Transactional
    public void refreshToken(String anonymousId) {
        log.info("Refreshing Twitter token for anonymous ID: {}", anonymousId);
        
        OAuthAccount account = accountRepository.findByAnonymousId(anonymousId)
                .orElseThrow(() -> new BadRequestException("OAuth account not found"));
        
        if (account.getRefreshToken() == null) {
            throw new BadRequestException("No refresh token available");
        }
        
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("refresh_token", account.getRefreshToken());
        formData.add("client_id", oauthConfig.getTwitter().getClientId());
        
        String auth = oauthConfig.getTwitter().getClientId() + ":" + 
                      oauthConfig.getTwitter().getClientSecret();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        
        try {
            String response = webClientBuilder.build()
                    .post()
                    .uri(oauthConfig.getTwitter().getTokenUrl())
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            JsonNode json = objectMapper.readTree(response);
            
            account.setAccessToken(json.get("access_token").asText());
            if (json.has("refresh_token")) {
                account.setRefreshToken(json.get("refresh_token").asText());
            }
            if (json.has("expires_in")) {
                account.setTokenExpiresAt(LocalDateTime.now().plusSeconds(json.get("expires_in").asLong()));
            }
            
            accountRepository.save(account);
            
            log.info("Successfully refreshed Twitter token for anonymous ID: {}", anonymousId);
        } catch (Exception e) {
            log.error("Error refreshing token", e);
            throw new BadRequestException("Failed to refresh token: " + e.getMessage());
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
    }
    
    @lombok.Data
    @lombok.Builder
    private static class TwitterUser {
        private String id;
        private String username;
        private String name;
        private Boolean verified;
        private String profileImageUrl;
    }
}

