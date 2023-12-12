package cn.hoxinte.pay.common.enums;

/**
 * 状态枚举
 *
 * @author dominate
 * @since 2022/01/19
 */
public enum State {
    /**
     * 正常启用 默认值
     */
    ENABLE(0, "启用"),
    /**
     * 未启用
     */
    UNUSED(1, "未启用"),
    /**
     * 禁用
     */
    DISABLED(2, "禁用");

    final int code;
    final String name;

    State(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static State getValueByCode(int code){
        for (State state : State.values()) {
            if(code == state.code){
                return state;
            }
        }
        return ENABLE;
    }
}
