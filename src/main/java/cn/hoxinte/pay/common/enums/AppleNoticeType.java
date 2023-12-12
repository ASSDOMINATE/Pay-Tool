package cn.hoxinte.pay.common.enums;

import java.util.Objects;

/**
 * 苹果通知类型枚举
 *
 * @author dominate
 * @since 2023-04-13
 */
public enum AppleNoticeType {
    /**
     * 购买类型
     */
    NO_FOLLOW_UP("无后续行动", "NO_FOLLOW_UP", 0),

    REFUND("取消购买", "REFUND", 1),
    DID_RENEW("订阅续费", "DID_RENEW", 2),
    RENEW_DISABLED("订阅取消", "RENEW_DISABLED", 3),
    DID_CHANGE_RENEWAL_STATUS("订阅续订状态的更改", "DID_CHANGE_RENEWAL_STATUS", "AUTO_RENEW_DISABLED", 4),
    ;

    final String name;
    final String sign;
    final String subSign;
    final int code;

    AppleNoticeType(String name, String sign, int code) {
        this.name = name;
        this.sign = sign;
        this.subSign = "";
        this.code = code;
    }

    AppleNoticeType(String name, String sign, String subSign, int code) {
        this.name = name;
        this.sign = sign;
        this.subSign = subSign;
        this.code = code;
    }

    public static AppleNoticeType getValueByCode(int code) {
        for (AppleNoticeType value : AppleNoticeType.values()) {
            if (code == value.code) {
                return value;
            }
        }
        return NO_FOLLOW_UP;
    }

    public static AppleNoticeType getValueBySign(String sign) {
        for (AppleNoticeType value : AppleNoticeType.values()) {
            if (Objects.equals(sign, value.sign)) {
                return value;
            }
        }
        return NO_FOLLOW_UP;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }

    public String getSign() {
        return sign;
    }

    public String getSubSign() {
        return subSign;
    }
}
