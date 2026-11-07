package cn.iocoder.financial.module.assets.service.assets;

import cn.iocoder.financial.framework.common.pojo.PageResult;
import cn.iocoder.financial.module.assets.controller.admin.assets.vo.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * 资产查询、历史及原子记账 Service 接口
 */
public interface AssetService {

    /**
     * 查询可见账户
     */
    List<AssetAccountRespVO> accounts(Long memberId, String category);

    /**
     * 还原全部历史日期的账户余额
     */
    AssetAccountSnapshotsRespVO accountSnapshots(Long memberId, boolean excludeFixed);

    /**
     * 导出可见范围的汇总、当前账户和全部历史
     */
    byte[] exportExcel(Long memberId, boolean excludeFixed, boolean hidden) throws IOException;

    /**
     * 查询可见成员的每日趋势
     */
    List<AssetTrendRespVO> trends(Long memberId, boolean excludeFixed, LocalDate start, LocalDate end);

    /**
     * 查询资产总览和近期记录
     */
    AssetDashboardRespVO dashboard(Long memberId, boolean excludeFixed);

    /**
     * 分页查询可见账户变动记录
     */
    PageResult<AssetRecordRespVO> history(Long memberId, String category, LocalDate start, LocalDate end, int page, int size);

    /**
     * 原子保存账户、批次、变动记录及每日快照
     */
    long submit(AssetSubmitReqVO request);
}
