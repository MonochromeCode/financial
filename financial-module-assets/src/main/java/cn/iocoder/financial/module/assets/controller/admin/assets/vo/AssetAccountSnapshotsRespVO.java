package cn.iocoder.financial.module.assets.controller.admin.assets.vo;

import cn.iocoder.financial.framework.common.pojo.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Schema(description = "管理后台 - 全部历史账户余额响应")
@Data
public class AssetAccountSnapshotsRespVO {

    @Schema(description = "账户表头")
    private List<AssetSnapshotColumnRespVO> columns;

    @Schema(description = "全部历史日期")
    private List<AssetSnapshotRowRespVO> rows;

}
