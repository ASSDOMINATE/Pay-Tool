package cn.hoxinte.pay.sys.exception;


import cn.hoxinte.pay.common.enums.ExceptionType;
import cn.hoxinte.pay.common.enums.ResponseType;

/**
 * 业务异常
 *
 * @author dominate
 */
public class BusinessException extends RuntimeException {

    private Integer exceptionCode;

    private String exceptionMsg;

    public static BusinessException create(ExceptionType exception) {
        return new BusinessException(exception.getCode(), exception.getMessage());
    }

    public static BusinessException create(ExceptionType exception,String message) {
        return new BusinessException(exception.getCode(), message);
    }

    public BusinessException(String message) {
        super(message);
        this.exceptionMsg = message;
        this.exceptionCode = ResponseType.FAILED.getCode();
    }

    public BusinessException(Integer exceptionCode, String exceptionMsg) {
        this.exceptionCode = exceptionCode;
        this.exceptionMsg = exceptionMsg;
    }

    public BusinessException(String message, Integer exceptionCode, String exceptionMsg) {
        super(message);
        this.exceptionCode = exceptionCode;
        this.exceptionMsg = exceptionMsg;
    }

    public BusinessException(String message, Throwable cause, Integer exceptionCode, String exceptionMsg) {
        super(message, cause);
        this.exceptionCode = exceptionCode;
        this.exceptionMsg = exceptionMsg;
    }

    public BusinessException(Throwable cause, Integer exceptionCode, String exceptionMsg) {
        super(cause);
        this.exceptionCode = exceptionCode;
        this.exceptionMsg = exceptionMsg;
    }

    public BusinessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Integer exceptionCode, String exceptionMsg) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.exceptionCode = exceptionCode;
        this.exceptionMsg = exceptionMsg;
    }

    public Integer getExceptionCode() {
        return exceptionCode;
    }

    public String getExceptionMsg() {
        return exceptionMsg;
    }
}
