package cn.iocoder.financial.server.module.assets.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public final class AssetWorkbook {
    private static final Map<String, String> BUCKETS = Map.of(
            "liquid", "活钱", "stable", "稳健资金", "long_term", "长期资金", "insurance", "保险保障");
    private static final Map<String, String> OPERATIONS = Map.of("create", "新增", "update", "修改", "delete", "删除");

    private AssetWorkbook() {
    }

    public static byte[] write(String familyName, String memberScope, boolean excludeFixed, boolean hidden,
                               Map<Long, String> members, Map<String, String> categories,
                               List<Map<String, Object>> accounts, Map<String, Object> summary,
                               List<Map<String, Object>> history) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Font headingFont = workbook.createFont();
            headingFont.setBold(true);
            headingFont.setColor(IndexedColors.WHITE.getIndex());
            CellStyle heading = workbook.createCellStyle();
            heading.setFont(headingFont);
            heading.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            heading.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            CellStyle money = workbook.createCellStyle();
            money.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

            Sheet overview = sheet(workbook, "资产汇总", List.of("项目", "内容"), heading);
            row(overview, money, "家庭账本", familyName);
            row(overview, money, "成员范围", memberScope);
            row(overview, money, "固定资产", excludeFixed ? "已排除" : "包含");
            row(overview, money, "导出时间", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            row(overview, money, "金额显示", hidden ? "已隐藏" : "人民币元；超过 Excel 15 位有效数字的金额以文本保留精度");
            row(overview, money, "总资产（元）", amount(summary.get("totalAssets"), hidden));
            row(overview, money, "总负债（元）", amount(summary.get("totalLiabilities"), hidden));
            row(overview, money, "净资产（元）", amount(summary.get("netAssets"), hidden));
            row(overview, money, "负债率", hidden ? "****" : ratio(summary.get("debtRatio")));
            row(overview, money, "账户数量", accounts.size());
            row(overview, money, "历史记录数量", history.size());
            overview.setColumnWidth(0, 24 * 256);
            overview.setColumnWidth(1, 72 * 256);

            Sheet details = sheet(workbook, "账户明细", List.of("成员", "资产分类", "账户名称", "余额（元）",
                    "资金用途", "保险类型", "数据日期", "数据来源", "备注"), heading);
            for (Map<String, Object> account : accounts) {
                String category = text(account.get("categoryCode"));
                String bucket = text(account.get("fundBucket"));
                row(details, money, members.getOrDefault(((Number) account.get("memberId")).longValue(), "家庭成员"),
                        categories.getOrDefault(category, category), text(account.get("name")),
                        amount(account.get("balance"), hidden),
                        "liability".equals(category) ? "不适用" : BUCKETS.getOrDefault(bucket, "未分配"),
                        text(account.get("insuranceType")), text(account.get("dataDate")),
                        "import".equals(account.get("sourceType")) ? "Excel 导入" : "手动更新", text(account.get("remark")));
            }
            details.setColumnWidth(2, 28 * 256);
            details.setColumnWidth(8, 45 * 256);
            details.setAutoFilter(new CellRangeAddress(0, details.getLastRowNum(), 0, 8));
            List<String> historyHeadings = List.of("记录编号", "账户编号", "成员", "资产分类", "操作时账户名称",
                    "变动前余额（元）", "变动后余额（元）", "变动金额（元）", "操作类型", "数据来源", "数据日期", "操作时间", "操作人", "操作人编号");
            Sheet historySheet = sheet(workbook, "历史账户明细", historyHeadings, heading);
            int historySheetNumber = 1;
            for (Map<String, Object> record : history) {
                if (historySheet.getLastRowNum() == 1_048_575) {
                    historySheet.setAutoFilter(new CellRangeAddress(0, historySheet.getLastRowNum(), 0, historyHeadings.size() - 1));
                    historySheet = sheet(workbook, "历史账户明细-" + ++historySheetNumber, historyHeadings, heading);
                }
                String category = text(record.get("categoryCode"));
                String operation = text(record.get("operation"));
                row(historySheet, money, text(record.get("id")), text(record.get("accountId")),
                        members.getOrDefault(((Number) record.get("memberId")).longValue(), "家庭成员"),
                        categories.getOrDefault(category, category), text(record.get("accountName")),
                        amount(record.get("previousBalance"), hidden), amount(record.get("currentBalance"), hidden),
                        amount(record.get("changeAmount"), hidden), OPERATIONS.getOrDefault(operation, operation),
                        "import".equals(record.get("sourceType")) ? "Excel 导入" : "手动更新",
                        text(record.get("dataDate")), text(record.get("createdAt")),
                        text(record.get("operatorName")), text(record.get("operatorUserId")));
            }
            historySheet.setAutoFilter(new CellRangeAddress(0, historySheet.getLastRowNum(), 0, historyHeadings.size() - 1));
            workbook.write(output);
            return output.toByteArray();
        }
    }

    private static Sheet sheet(Workbook workbook, String name, List<String> headings, CellStyle heading) {
        Sheet sheet = workbook.createSheet(name);
        Row row = sheet.createRow(0);
        row.setHeightInPoints(26);
        for (int index = 0; index < headings.size(); index++) {
            Cell cell = row.createCell(index);
            cell.setCellValue(headings.get(index));
            cell.setCellStyle(heading);
            sheet.setColumnWidth(index, 20 * 256);
        }
        sheet.createFreezePane(0, 1);
        return sheet;
    }

    private static void row(Sheet sheet, CellStyle money, Object... values) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        for (int index = 0; index < values.length; index++) {
            Cell cell = row.createCell(index);
            Object value = values[index];
            if (value instanceof BigDecimal decimal && decimal.stripTrailingZeros().precision() <= 15) {
                cell.setCellValue(decimal.doubleValue());
                cell.setCellStyle(money);
            } else if (value instanceof Integer count) {
                cell.setCellValue(count);
            } else if (value instanceof BigDecimal decimal) {
                cell.setCellValue(decimal.toPlainString());
            } else {
                cell.setCellValue(text(value));
            }
        }
    }

    private static Object amount(Object value, boolean hidden) {
        if (hidden) return "****";
        return value == null ? "待填写" : new BigDecimal(value.toString()).setScale(2);
    }

    private static String ratio(Object value) {
        return value == null ? "--" : value + "%";
    }

    private static String text(Object value) {
        return value == null ? "" : value.toString();
    }
}
