package cn.iocoder.financial.module.assets.service.assets;

import java.math.BigDecimal;
import java.util.*;

public final class AssetAccountSnapshots {
    private AssetAccountSnapshots() {
    }

    public static Map<String, Object> build(List<Map<String, Object>> records, List<String> snapshotDates,
                                             Set<Long> visibleMembers, boolean excludeFixed) {
        Map<Long, Map<String, Object>> states = new LinkedHashMap<>();
        Map<String, Map<String, Object>> columns = new LinkedHashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        Set<String> dates = new TreeSet<>(snapshotDates);
        for (Map<String, Object> record : records) {
            if (visibleMembers.contains(((Number) record.get("memberId")).longValue())) {
                dates.add(record.get("date").toString());
            }
        }
        int recordIndex = 0;
        for (String date : dates) {
            while (recordIndex < records.size() && records.get(recordIndex).get("date").toString().compareTo(date) <= 0) {
                Map<String, Object> record = records.get(recordIndex++);
                long accountId = ((Number) record.get("accountId")).longValue();
                boolean visible = visibleMembers.contains(((Number) record.get("memberId")).longValue());
                if (visible && !(excludeFixed && "fixed".equals(record.get("categoryCode")))) {
                    String key = Long.toString(accountId);
                    columns.put(key, Map.of("id", key, "name", record.get("accountName"),
                            "memberId", record.get("memberId"), "categoryCode", record.get("categoryCode")));
                }
                if ("delete".equals(record.get("operation"))) {
                    states.remove(accountId);
                } else {
                    states.put(accountId, record);
                }
            }
            rows.add(snapshot(date, states, visibleMembers, excludeFixed));
        }
        return Map.of("columns", new ArrayList<>(columns.values()), "rows", rows.reversed());
    }

    private static Map<String, Object> snapshot(String date, Map<Long, Map<String, Object>> states,
                                                 Set<Long> visibleMembers, boolean excludeFixed) {
        Map<String, Object> amounts = new LinkedHashMap<>();
        BigDecimal assets = BigDecimal.ZERO.setScale(2);
        BigDecimal liabilities = BigDecimal.ZERO.setScale(2);
        for (Map.Entry<Long, Map<String, Object>> entry : states.entrySet()) {
            Map<String, Object> record = entry.getValue();
            if (!visibleMembers.contains(((Number) record.get("memberId")).longValue())
                    || excludeFixed && "fixed".equals(record.get("categoryCode"))) continue;
            Object balance = record.get("currentBalance");
            amounts.put(entry.getKey().toString(), balance);
            BigDecimal amount = balance == null ? BigDecimal.ZERO : new BigDecimal(balance.toString());
            if ("liability".equals(record.get("categoryCode"))) liabilities = liabilities.add(amount);
            else assets = assets.add(amount);
        }
        return Map.of("date", date, "amounts", amounts, "totalAssets", assets,
                "totalLiabilities", liabilities, "netAssets", assets.subtract(liabilities));
    }
}
