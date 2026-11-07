package cn.iocoder.financial.module.assets.enums;

import cn.iocoder.financial.framework.common.exception.ErrorCode;

/**
 * 家庭资产模块错误码。
 */
public interface ErrorCodeConstants {

    ErrorCode ASSET_INVALID = new ErrorCode(1_050_000_001, "{}");
    ErrorCode ASSET_FORBIDDEN = new ErrorCode(403, "没有权限操作该家庭成员的资产");

}
