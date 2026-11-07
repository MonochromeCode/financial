package cn.iocoder.financial.module.assets.service.assets;

import cn.iocoder.financial.module.assets.controller.admin.assets.vo.*;
import cn.iocoder.financial.module.assets.convert.assets.AssetConvert;

import cn.iocoder.financial.framework.tenant.core.context.TenantContextHolder;



import cn.iocoder.financial.module.assets.dal.assets.AssetRepository;
import cn.iocoder.financial.module.assets.enums.AssetCategory;
import cn.iocoder.financial.module.assets.enums.AssetDictType;
import cn.iocoder.financial.module.assets.service.assets.AssetAccessService.Scope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.financial.module.assets.service.assets.AssetValues.id;
import static cn.iocoder.financial.module.assets.service.assets.AssetValues.invalid;
import static cn.iocoder.financial.module.assets.service.assets.AssetValues.userId;
import static cn.iocoder.financial.module.assets.service.assets.AssetValues.values;

/**
 * 家庭账本、成员与分类管理 Service 实现类
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AssetFamilyServiceImpl implements AssetFamilyService {
    @Resource
    private AssetRepository repository;
    @Resource
    private AssetAccessService access;
    @Resource
    private AssetDictionaryService dictionaries;
    @Value("${financial.assets.max-account-balance:999999999999.99}")
    private BigDecimal maxBalance;

    @Override
    public AssetContextRespVO context() {
        if (repository.selectFamily(Map.of("userId", userId())) == null) {
            return AssetConvert.convert(values("family", null, "userId", userId(), "tenantId", TenantContextHolder.getRequiredTenantId(),
                    "options", dictionaries.context()), AssetContextRespVO.class);
        }
        Scope scope = access.scope(false);
        return AssetConvert.convert(values("family", scope.family(), "userId", userId(), "tenantId", TenantContextHolder.getRequiredTenantId(),
                "owner", scope.owner(), "currentMemberId", scope.memberId(), "members", scope.members().stream()
                        .filter(member -> scope.visibleIds().contains(id(member, "id"))).toList(),
                "categories", repository.selectCategories(Map.of("familyId", scope.familyId())),
                "maxBalance", maxBalance.toPlainString(), "importEnabled", false, "syncEnabled", false,
                "options", dictionaries.context()), AssetContextRespVO.class);
    }

    @Override
    public long initialize(AssetFamilySaveReqVO request) {
        if (repository.selectFamily(Map.of("userId", userId())) != null) {
            throw invalid("你已经加入家庭账本");
        }
        long familyId;
        try {
            familyId = repository.insertFamily(values("name", request.getName().trim(), "userId", userId(), "allow", request.isAllowViewOthers()));
            repository.insertOwner(Map.of("familyId", familyId, "userId", userId()));
        } catch (DuplicateKeyException exception) {
            throw invalid("你已经加入家庭账本，请刷新页面");
        }
        Map<String, String> categoryLabels = dictionaries.labels(AssetDictType.CATEGORY);
        for (AssetCategory category : AssetCategory.values()) {
            dictionaries.validateSelection(AssetDictType.CATEGORY, category.getCode(), null);
            repository.insertCategory(values("familyId", familyId, "code", category.getCode(),
                    "name", categoryLabels.get(category.getCode()), "icon", category.getIcon(),
                    "color", category.getColor(), "description", category.getDescription(), "sort", category.ordinal()));
        }
        return familyId;
    }

    @Override
    public void saveFamily(AssetFamilySaveReqVO request) {
        Scope scope = access.scope(true);
        access.owner(scope);
        repository.updateFamily(values("familyId", scope.familyId(), "name", request.getName().trim(), "allow", request.isAllowViewOthers()));
    }

    @Override
    public void saveMember(AssetMemberSaveReqVO request) {
        Scope scope = access.scope(true);
        access.owner(scope);
        if (request.getUserId() != null && repository.selectEnabledUser(Map.of("userId", request.getUserId())) == null) {
            throw invalid("关联的系统用户不存在或已停用");
        }
        if (request.getId() != null) {
            Map<String, Object> member = scope.members().stream().filter(item -> id(item, "id") == request.getId())
                    .findFirst().orElseThrow(AssetValues::forbidden);
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
                repository.insertMember(params);
            } else {
                repository.updateMember(params);
            }
        } catch (DuplicateKeyException exception) {
            throw invalid("该系统用户已经关联家庭成员，请勿重复关联");
        }
    }

    @Override
    public void deleteMember(long memberId) {
        Scope scope = access.scope(true);
        access.owner(scope);
        access.editable(scope, memberId);
        if (memberId == scope.memberId()) {
            throw invalid("不能删除家庭所有者");
        }
        Map<String, Object> params = Map.of("familyId", scope.familyId(), "memberId", memberId);
        if (repository.selectMemberAccount(params) != null
                || repository.selectMemberSnapshot(params) != null
                || repository.selectMemberRecord(params) != null) {
            throw invalid("该成员已有关联资产或历史快照，不能删除；可以修改名称或解除用户关联");
        }
        repository.deleteMember(params);
    }

    @Override
    public void saveCategory(AssetCategorySaveReqVO request) {
        Scope scope = access.scope(true);
        access.owner(scope);
        Map<String, Object> category = repository.selectCategory(Map.of("familyId", scope.familyId(), "id", request.getId()));
        if (category == null) {
            throw invalid("分类不存在");
        }
        if (!request.isEnabled() && repository.selectCategoryAccount(Map.of("familyId", scope.familyId(), "code", category.get("code"))) != null) {
            throw invalid("分类下还有账户，请先转移或删除账户后再停用");
        }
        repository.updateCategory(values("familyId", scope.familyId(), "id", request.getId(), "name", request.getName().trim(),
                "icon", request.getIcon(), "color", request.getColor(), "description", request.getDescription(),
                "sort", request.getSortOrder(), "enabled", request.isEnabled()));
    }
}
