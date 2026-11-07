package cn.iocoder.financial.module.assets.controller.admin.assets.vo;

import cn.iocoder.financial.framework.common.pojo.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Schema(description = "管理后台 - 资产汇总响应")
@Data
public class AssetSummaryRespVO {

    @Schema(description = "总资产")
    private String totalAssets;

    @Schema(description = "总负债")
    private String totalLiabilities;

    @Schema(description = "净资产")
    private String netAssets;

    @Schema(description = "负债率，零资产时为空")
    private String debtRatio;

}
