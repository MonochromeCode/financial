CREATE TABLE IF NOT EXISTS financial_family (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(80) NOT NULL,
    owner_user_id BIGINT NOT NULL,
    allow_view_others BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_owner (tenant_id, owner_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS financial_member (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    family_id BIGINT NOT NULL,
    user_id BIGINT NULL,
    display_name VARCHAR(40) NOT NULL,
    role VARCHAR(12) NOT NULL DEFAULT 'member',
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user (tenant_id, user_id),
    KEY idx_family (tenant_id, family_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS financial_category (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    family_id BIGINT NOT NULL,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(40) NOT NULL,
    icon VARCHAR(40) NOT NULL,
    color VARCHAR(7) NOT NULL,
    description VARCHAR(400) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_category (tenant_id, family_id, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS financial_account (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    family_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    category_code VARCHAR(20) NOT NULL,
    name VARCHAR(80) NOT NULL,
    template_code VARCHAR(30) NULL,
    balance DECIMAL(18,2) NULL,
    currency CHAR(3) NOT NULL DEFAULT 'CNY',
    fund_bucket VARCHAR(16) NULL,
    insurance_type VARCHAR(20) NULL,
    source_type VARCHAR(12) NOT NULL DEFAULT 'manual',
    data_date DATE NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    remark VARCHAR(500) NOT NULL DEFAULT '',
    version BIGINT NOT NULL DEFAULT 1,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    creator BIGINT NOT NULL,
    updater BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_accounts (tenant_id, family_id, deleted, member_id, category_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS financial_update_batch (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    family_id BIGINT NOT NULL,
    operator_user_id BIGINT NOT NULL,
    request_id VARCHAR(64) NOT NULL,
    request_hash CHAR(64) NOT NULL,
    update_date DATE NOT NULL,
    total_assets DECIMAL(24,2) NOT NULL DEFAULT 0,
    total_liabilities DECIMAL(24,2) NOT NULL DEFAULT 0,
    net_assets DECIMAL(24,2) NOT NULL DEFAULT 0,
    source VARCHAR(12) NOT NULL DEFAULT 'manual',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_request (tenant_id, family_id, request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS financial_update_record (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    family_id BIGINT NOT NULL,
    batch_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    category_code VARCHAR(20) NOT NULL,
    account_name VARCHAR(80) NOT NULL,
    previous_balance DECIMAL(18,2) NULL,
    current_balance DECIMAL(18,2) NULL,
    change_amount DECIMAL(19,2) NOT NULL,
    operation VARCHAR(12) NOT NULL,
    source_type VARCHAR(12) NOT NULL DEFAULT 'manual',
    operator_user_id BIGINT NOT NULL,
    data_date DATE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_records (tenant_id, family_id, member_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS financial_snapshot (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    family_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    snapshot_date DATE NOT NULL,
    liquid_amount DECIMAL(24,2) NOT NULL,
    fixed_amount DECIMAL(24,2) NOT NULL,
    investment_amount DECIMAL(24,2) NOT NULL,
    receivable_amount DECIMAL(24,2) NOT NULL,
    liability_amount DECIMAL(24,2) NOT NULL,
    total_assets DECIMAL(24,2) NOT NULL,
    net_assets DECIMAL(24,2) NOT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_day (tenant_id, family_id, member_id, snapshot_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
