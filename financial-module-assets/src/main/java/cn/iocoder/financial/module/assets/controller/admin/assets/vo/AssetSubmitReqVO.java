package cn.iocoder.financial.module.assets.controller.admin.assets.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "管理后台 - 资产记账提交请求")
@Data
public class AssetSubmitReqVO {

    @Schema(description = "提交去重编号")
    @NotBlank
    @Pattern(regexp = "[a-zA-Z0-9-]{16,64}")
    private String requestId;
    @Schema(description = "记账日期")
    @NotNull
    @PastOrPresent
    private LocalDate updateDate;
    @Schema(description = "变更的账户")
    @NotEmpty
    @Size(max = 300)
    private List<@Valid AssetAccountSaveReqVO> accounts;
}
