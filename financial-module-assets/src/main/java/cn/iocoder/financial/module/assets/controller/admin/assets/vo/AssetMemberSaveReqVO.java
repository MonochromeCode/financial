package cn.iocoder.financial.module.assets.controller.admin.assets.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "管理后台 - 家庭成员请求")
@Data
public class AssetMemberSaveReqVO {

    @Schema(description = "编号")
    private Long id;
    @Schema(description = "关联的系统用户编号")
    private Long userId;
    @Schema(description = "成员名称")
    @NotBlank
    @Size(max = 40)
    private String displayName;
    @Schema(description = "排序")
    @Min(0)
    @Max(10000)
    private int sortOrder;
}
