package cn.iocoder.financial.module.assets.controller.admin.assets.vo;

import cn.iocoder.financial.framework.common.pojo.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Schema(description = "管理后台 - 资产总览响应")
@Data
@EqualsAndHashCode(callSuper = true)
public class AssetOverviewRespVO extends AssetSummaryRespVO {

    @Schema(description = "最近更新时间戳")
    private Long updatedAt;

}
