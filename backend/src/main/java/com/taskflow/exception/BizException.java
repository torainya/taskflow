package com.taskflow.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 业务异常：携带给前端的错误码与 HTTP 状态。
 * 由全局异常处理器统一转换为 JSON 错误体。
 */
@Getter
public class BizException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public BizException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static BizException badRequest(String code, String message) {
        return new BizException(HttpStatus.BAD_REQUEST, code, message);
    }

    public static BizException notFound(String code, String message) {
        return new BizException(HttpStatus.NOT_FOUND, code, message);
    }

    public static BizException unauthorized(String code, String message) {
        return new BizException(HttpStatus.UNAUTHORIZED, code, message);
    }

    public static BizException conflict(String code, String message) {
        return new BizException(HttpStatus.CONFLICT, code, message);
    }
}
