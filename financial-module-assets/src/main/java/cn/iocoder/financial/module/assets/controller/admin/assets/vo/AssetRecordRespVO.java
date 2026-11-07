package cn.iocoder.financial.module.assets.controller.admin.assets.vo;

import cn.iocoder.financial.framework.common.pojo.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Schema(description = "管理后台 - 账户变动记录响应")
@Data
public class AssetRecordRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "批次编号")
    private Long batchId;

    @Schema(description = "账户编号")
    private Long accountId;

    @Schema(description = "成员编号")
    private Long memberId;

    @Schema(description = "分类编码")
    private String categoryCode;

    @Schema(description = "操作时的账户名称")
    private String accountName;

    @Schema(description = "变更前余额")
    private String previousBalance;

    @Schema(description = "变更后余额")
    private String currentBalance;

    @Schema(description = "变动金额")
    private String changeAmount;

    @Schema(description = "操作类型")
    private String operation;

    @Schema(description = "数据来源")
    private String sourceType;

    @Schema(description = "操作用户编号")
    private Long operatorUserId;

    @Schema(description = "账户数据日期")
    private String dataDate;

    @Schema(description = "操作时间戳")
    private Long createdAt;

    @Schema(description = "操作人名称")
    private String operatorName;

}
