package cn.iocoder.financial.module.assets.service.assets;

import cn.iocoder.financial.framework.common.enums.CommonStatusEnum;
import cn.iocoder.financial.module.assets.controller.admin.assets.vo.*;
import cn.iocoder.financial.module.assets.enums.AssetDictType;
import cn.iocoder.financial.module.system.dal.dataobject.dict.DictDataDO;
import cn.iocoder.financial.module.system.dal.dataobject.dict.DictTypeDO;
import cn.iocoder.financial.module.system.service.dict.DictDataService;
import cn.iocoder.financial.module.system.service.dict.DictTypeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.financial.module.assets.service.assets.AssetValues.invalid;

/**
 * 资产字典及历史选项兼容 Service 实现类
 */
@Service
public class AssetDictionaryServiceImpl implements AssetDictionaryService {


    @Resource
    private DictDataService dataService;
    @Resource
    private DictTypeService typeService;

    private boolean enabled(String type) {
        DictTypeDO definition = typeService.getDictType(type);
        return definition != null && CommonStatusEnum.ENABLE.getStatus().equals(definition.getStatus());
    }

    @Override
    public List<AssetDictionaryOptionRespVO> options(String type) {
        boolean typeEnabled = enabled(type);
        return dataService.getDictDataListByDictType(type).stream().map(item -> new AssetDictionaryOptionRespVO(
                item.getValue(), item.getLabel(), !typeEnabled || !CommonStatusEnum.ENABLE.getStatus().equals(item.getStatus()),
                Objects.toString(item.getCssClass(), ""), Objects.toString(item.getRemark(), ""))).toList();
    }

    @Override
    public AssetDictionaryOptionsRespVO context() {
        return new AssetDictionaryOptionsRespVO(options(AssetDictType.CATEGORY), options(AssetDictType.TEMPLATE),
                options(AssetDictType.FUND_BUCKET), options(AssetDictType.INSURANCE_TYPE),
                options(AssetDictType.OPERATION), options(AssetDictType.SOURCE));
    }

    @Override
    public Map<String, String> labels(String type) {
        Map<String, String> result = new LinkedHashMap<>();
        dataService.getDictDataListByDictType(type).forEach(item -> result.put(item.getValue(), item.getLabel()));
        return result;
    }

    @Override
    public void validateSelection(String type, String value, String previous) {
        if (value == null || Objects.equals(value, previous)) return;
        DictDataDO item = dataService.getDictData(type, value);
        if (!enabled(type) || item == null || !CommonStatusEnum.ENABLE.getStatus().equals(item.getStatus())) {
            throw invalid("选项不存在或已停用，请刷新页面后重新选择");
        }
    }
}
