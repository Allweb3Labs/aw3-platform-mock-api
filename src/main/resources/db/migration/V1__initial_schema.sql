-- AW3 Platform Initial Database Schema

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(36) PRIMARY KEY,
    wallet_address VARCHAR(42) UNIQUE NOT NULL,
    did_identifier VARCHAR(100) UNIQUE NOT NULL,
    user_role VARCHAR(20) NOT NULL,
    email VARCHAR(255),
    username VARCHAR(100),
    display_name VARCHAR(255),
    bio TEXT,
    avatar_url VARCHAR(500),
    profile_data JSON,
    reputation_score DECIMAL(10,2) DEFAULT 0.00,
    cumulative_spend DECIMAL(20,2) DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_verified BOOLEAN DEFAULT FALSE,
    kyc_verified BOOLEAN DEFAULT FALSE,
    social_verifications JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    INDEX idx_users_wallet (wallet_address),
    INDEX idx_users_did (did_identifier),
    INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Campaigns Table
CREATE TABLE IF NOT EXISTS campaigns (
    campaign_id VARCHAR(36) PRIMARY KEY,
    project_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    kpi_targets JSON,
    budget_amount DECIMAL(20,2) NOT NULL,
    budget_token VARCHAR(20) NOT NULL DEFAULT 'USDC',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    deadline TIMESTAMP NOT NULL,
    contract_address VARCHAR(42),
    chain_id INT,
    escrow_balance DECIMAL(20,2) DEFAULT 0.00,
    service_fee DECIMAL(20,2),
    oracle_fee DECIMAL(20,2),
    total_fee DECIMAL(20,2),
    fee_estimate_id VARCHAR(100),
    aw3_token_payment_enabled BOOLEAN DEFAULT FALSE,
    required_reputation DECIMAL(10,2),
    number_of_creators INT,
    complexity VARCHAR(20),
    campaign_metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    start_date TIMESTAMP,
    completion_date TIMESTAMP,
    INDEX idx_campaigns_project (project_id),
    INDEX idx_campaigns_status (status, created_at),
    INDEX idx_campaigns_deadline (deadline),
    FOREIGN KEY (project_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Applications Table
CREATE TABLE IF NOT EXISTS applications (
    application_id VARCHAR(36) PRIMARY KEY,
    campaign_id VARCHAR(36) NOT NULL,
    creator_id VARCHAR(36) NOT NULL,
    proposed_rate DECIMAL(20,2),
    proposal TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    portfolio_links JSON,
    relevant_experience TEXT,
    estimated_completion_days INT,
    match_score DECIMAL(5,4),
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    reviewed_by VARCHAR(36),
    rejection_reason TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_applications_creator (creator_id),
    INDEX idx_applications_campaign (campaign_id),
    INDEX idx_applications_lookup (campaign_id, creator_id, status),
    INDEX idx_applications_status (status, applied_at),
    UNIQUE KEY unique_application (campaign_id, creator_id),
    FOREIGN KEY (campaign_id) REFERENCES campaigns(campaign_id),
    FOREIGN KEY (creator_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Deliverables Table
CREATE TABLE IF NOT EXISTS deliverables (
    deliverable_id VARCHAR(36) PRIMARY KEY,
    application_id VARCHAR(36) NOT NULL,
    campaign_id VARCHAR(36) NOT NULL,
    creator_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    content_url VARCHAR(1000),
    proof_urls JSON,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    milestone_id VARCHAR(100),
    payment_amount DECIMAL(20,2),
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP,
    verified_by VARCHAR(36),
    verification_notes TEXT,
    rejection_reason TEXT,
    kpi_results JSON,
    oracle_verification_id VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_deliverables_application (application_id),
    INDEX idx_deliverables_creator (creator_id),
    INDEX idx_deliverables_campaign (campaign_id),
    INDEX idx_deliverables_status (status, submitted_at),
    FOREIGN KEY (application_id) REFERENCES applications(application_id),
    FOREIGN KEY (campaign_id) REFERENCES campaigns(campaign_id),
    FOREIGN KEY (creator_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Reputation Records Table
CREATE TABLE IF NOT EXISTS reputation_records (
    record_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    campaign_id VARCHAR(36),
    record_type VARCHAR(50) NOT NULL,
    score_delta DECIMAL(10,2) NOT NULL,
    previous_score DECIMAL(10,2),
    new_score DECIMAL(10,2),
    performance_metrics JSON,
    reason TEXT,
    adjusted_by VARCHAR(36),
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verification_hash VARCHAR(100),
    INDEX idx_reputation_user (user_id, recorded_at),
    INDEX idx_reputation_campaign (campaign_id),
    INDEX idx_reputation_type (record_type, recorded_at),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (campaign_id) REFERENCES campaigns(campaign_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Platform Fees Table
CREATE TABLE IF NOT EXISTS platform_fees (
    fee_id VARCHAR(36) PRIMARY KEY,
    campaign_id VARCHAR(36),
    project_id VARCHAR(36) NOT NULL,
    creator_id VARCHAR(36),
    fee_type VARCHAR(50) NOT NULL,
    base_amount DECIMAL(20,2) NOT NULL,
    discount_amount DECIMAL(20,2) DEFAULT 0.00,
    final_amount DECIMAL(20,2) NOT NULL,
    calculation_snapshot JSON,
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    transaction_hash VARCHAR(100),
    charged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP,
    INDEX idx_platform_fees_charged_at (charged_at),
    INDEX idx_platform_fees_project (project_id, charged_at),
    INDEX idx_platform_fees_type (fee_type, charged_at),
    INDEX idx_platform_fees_campaign (campaign_id),
    FOREIGN KEY (campaign_id) REFERENCES campaigns(campaign_id),
    FOREIGN KEY (project_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- CVPI Scores Table
CREATE TABLE IF NOT EXISTS cvpi_scores (
    cvpi_id VARCHAR(36) PRIMARY KEY,
    creator_id VARCHAR(36) NOT NULL,
    campaign_id VARCHAR(36) NOT NULL,
    cvpi_score DECIMAL(10,4) NOT NULL,
    total_cost DECIMAL(20,2) NOT NULL,
    verified_impact_score DECIMAL(20,2) NOT NULL,
    kpi_achievements JSON,
    category VARCHAR(50),
    platform_average_cvpi DECIMAL(10,4),
    percentile_rank DECIMAL(5,2),
    verification_id VARCHAR(100),
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_cvpi_creator (creator_id, calculated_at),
    INDEX idx_cvpi_campaign (campaign_id),
    INDEX idx_cvpi_score (cvpi_score),
    UNIQUE KEY unique_campaign_creator_cvpi (campaign_id, creator_id),
    FOREIGN KEY (creator_id) REFERENCES users(user_id),
    FOREIGN KEY (campaign_id) REFERENCES campaigns(campaign_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

