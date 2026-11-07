CREATE TABLE IF NOT EXISTS financial_family (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    tenant_id BIGINT NOT NULL COMMENT '租户编号',
    name VARCHAR(80) NOT NULL COMMENT '家庭账本名称',
    owner_user_id BIGINT NOT NULL COMMENT '账本所有者系统用户编号',
    allow_view_others BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否允许普通成员查看其他成员资产，不授予编辑权限',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    UNIQUE KEY uk_owner (tenant_id, owner_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭资产账本';

CREATE TABLE IF NOT EXISTS financial_member (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    tenant_id BIGINT NOT NULL COMMENT '租户编号',
    family_id BIGINT NOT NULL COMMENT '家庭账本编号',
    user_id BIGINT NULL COMMENT '关联的系统用户编号，NULL 表示未关联登录用户',
    display_name VARCHAR(40) NOT NULL COMMENT '成员显示名称',
    role VARCHAR(12) NOT NULL DEFAULT 'member' COMMENT '成员角色：owner 所有者，member 普通成员',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '显示顺序，数值越小越靠前',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user (tenant_id, user_id),
    KEY idx_family (tenant_id, family_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭资产成员';

CREATE TABLE IF NOT EXISTS financial_category (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    tenant_id BIGINT NOT NULL COMMENT '租户编号',
    family_id BIGINT NOT NULL COMMENT '家庭账本编号',
    code VARCHAR(20) NOT NULL COMMENT '分类编码：liquid 流动资金、fixed 固定资产、investment 投资理财、receivable 应收款、liability 负债',
    name VARCHAR(40) NOT NULL COMMENT '分类显示名称',
    icon VARCHAR(40) NOT NULL COMMENT '分类图标标识',
    color VARCHAR(7) NOT NULL COMMENT '分类颜色，格式为 #RRGGBB',
    description VARCHAR(400) NOT NULL COMMENT '分类说明',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '显示顺序，数值越小越靠前',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用分类',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    UNIQUE KEY uk_category (tenant_id, family_id, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭资产分类';

CREATE TABLE IF NOT EXISTS financial_account (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    tenant_id BIGINT NOT NULL COMMENT '租户编号',
    family_id BIGINT NOT NULL COMMENT '家庭账本编号',
    member_id BIGINT NOT NULL COMMENT '家庭成员编号',
    category_code VARCHAR(20) NOT NULL COMMENT '资产分类编码：liquid、fixed、investment、receivable、liability',
    name VARCHAR(80) NOT NULL COMMENT '账户名称',
    template_code VARCHAR(30) NULL COMMENT '系统账户模板编码，NULL 表示自定义账户',
    balance DECIMAL(18,2) NULL COMMENT '账户余额或估值，单位元；NULL 表示待填写，0 表示明确为零，负债按正数记录',
    currency CHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '币种代码，当前业务仅使用 CNY 人民币',
    fund_bucket VARCHAR(16) NULL COMMENT '资金用途：liquid 活钱、stable 稳健、long_term 长期、insurance 保险；NULL 表示未分配',
    insurance_type VARCHAR(20) NULL COMMENT '保险类型：意外、医疗、重疾、寿险、其他；仅保险用途账户可填写',
    source_type VARCHAR(12) NOT NULL DEFAULT 'manual' COMMENT '数据来源：manual 手动录入，import 导入',
    data_date DATE NOT NULL COMMENT '账户数据日期，可早于记账日期，不得晚于今天',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '显示顺序，数值越小越靠前',
    remark VARCHAR(500) NOT NULL DEFAULT '' COMMENT '账户备注',
    version BIGINT NOT NULL DEFAULT 1 COMMENT '乐观锁版本号，每次修改加一',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '逻辑删除标记：0 正常，1 已删除',
    creator BIGINT NOT NULL COMMENT '创建人系统用户编号',
    updater BIGINT NOT NULL COMMENT '最后修改人系统用户编号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    KEY idx_accounts (tenant_id, family_id, deleted, member_id, category_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭资产账户';

CREATE TABLE IF NOT EXISTS financial_update_batch (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    tenant_id BIGINT NOT NULL COMMENT '租户编号',
    family_id BIGINT NOT NULL COMMENT '家庭账本编号',
    operator_user_id BIGINT NOT NULL COMMENT '操作人系统用户编号',
    request_id VARCHAR(64) NOT NULL COMMENT '客户端提交请求编号，同一租户和家庭内唯一，用于幂等',
    request_hash CHAR(64) NOT NULL COMMENT '提交内容的 SHA-256 摘要，防止同一请求编号提交不同内容',
    update_date DATE NOT NULL COMMENT '记账日期，用于生成每日快照，不得早于家庭最近快照日期或晚于今天',
    total_assets DECIMAL(24,2) NOT NULL DEFAULT 0 COMMENT '总资产金额，单位元，不含负债',
    total_liabilities DECIMAL(24,2) NOT NULL DEFAULT 0 COMMENT '总负债金额，单位元，按正数记录',
    net_assets DECIMAL(24,2) NOT NULL DEFAULT 0 COMMENT '净资产金额，单位元，总资产减总负债，可为负数',
    source VARCHAR(12) NOT NULL DEFAULT 'manual' COMMENT '批次来源：manual 手动录入，import 导入',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '本次提交的操作时间',
    UNIQUE KEY uk_request (tenant_id, family_id, request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭资产更新批次';

CREATE TABLE IF NOT EXISTS financial_update_record (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    tenant_id BIGINT NOT NULL COMMENT '租户编号',
    family_id BIGINT NOT NULL COMMENT '家庭账本编号',
    batch_id BIGINT NOT NULL COMMENT '所属资产更新批次编号',
    account_id BIGINT NOT NULL COMMENT '资产账户编号，账户删除后仍保留记录',
    member_id BIGINT NOT NULL COMMENT '家庭成员编号',
    category_code VARCHAR(20) NOT NULL COMMENT '资产分类编码：liquid、fixed、investment、receivable、liability',
    account_name VARCHAR(80) NOT NULL COMMENT '操作时的账户名称，保留历史值',
    previous_balance DECIMAL(18,2) NULL COMMENT '修改前余额，单位元；NULL 表示此前待填写或账户不存在',
    current_balance DECIMAL(18,2) NULL COMMENT '修改后余额，单位元；NULL 表示待填写或本次删除账户',
    change_amount DECIMAL(19,2) NOT NULL COMMENT '余额变化金额，单位元；修改后减修改前，NULL 按零计算',
    operation VARCHAR(12) NOT NULL COMMENT '操作类型：create 新建、update 更新、delete 删除',
    source_type VARCHAR(12) NOT NULL DEFAULT 'manual' COMMENT '数据来源：manual 手动录入，import 导入',
    operator_user_id BIGINT NOT NULL COMMENT '操作人系统用户编号',
    data_date DATE NOT NULL COMMENT '账户数据日期，可早于记账日期，不得晚于今天',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '本次操作时间，与账户数据日期分别记录',
    KEY idx_records (tenant_id, family_id, member_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭资产账户更新明细';

CREATE TABLE IF NOT EXISTS financial_snapshot (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    tenant_id BIGINT NOT NULL COMMENT '租户编号',
    family_id BIGINT NOT NULL COMMENT '家庭账本编号',
    member_id BIGINT NOT NULL COMMENT '家庭成员编号，0 表示家庭汇总，其余为成员快照',
    snapshot_date DATE NOT NULL COMMENT '快照记账日期，同一家庭和成员同日仅保留最后一次结果',
    liquid_amount DECIMAL(24,2) NOT NULL COMMENT '流动资金金额，单位元',
    fixed_amount DECIMAL(24,2) NOT NULL COMMENT '固定资产估值，单位元',
    investment_amount DECIMAL(24,2) NOT NULL COMMENT '投资理财金额，单位元',
    receivable_amount DECIMAL(24,2) NOT NULL COMMENT '应收款金额，单位元',
    liability_amount DECIMAL(24,2) NOT NULL COMMENT '总负债金额，单位元，按正数记录',
    total_assets DECIMAL(24,2) NOT NULL COMMENT '总资产金额，单位元，不含负债',
    net_assets DECIMAL(24,2) NOT NULL COMMENT '净资产金额，单位元，总资产减总负债，可为负数',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    UNIQUE KEY uk_day (tenant_id, family_id, member_id, snapshot_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭资产每日快照';
