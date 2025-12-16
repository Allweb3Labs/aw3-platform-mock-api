-- OAuth Accounts Table
-- Stores OAuth authentication data for Twitter, Discord, and Telegram
-- Personal data stored only in backend database, never on-chain
CREATE TABLE oauth_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    anonymous_id VARCHAR(64) NOT NULL UNIQUE COMMENT 'SHA-256 hash for anonymous mapping',
    provider VARCHAR(20) NOT NULL COMMENT 'OAuth provider: twitter, discord, telegram',
    provider_user_id VARCHAR(255) NOT NULL COMMENT 'User ID from OAuth provider',
    username VARCHAR(255) COMMENT 'Username from provider',
    display_name VARCHAR(255) COMMENT 'Display name (Discord global_name)',
    email VARCHAR(255) COMMENT 'Email if provided by OAuth scope',
    avatar_url VARCHAR(512) COMMENT 'User avatar URL',
    access_token TEXT NOT NULL COMMENT 'Encrypted OAuth access token',
    refresh_token TEXT COMMENT 'Encrypted OAuth refresh token',
    token_expires_at DATETIME COMMENT 'Token expiration timestamp',
    scopes VARCHAR(512) COMMENT 'OAuth scopes granted',
    user_id BIGINT COMMENT 'Link to main user entity',
    metadata TEXT COMMENT 'Additional metadata in JSON format',
    verified BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Account verification status',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_anonymous_id (anonymous_id),
    INDEX idx_provider_user (provider, provider_user_id),
    INDEX idx_user_id (user_id),
    
    UNIQUE KEY uk_provider_user (provider, provider_user_id),
    
    CONSTRAINT fk_oauth_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- OAuth State Table
-- Stores OAuth state parameters and PKCE verifiers for CSRF protection
CREATE TABLE oauth_states (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    state VARCHAR(64) NOT NULL UNIQUE COMMENT 'State parameter for CSRF protection',
    provider VARCHAR(20) NOT NULL COMMENT 'OAuth provider',
    code_verifier VARCHAR(128) COMMENT 'PKCE code verifier for Twitter',
    code_challenge VARCHAR(128) COMMENT 'PKCE code challenge',
    wallet_address VARCHAR(42) COMMENT 'User wallet address if available',
    ip_address VARCHAR(45) COMMENT 'IP address for security tracking',
    user_agent VARCHAR(512) COMMENT 'User agent for security tracking',
    expires_at DATETIME NOT NULL COMMENT 'State expiration (10 minutes)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether state has been used',
    
    INDEX idx_state (state),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- OAuth Audit Log Table
-- Tracks OAuth authentication events for security monitoring
CREATE TABLE oauth_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    anonymous_id VARCHAR(64) COMMENT 'Anonymous user ID',
    provider VARCHAR(20) NOT NULL COMMENT 'OAuth provider',
    event_type VARCHAR(50) NOT NULL COMMENT 'Event type: auth_initiated, auth_success, auth_failed, token_refresh, logout',
    ip_address VARCHAR(45) COMMENT 'IP address',
    user_agent VARCHAR(512) COMMENT 'User agent',
    error_code VARCHAR(100) COMMENT 'Error code if failed',
    error_message TEXT COMMENT 'Error message if failed',
    metadata TEXT COMMENT 'Additional event metadata in JSON',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_anonymous_id (anonymous_id),
    INDEX idx_provider (provider),
    INDEX idx_event_type (event_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

