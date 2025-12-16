package com.aw3.platform.service;

import com.aw3.platform.config.OAuthConfig;
import com.aw3.platform.dto.auth.OAuthAuthenticationResponse;
import com.aw3.platform.dto.auth.TelegramAuthRequest;
import com.aw3.platform.entity.OAuthAccount;
import com.aw3.platform.exception.BadRequestException;
import com.aw3.platform.repository.OAuthAccountRepository;
import com.aw3.platform.security.JwtService;
import com.aw3.platform.util.OAuthCryptoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

/**
 * Service for Telegram OAuth authentication
 * Uses Telegram Login Widget authentication method
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramOAuthService {
    
    private final OAuthConfig oauthConfig;
    private final OAuthAccountRepository accountRepository;
    private final JwtService jwtService;
    
    private static final String PROVIDER = "telegram";
    
    /**
     * Authenticate user with Telegram login widget data
     */
    @Transactional
    public OAuthAuthenticationResponse authenticate(TelegramAuthRequest request) {
        log.info("Authenticating Telegram user with ID: {}", request.getId());
        
        // Verify auth data hash
        if (!verifyTelegramAuth(request)) {
            log.error("Invalid Telegram authentication hash");
            throw new BadRequestException("Invalid authentication data");
        }
        
        // Check auth date expiration (24 hours by default)
        long currentTime = System.currentTimeMillis() / 1000;
        long timeDiff = currentTime - request.getAuthDate();
        
        if (timeDiff > oauthConfig.getTelegram().getAuthExpirySeconds()) {
            log.error("Telegram authentication data expired. Time diff: {} seconds", timeDiff);
            throw new BadRequestException("Authentication data has expired");
        }
        
        // Generate anonymous ID
        String anonymousId = OAuthCryptoUtil.generateAnonymousId(
                PROVIDER,
                request.getId(),
                oauthConfig.getAnonymousIdSalt()
        );
        
        // Build display name
        String displayName = buildDisplayName(request);
        
        // Store or update OAuth account
        OAuthAccount account = accountRepository
                .findByProviderAndProviderUserId(PROVIDER, request.getId())
                .orElse(new OAuthAccount());
        
        account.setAnonymousId(anonymousId);
        account.setProvider(PROVIDER);
        account.setProviderUserId(request.getId());
        account.setUsername(request.getUsername());
        account.setDisplayName(displayName);
        account.setAvatarUrl(request.getPhotoUrl());
        // Telegram doesn't provide OAuth tokens via login widget
        account.setAccessToken("telegram_login_widget");
        account.setVerified(true); // Telegram authentication is inherently verified
        
        accountRepository.save(account);
        
        // Generate JWT for session
        String jwt = jwtService.generateToken(anonymousId);
        
        log.info("Successfully authenticated Telegram user with anonymous ID: {}", anonymousId);
        
        return OAuthAuthenticationResponse.builder()
                .anonymousId(anonymousId)
                .provider(PROVIDER)
                .username(request.getUsername())
                .displayName(displayName)
                .avatarUrl(request.getPhotoUrl())
                .accessToken(jwt)
                .verified(true)
                .build();
    }
    
    /**
     * Verify Telegram authentication data
     * Implements Telegram's hash verification algorithm
     */
    private boolean verifyTelegramAuth(TelegramAuthRequest request) {
        log.info("Verifying Telegram authentication data");
        
        // Build check string from auth data (sorted alphabetically)
        Map<String, String> authData = new TreeMap<>();
        authData.put("id", request.getId());
        authData.put("first_name", request.getFirstName());
        authData.put("auth_date", String.valueOf(request.getAuthDate()));
        
        if (request.getLastName() != null && !request.getLastName().isEmpty()) {
            authData.put("last_name", request.getLastName());
        }
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            authData.put("username", request.getUsername());
        }
        if (request.getPhotoUrl() != null && !request.getPhotoUrl().isEmpty()) {
            authData.put("photo_url", request.getPhotoUrl());
        }
        
        // Build check string
        StringBuilder checkString = new StringBuilder();
        authData.forEach((key, value) -> {
            if (checkString.length() > 0) {
                checkString.append("\n");
            }
            checkString.append(key).append("=").append(value);
        });
        
        // Verify hash
        boolean isValid = OAuthCryptoUtil.verifyTelegramAuth(
                checkString.toString(),
                request.getHash(),
                oauthConfig.getTelegram().getBotToken()
        );
        
        if (!isValid) {
            log.error("Telegram authentication hash verification failed");
        }
        
        return isValid;
    }
    
    /**
     * Build display name from Telegram auth data
     */
    private String buildDisplayName(TelegramAuthRequest request) {
        if (request.getLastName() != null && !request.getLastName().isEmpty()) {
            return request.getFirstName() + " " + request.getLastName();
        }
        return request.getFirstName();
    }
    
    /**
     * Get Telegram widget configuration for frontend
     */
    public Map<String, String> getWidgetConfig() {
        return Map.of(
                "botUsername", oauthConfig.getTelegram().getBotUsername(),
                "authUrl", "/api/auth/telegram/callback"
        );
    }
    
    /**
     * Disconnect Telegram account
     */
    @Transactional
    public void disconnect(String anonymousId) {
        log.info("Disconnecting Telegram account for anonymous ID: {}", anonymousId);
        
        OAuthAccount account = accountRepository.findByAnonymousId(anonymousId)
                .orElseThrow(() -> new BadRequestException("OAuth account not found"));
        
        if (!PROVIDER.equals(account.getProvider())) {
            throw new BadRequestException("Account is not a Telegram account");
        }
        
        accountRepository.delete(account);
        
        log.info("Successfully disconnected Telegram account");
    }
}

