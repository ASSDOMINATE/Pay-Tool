package cn.hoxinte.pay.common.enums;

/**
 * 购买类型枚举
 *
 * @author dominate
 * @since 2023-04-13
 */
public enum BuyType {
    /**
     * 购买类型
     */
    COMMON("通用", 0),
    APPLE_SUB("苹果订阅", 1),
    ;

    final String name;
    final int code;

    BuyType(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public static BuyType getValueByCode(int code) {
        for (BuyType value : BuyType.values()) {
            if (code == value.code) {
                return value;
            }
        }
        return COMMON;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }

}
