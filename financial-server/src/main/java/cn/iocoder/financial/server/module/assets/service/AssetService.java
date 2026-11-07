package cn.iocoder.financial.server.module.assets.service;

import cn.hutool.crypto.digest.DigestUtil;
import cn.iocoder.financial.framework.common.exception.ServiceException;
import cn.iocoder.financial.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.financial.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.financial.server.module.assets.controller.admin.vo.AssetRequests.*;
import cn.iocoder.financial.server.module.assets.dal.AssetRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class AssetService {
    private static final List<String> CODES = List.of("liquid", "fixed", "investment", "receivable", "liability");
    private static final Set<String> TEMPLATES = Set.of("bank", "card", "cash", "alipay", "wechat", "ecny");
    private static final String FAMILY_SQL = """
            SELECT f.id, f.name, f.owner_user_id AS ownerUserId, f.allow_view_others AS allowViewOthers,
                   m.id AS currentMemberId
            FROM financial_family f JOIN financial_member m ON m.family_id=f.id AND m.tenant_id=f.tenant_id
            WHERE f.tenant_id=:tenantId AND m.user_id=:userId
            """;
    private static final String MEMBERS_SQL = """
            SELECT id, user_id AS userId, display_name AS displayName, role, sort_order AS sortOrder
            FROM financial_member WHERE tenant_id=:tenantId AND family_id=:familyId ORDER BY sort_order,id
            """;
    private static final String CATEGORIES_SQL = """
            SELECT id, code, name, icon, color, description, sort_order AS sortOrder, enabled
            FROM financial_category WHERE tenant_id=:tenantId AND family_id=:familyId ORDER BY sort_order,id
            """;
    private static final String ACCOUNTS_SQL = """
            SELECT id, member_id AS memberId, category_code AS categoryCode, name, template_code AS templateCode,
                   balance, fund_bucket AS fundBucket, insurance_type AS insuranceType, data_date AS dataDate,
                   sort_order AS sortOrder, remark, version, source_type AS sourceType, updated_at AS updatedAt
            FROM financial_account WHERE tenant_id=:tenantId AND family_id=:familyId AND deleted=0
            """;
    private final AssetRepository repository;
    private final BigDecimal maxBalance;

    public AssetService(AssetRepository repository,
                        @Value("${financial.assets.max-account-balance:999999999999.99}") BigDecimal maxBalance) {
        this.repository = repository;
        this.maxBalance = maxBalance;
    }

    private ServiceException invalid(String message) {
        return new ServiceException(1_050_000_001, message);
    }

    private ServiceException forbidden() {
        return new ServiceException(403, "没有权限操作该家庭成员的资产");
    }

    private long userId() {
        Long id = SecurityFrameworkUtils.getLoginUserId();
        if (id == null) {
            throw new ServiceException(401, "请先登录");
        }
        return id;
    }

    private long id(Map<String, Object> row, String key) {
        return ((Number) row.get(key)).longValue();
    }

    private boolean flag(Object value) {
        return Boolean.TRUE.equals(value) || value instanceof Number number && number.intValue() == 1;
    }

    private BigDecimal amount(Object value) {
        return value == null ? BigDecimal.ZERO.setScale(2) : new BigDecimal(value.toString()).setScale(2);
    }

    private Map<String, Object> values(Object... pairs) {
        Map<String, Object> result = new HashMap<>();
        for (int index = 0; index < pairs.length; index += 2) {
            result.put((String) pairs[index], pairs[index + 1]);
        }
        return result;
    }

    private record Scope(Map<String, Object> family, long familyId, long memberId, boolean owner,
                         List<Map<String, Object>> members, List<Long> visibleIds) {
    }

    private Scope scope(boolean lock) {
        Map<String, Object> family = repository.one(FAMILY_SQL + (lock ? " FOR UPDATE" : ""), Map.of("userId", userId()));
        if (family == null) {
            throw invalid("请先建立家庭账本，或请家庭所有者将你的系统用户编号关联到成员");
        }
        long familyId = id(family, "id");
        long memberId = id(family, "currentMemberId");
        boolean owner = id(family, "ownerUserId") == userId();
        List<Map<String, Object>> members = repository.list(MEMBERS_SQL, Map.of("familyId", familyId));
        List<Long> visible = members.stream().map(member -> id(member, "id"))
                .filter(member -> owner || flag(family.get("allowViewOthers")) || member == memberId).toList();
        return new Scope(family, familyId, memberId, owner, members, visible);
    }

    private List<Long> visible(Scope scope, Long memberId) {
        if (memberId == null) {
            return scope.visibleIds();
        }
        if (!scope.visibleIds().contains(memberId)) {
            throw forbidden();
        }
        return List.of(memberId);
    }

    private void editable(Scope scope, long memberId) {
        if (scope.members().stream().noneMatch(member -> id(member, "id") == memberId)
                || !scope.owner() && scope.memberId() != memberId) {
            throw forbidden();
        }
    }

    private void owner(Scope scope) {
        if (!scope.owner()) {
            throw forbidden();
        }
    }

    public Map<String, Object> context() {
        if (repository.one(FAMILY_SQL, Map.of("userId", userId())) == null) {
            return values("family", null, "userId", userId(), "tenantId", TenantContextHolder.getRequiredTenantId());
        }
        Scope scope = scope(false);
        return values("family", scope.family(), "userId", userId(), "tenantId", TenantContextHolder.getRequiredTenantId(),
                "owner", scope.owner(), "currentMemberId", scope.memberId(), "members", scope.members().stream()
                        .filter(member -> scope.visibleIds().contains(id(member, "id"))).toList(),
                "categories", repository.list(CATEGORIES_SQL, Map.of("familyId", scope.familyId())),
                "maxBalance", maxBalance.toPlainString(), "importEnabled", false, "syncEnabled", false);
    }

    public long initialize(FamilySave request) {
        if (repository.one(FAMILY_SQL, Map.of("userId", userId())) != null) {
            throw invalid("你已经加入家庭账本");
        }
        long familyId;
        try {
            familyId = repository.insert("""
                    INSERT INTO financial_family(tenant_id,name,owner_user_id,allow_view_others)
                    VALUES(:tenantId,:name,:userId,:allow)
                    """, values("name", request.getName().trim(), "userId", userId(), "allow", request.isAllowViewOthers()));
            repository.insert("""
                    INSERT INTO financial_member(tenant_id,family_id,user_id,display_name,role)
                    VALUES(:tenantId,:familyId,:userId,'我','owner')
                    """, Map.of("familyId", familyId, "userId", userId()));
        } catch (DuplicateKeyException exception) {
            throw invalid("你已经加入家庭账本，请刷新页面");
        }
        String[][] defaults = {
                {"liquid", "流动资金", "wallet", "#3b82f6", "可随时或短期使用的资金，例如现金、银行卡、支付宝。长期投资应计入投资理财；计入总资产。"},
                {"fixed", "固定资产", "house", "#a78bfa", "房产、车辆等实物资产，可录入当前估值。可在首页选择排除固定资产。"},
                {"investment", "投资理财", "chart", "#f59e0b", "基金、股票、理财产品及证券账户的当前市值。不含活期现金；计入总资产。"},
                {"receivable", "应收款", "handshake", "#14b8a6", "借款、押金、未到账款项等应归还资金。借入资金应计入负债；计入总资产。"},
                {"liability", "负债", "credit-card", "#f97373", "房贷、车贷、信用卡等待偿还款项。请输入正数，由系统从总资产中扣除。"}
        };
        for (int index = 0; index < defaults.length; index++) {
            String[] item = defaults[index];
            repository.insert("""
                    INSERT INTO financial_category(tenant_id,family_id,code,name,icon,color,description,sort_order)
                    VALUES(:tenantId,:familyId,:code,:name,:icon,:color,:description,:sort)
                    """, values("familyId", familyId, "code", item[0], "name", item[1], "icon", item[2],
                    "color", item[3], "description", item[4], "sort", index));
        }
        return familyId;
    }

    public void saveFamily(FamilySave request) {
        Scope scope = scope(true);
        owner(scope);
        repository.update("""
                UPDATE financial_family SET name=:name,allow_view_others=:allow
                WHERE tenant_id=:tenantId AND id=:familyId
                """, values("familyId", scope.familyId(), "name", request.getName().trim(), "allow", request.isAllowViewOthers()));
    }

    public void saveMember(MemberSave request) {
        Scope scope = scope(true);
        owner(scope);
        if (request.getUserId() != null && repository.one("""
                SELECT id FROM system_users WHERE tenant_id=:tenantId AND id=:userId AND deleted=0 AND status=0
                """, Map.of("userId", request.getUserId())) == null) {
            throw invalid("关联的系统用户不存在或已停用");
        }
        if (request.getId() != null) {
            Map<String, Object> member = scope.members().stream().filter(item -> id(item, "id") == request.getId())
                    .findFirst().orElseThrow(this::forbidden);
            if ("owner".equals(member.get("role")) && !Objects.equals(request.getUserId(), userId())) {
                throw invalid("不能更换所有者的关联用户");
            }
        }
        Map<String, Object> params = values("familyId", scope.familyId(), "id", request.getId(),
                "userId", request.getUserId(), "name", request.getDisplayName().trim(), "sort", request.getSortOrder());
        try {
            if (request.getId() == null) {
                if (scope.members().size() >= 50) {
                    throw invalid("一个家庭最多支持 50 位成员");
                }
                repository.insert("""
                        INSERT INTO financial_member(tenant_id,family_id,user_id,display_name,sort_order)
                        VALUES(:tenantId,:familyId,:userId,:name,:sort)
                        """, params);
            } else {
                repository.update("""
                        UPDATE financial_member SET user_id=:userId,display_name=:name,sort_order=:sort
                        WHERE tenant_id=:tenantId AND family_id=:familyId AND id=:id
                        """, params);
            }
        } catch (DuplicateKeyException exception) {
            throw invalid("该系统用户已经关联家庭成员，请勿重复关联");
        }
    }

    public void deleteMember(long memberId) {
        Scope scope = scope(true);
        owner(scope);
        editable(scope, memberId);
        if (memberId == scope.memberId()) {
            throw invalid("不能删除家庭所有者");
        }
        Map<String, Object> params = Map.of("familyId", scope.familyId(), "memberId", memberId);
        if (repository.one("SELECT id FROM financial_account WHERE tenant_id=:tenantId AND family_id=:familyId AND member_id=:memberId LIMIT 1", params) != null
                || repository.one("SELECT id FROM financial_snapshot WHERE tenant_id=:tenantId AND family_id=:familyId AND member_id=:memberId LIMIT 1", params) != null
                || repository.one("SELECT id FROM financial_update_record WHERE tenant_id=:tenantId AND family_id=:familyId AND member_id=:memberId LIMIT 1", params) != null) {
            throw invalid("该成员已有关联资产或历史快照，不能删除；可以修改名称或解除用户关联");
        }
        repository.update("DELETE FROM financial_member WHERE tenant_id=:tenantId AND family_id=:familyId AND id=:memberId", params);
    }

    public void saveCategory(CategorySave request) {
        Scope scope = scope(true);
        owner(scope);
        Map<String, Object> category = repository.one("""
                SELECT code FROM financial_category WHERE tenant_id=:tenantId AND family_id=:familyId AND id=:id
                """, Map.of("familyId", scope.familyId(), "id", request.getId()));
        if (category == null) {
            throw invalid("分类不存在");
        }
        if (!request.isEnabled() && repository.one("""
                SELECT id FROM financial_account WHERE tenant_id=:tenantId AND family_id=:familyId
                AND category_code=:code AND deleted=0 LIMIT 1
                """, Map.of("familyId", scope.familyId(), "code", category.get("code"))) != null) {
            throw invalid("分类下还有账户，请先转移或删除账户后再停用");
        }
        repository.update("""
                UPDATE financial_category SET name=:name,icon=:icon,color=:color,description=:description,
                sort_order=:sort,enabled=:enabled WHERE tenant_id=:tenantId AND family_id=:familyId AND id=:id
                """, values("familyId", scope.familyId(), "id", request.getId(), "name", request.getName().trim(),
                "icon", request.getIcon(), "color", request.getColor(), "description", request.getDescription(),
                "sort", request.getSortOrder(), "enabled", request.isEnabled()));
    }

    public List<Map<String, Object>> accounts(Long memberId, String category) {
        Scope scope = scope(false);
        Map<String, Object> params = values("familyId", scope.familyId(), "members", visible(scope, memberId), "category", category);
        return repository.list(ACCOUNTS_SQL + " AND member_id IN (:members)"
                + (category == null ? "" : " AND category_code=:category") + " ORDER BY sort_order,id", params);
    }

    private Map<String, BigDecimal> totals(List<Map<String, Object>> accounts, boolean excludeFixed) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        CODES.forEach(code -> result.put(code, amount(null)));
        for (Map<String, Object> account : accounts) {
            String code = (String) account.get("categoryCode");
            if (!(excludeFixed && "fixed".equals(code))) {
                result.merge(code, amount(account.get("balance")), BigDecimal::add);
            }
        }
        return result;
    }

    private Map<String, Object> summary(Map<String, BigDecimal> totals) {
        BigDecimal total = CODES.stream().filter(code -> !"liability".equals(code))
                .map(totals::get).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal liability = totals.get("liability");
        return values("totalAssets", total, "totalLiabilities", liability, "netAssets", total.subtract(liability),
                "debtRatio", total.signum() == 0 ? null : liability.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP));
    }

    private List<Map<String, Object>> snapshots(Scope scope, List<Long> members, boolean excludeFixed,
                                                LocalDate start, LocalDate end) {
        Map<String, Object> params = values("familyId", scope.familyId(), "members", members, "start", start, "end", end);
        String sql = """
                SELECT snapshot_date AS date,SUM(liquid_amount) AS liquid,SUM(fixed_amount) AS fixed,
                SUM(investment_amount) AS investment,SUM(receivable_amount) AS receivable,SUM(liability_amount) AS liability
                FROM financial_snapshot WHERE tenant_id=:tenantId AND family_id=:familyId AND member_id IN (:members)
                """ + (start == null ? "" : " AND snapshot_date>=:start") + (end == null ? "" : " AND snapshot_date<=:end")
                + " GROUP BY snapshot_date ORDER BY snapshot_date";
        return repository.list(sql, params).stream().map(row -> {
            Map<String, BigDecimal> totals = new LinkedHashMap<>();
            CODES.forEach(code -> totals.put(code, excludeFixed && "fixed".equals(code) ? amount(null) : amount(row.get(code))));
            Map<String, Object> result = summary(totals);
            result.put("amounts", totals);
            result.put("date", row.get("date").toString());
            return result;
        }).toList();
    }

    public List<Map<String, Object>> trends(Long memberId, boolean excludeFixed, LocalDate start, LocalDate end) {
        Scope scope = scope(false);
        if (start != null && end != null && start.isAfter(end)) {
            throw invalid("开始日期不能晚于结束日期");
        }
        return snapshots(scope, visible(scope, memberId), excludeFixed, start, end);
    }

    private Map<String, Object> monthlyChanges(List<Map<String, Object>> snapshots, LocalDate today) {
        Map<String, Object> latest = snapshots.isEmpty() ? null : snapshots.getLast();
        LocalDate latestDate = latest == null ? null : LocalDate.parse(latest.get("date").toString());
        LocalDate currentMonth = today.withDayOfMonth(1);
        boolean fallback = latestDate != null && latestDate.isBefore(currentMonth);
        LocalDate comparisonMonth = (latestDate == null ? today : latestDate).withDayOfMonth(1);
        Map<String, Object> baseline = snapshots.stream()
                .filter(row -> LocalDate.parse(row.get("date").toString()).isBefore(comparisonMonth))
                .reduce((previous, current) -> current).orElse(null);
        Map<String, Object> changes = new LinkedHashMap<>();
        for (String key : List.of("totalAssets", "totalLiabilities", "netAssets")) {
            changes.put(key, amount(latest == null ? null : latest.get(key))
                    .subtract(amount(baseline == null ? null : baseline.get(key))));
        }
        Map<?, ?> latestAmounts = latest == null ? Map.of() : (Map<?, ?>) latest.get("amounts");
        Map<?, ?> baselineAmounts = baseline == null ? Map.of() : (Map<?, ?>) baseline.get("amounts");
        Map<String, BigDecimal> categoryChanges = new LinkedHashMap<>();
        CODES.forEach(code -> categoryChanges.put(code, amount(latestAmounts.get(code)).subtract(amount(baselineAmounts.get(code)))));
        changes.put("categories", categoryChanges);
        changes.put("firstRecord", baseline == null);
        changes.put("baselineDate", baseline == null ? null : baseline.get("date"));
        changes.put("latestDate", latestDate == null ? null : latestDate.toString());
        changes.put("fallback", fallback);
        return changes;
    }

    public Map<String, Object> dashboard(Long memberId, boolean excludeFixed) {
        Scope scope = scope(false);
        List<Long> members = visible(scope, memberId);
        Map<String, Object> params = Map.of("familyId", scope.familyId(), "members", members);
        List<Map<String, Object>> accounts = repository.list(ACCOUNTS_SQL + " AND member_id IN (:members)", params);
        Map<String, BigDecimal> totals = totals(accounts, excludeFixed);
        Map<String, Object> overview = summary(totals);
        Map<String, Object> latest = repository.one("""
                SELECT MAX(created_at) AS updatedAt FROM financial_update_record
                WHERE tenant_id=:tenantId AND family_id=:familyId AND member_id IN (:members)
                """, params);
        overview.put("updatedAt", latest == null ? null : latest.get("updatedAt"));
        List<Map<String, Object>> snapshots = snapshots(scope, members, excludeFixed, null, null);
        Map<String, Object> changes = monthlyChanges(snapshots, LocalDate.now());
        Map<String, BigDecimal> buckets = new LinkedHashMap<>();
        List.of("liquid", "stable", "long_term", "insurance", "unassigned").forEach(key -> buckets.put(key, amount(null)));
        Set<String> insuranceTypes = new LinkedHashSet<>();
        for (Map<String, Object> account : accounts) {
            if ("liability".equals(account.get("categoryCode")) || excludeFixed && "fixed".equals(account.get("categoryCode"))) {
                continue;
            }
            String bucket = account.get("fundBucket") == null ? "unassigned" : account.get("fundBucket").toString();
            buckets.merge(bucket, amount(account.get("balance")), BigDecimal::add);
            if ("insurance".equals(bucket) && account.get("insuranceType") != null) {
                insuranceTypes.add(account.get("insuranceType").toString());
            }
        }
        BigDecimal total = amount(overview.get("totalAssets"));
        List<Map<String, Object>> composition = new ArrayList<>();
        for (Map<String, Object> category : repository.list(CATEGORIES_SQL, Map.of("familyId", scope.familyId()))) {
            String code = category.get("code").toString();
            if ((!flag(category.get("enabled")) && totals.get(code).signum() == 0) || excludeFixed && "fixed".equals(code)) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>(category);
            item.put("amount", totals.get(code));
            item.put("ratio", "liability".equals(code) || total.signum() == 0 ? BigDecimal.ZERO
                    : totals.get(code).multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP));
            composition.add(item);
        }
        return values("overview", overview, "accounts", accounts, "monthly", changes, "composition", composition, "buckets", buckets,
                "insuranceTypes", insuranceTypes, "accountCount", accounts.size(),
                "trends", snapshots,
                "records", history(memberId, null, null, null, 1, 6));
    }

    public Map<String, Object> history(Long memberId, String category, LocalDate start, LocalDate end, int page, int size) {
        if (page < 1 || page > 100000 || size < 1 || size > 100) {
            throw invalid("分页参数无效");
        }
        Scope scope = scope(false);
        Map<String, Object> params = values("familyId", scope.familyId(), "members", visible(scope, memberId),
                "category", category, "start", start, "end", end, "offset", (page - 1) * size, "size", size);
        String where = " WHERE r.tenant_id=:tenantId AND r.family_id=:familyId AND r.member_id IN (:members)"
                + (category == null ? "" : " AND r.category_code=:category")
                + (start == null ? "" : " AND r.data_date>=:start") + (end == null ? "" : " AND r.data_date<=:end");
        List<Map<String, Object>> records = repository.list("""
                SELECT r.id,r.batch_id AS batchId,r.account_id AS accountId,r.member_id AS memberId,
                r.category_code AS categoryCode,r.account_name AS accountName,r.previous_balance AS previousBalance,
                r.current_balance AS currentBalance,r.change_amount AS changeAmount,r.operation,r.source_type AS sourceType,
                r.operator_user_id AS operatorUserId,r.data_date AS dataDate,r.created_at AS createdAt,
                u.nickname AS operatorName
                FROM financial_update_record r LEFT JOIN system_users u ON u.id=r.operator_user_id AND u.tenant_id=r.tenant_id
                """ + where + " ORDER BY r.id DESC LIMIT :size OFFSET :offset", params);
        Map<String, Object> count = repository.one("SELECT COUNT(*) AS total FROM financial_update_record r" + where, params);
        return values("list", records, "total", count.get("total"));
    }

    public long submit(Submit request) {
        Scope scope = scope(true);
        Map<String, Object> params = values("familyId", scope.familyId(), "requestId", request.getRequestId());
        String hash = DigestUtil.sha256Hex(request.getUpdateDate() + ":" + request.getAccounts().toString());
        Map<String, Object> existingBatch = repository.one("""
                SELECT id,operator_user_id AS operatorUserId,request_hash AS requestHash FROM financial_update_batch
                WHERE tenant_id=:tenantId AND family_id=:familyId AND request_id=:requestId
                """, params);
        if (existingBatch != null) {
            if (id(existingBatch, "operatorUserId") != userId() || !hash.equals(existingBatch.get("requestHash"))) {
                throw invalid("提交编号已经使用，请刷新后重试");
            }
            return id(existingBatch, "id");
        }
        Map<String, Object> lastDate = repository.one("""
                SELECT MAX(snapshot_date) AS date FROM financial_snapshot WHERE tenant_id=:tenantId AND family_id=:familyId
                """, Map.of("familyId", scope.familyId()));
        if (lastDate != null && lastDate.get("date") != null
                && request.getUpdateDate().isBefore(LocalDate.parse(lastDate.get("date").toString()))) {
            throw invalid("记账日期不能早于家庭最近快照日期；账户数据日期可以单独填写");
        }
        Map<Long, Map<String, Object>> existingAccounts = new HashMap<>();
        repository.list(ACCOUNTS_SQL, Map.of("familyId", scope.familyId()))
                .forEach(account -> existingAccounts.put(id(account, "id"), account));
        List<Map<String, Object>> categories = repository.list(CATEGORIES_SQL, Map.of("familyId", scope.familyId()));
        Set<Long> submittedIds = new HashSet<>();
        for (AccountChange change : request.getAccounts()) {
            editable(scope, change.getMemberId());
            if (change.getBalance() != null && change.getBalance().compareTo(maxBalance) > 0) {
                throw invalid("单账户金额不能超过 " + maxBalance.toPlainString() + " 元");
            }
            Map<String, Object> old = change.getId() == null ? null : existingAccounts.get(change.getId());
            if (change.getId() != null) {
                if (old == null) {
                    throw invalid("账户不存在或已删除，请刷新后重试");
                }
                editable(scope, id(old, "memberId"));
                if (!submittedIds.add(change.getId()) || !Objects.equals(change.getVersion(), id(old, "version"))) {
                    throw invalid("账户已被其他操作更新，请保留草稿并重新载入最新数据");
                }
            } else if (change.isDeleted()) {
                throw invalid("不能删除尚未保存的账户");
            }
            if (categories.stream().noneMatch(category -> category.get("code").equals(change.getCategoryCode())
                    && flag(category.get("enabled"))) && !change.isDeleted()) {
                throw invalid("资产分类不存在或已停用");
            }
            if (change.getTemplateCode() != null && (!TEMPLATES.contains(change.getTemplateCode()) || !"liquid".equals(change.getCategoryCode()))) {
                throw invalid("账户模板无效");
            }
            if (old != null && !Objects.equals(old.get("templateCode"), change.getTemplateCode())) {
                throw invalid("已保存账户的模板不能修改");
            }
            if ("liability".equals(change.getCategoryCode()) && change.getFundBucket() != null) {
                throw invalid("负债不能归入四笔钱");
            }
            if (change.getInsuranceType() != null && !"insurance".equals(change.getFundBucket())) {
                throw invalid("仅保险保障账户可设置保险类型");
            }
        }
        long batchId = repository.insert("""
                INSERT INTO financial_update_batch(tenant_id,family_id,operator_user_id,request_id,request_hash,update_date)
                VALUES(:tenantId,:familyId,:userId,:requestId,:hash,:date)
                """, values("familyId", scope.familyId(), "userId", userId(), "requestId", request.getRequestId(),
                "hash", hash, "date", request.getUpdateDate()));
        for (AccountChange change : request.getAccounts()) {
            Map<String, Object> old = change.getId() == null ? null : existingAccounts.get(change.getId());
            Map<String, Object> accountParams = values("familyId", scope.familyId(), "member", change.getMemberId(),
                    "category", change.getCategoryCode(), "name", change.getName().trim(), "template", change.getTemplateCode(),
                    "balance", change.getBalance(), "bucket", change.getFundBucket(), "insurance", change.getInsuranceType(),
                    "date", change.getDataDate(), "sort", change.getSortOrder(), "remark", Objects.toString(change.getRemark(), ""),
                    "userId", userId(), "id", change.getId(), "deleted", change.isDeleted());
            long accountId;
            if (old == null) {
                accountId = repository.insert("""
                        INSERT INTO financial_account(tenant_id,family_id,member_id,category_code,name,template_code,balance,
                        fund_bucket,insurance_type,data_date,sort_order,remark,creator,updater)
                        VALUES(:tenantId,:familyId,:member,:category,:name,:template,:balance,:bucket,:insurance,:date,:sort,:remark,:userId,:userId)
                        """, accountParams);
            } else {
                accountId = change.getId();
                repository.update("""
                        UPDATE financial_account SET member_id=:member,category_code=:category,name=:name,balance=:balance,
                        fund_bucket=:bucket,insurance_type=:insurance,data_date=:date,sort_order=:sort,remark=:remark,
                        updater=:userId,deleted=:deleted,version=version+1
                        WHERE tenant_id=:tenantId AND family_id=:familyId AND id=:id
                        """, accountParams);
            }
            Object previous = old == null ? null : old.get("balance");
            Object current = change.isDeleted() ? null : change.getBalance();
            repository.insert("""
                    INSERT INTO financial_update_record(tenant_id,family_id,batch_id,account_id,member_id,category_code,account_name,
                    previous_balance,current_balance,change_amount,operation,operator_user_id,data_date)
                    VALUES(:tenantId,:familyId,:batch,:account,:member,:category,:name,:previous,:current,:delta,:operation,:userId,:date)
                    """, values("familyId", scope.familyId(), "batch", batchId, "account", accountId,
                    "member", change.getMemberId(), "category", change.getCategoryCode(), "name", change.getName().trim(),
                    "previous", previous, "current", current, "delta", amount(current).subtract(amount(previous)),
                    "operation", change.isDeleted() ? "delete" : old == null ? "create" : "update", "userId", userId(), "date", change.getDataDate()));
        }
        Map<String, Object> duplicate = repository.one("""
                SELECT template_code FROM financial_account WHERE tenant_id=:tenantId AND family_id=:familyId
                AND deleted=0 AND template_code IS NOT NULL GROUP BY member_id,template_code HAVING COUNT(*)>1 LIMIT 1
                """, Map.of("familyId", scope.familyId()));
        if (duplicate != null) {
            throw invalid("同一成员不能重复添加相同的系统账户模板，请改用自定义账户");
        }
        List<Map<String, Object>> allAccounts = repository.list(ACCOUNTS_SQL, Map.of("familyId", scope.familyId()));
        Map<String, BigDecimal> familyTotals = totals(allAccounts, false);
        Map<String, Object> familySummary = summary(familyTotals);
        repository.update("""
                UPDATE financial_update_batch SET total_assets=:total,total_liabilities=:liability,net_assets=:net
                WHERE tenant_id=:tenantId AND family_id=:familyId AND id=:id
                """, values("familyId", scope.familyId(), "id", batchId, "total", familySummary.get("totalAssets"),
                "liability", familySummary.get("totalLiabilities"), "net", familySummary.get("netAssets")));
        saveSnapshot(scope.familyId(), 0, request.getUpdateDate(), familyTotals);
        for (Map<String, Object> member : scope.members()) {
            long memberId = id(member, "id");
            saveSnapshot(scope.familyId(), memberId, request.getUpdateDate(),
                    totals(allAccounts.stream().filter(account -> id(account, "memberId") == memberId).toList(), false));
        }
        return batchId;
    }

    private void saveSnapshot(long familyId, long memberId, LocalDate date, Map<String, BigDecimal> totals) {
        Map<String, Object> params = new HashMap<>(totals);
        params.putAll(values("familyId", familyId, "memberId", memberId, "date", date,
                "total", summary(totals).get("totalAssets"), "net", summary(totals).get("netAssets")));
        repository.update("""
                INSERT INTO financial_snapshot(tenant_id,family_id,member_id,snapshot_date,liquid_amount,fixed_amount,
                investment_amount,receivable_amount,liability_amount,total_assets,net_assets)
                VALUES(:tenantId,:familyId,:memberId,:date,:liquid,:fixed,:investment,:receivable,:liability,:total,:net)
                ON DUPLICATE KEY UPDATE liquid_amount=:liquid,fixed_amount=:fixed,investment_amount=:investment,
                receivable_amount=:receivable,liability_amount=:liability,total_assets=:total,net_assets=:net,updated_at=CURRENT_TIMESTAMP
                """, params);
    }
}
