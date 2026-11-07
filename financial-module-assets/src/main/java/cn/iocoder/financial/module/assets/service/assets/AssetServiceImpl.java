package cn.iocoder.financial.module.assets.service.assets;

import cn.hutool.crypto.digest.DigestUtil;
import cn.iocoder.financial.framework.common.pojo.PageResult;
import cn.iocoder.financial.module.assets.convert.assets.AssetConvert;
import cn.iocoder.financial.module.assets.controller.admin.assets.vo.*;
import cn.iocoder.financial.module.assets.dal.assets.AssetRepository;
import cn.iocoder.financial.module.assets.enums.AssetDictType;
import cn.iocoder.financial.module.assets.enums.AssetCategory;
import cn.iocoder.financial.module.assets.service.assets.AssetAccessService.Scope;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

import static cn.iocoder.financial.module.assets.service.assets.AssetAmounts.amount;
import static cn.iocoder.financial.module.assets.service.assets.AssetAmounts.monthlyChanges;
import static cn.iocoder.financial.module.assets.service.assets.AssetAmounts.summary;
import static cn.iocoder.financial.module.assets.service.assets.AssetAmounts.totals;
import static cn.iocoder.financial.module.assets.service.assets.AssetValues.flag;
import static cn.iocoder.financial.module.assets.service.assets.AssetValues.id;
import static cn.iocoder.financial.module.assets.service.assets.AssetValues.invalid;
import static cn.iocoder.financial.module.assets.service.assets.AssetValues.userId;
import static cn.iocoder.financial.module.assets.service.assets.AssetValues.values;

/**
 * 资产查询、历史及原子记账 Service 实现类
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AssetServiceImpl implements AssetService {
    @Resource
    private AssetRepository repository;
    @Resource
    private AssetAccessService access;
    @Resource
    private AssetDictionaryService dictionaries;
    @Value("${financial.assets.max-account-balance:999999999999.99}")
    private BigDecimal maxBalance;

    @Override
    public List<AssetAccountRespVO> accounts(Long memberId, String category) {
        Scope scope = access.scope(false);
        Map<String, Object> params = values("familyId", scope.familyId(), "members", access.visible(scope, memberId), "category", category);
        return AssetConvert.convertList(repository.selectAccounts(params), AssetAccountRespVO.class);
    }

    @Override
    public AssetAccountSnapshotsRespVO accountSnapshots(Long memberId, boolean excludeFixed) {
        Scope scope = access.scope(false);
        List<Long> members = access.visible(scope, memberId);
        return AssetConvert.convert(accountSnapshots(scope, members, excludeFixed), AssetAccountSnapshotsRespVO.class);
    }

    private Map<String, Object> accountSnapshots(Scope scope, List<Long> members, boolean excludeFixed) {
        List<Map<String, Object>> records = repository.selectAccountHistory(Map.of("familyId", scope.familyId(), "members", members));
        List<String> dates = repository.selectSnapshotDates(Map.of("familyId", scope.familyId(), "members", members)).stream()
                .map(row -> row.get("date").toString()).toList();
        return AssetAccountSnapshots.build(records, dates, new HashSet<>(members), excludeFixed);
    }

    @Override
    public byte[] exportExcel(Long memberId, boolean excludeFixed, boolean hidden) throws IOException {
        Scope scope = access.scope(false);
        List<Long> visibleMembers = access.visible(scope, memberId);
        List<Map<String, Object>> accounts = repository.selectExportAccounts(Map.of("familyId", scope.familyId(), "members", visibleMembers, "excludeFixed", excludeFixed));
        Map<Long, String> members = new LinkedHashMap<>();
        scope.members().stream().filter(member -> visibleMembers.contains(id(member, "id")))
                .forEach(member -> members.put(id(member, "id"), member.get("displayName").toString()));
        Map<String, String> categories = new LinkedHashMap<>();
        repository.selectCategories(Map.of("familyId", scope.familyId()))
                .forEach(category -> categories.put(category.get("code").toString(), category.get("name").toString()));
        List<Map<String, Object>> history = repository.selectExportHistory(Map.of("familyId", scope.familyId(), "members", visibleMembers, "excludeFixed", excludeFixed));
        return AssetWorkbook.write(scope.family().get("name").toString(), String.join("、", members.values()),
                excludeFixed, hidden, members, categories, accounts, summary(totals(accounts, false)),
                accountSnapshots(scope, visibleMembers, excludeFixed), history,
                dictionaries.labels(AssetDictType.OPERATION), dictionaries.labels(AssetDictType.SOURCE));
    }

    private List<Map<String, Object>> snapshots(Scope scope, List<Long> members, boolean excludeFixed,
                                                LocalDate start, LocalDate end) {
        Map<String, Object> params = values("familyId", scope.familyId(), "members", members, "start", start, "end", end);
        return repository.selectSnapshots(params).stream().map(row -> {
            Map<String, BigDecimal> totals = new LinkedHashMap<>();
            AssetCategory.codes().forEach(code -> totals.put(code, excludeFixed && "fixed".equals(code) ? amount(null) : amount(row.get(code))));
            Map<String, Object> result = summary(totals);
            result.put("amounts", totals);
            result.put("date", row.get("date").toString());
            return result;
        }).toList();
    }

    @Override
    public List<AssetTrendRespVO> trends(Long memberId, boolean excludeFixed, LocalDate start, LocalDate end) {
        Scope scope = access.scope(false);
        if (start != null && end != null && start.isAfter(end)) {
            throw invalid("开始日期不能晚于结束日期");
        }
        return AssetConvert.convertList(snapshots(scope, access.visible(scope, memberId), excludeFixed, start, end), AssetTrendRespVO.class);
    }

    @Override
    public AssetDashboardRespVO dashboard(Long memberId, boolean excludeFixed) {
        Scope scope = access.scope(false);
        List<Long> members = access.visible(scope, memberId);
        Map<String, Object> params = Map.of("familyId", scope.familyId(), "members", members);
        List<Map<String, Object>> accounts = repository.selectVisibleAccounts(params);
        Map<String, BigDecimal> totals = totals(accounts, excludeFixed);
        Map<String, Object> overview = summary(totals);
        Map<String, Object> latest = repository.selectLatestUpdate(params);
        overview.put("updatedAt", latest == null ? null : latest.get("updatedAt"));
        List<Map<String, Object>> snapshots = snapshots(scope, members, excludeFixed, null, null);
        Map<String, Object> changes = monthlyChanges(snapshots, LocalDate.now());
        Map<String, BigDecimal> buckets = new LinkedHashMap<>();
        dictionaries.labels(AssetDictType.FUND_BUCKET).keySet().forEach(key -> buckets.put(key, amount(null)));
        buckets.put("unassigned", amount(null));
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
        for (Map<String, Object> category : repository.selectCategories(Map.of("familyId", scope.familyId()))) {
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
        return AssetConvert.convert(values("overview", overview, "accounts", accounts, "monthly", changes, "composition", composition, "buckets", buckets,
                "insuranceTypes", insuranceTypes, "accountCount", accounts.size(),
                "trends", snapshots,
                "records", history(memberId, null, null, null, 1, 6)), AssetDashboardRespVO.class);
    }

    @Override
    public PageResult<AssetRecordRespVO> history(Long memberId, String category, LocalDate start, LocalDate end, int page, int size) {
        if (page < 1 || page > 100000 || size < 1 || size > 100) {
            throw invalid("分页参数无效");
        }
        Scope scope = access.scope(false);
        Map<String, Object> params = values("familyId", scope.familyId(), "members", access.visible(scope, memberId),
                "category", category, "start", start, "end", end, "offset", (page - 1) * size, "size", size);
        List<Map<String, Object>> records = repository.selectHistory(params);
        Map<String, Object> count = repository.countHistory(params);
        return new PageResult<>(AssetConvert.convertList(records, AssetRecordRespVO.class), ((Number) count.get("total")).longValue());
    }

    @Override
    public long submit(AssetSubmitReqVO request) {
        Scope scope = access.scope(true);
        Map<String, Object> params = values("familyId", scope.familyId(), "requestId", request.getRequestId());
        String hash = DigestUtil.sha256Hex(request.getUpdateDate() + ":" + request.getAccounts().toString());
        Map<String, Object> existingBatch = repository.selectBatch(params);
        if (existingBatch != null) {
            if (id(existingBatch, "operatorUserId") != userId() || !hash.equals(existingBatch.get("requestHash"))) {
                throw invalid("提交编号已经使用，请刷新后重试");
            }
            return id(existingBatch, "id");
        }
        Map<String, Object> lastDate = repository.selectLatestSnapshot(Map.of("familyId", scope.familyId()));
        if (lastDate != null && lastDate.get("date") != null
                && request.getUpdateDate().isBefore(LocalDate.parse(lastDate.get("date").toString()))) {
            throw invalid("记账日期不能早于家庭最近快照日期；账户数据日期可以单独填写");
        }
        Map<Long, Map<String, Object>> existingAccounts = new HashMap<>();
        repository.selectFamilyAccounts(Map.of("familyId", scope.familyId()))
                .forEach(account -> existingAccounts.put(id(account, "id"), account));
        List<Map<String, Object>> categories = repository.selectCategories(Map.of("familyId", scope.familyId()));
        Set<Long> submittedIds = new HashSet<>();
        for (AssetAccountSaveReqVO change : request.getAccounts()) {
            access.editable(scope, change.getMemberId());
            if (change.getBalance() != null && change.getBalance().compareTo(maxBalance) > 0) {
                throw invalid("单账户金额不能超过 " + maxBalance.toPlainString() + " 元");
            }
            Map<String, Object> old = change.getId() == null ? null : existingAccounts.get(change.getId());
            if (change.getId() != null) {
                if (old == null) {
                    throw invalid("账户不存在或已删除，请刷新后重试");
                }
                access.editable(scope, id(old, "memberId"));
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
            if (change.getTemplateCode() != null && !"liquid".equals(change.getCategoryCode())) {
                throw invalid("账户模板无效");
            }
            if (!change.isDeleted()) {
                dictionaries.validateSelection(AssetDictType.TEMPLATE, change.getTemplateCode(), old == null ? null : (String) old.get("templateCode"));
                dictionaries.validateSelection(AssetDictType.FUND_BUCKET, change.getFundBucket(), old == null ? null : (String) old.get("fundBucket"));
                dictionaries.validateSelection(AssetDictType.INSURANCE_TYPE, change.getInsuranceType(), old == null ? null : (String) old.get("insuranceType"));
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
        long batchId = repository.insertBatch(values("familyId", scope.familyId(), "userId", userId(), "requestId", request.getRequestId(),
                "hash", hash, "date", request.getUpdateDate()));
        for (AssetAccountSaveReqVO change : request.getAccounts()) {
            Map<String, Object> old = change.getId() == null ? null : existingAccounts.get(change.getId());
            Map<String, Object> accountParams = values("familyId", scope.familyId(), "member", change.getMemberId(),
                    "category", change.getCategoryCode(), "name", change.getName().trim(), "template", change.getTemplateCode(),
                    "balance", change.getBalance(), "bucket", change.getFundBucket(), "insurance", change.getInsuranceType(),
                    "date", change.getDataDate(), "sort", change.getSortOrder(), "remark", Objects.toString(change.getRemark(), ""),
                    "userId", userId(), "id", change.getId(), "deleted", change.isDeleted());
            long accountId;
            if (old == null) {
                accountId = repository.insertAccount(accountParams);
            } else {
                accountId = change.getId();
                repository.updateAccount(accountParams);
            }
            Object previous = old == null ? null : old.get("balance");
            Object current = change.isDeleted() ? null : change.getBalance();
            repository.insertRecord(values("familyId", scope.familyId(), "batch", batchId, "account", accountId,
                    "member", change.getMemberId(), "category", change.getCategoryCode(), "name", change.getName().trim(),
                    "previous", previous, "current", current, "delta", amount(current).subtract(amount(previous)),
                    "operation", change.isDeleted() ? "delete" : old == null ? "create" : "update", "userId", userId(), "date", change.getDataDate()));
        }
        Map<String, Object> duplicate = repository.selectDuplicateTemplate(Map.of("familyId", scope.familyId()));
        if (duplicate != null) {
            throw invalid("同一成员不能重复添加相同的系统账户模板，请改用自定义账户");
        }
        List<Map<String, Object>> allAccounts = repository.selectFamilyAccounts(Map.of("familyId", scope.familyId()));
        Map<String, BigDecimal> familyTotals = totals(allAccounts, false);
        Map<String, Object> familySummary = summary(familyTotals);
        repository.updateBatchTotals(values("familyId", scope.familyId(), "id", batchId, "total", familySummary.get("totalAssets"),
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
        repository.upsertSnapshot(params);
    }
}
