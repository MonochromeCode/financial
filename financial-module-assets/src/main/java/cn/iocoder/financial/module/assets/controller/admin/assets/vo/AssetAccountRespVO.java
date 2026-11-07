package cn.iocoder.financial.module.assets.controller.admin.assets.vo;

import cn.iocoder.financial.framework.common.pojo.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Schema(description = "管理后台 - 资产账户响应")
@Data
public class AssetAccountRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "成员编号")
    private Long memberId;

    @Schema(description = "分类编码")
    private String categoryCode;

    @Schema(description = "账户名称")
    private String name;

    @Schema(description = "模板编码")
    private String templateCode;

    @Schema(description = "余额，空值表示待填写")
    private String balance;

    @Schema(description = "资金用途")
    private String fundBucket;

    @Schema(description = "保险类型")
    private String insuranceType;

    @Schema(description = "账户数据日期")
    private String dataDate;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "账户版本")
    private Long version;

    @Schema(description = "数据来源")
    private String sourceType;

    @Schema(description = "更新时间戳")
    private Long updatedAt;

}
