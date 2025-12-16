package com.aw3.platform.repository;

import com.aw3.platform.entity.OAuthAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {
    
    /**
     * Find OAuth account by anonymous ID
     */
    Optional<OAuthAccount> findByAnonymousId(String anonymousId);
    
    /**
     * Find OAuth account by provider and provider user ID
     */
    Optional<OAuthAccount> findByProviderAndProviderUserId(String provider, String providerUserId);
    
    /**
     * Find all OAuth accounts for a user
     */
    List<OAuthAccount> findByUserId(Long userId);
    
    /**
     * Find OAuth account by user ID and provider
     */
    Optional<OAuthAccount> findByUserIdAndProvider(Long userId, String provider);
    
    /**
     * Check if provider account exists
     */
    boolean existsByProviderAndProviderUserId(String provider, String providerUserId);
    
    /**
     * Check if anonymous ID exists
     */
    boolean existsByAnonymousId(String anonymousId);
    
    /**
     * Find accounts with expiring tokens (within the next hour)
     */
    @Query("SELECT o FROM OAuthAccount o WHERE o.tokenExpiresAt IS NOT NULL " +
           "AND o.tokenExpiresAt < :expiryThreshold AND o.refreshToken IS NOT NULL")
    List<OAuthAccount> findAccountsWithExpiringTokens(@Param("expiryThreshold") LocalDateTime expiryThreshold);
    
    /**
     * Find verified accounts by provider
     */
    List<OAuthAccount> findByProviderAndVerifiedTrue(String provider);
    
    /**
     * Count accounts by provider
     */
    long countByProvider(String provider);
    
    /**
     * Delete unverified accounts older than specified date
     */
    void deleteByVerifiedFalseAndCreatedAtBefore(LocalDateTime cutoffDate);
}

