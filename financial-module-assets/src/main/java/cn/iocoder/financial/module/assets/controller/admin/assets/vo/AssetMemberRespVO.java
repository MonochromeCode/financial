package cn.iocoder.financial.module.assets.controller.admin.assets.vo;

import cn.iocoder.financial.framework.common.pojo.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Schema(description = "管理后台 - 家庭成员响应")
@Data
public class AssetMemberRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "关联的系统用户编号")
    private Long userId;

    @Schema(description = "成员名称")
    private String displayName;

    @Schema(description = "家庭角色")
    private String role;

    @Schema(description = "排序")
    private Integer sortOrder;

}
