package cn.iocoder.financial.module.assets.controller.admin.assets.vo;

import cn.iocoder.financial.framework.common.pojo.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Schema(description = "管理后台 - 资产组成响应")
@Data
@EqualsAndHashCode(callSuper = true)
public class AssetCompositionRespVO extends AssetCategoryRespVO {

    @Schema(description = "分类金额")
    private String amount;

    @Schema(description = "分类占比")
    private String ratio;

}
