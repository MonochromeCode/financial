package cn.iocoder.financial.module.assets.controller.admin.assets.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "管理后台 - 账户变更请求")
@Data
public class AssetAccountSaveReqVO {

    @Schema(description = "编号")
    private Long id;
    @Schema(description = "账户版本")
    private Long version;
    @Schema(description = "成员编号")
    @NotNull
    private Long memberId;
    @Schema(description = "分类编码")
    @NotBlank
    @Size(max = 20)
    private String categoryCode;
    @Schema(description = "名称")
    @NotBlank
    @Size(max = 80)
    private String name;
    @Schema(description = "账户模板编码")

    @Size(max = 30)
    private String templateCode;
    @Schema(description = "余额，空值表示待填写")
    @DecimalMin("0")
    @Digits(integer = 16, fraction = 2)
    private BigDecimal balance;
    @Schema(description = "资金用途")

    @Size(max = 16)
    private String fundBucket;
    @Schema(description = "保险类型")

    @Size(max = 20)
    private String insuranceType;
    @Schema(description = "账户数据日期")
    @NotNull
    @PastOrPresent
    private LocalDate dataDate;
    @Schema(description = "排序")
    @Min(0)
    @Max(10000)
    private int sortOrder;
    @Schema(description = "备注")

    @Size(max = 500)
    private String remark;
    @Schema(description = "是否删除")
    private boolean deleted;

    /**
     * 保持已有批次摘要的账户文本格式，确保迁移前提交编号仍可幂等重试。
     */
    @Override
    public String toString() {
        return "AssetRequests.AccountChange(id=" + id + ", version=" + version + ", memberId=" + memberId + ", categoryCode=" + categoryCode + ", name=" + name + ", templateCode=" + templateCode + ", balance=" + balance + ", fundBucket=" + fundBucket + ", insuranceType=" + insuranceType + ", dataDate=" + dataDate + ", sortOrder=" + sortOrder + ", remark=" + remark + ", deleted=" + deleted + ")";
    }
}
