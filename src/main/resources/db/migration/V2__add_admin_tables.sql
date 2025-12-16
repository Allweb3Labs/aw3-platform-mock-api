-- Add Audit Logs table
CREATE TABLE IF NOT EXISTS audit_logs (
    audit_log_id VARCHAR(36) PRIMARY KEY,
    admin_user_id VARCHAR(36) NOT NULL,
    admin_wallet_address VARCHAR(255) NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    target_entity_type VARCHAR(50),
    target_entity_id VARCHAR(36),
    request_data TEXT,
    response_data TEXT,
    ip_address VARCHAR(50) NOT NULL,
    reason TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    INDEX idx_admin_user_id (admin_user_id),
    INDEX idx_action_type (action_type),
    INDEX idx_target_entity_id (target_entity_id),
    INDEX idx_timestamp (timestamp)
);

-- Add Disputes table
CREATE TABLE IF NOT EXISTS disputes (
    dispute_id VARCHAR(36) PRIMARY KEY,
    campaign_id VARCHAR(36) NOT NULL,
    initiator_user_id VARCHAR(36) NOT NULL,
    respondent_user_id VARCHAR(36) NOT NULL,
    status VARCHAR(50) NOT NULL,
    reason TEXT NOT NULL,
    evidence TEXT,
    resolution VARCHAR(50),
    resolution_notes TEXT,
    resolved_by_admin_id VARCHAR(36),
    resolved_at TIMESTAMP,
    escalated BOOLEAN DEFAULT FALSE,
    dao_proposal_id VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_campaign_id (campaign_id),
    INDEX idx_status (status),
    INDEX idx_escalated (escalated),
    INDEX idx_created_at (created_at)
);

-- Add indexes for better query performance on existing tables
CREATE INDEX IF NOT EXISTS idx_users_role ON users(user_role);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_campaigns_status ON campaigns(status);
CREATE INDEX IF NOT EXISTS idx_applications_status ON applications(status);

