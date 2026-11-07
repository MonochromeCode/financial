package cn.iocoder.financial.module.assets.controller.admin.assets.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "管理后台 - 资产分类请求")
@Data
public class AssetCategorySaveReqVO {

    @Schema(description = "编号")
    @NotNull
    private Long id;
    @Schema(description = "名称")
    @NotBlank
    @Size(max = 40)
    private String name;
    @Schema(description = "图标")
    @NotBlank
    @Size(max = 40)
    private String icon;
    @Schema(description = "颜色")
    @NotNull
    @Pattern(regexp = "#[0-9a-fA-F]{6}")
    private String color;
    @Schema(description = "说明")
    @NotBlank
    @Size(max = 400)
    private String description;
    @Schema(description = "排序")
    @Min(0)
    @Max(10000)
    private int sortOrder;
    @Schema(description = "是否启用")
    private boolean enabled;
}
