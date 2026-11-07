package cn.iocoder.financial.module.assets.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum AssetCategory {
    LIQUID("liquid", "wallet", "#3b82f6", "可随时或短期使用的资金，例如现金、银行卡、支付宝。长期投资应计入投资理财；计入总资产。"),
    FIXED("fixed", "house", "#a78bfa", "房产、车辆等实物资产，可录入当前估值。可在首页选择排除固定资产。"),
    INVESTMENT("investment", "chart", "#f59e0b", "基金、股票、理财产品及证券账户的当前市值。不含活期现金；计入总资产。"),
    RECEIVABLE("receivable", "handshake", "#14b8a6", "借款、押金、未到账款项等应归还资金。借入资金应计入负债；计入总资产。"),
    LIABILITY("liability", "credit-card", "#f97373", "房贷、车贷、信用卡等待偿还款项。请输入正数，由系统从总资产中扣除。");

    private final String code;
    private final String icon;
    private final String color;
    private final String description;

    public static List<String> codes() {
        return Arrays.stream(values()).map(AssetCategory::getCode).toList();
    }
}
