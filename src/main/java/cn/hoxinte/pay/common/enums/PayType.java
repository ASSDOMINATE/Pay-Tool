package cn.hoxinte.pay.common.enums;

import java.math.BigDecimal;

/**
 * 支付类型
 *
 * @author dominate
 * @since 2023-04-19
 */
public enum PayType {
    /**
     * 三方支付
     */
    NONE("无", 0),
    ALIPAY("支付宝", 1),
    WECHAT("微信", 2),
    APPLE("苹果", 3),
    WECHAT_NATIVE("微信-NATIVE", 4),
    ;

    private final String name;
    private final int dbCode;

    PayType(String name, int dbCode) {
        this.name = name;
        this.dbCode = dbCode;
    }

    public String getName() {
        return name;
    }

    public int getDbCode() {
        return dbCode;
    }

    public String payMessage(BigDecimal balance, String buyTargetName) {
        String balanceStr = balance.toString();
        balanceStr = balanceStr.substring(0, balanceStr.indexOf(".") + 3);
        return this.name + " 支付 " + balanceStr + " 购买 " + buyTargetName;
    }


    public static PayType getValueByDbCode(int dbCode) {
        for (PayType type : PayType.values()) {
            if (dbCode == type.getDbCode()) {
                return type;
            }
        }
        return NONE;
    }

    public static String createPayMessage(int dbCode, BigDecimal balance, String buyTargetName) {
        PayType type = getValueByDbCode(dbCode);
        return type.payMessage(balance, buyTargetName);
    }

}
