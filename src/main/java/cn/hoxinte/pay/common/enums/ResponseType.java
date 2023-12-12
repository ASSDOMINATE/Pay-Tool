package cn.hoxinte.pay.common.enums;

/**
 * 返回结果枚举
 *
 * @author null
 * @date 2021/11/25
 */
public enum ResponseType {

    /**
     * code: 200
     * 请求成功
     */
    SUCCESS(200, "请求成功", true),


    RESUME_SUCCESS(200,"已恢复全部购买信息",true),
    /**
     * code:400
     * 异常
     */
    ERROR(400, "异常", false),
    /**
     * code：402
     * 登录状态异常
     */
    LOGIN_STATUS_ERROR(402, "登录状态异常", false),
    /**
     * code：403
     * 参数错误
     */
    HAS_WRONG_PARAM(403, "参数错误", false),
    /**
     * code：404
     * 资源不存在
     */
    SOURCE_NOT_FOUND(404, "资源不存在", false),
    /**
     * code：405
     * 密码错误
     */
    WRONG_PASSWORD(405, "密码错误", false),
    /**
     * code：406
     * 登录标识不存在
     */
    LOGIN_SIGN_NOT_FOUND(406, "登陆账号不存在", false),
    /**
     * code：407
     * 身份状态错误
     */
    IDENTITY_STATE_WRONG(407, "身份状态错误", false),
    /**
     * code :408
     * 无效 Token
     */
    INVALID_TOKEN(408, "请登录后使用", false),
    /**
     * code :410
     * 冲突
     */
    CONFLICT(410, "冲突", false),
    /**
     * code:411
     * 资源不唯一
     */
    NON_UNIQUENESS(411, "资源不唯一", false),
    /**
     * code：412
     * 无权限
     */
    NO_PERMISSION(412, "无权限", false),
    /**
     * code：413
     * 手机验证码错误
     */
    MOBILE_VALID_CODE_ERROR(413, "手机验证码错误，请确认后重试", false),

    CARD_NOT_VALID_ERROR(420, "会员卡无效", false),
    CARD_NOT_BUY_ERROR(421, "没有购买过会员", false),

    LOGIN_NOT_BIND_PHONE(ERROR.code, "此手机号未绑定任何账号",false),

    /**
     * code:500
     * 请求失败
     */
    FAILED(500, "请求失败", false),
    SERVER_ERROR(500,"请求参数可能异常，请确认后重试",false)

    ;

    private final int code;
    private final String msg;
    private final boolean success;


    ResponseType(int code, String desc, boolean success) {
        this.code = code;
        this.msg = desc;
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public boolean isSuccess() {
        return success;
    }
}
