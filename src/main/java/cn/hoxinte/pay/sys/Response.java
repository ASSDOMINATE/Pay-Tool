package cn.hoxinte.pay.sys;

import cn.hoxinte.pay.common.enums.ResponseType;
import cn.hoxinte.pay.sys.exception.BusinessException;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * 返回结果
 *
 * @param <T>
 * @author dominate
 * @since 2022/02/25
 */
@Data
public class Response<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;

    private String msg;

    private T data;

    private Boolean success;

    private Response(int code, T data, String msg, boolean success) {
        super();
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.success = success;
    }


    private static <T> Response<T> data(int code, T data, String msg, boolean success) {
        return new Response<>(code, data, msg, success);
    }

    private static <T> Response<T> msg(int code, String msg, boolean success) {
        return new Response<>(code, null, msg, success);
    }


    /**
     * 返回失败
     *
     * @param exception 失败异常
     * @return 返回失败异常
     */
    public static <T> Response<T> failed(BusinessException exception) {
        return msg(exception.getExceptionCode(), exception.getExceptionMsg(), false);
    }

    /**
     * 返回失败
     *
     * @param message 失败异常
     * @return 返回失败异常
     */
    public static <T> Response<T> error(String message) {
        return msg(ResponseType.ERROR.getCode(), message, false);
    }

    /**
     * 返回失败
     *
     * @param status 失败HTTP状态值
     * @return 返回失败HTTP状态值
     */
    public static <T> Response<T> failed(HttpStatus status) {
        return msg(status.value(), status.getReasonPhrase(), false);
    }

    /**
     * 返回默认失败
     *
     * @return 失败HTTP状态值
     */
    public static <T> Response<T> failed() {
        return msg(ResponseType.FAILED.getCode(), ResponseType.FAILED.getMsg(), ResponseType.FAILED.isSuccess());
    }


    /**
     * 返回默认成功
     *
     * @return 成功返回体
     */
    public static <T> Response<T> success(String message) {
        return msg(ResponseType.SUCCESS.getCode(), message, ResponseType.SUCCESS.isSuccess());
    }

    /**
     * 返回默认成功
     *
     * @return 成功返回体
     */
    public static <T> Response<T> success() {
        return msg(ResponseType.SUCCESS.getCode(), ResponseType.SUCCESS.getMsg(), ResponseType.SUCCESS.isSuccess());
    }

    /**
     * 返回成功
     *
     * @param status 成功HTTP状态值
     * @return 返回成功HTTP状态值
     */
    public static <T> Response<T> success(HttpStatus status) {
        return msg(status.value(), status.getReasonPhrase(), true);
    }

    /**
     * 返回状态值
     *
     * @param code 返回状态
     * @return 返回结果
     */
    public static <T> Response<T> code(ResponseType code) {
        return msg(code.getCode(), code.getMsg(), code.isSuccess());
    }


    /**
     * 返回数据
     *
     * @param code 返回状态
     * @param data 数据
     * @return 返回结果
     */
    public static <T> Response<T> data(ResponseType code, T data) {
        return data(code.getCode(), data, code.getMsg(), code.isSuccess());
    }

    /**
     * 返回数据
     * 成功状态
     *
     * @param data 数据
     * @return 返回结果
     */
    public static <T> Response<T> data(T data) {
        return data(ResponseType.SUCCESS, data);
    }

}
