package cn.iocoder.financial.module.assets.controller.admin.assets.vo;

import cn.iocoder.financial.framework.common.pojo.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Schema(description = "管理后台 - 家庭资产总览响应")
@Data
public class AssetDashboardRespVO {

    @Schema(description = "汇总")
    private AssetOverviewRespVO overview;

    @Schema(description = "可见账户")
    private List<AssetAccountRespVO> accounts;

    @Schema(description = "月度变化")
    private AssetMonthlyRespVO monthly;

    @Schema(description = "资产组成")
    private List<AssetCompositionRespVO> composition;

    @Schema(description = "各资金用途金额")
    private Map<String, String> buckets;

    @Schema(description = "保险类型")
    private Set<String> insuranceTypes;

    @Schema(description = "账户数量")
    private Integer accountCount;

    @Schema(description = "每日趋势")
    private List<AssetTrendRespVO> trends;

    @Schema(description = "最近记录")
    private PageResult<AssetRecordRespVO> records;

}
