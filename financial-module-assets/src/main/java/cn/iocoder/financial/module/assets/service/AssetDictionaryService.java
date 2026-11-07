package cn.iocoder.financial.module.assets.service;

import cn.iocoder.financial.framework.common.enums.CommonStatusEnum;
import cn.iocoder.financial.framework.common.exception.ServiceException;
import cn.iocoder.financial.module.assets.enums.AssetDictType;
import cn.iocoder.financial.module.system.dal.dataobject.dict.DictDataDO;
import cn.iocoder.financial.module.system.dal.dataobject.dict.DictTypeDO;
import cn.iocoder.financial.module.system.service.dict.DictDataService;
import cn.iocoder.financial.module.system.service.dict.DictTypeService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class AssetDictionaryService {
    public record Option(String value, String label, boolean disabled, String color, String description) {
    }

    public record Options(List<Option> categories, List<Option> templates, List<Option> buckets,
                          List<Option> insuranceTypes, List<Option> operations, List<Option> sources) {
    }

    private final DictDataService dataService;
    private final DictTypeService typeService;

    public AssetDictionaryService(DictDataService dataService, DictTypeService typeService) {
        this.dataService = dataService;
        this.typeService = typeService;
    }

    private boolean enabled(String type) {
        DictTypeDO definition = typeService.getDictType(type);
        return definition != null && CommonStatusEnum.ENABLE.getStatus().equals(definition.getStatus());
    }

    public List<Option> options(String type) {
        boolean typeEnabled = enabled(type);
        return dataService.getDictDataListByDictType(type).stream().map(item -> new Option(
                item.getValue(), item.getLabel(), !typeEnabled || !CommonStatusEnum.ENABLE.getStatus().equals(item.getStatus()),
                Objects.toString(item.getCssClass(), ""), Objects.toString(item.getRemark(), ""))).toList();
    }

    public Options context() {
        return new Options(options(AssetDictType.CATEGORY), options(AssetDictType.TEMPLATE),
                options(AssetDictType.FUND_BUCKET), options(AssetDictType.INSURANCE_TYPE),
                options(AssetDictType.OPERATION), options(AssetDictType.SOURCE));
    }

    public Map<String, String> labels(String type) {
        Map<String, String> result = new LinkedHashMap<>();
        dataService.getDictDataListByDictType(type).forEach(item -> result.put(item.getValue(), item.getLabel()));
        return result;
    }

    public void validateSelection(String type, String value, String previous) {
        if (value == null || Objects.equals(value, previous)) return;
        DictDataDO item = dataService.getDictData(type, value);
        if (!enabled(type) || item == null || !CommonStatusEnum.ENABLE.getStatus().equals(item.getStatus())) {
            throw new ServiceException(1_050_000_001, "选项不存在或已停用，请刷新页面后重新选择");
        }
    }
}
