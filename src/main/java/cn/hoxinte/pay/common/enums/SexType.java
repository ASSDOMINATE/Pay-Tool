package cn.hoxinte.pay.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 性别枚举
 *
 * @author dominate
 * @since 2022/01/19
 */
public enum SexType {
    /**
     * 未知/未定义性别 默认值
     */
    UNKNOWN(0, "", "未知"),
    /**
     * 男
     */
    MALE(1, "M", "男"),
    /**
     * 女
     */
    FEMALE(2, "F", "女");

    final int code;
    final String sign;
    final String name;

    SexType(int code, String sign, String name) {
        this.code = code;
        this.sign = sign;
        this.name = name;
    }

    public static int checkSexCode(Integer sex) {
        if (null == sex) {
            return UNKNOWN.code;
        }
        for (SexType value : SexType.values()) {
            if (sex == value.code) {
                return value.code;
            }
        }
        return UNKNOWN.code;
    }

    public static int checkSexCode(String sex) {
        if (StringUtils.isEmpty(sex)) {
            return UNKNOWN.code;
        }
        for (SexType sexEnum : SexType.values()) {
            if (sex.equals(sexEnum.sign)) {
                return sexEnum.code;
            }
        }
        return UNKNOWN.code;
    }

    public int getCode() {
        return code;
    }

    public String getSign() {
        return sign;
    }

    public String getName() {
        return name;
    }
}
