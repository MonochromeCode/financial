package cn.iocoder.financial.module.assets.controller.admin.assets.vo;

import cn.iocoder.financial.framework.common.pojo.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Schema(description = "管理后台 - 资产字典配置响应")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetDictionaryOptionsRespVO {

    @Schema(description = "分类")
    private List<AssetDictionaryOptionRespVO> categories;

    @Schema(description = "账户模板")
    private List<AssetDictionaryOptionRespVO> templates;

    @Schema(description = "资金用途")
    private List<AssetDictionaryOptionRespVO> buckets;

    @Schema(description = "保险类型")
    private List<AssetDictionaryOptionRespVO> insuranceTypes;

    @Schema(description = "操作类型")
    private List<AssetDictionaryOptionRespVO> operations;

    @Schema(description = "数据来源")
    private List<AssetDictionaryOptionRespVO> sources;

}
