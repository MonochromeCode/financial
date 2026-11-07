package cn.iocoder.financial.module.assets.service.assets;

import cn.iocoder.financial.module.assets.controller.admin.assets.vo.AssetCategorySaveReqVO;
import cn.iocoder.financial.module.assets.controller.admin.assets.vo.AssetContextRespVO;
import cn.iocoder.financial.module.assets.controller.admin.assets.vo.AssetFamilySaveReqVO;
import cn.iocoder.financial.module.assets.controller.admin.assets.vo.AssetMemberSaveReqVO;

/**
 * 家庭账本、成员与分类管理 Service 接口
 */
public interface AssetFamilyService {

    /**
     * 查询当前用户账本配置
     */
    AssetContextRespVO context();

    /**
     * 建立当前用户的家庭账本
     */
    long initialize(AssetFamilySaveReqVO request);

    /**
     * 保存家庭名称及查看权限
     */
    void saveFamily(AssetFamilySaveReqVO request);

    /**
     * 保存家庭成员及系统用户关联
     */
    void saveMember(AssetMemberSaveReqVO request);

    /**
     * 删除没有资产和历史的家庭成员
     */
    void deleteMember(long memberId);

    /**
     * 保存家庭分类配置
     */
    void saveCategory(AssetCategorySaveReqVO request);
}
