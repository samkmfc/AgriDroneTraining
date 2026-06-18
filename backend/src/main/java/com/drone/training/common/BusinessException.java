package com.drone.training.common;

/**
 * 业务异常
 *
 * @author 罗健 202308852
 */
public class BusinessException extends RuntimeException {

    private Integer code = ResultCode.ERROR.getCode();

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public Integer getCode() {
        return code;
    }
}
