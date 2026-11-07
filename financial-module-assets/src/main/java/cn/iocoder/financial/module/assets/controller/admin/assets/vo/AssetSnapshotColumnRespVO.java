package cn.iocoder.financial.module.assets.controller.admin.assets.vo;

import cn.iocoder.financial.framework.common.pojo.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Schema(description = "管理后台 - 历史账户列响应")
@Data
public class AssetSnapshotColumnRespVO {

    @Schema(description = "账户编号")
    private String id;

    @Schema(description = "账户名称")
    private String name;

    @Schema(description = "成员编号")
    private Long memberId;

    @Schema(description = "分类编码")
    private String categoryCode;

}
