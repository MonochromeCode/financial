package cn.iocoder.financial.module.assets.service;

import cn.iocoder.financial.module.assets.enums.AssetCategory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AssetAmounts {
    private static final List<String> CODES = AssetCategory.codes();

    private AssetAmounts() {
    }

    private static Map<String, Object> values(Object... pairs) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int index = 0; index < pairs.length; index += 2) {
            result.put((String) pairs[index], pairs[index + 1]);
        }
        return result;
    }

    public static BigDecimal amount(Object value) {
        return value == null ? BigDecimal.ZERO.setScale(2) : new BigDecimal(value.toString()).setScale(2);
    }

    public static Map<String, BigDecimal> totals(List<Map<String, Object>> accounts, boolean excludeFixed) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        CODES.forEach(code -> result.put(code, amount(null)));
        for (Map<String, Object> account : accounts) {
            String code = (String) account.get("categoryCode");
            if (!(excludeFixed && "fixed".equals(code))) {
                result.merge(code, amount(account.get("balance")), BigDecimal::add);
            }
        }
        return result;
    }

    public static Map<String, Object> summary(Map<String, BigDecimal> totals) {
        BigDecimal total = CODES.stream().filter(code -> !"liability".equals(code))
                .map(totals::get).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal liability = totals.get("liability");
        return values("totalAssets", total, "totalLiabilities", liability, "netAssets", total.subtract(liability),
                "debtRatio", total.signum() == 0 ? null : liability.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP));
    }

    public static Map<String, Object> monthlyChanges(List<Map<String, Object>> snapshots, LocalDate today) {
        Map<String, Object> latest = snapshots.isEmpty() ? null : snapshots.getLast();
        LocalDate latestDate = latest == null ? null : LocalDate.parse(latest.get("date").toString());
        LocalDate currentMonth = today.withDayOfMonth(1);
        boolean fallback = latestDate != null && latestDate.isBefore(currentMonth);
        LocalDate comparisonMonth = (latestDate == null ? today : latestDate).withDayOfMonth(1);
        Map<String, Object> baseline = snapshots.stream()
                .filter(row -> LocalDate.parse(row.get("date").toString()).isBefore(comparisonMonth))
                .reduce((previous, current) -> current).orElse(null);
        Map<String, Object> changes = new LinkedHashMap<>();
        for (String key : List.of("totalAssets", "totalLiabilities", "netAssets")) {
            changes.put(key, amount(latest == null ? null : latest.get(key))
                    .subtract(amount(baseline == null ? null : baseline.get(key))));
        }
        Map<?, ?> latestAmounts = latest == null ? Map.of() : (Map<?, ?>) latest.get("amounts");
        Map<?, ?> baselineAmounts = baseline == null ? Map.of() : (Map<?, ?>) baseline.get("amounts");
        Map<String, BigDecimal> categoryChanges = new LinkedHashMap<>();
        CODES.forEach(code -> categoryChanges.put(code, amount(latestAmounts.get(code)).subtract(amount(baselineAmounts.get(code)))));
        changes.put("categories", categoryChanges);
        changes.put("firstRecord", baseline == null);
        changes.put("baselineDate", baseline == null ? null : baseline.get("date"));
        changes.put("latestDate", latestDate == null ? null : latestDate.toString());
        changes.put("fallback", fallback);
        return changes;
    }
}
