package cn.iocoder.financial.module.assets.controller.admin.assets.vo;

import cn.iocoder.financial.framework.common.pojo.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Schema(description = "管理后台 - 家庭账本响应")
@Data
public class AssetFamilyRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "所有者用户编号")
    private Long ownerUserId;

    @Schema(description = "是否允许查看其他成员")
    private Boolean allowViewOthers;

    @Schema(description = "当前成员编号")
    private Long currentMemberId;

}
