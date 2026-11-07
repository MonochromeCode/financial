package cn.iocoder.financial.module.assets.controller.admin.assets;

import cn.iocoder.financial.framework.common.pojo.CommonResult;
import cn.iocoder.financial.framework.common.pojo.PageResult;
import cn.iocoder.financial.module.assets.controller.admin.assets.vo.*;
import cn.iocoder.financial.module.assets.service.assets.AssetService;
import cn.iocoder.financial.module.assets.service.assets.AssetFamilyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
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
import java.util.List;

import static cn.iocoder.financial.framework.common.pojo.CommonResult.success;

@Tag(name = "家庭资产记账")
@RestController
@Validated
@RequestMapping("/finance/assets")
@PreAuthorize("isAuthenticated()")
public class AssetController {
    @Resource
    private AssetService assetService;

    @Resource
    private AssetFamilyService assetFamilyService;

    @GetMapping("/context")
    @Operation(summary = "获取当前用户的家庭、成员和资产分类配置")
    public CommonResult<AssetContextRespVO> context() {
        return success(assetFamilyService.context());
    }

    @PostMapping("/initialize")
    @Operation(summary = "建立家庭账本")
    public CommonResult<Long> initialize(@Valid @RequestBody AssetFamilySaveReqVO request) {
        return success(assetFamilyService.initialize(request));
    }

    @PutMapping("/family")
    @Operation(summary = "配置家庭权限")
    public CommonResult<Boolean> family(@Valid @RequestBody AssetFamilySaveReqVO request) {
        assetFamilyService.saveFamily(request);
        return success(true);
    }

    @PutMapping("/member")
    @Operation(summary = "新增或修改家庭成员")
    public CommonResult<Boolean> member(@Valid @RequestBody AssetMemberSaveReqVO request) {
        assetFamilyService.saveMember(request);
        return success(true);
    }

    @DeleteMapping("/member")
    @Operation(summary = "删除未关联资产和历史的家庭成员")
    public CommonResult<Boolean> deleteMember(@RequestParam long id) {
        assetFamilyService.deleteMember(id);
        return success(true);
    }

    @PutMapping("/category")
    @Operation(summary = "配置资产分类")
    public CommonResult<Boolean> category(@Valid @RequestBody AssetCategorySaveReqVO request) {
        assetFamilyService.saveCategory(request);
        return success(true);
    }

    @GetMapping("/accounts")
    @Operation(summary = "获取有权查看的资产账户")
    public CommonResult<List<AssetAccountRespVO>> accounts(@RequestParam(required = false) Long memberId,
                                                           @RequestParam(required = false) String category) {
        return success(assetService.accounts(memberId, category));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "获取家庭资产总览")
    public CommonResult<AssetDashboardRespVO> dashboard(@RequestParam(required = false) Long memberId,
                                                       @RequestParam(defaultValue = "false") boolean excludeFixed) {
        return success(assetService.dashboard(memberId, excludeFixed));
    }

    @GetMapping("/account-snapshots")
    @Operation(summary = "按历史记账日期查看可见账户的逐项余额")
    public CommonResult<AssetAccountSnapshotsRespVO> accountSnapshots(@RequestParam(required = false) Long memberId,
            @RequestParam(defaultValue = "false") boolean excludeFixed) {
        return success(assetService.accountSnapshots(memberId, excludeFixed));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出当前可见范围的家庭资产汇总、历史日期账户余额和全部变更记录")
    public ResponseEntity<byte[]> exportExcel(@RequestParam(required = false) Long memberId,
            @RequestParam(defaultValue = "false") boolean excludeFixed,
            @RequestParam(defaultValue = "false") boolean hidden) throws IOException {
        byte[] workbook = assetService.exportExcel(memberId, excludeFixed, hidden);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("家庭资产-" + LocalDate.now() + ".xlsx", StandardCharsets.UTF_8).build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(workbook);
    }

    @GetMapping("/trends")
    @Operation(summary = "获取按日去重的资产趋势和月历")
    public CommonResult<List<AssetTrendRespVO>> trends(@RequestParam(required = false) Long memberId,
            @RequestParam(defaultValue = "false") boolean excludeFixed,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return success(assetService.trends(memberId, excludeFixed, start, end));
    }

    @GetMapping("/history")
    @Operation(summary = "分页查询有权查看的更新记录及明细")
    public CommonResult<PageResult<AssetRecordRespVO>> history(@RequestParam(required = false) Long memberId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        return success(assetService.history(memberId, category, start, end, page, size));
    }

    @PostMapping("/submit")
    @Operation(summary = "原子提交资产修改、更新批次、账户记录和每日快照")
    public CommonResult<Long> submit(@Valid @RequestBody AssetSubmitReqVO request) {
        return success(assetService.submit(request));
    }
}
