package cn.iocoder.financial.module.assets.controller.admin.assets.vo;

import cn.iocoder.financial.framework.common.pojo.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Schema(description = "管理后台 - 资产字典选项响应")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetDictionaryOptionRespVO {

    @Schema(description = "字典编码")
    private String value;

    @Schema(description = "显示名称")
    private String label;

    @Schema(description = "是否停用")
    private boolean disabled;

    @Schema(description = "颜色")
    private String color;

    @Schema(description = "说明")
    private String description;

}
