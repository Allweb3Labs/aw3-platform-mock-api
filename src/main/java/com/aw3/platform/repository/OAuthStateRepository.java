package com.aw3.platform.repository;

import com.aw3.platform.entity.OAuthState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OAuthStateRepository extends JpaRepository<OAuthState, Long> {
    
    /**
     * Find OAuth state by state parameter
     */
    Optional<OAuthState> findByState(String state);
    
    /**
     * Find OAuth state by state and provider
     */
    Optional<OAuthState> findByStateAndProvider(String state, String provider);
    
    /**
     * Delete expired states
     */
    void deleteByExpiresAtBefore(LocalDateTime cutoffDate);
    
    /**
     * Delete used states
     */
    void deleteByUsedTrue();
    
    /**
     * Check if state exists and is not expired
     */
    boolean existsByStateAndUsedFalseAndExpiresAtAfter(String state, LocalDateTime now);
}

