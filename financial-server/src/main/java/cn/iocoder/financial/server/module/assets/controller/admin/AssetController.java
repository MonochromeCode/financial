package cn.iocoder.financial.server.module.assets.controller.admin;

import cn.iocoder.financial.framework.common.pojo.CommonResult;
import cn.iocoder.financial.server.module.assets.controller.admin.vo.AssetRequests.*;
import cn.iocoder.financial.server.module.assets.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.financial.framework.common.pojo.CommonResult.success;

@Tag(name = "家庭资产记账")
@RestController
@RequestMapping("/finance/assets")
@PreAuthorize("isAuthenticated()")
public class AssetController {
    private final AssetService service;

    public AssetController(AssetService service) {
        this.service = service;
    }

    private Object decimalStrings(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal.toPlainString();
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((key, item) -> result.put(key.toString(), decimalStrings(item)));
            return result;
        }
        if (value instanceof List<?> list) {
            return list.stream().map(this::decimalStrings).toList();
        }
        return value;
    }

    @GetMapping("/context")
    @Operation(summary = "获取当前用户的家庭、成员和资产分类配置")
    public CommonResult<?> context() {
        return success(decimalStrings(service.context()));
    }

    @PostMapping("/initialize")
    @Operation(summary = "建立家庭账本")
    public CommonResult<Long> initialize(@Valid @RequestBody FamilySave request) {
        return success(service.initialize(request));
    }

    @PutMapping("/family")
    @Operation(summary = "配置家庭权限")
    public CommonResult<Boolean> family(@Valid @RequestBody FamilySave request) {
        service.saveFamily(request);
        return success(true);
    }

    @PutMapping("/member")
    @Operation(summary = "新增或修改家庭成员")
    public CommonResult<Boolean> member(@Valid @RequestBody MemberSave request) {
        service.saveMember(request);
        return success(true);
    }

    @DeleteMapping("/member")
    @Operation(summary = "删除未关联资产和历史的家庭成员")
    public CommonResult<Boolean> deleteMember(@RequestParam long id) {
        service.deleteMember(id);
        return success(true);
    }

    @PutMapping("/category")
    @Operation(summary = "配置资产分类")
    public CommonResult<Boolean> category(@Valid @RequestBody CategorySave request) {
        service.saveCategory(request);
        return success(true);
    }

    @GetMapping("/accounts")
    @Operation(summary = "获取有权查看的资产账户")
    public CommonResult<?> accounts(@RequestParam(required = false) Long memberId,
                                                           @RequestParam(required = false) String category) {
        return success(decimalStrings(service.accounts(memberId, category)));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "获取家庭资产总览")
    public CommonResult<?> dashboard(@RequestParam(required = false) Long memberId,
                                                       @RequestParam(defaultValue = "false") boolean excludeFixed) {
        return success(decimalStrings(service.dashboard(memberId, excludeFixed)));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出当前可见范围的家庭资产汇总、当前账户和全部历史明细")
    public ResponseEntity<byte[]> exportExcel(@RequestParam(required = false) Long memberId,
            @RequestParam(defaultValue = "false") boolean excludeFixed,
            @RequestParam(defaultValue = "false") boolean hidden) throws IOException {
        byte[] workbook = service.exportExcel(memberId, excludeFixed, hidden);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("家庭资产-" + LocalDate.now() + ".xlsx", StandardCharsets.UTF_8).build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(workbook);
    }

    @GetMapping("/trends")
    @Operation(summary = "获取按日去重的资产趋势和月历")
    public CommonResult<?> trends(@RequestParam(required = false) Long memberId,
            @RequestParam(defaultValue = "false") boolean excludeFixed,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return success(decimalStrings(service.trends(memberId, excludeFixed, start, end)));
    }

    @GetMapping("/history")
    @Operation(summary = "分页查询有权查看的更新记录及明细")
    public CommonResult<?> history(@RequestParam(required = false) Long memberId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        return success(decimalStrings(service.history(memberId, category, start, end, page, size)));
    }

    @PostMapping("/submit")
    @Operation(summary = "原子提交资产修改、更新批次、账户记录和每日快照")
    public CommonResult<Long> submit(@Valid @RequestBody Submit request) {
        return success(service.submit(request));
    }
}
