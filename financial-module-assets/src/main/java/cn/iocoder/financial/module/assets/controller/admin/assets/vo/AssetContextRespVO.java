package cn.iocoder.financial.module.assets.controller.admin.assets.vo;

import cn.iocoder.financial.framework.common.pojo.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Schema(description = "管理后台 - 家庭资产配置响应")
@Data
public class AssetContextRespVO {

    @Schema(description = "家庭账本，未加入时为空")
    private AssetFamilyRespVO family;

    @Schema(description = "当前用户编号")
    private Long userId;

    @Schema(description = "当前租户编号")
    private Long tenantId;

    @Schema(description = "是否为所有者")
    private Boolean owner;

    @Schema(description = "当前成员编号")
    private Long currentMemberId;

    @Schema(description = "可见成员")
    private List<AssetMemberRespVO> members;

    @Schema(description = "家庭分类")
    private List<AssetCategoryRespVO> categories;

    @Schema(description = "单账户金额上限")
    private String maxBalance;

    @Schema(description = "是否开放导入")
    private Boolean importEnabled;

    @Schema(description = "是否开放同步")
    private Boolean syncEnabled;

    @Schema(description = "字典选项")
    private AssetDictionaryOptionsRespVO options;

}
