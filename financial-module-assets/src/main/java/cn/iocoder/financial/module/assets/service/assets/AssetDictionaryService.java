package cn.iocoder.financial.module.assets.service.assets;

import cn.iocoder.financial.module.assets.controller.admin.assets.vo.*;

import java.util.List;
import java.util.Map;

/**
 * 资产字典及历史选项兼容 Service 接口
 */
public interface AssetDictionaryService {

    /**
     * 查询字典选项，包含停用标记
     */
    List<AssetDictionaryOptionRespVO> options(String type);

    /**
     * 查询当前用户账本配置
     */
    AssetDictionaryOptionsRespVO context();

    /**
     * 查询包含停用项的历史显示标签
     */
    Map<String, String> labels(String type);

    /**
     * 校验新选项，允许保留历史选项
     */
    void validateSelection(String type, String value, String previous);
}
