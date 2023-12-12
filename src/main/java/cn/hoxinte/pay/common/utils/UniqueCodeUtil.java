package cn.hoxinte.pay.common.utils;

import cn.hoxinte.tool.utils.RandomUtil;

/**
 * 随机编码生成工具
 *
 * @author ASSDOMINATE
 * @since 2022/05/13
 */
public final class UniqueCodeUtil {

    private static final int PAY_ORDER_CODE_LENGTH = 24;

    public static String createPayOrder(int payTypeCode) {
        return payTypeCode + RandomUtil.createUniqueCode(PAY_ORDER_CODE_LENGTH);
    }

}
