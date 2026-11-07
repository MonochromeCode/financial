package cn.iocoder.financial.module.assets.controller.admin.assets.vo;

import cn.iocoder.financial.framework.common.pojo.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Schema(description = "管理后台 - 每日资产趋势响应")
@Data
@EqualsAndHashCode(callSuper = true)
public class AssetTrendRespVO extends AssetSummaryRespVO {

    @Schema(description = "记账日期")
    private String date;

    @Schema(description = "各分类金额")
    private Map<String, String> amounts;

}
