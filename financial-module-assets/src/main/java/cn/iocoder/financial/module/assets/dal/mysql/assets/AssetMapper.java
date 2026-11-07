package cn.iocoder.financial.module.assets.dal.mysql.assets;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
@InterceptorIgnore(tenantLine = "true", dataPermission = "true")
public interface AssetMapper {
    Map<String, Object> selectFamily(Map<String, Object> params);

    List<Map<String, Object>> selectMembers(Map<String, Object> params);

    List<Map<String, Object>> selectCategories(Map<String, Object> params);

    int insertFamily(Map<String, Object> params);

    int insertOwner(Map<String, Object> params);

    int insertCategory(Map<String, Object> params);

    int updateFamily(Map<String, Object> params);

    Map<String, Object> selectEnabledUser(Map<String, Object> params);

    int insertMember(Map<String, Object> params);

    int updateMember(Map<String, Object> params);

    Map<String, Object> selectMemberAccount(Map<String, Object> params);

    Map<String, Object> selectMemberSnapshot(Map<String, Object> params);

    Map<String, Object> selectMemberRecord(Map<String, Object> params);

    int deleteMember(Map<String, Object> params);

    Map<String, Object> selectCategory(Map<String, Object> params);

    Map<String, Object> selectCategoryAccount(Map<String, Object> params);

    int updateCategory(Map<String, Object> params);

    List<Map<String, Object>> selectAccounts(Map<String, Object> params);

    List<Map<String, Object>> selectAccountHistory(Map<String, Object> params);

    List<Map<String, Object>> selectSnapshotDates(Map<String, Object> params);

    List<Map<String, Object>> selectExportAccounts(Map<String, Object> params);

    List<Map<String, Object>> selectExportHistory(Map<String, Object> params);

    List<Map<String, Object>> selectSnapshots(Map<String, Object> params);

    List<Map<String, Object>> selectVisibleAccounts(Map<String, Object> params);

    Map<String, Object> selectLatestUpdate(Map<String, Object> params);

    List<Map<String, Object>> selectHistory(Map<String, Object> params);

    Map<String, Object> countHistory(Map<String, Object> params);

    Map<String, Object> selectBatch(Map<String, Object> params);

    Map<String, Object> selectLatestSnapshot(Map<String, Object> params);

    List<Map<String, Object>> selectFamilyAccounts(Map<String, Object> params);

    int insertBatch(Map<String, Object> params);

    int insertAccount(Map<String, Object> params);

    int updateAccount(Map<String, Object> params);

    int insertRecord(Map<String, Object> params);

    Map<String, Object> selectDuplicateTemplate(Map<String, Object> params);

    int updateBatchTotals(Map<String, Object> params);

    int upsertSnapshot(Map<String, Object> params);
}
