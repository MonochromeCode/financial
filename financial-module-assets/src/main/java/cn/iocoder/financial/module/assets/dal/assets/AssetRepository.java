package cn.iocoder.financial.module.assets.dal.assets;

import cn.iocoder.financial.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.financial.module.assets.dal.mysql.assets.AssetMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AssetRepository {
    @Resource
    private AssetMapper mapper;

    private Map<String, Object> scoped(Map<String, ?> values) {
        Map<String, Object> params = new HashMap<>(values);
        params.put("tenantId", TenantContextHolder.getRequiredTenantId());
        return params;
    }

    private Map<String, Object> normalize(Map<String, Object> row, String... fields) {
        if (row != null) {
            for (String field : fields) row.putIfAbsent(field, null);
            row.replaceAll((key, value) -> value instanceof java.sql.Date date
                    ? date.toLocalDate().toString() : value);
        }
        return row;
    }

    private List<Map<String, Object>> normalize(List<Map<String, Object>> rows, String... fields) {
        rows.forEach(row -> normalize(row, fields));
        return rows;
    }

    public Map<String, Object> selectFamily(Map<String, ?> values) {
        return normalize(mapper.selectFamily(scoped(values)), "id", "name", "ownerUserId", "allowViewOthers", "currentMemberId");
    }

    public List<Map<String, Object>> selectMembers(Map<String, ?> values) {
        return normalize(mapper.selectMembers(scoped(values)), "id", "userId", "displayName", "role", "sortOrder");
    }

    public List<Map<String, Object>> selectCategories(Map<String, ?> values) {
        return normalize(mapper.selectCategories(scoped(values)), "id", "code", "name", "icon", "color", "description", "sortOrder", "enabled");
    }

    public long insertFamily(Map<String, ?> values) {
        Map<String, Object> params = scoped(values);
        mapper.insertFamily(params);
        return ((Number) params.get("id")).longValue();
    }

    public long insertOwner(Map<String, ?> values) {
        Map<String, Object> params = scoped(values);
        mapper.insertOwner(params);
        return ((Number) params.get("id")).longValue();
    }

    public long insertCategory(Map<String, ?> values) {
        Map<String, Object> params = scoped(values);
        mapper.insertCategory(params);
        return ((Number) params.get("id")).longValue();
    }

    public int updateFamily(Map<String, ?> values) {
        return mapper.updateFamily(scoped(values));
    }

    public Map<String, Object> selectEnabledUser(Map<String, ?> values) {
        return normalize(mapper.selectEnabledUser(scoped(values)), "id");
    }

    public long insertMember(Map<String, ?> values) {
        Map<String, Object> params = scoped(values);
        mapper.insertMember(params);
        return ((Number) params.get("id")).longValue();
    }

    public int updateMember(Map<String, ?> values) {
        return mapper.updateMember(scoped(values));
    }

    public Map<String, Object> selectMemberAccount(Map<String, ?> values) {
        return normalize(mapper.selectMemberAccount(scoped(values)), "id");
    }

    public Map<String, Object> selectMemberSnapshot(Map<String, ?> values) {
        return normalize(mapper.selectMemberSnapshot(scoped(values)), "id");
    }

    public Map<String, Object> selectMemberRecord(Map<String, ?> values) {
        return normalize(mapper.selectMemberRecord(scoped(values)), "id");
    }

    public int deleteMember(Map<String, ?> values) {
        return mapper.deleteMember(scoped(values));
    }

    public Map<String, Object> selectCategory(Map<String, ?> values) {
        return normalize(mapper.selectCategory(scoped(values)), "code");
    }

    public Map<String, Object> selectCategoryAccount(Map<String, ?> values) {
        return normalize(mapper.selectCategoryAccount(scoped(values)), "id");
    }

    public int updateCategory(Map<String, ?> values) {
        return mapper.updateCategory(scoped(values));
    }

    public List<Map<String, Object>> selectAccounts(Map<String, ?> values) {
        return normalize(mapper.selectAccounts(scoped(values)), "id", "memberId", "categoryCode", "name", "templateCode", "balance", "fundBucket", "insuranceType", "dataDate", "sortOrder", "remark", "version", "sourceType", "updatedAt");
    }

    public List<Map<String, Object>> selectAccountHistory(Map<String, ?> values) {
        return normalize(mapper.selectAccountHistory(scoped(values)), "id", "accountId", "memberId", "categoryCode", "accountName", "currentBalance", "operation", "date");
    }

    public List<Map<String, Object>> selectSnapshotDates(Map<String, ?> values) {
        return normalize(mapper.selectSnapshotDates(scoped(values)), "date");
    }

    public List<Map<String, Object>> selectExportAccounts(Map<String, ?> values) {
        return normalize(mapper.selectExportAccounts(scoped(values)), "id", "memberId", "categoryCode", "name", "templateCode", "balance", "fundBucket", "insuranceType", "dataDate", "sortOrder", "remark", "version", "sourceType", "updatedAt");
    }

    public List<Map<String, Object>> selectExportHistory(Map<String, ?> values) {
        return normalize(mapper.selectExportHistory(scoped(values)), "id", "accountId", "memberId", "categoryCode", "accountName", "previousBalance", "currentBalance", "changeAmount", "operation", "sourceType", "dataDate", "createdAt", "operatorUserId", "operatorName");
    }

    public List<Map<String, Object>> selectSnapshots(Map<String, ?> values) {
        return normalize(mapper.selectSnapshots(scoped(values)), "date", "liquid", "fixed", "investment", "receivable", "liability");
    }

    public List<Map<String, Object>> selectVisibleAccounts(Map<String, ?> values) {
        return normalize(mapper.selectVisibleAccounts(scoped(values)), "id", "memberId", "categoryCode", "name", "templateCode", "balance", "fundBucket", "insuranceType", "dataDate", "sortOrder", "remark", "version", "sourceType", "updatedAt");
    }

    public Map<String, Object> selectLatestUpdate(Map<String, ?> values) {
        return normalize(mapper.selectLatestUpdate(scoped(values)), "updatedAt");
    }

    public List<Map<String, Object>> selectHistory(Map<String, ?> values) {
        return normalize(mapper.selectHistory(scoped(values)), "id", "batchId", "accountId", "memberId", "categoryCode", "accountName", "previousBalance", "currentBalance", "changeAmount", "operation", "sourceType", "operatorUserId", "dataDate", "createdAt", "operatorName");
    }

    public Map<String, Object> countHistory(Map<String, ?> values) {
        return normalize(mapper.countHistory(scoped(values)), "total");
    }

    public Map<String, Object> selectBatch(Map<String, ?> values) {
        return normalize(mapper.selectBatch(scoped(values)), "id", "operatorUserId", "requestHash");
    }

    public Map<String, Object> selectLatestSnapshot(Map<String, ?> values) {
        return normalize(mapper.selectLatestSnapshot(scoped(values)), "date");
    }

    public List<Map<String, Object>> selectFamilyAccounts(Map<String, ?> values) {
        return normalize(mapper.selectFamilyAccounts(scoped(values)), "id", "memberId", "categoryCode", "name", "templateCode", "balance", "fundBucket", "insuranceType", "dataDate", "sortOrder", "remark", "version", "sourceType", "updatedAt");
    }

    public long insertBatch(Map<String, ?> values) {
        Map<String, Object> params = scoped(values);
        mapper.insertBatch(params);
        return ((Number) params.get("id")).longValue();
    }

    public long insertAccount(Map<String, ?> values) {
        Map<String, Object> params = scoped(values);
        mapper.insertAccount(params);
        return ((Number) params.get("id")).longValue();
    }

    public int updateAccount(Map<String, ?> values) {
        return mapper.updateAccount(scoped(values));
    }

    public long insertRecord(Map<String, ?> values) {
        Map<String, Object> params = scoped(values);
        mapper.insertRecord(params);
        return ((Number) params.get("id")).longValue();
    }

    public Map<String, Object> selectDuplicateTemplate(Map<String, ?> values) {
        return normalize(mapper.selectDuplicateTemplate(scoped(values)), "template_code");
    }

    public int updateBatchTotals(Map<String, ?> values) {
        return mapper.updateBatchTotals(scoped(values));
    }

    public int upsertSnapshot(Map<String, ?> values) {
        return mapper.upsertSnapshot(scoped(values));
    }
}
