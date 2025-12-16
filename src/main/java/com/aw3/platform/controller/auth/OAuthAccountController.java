package com.aw3.platform.controller.auth;

import com.aw3.platform.dto.auth.OAuthAccountInfoResponse;
import com.aw3.platform.entity.OAuthAccount;
import com.aw3.platform.exception.NotFoundException;
import com.aw3.platform.repository.OAuthAccountRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OAuth Account Management Controller
 * Provides endpoints for managing OAuth accounts
 */
@RestController
@RequestMapping("/api/auth/oauth/accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "OAuth Account Management", description = "Manage OAuth account connections")
@SecurityRequirement(name = "bearerAuth")
public class OAuthAccountController {
    
    private final OAuthAccountRepository accountRepository;
    
    @GetMapping("/{anonymousId}")
    @Operation(
        summary = "Get OAuth account information",
        description = "Retrieves OAuth account details by anonymous ID"
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OAuthAccountInfoResponse> getAccount(
            @Parameter(description = "Anonymous user ID") @PathVariable String anonymousId) {
        log.info("Fetching OAuth account for anonymous ID: {}", anonymousId);
        
        OAuthAccount account = accountRepository.findByAnonymousId(anonymousId)
                .orElseThrow(() -> new NotFoundException("OAuth account not found"));
        
        OAuthAccountInfoResponse response = mapToResponse(account);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/by-provider/{provider}")
    @Operation(
        summary = "Get verified accounts by provider",
        description = "Retrieves all verified OAuth accounts for a specific provider"
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OAuthAccountInfoResponse>> getAccountsByProvider(
            @Parameter(description = "OAuth provider (twitter, discord, telegram)") 
            @PathVariable String provider) {
        log.info("Fetching verified accounts for provider: {}", provider);
        
        List<OAuthAccount> accounts = accountRepository.findByProviderAndVerifiedTrue(provider);
        List<OAuthAccountInfoResponse> responses = accounts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/user/{userId}")
    @Operation(
        summary = "Get all OAuth accounts for a user",
        description = "Retrieves all OAuth account connections for a user"
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OAuthAccountInfoResponse>> getUserAccounts(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        log.info("Fetching OAuth accounts for user ID: {}", userId);
        
        List<OAuthAccount> accounts = accountRepository.findByUserId(userId);
        List<OAuthAccountInfoResponse> responses = accounts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    @DeleteMapping("/{anonymousId}")
    @Operation(
        summary = "Delete OAuth account",
        description = "Removes OAuth account connection"
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAccount(
            @Parameter(description = "Anonymous user ID") @PathVariable String anonymousId) {
        log.info("Deleting OAuth account for anonymous ID: {}", anonymousId);
        
        OAuthAccount account = accountRepository.findByAnonymousId(anonymousId)
                .orElseThrow(() -> new NotFoundException("OAuth account not found"));
        
        accountRepository.delete(account);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Map entity to response DTO
     */
    private OAuthAccountInfoResponse mapToResponse(OAuthAccount account) {
        List<String> scopes = null;
        if (account.getScopes() != null) {
            scopes = Arrays.asList(account.getScopes().split(" "));
        }
        
        return OAuthAccountInfoResponse.builder()
                .anonymousId(account.getAnonymousId())
                .provider(account.getProvider())
                .username(account.getUsername())
                .displayName(account.getDisplayName())
                .email(account.getEmail())
                .avatarUrl(account.getAvatarUrl())
                .scopes(scopes)
                .verified(account.getVerified())
                .tokenExpiresAt(account.getTokenExpiresAt())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}

