package cn.iocoder.financial.server.module.assets.controller.admin.vo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class AssetRequests {
    private AssetRequests() {
    }

    @Data
    public static class FamilySave {
        @NotBlank @Size(max = 80)
        private String name;
        private boolean allowViewOthers;
    }

    @Data
    public static class MemberSave {
        private Long id;
        private Long userId;
        @NotBlank @Size(max = 40)
        private String displayName;
        @Min(0) @Max(10000)
        private int sortOrder;
    }

    @Data
    public static class CategorySave {
        @NotNull
        private Long id;
        @NotBlank @Size(max = 40)
        private String name;
        @NotBlank @Size(max = 40)
        private String icon;
        @NotNull @Pattern(regexp = "#[0-9a-fA-F]{6}")
        private String color;
        @NotBlank @Size(max = 400)
        private String description;
        @Min(0) @Max(10000)
        private int sortOrder;
        private boolean enabled;
    }

    @Data
    public static class AccountChange {
        private Long id;
        private Long version;
        @NotNull
        private Long memberId;
        @NotBlank @Size(max = 20)
        private String categoryCode;
        @NotBlank @Size(max = 80)
        private String name;
        @Size(max = 30)
        private String templateCode;
        @DecimalMin("0") @Digits(integer = 16, fraction = 2)
        private BigDecimal balance;
        @Pattern(regexp = "liquid|stable|long_term|insurance")
        private String fundBucket;
        @Pattern(regexp = "意外|医疗|重疾|寿险|其他")
        private String insuranceType;
        @NotNull @PastOrPresent
        private LocalDate dataDate;
        @Min(0) @Max(10000)
        private int sortOrder;
        @Size(max = 500)
        private String remark;
        private boolean deleted;
    }

    @Data
    public static class Submit {
        @NotBlank @Pattern(regexp = "[a-zA-Z0-9-]{16,64}")
        private String requestId;
        @NotNull @PastOrPresent
        private LocalDate updateDate;
        @NotEmpty @Size(max = 300)
        private List<@Valid AccountChange> accounts;
    }
}
