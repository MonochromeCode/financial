package cn.iocoder.financial.module.assets.controller.admin.assets.vo;

import cn.iocoder.financial.framework.common.pojo.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Schema(description = "管理后台 - 月度变化响应")
@Data
public class AssetMonthlyRespVO {

    @Schema(description = "总资产变化")
    private String totalAssets;

    @Schema(description = "总负债变化")
    private String totalLiabilities;

    @Schema(description = "净资产变化")
    private String netAssets;

    @Schema(description = "各分类变化")
    private Map<String, String> categories;

    @Schema(description = "是否首次记录")
    private Boolean firstRecord;

    @Schema(description = "对比日期")
    private String baselineDate;

    @Schema(description = "最新记账日期")
    private String latestDate;

    @Schema(description = "是否使用最近有记录月份")
    private Boolean fallback;

}
