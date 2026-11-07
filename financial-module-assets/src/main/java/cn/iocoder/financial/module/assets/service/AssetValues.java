package cn.iocoder.financial.module.assets.service;

import cn.iocoder.financial.framework.common.exception.ServiceException;
import cn.iocoder.financial.framework.security.core.util.SecurityFrameworkUtils;

import java.util.HashMap;
import java.util.Map;

public final class AssetValues {
    private AssetValues() {
    }

    public static ServiceException invalid(String message) {
        return new ServiceException(1_050_000_001, message);
    }

    public static ServiceException forbidden() {
        return new ServiceException(403, "没有权限操作该家庭成员的资产");
    }

    public static long userId() {
        Long id = SecurityFrameworkUtils.getLoginUserId();
        if (id == null) {
            throw new ServiceException(401, "请先登录");
        }
        return id;
    }

    public static long id(Map<String, Object> row, String key) {
        return ((Number) row.get(key)).longValue();
    }

    public static boolean flag(Object value) {
        return Boolean.TRUE.equals(value) || value instanceof Number number && number.intValue() == 1;
    }

    public static Map<String, Object> values(Object... pairs) {
        Map<String, Object> result = new HashMap<>();
        for (int index = 0; index < pairs.length; index += 2) {
            result.put((String) pairs[index], pairs[index + 1]);
        }
        return result;
    }
}
