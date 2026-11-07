package cn.iocoder.financial.module.assets.service.assets;

import cn.iocoder.financial.framework.common.exception.ServiceException;
import cn.iocoder.financial.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.financial.framework.security.core.util.SecurityFrameworkUtils;

import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.financial.module.assets.enums.ErrorCodeConstants.ASSET_FORBIDDEN;
import static cn.iocoder.financial.module.assets.enums.ErrorCodeConstants.ASSET_INVALID;

public final class AssetValues {
    private AssetValues() {
    }

    public static ServiceException invalid(String message) {
        return ServiceExceptionUtil.exception(ASSET_INVALID, message);
    }

    public static ServiceException forbidden() {
        return ServiceExceptionUtil.exception(ASSET_FORBIDDEN);
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
