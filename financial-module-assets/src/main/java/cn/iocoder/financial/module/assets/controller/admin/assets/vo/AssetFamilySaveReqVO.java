package cn.iocoder.financial.module.assets.controller.admin.assets.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "管理后台 - 家庭配置请求")
@Data
public class AssetFamilySaveReqVO {

    @Schema(description = "名称")
    @NotBlank
    @Size(max = 80)
    private String name;
    @Schema(description = "是否允许查看其他成员")
    private boolean allowViewOthers;
}
