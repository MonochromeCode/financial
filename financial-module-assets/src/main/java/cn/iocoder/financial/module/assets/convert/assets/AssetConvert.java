package cn.iocoder.financial.module.assets.convert.assets;

import cn.iocoder.financial.framework.common.util.object.BeanUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 资产计算结果与接口 VO 的转换，统一保留金额精度和既有时间戳契约。
 */
public final class AssetConvert {

    private AssetConvert() {
    }

    public static <T> T convert(Object source, Class<T> targetType) {
        return BeanUtils.toBean(decimalStrings(source), targetType);
    }

    public static <T> List<T> convertList(List<Map<String, Object>> source, Class<T> targetType) {
        return source.stream().map(row -> convert(row, targetType)).toList();
    }

    private static Object decimalStrings(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal.toPlainString();
        }
        if (value instanceof Date date) {
            return date.getTime();
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((key, item) -> result.put(key.toString(), decimalStrings(item)));
            return result;
        }
        if (value instanceof List<?> list) {
            return list.stream().map(AssetConvert::decimalStrings).toList();
        }
        return value;
    }
}
