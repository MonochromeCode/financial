package cn.iocoder.financial.module.assets.controller.admin.assets.vo;

import cn.iocoder.financial.framework.common.pojo.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Schema(description = "管理后台 - 历史日期账户余额响应")
@Data
public class AssetSnapshotRowRespVO {

    @Schema(description = "记账日期")
    private String date;

    @Schema(description = "逐账户余额，空值表示待填写")
    private Map<String, String> amounts;

    @Schema(description = "总资产")
    private String totalAssets;

    @Schema(description = "总负债")
    private String totalLiabilities;

    @Schema(description = "净资产")
    private String netAssets;

}
