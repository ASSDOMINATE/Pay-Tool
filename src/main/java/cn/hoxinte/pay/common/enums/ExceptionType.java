package cn.hoxinte.pay.common.enums;

/**
 * 异常枚举
 *
 * @author dominate
 * @since 2022/07/11
 */
public enum ExceptionType {

    /**
     * 自定义异常
     */
    UNKNOWN(ResponseType.FAILED),

    INVALID_TOKEN(ResponseType.INVALID_TOKEN),
    LOGIN_STATE_ERROR(ResponseType.LOGIN_STATUS_ERROR),
    NO_PERMISSION(ResponseType.NO_PERMISSION),

    PARAM_ERROR(ResponseType.HAS_WRONG_PARAM),

    ERROR(ResponseType.ERROR.getCode(), "请求异常"),
    EMPTY_ERROR(ResponseType.ERROR.getCode(), ""),

    APPLE_ORDER_USED(ResponseType.ERROR.getCode(), "当前订单已被其他账号使用"),
    APPLE_ORDER_PRODUCT_WRONG(ResponseType.ERROR.getCode(), "当前订单绑定的商品有误"),


    HAS_CARD_BINDING(ResponseType.ERROR.getCode(), "存在已绑定的会员卡"),
    NOT_FOUND_CARD(ResponseType.ERROR.getCode(), "无效的会员卡"),

    USER_INFO_EXISTED(ResponseType.ERROR.getCode(), "用户信息已存在"),
    USER_INFO_NAME_EXISTED(ResponseType.ERROR.getCode(), "该用户名称已存在"),
    USER_INFO_PHONE_EXISTED(ResponseType.ERROR.getCode(), "该手机号已绑定其他账号"),
    USER_INFO_EMAIL_EXISTED(ResponseType.ERROR.getCode(), "该邮箱已绑定其他账号"),
    USER_INFO_IDENTITY_EXISTED(ResponseType.ERROR.getCode(), "该身份证号已存在"),

    LOGIN_NOT_BIND_PHONE(ResponseType.ERROR.getCode(), "此手机号未绑定任何账号"),
    SEND_SMS_ERROR(ResponseType.ERROR.getCode(), "验证码发送失败，可能手机号已到发送限制"),
    NOT_BIND_PHONE(ResponseType.ERROR.getCode(), "未绑定手机号"),
    IS_BIND_PHONE(ResponseType.ERROR.getCode(), "已绑定手机号，请解除绑后再进行绑定"),
    USER_PASSWORD_ERROR(ResponseType.WRONG_PASSWORD),

    NOT_BUY_USING(ResponseType.CARD_NOT_BUY_ERROR.getCode(), "没有购买过会员卡"),
    NOT_CARD_USING(ResponseType.CARD_NOT_VALID_ERROR.getCode(), "没有生效的会员卡"),

    SEND_FREQ_LIMIT(ResponseType.ERROR.getCode(), "已达到发送频率限制，请稍后再试，或购买会员解除限制"),
    SEND_COUNT_LIMIT(ResponseType.ERROR.getCode(), "已达到发送次数限制，购买会员可解除限制"),
    SEND_CARD_LIMIT(ResponseType.CARD_NOT_VALID_ERROR.getCode(), "会员权益已使用完，请购买后再试"),


    PAY_ORDER_TYPE_ERROR(ResponseType.ERROR.getCode(), "支付订单对应支付类型错误"),
    PAY_ORDER_NOT_FOUND(ResponseType.ERROR.getCode(), "支付订单号错误"),
    PAY_ORDER_MUST_SAME_USER(ResponseType.ERROR.getCode(), "支付订单号必须是下单用户确认"),

    PAY_NOT_FOUND_ORDER(ResponseType.ERROR.getCode(), "无此订单信息"),
    PAY_NOT_COMPLETED(ResponseType.ERROR.getCode(), "支付正在处理中，如已完成将会自动到账"),
    PAY_PRICE_ERROR(ResponseType.ERROR.getCode(), "支付金额错误"),


    ;

    final int code;
    final String message;

    ExceptionType(int code, String message) {
        this.code = code;
        this.message = message;
    }

    ExceptionType(ResponseType responseType) {
        this.code = responseType.getCode();
        this.message = responseType.getMsg();
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
