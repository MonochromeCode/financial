package cn.iocoder.financial.module.assets.service.assets;

import java.util.List;
import java.util.Map;

/**
 * 家庭及成员数据权限 Service 接口
 */
public interface AssetAccessService {
    record Scope(Map<String, Object> family, long familyId, long memberId, boolean owner,
                         List<Map<String, Object>> members, List<Long> visibleIds) {
    }

    /**
     * 获取家庭权限范围，写入时锁定家庭行
     */
    Scope scope(boolean lock);

    /**
     * 校验并返回可见成员编号
     */
    List<Long> visible(Scope scope, Long memberId);

    /**
     * 校验成员编辑权限
     */
    void editable(Scope scope, long memberId);

    /**
     * 校验家庭所有者权限
     */
    void owner(Scope scope);
}
