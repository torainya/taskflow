package com.taskflow.web;

import com.taskflow.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Optional;

/**
 * 全局异常处理：把各类异常收敛为统一 JSON 错误体 {code, message, timestamp}。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：直接透传业务错误码 */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiError> handleBiz(BizException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiError.of(ex.getCode(), ex.getMessage()));
    }

    /** 请求体参数校验失败（@Valid） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Optional<FieldError> first = ex.getBindingResult().getFieldErrors().stream().findFirst();
        String message = first.map(FieldError::getDefaultMessage).orElse("请求参数不合法");
        return ResponseEntity.badRequest().body(ApiError.of("VALIDATION_ERROR", message));
    }

    /** 请求体缺失 / JSON 格式错误 / 枚举值不合法 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(ApiError.of("BAD_REQUEST", "请求体缺失或格式错误"));
    }

    /** URL 参数类型错误 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest()
                .body(ApiError.of("BAD_REQUEST", "参数 " + ex.getName() + " 格式错误"));
    }

    /** 缺少必填参数 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest()
                .body(ApiError.of("BAD_REQUEST", "缺少参数 " + ex.getParameterName()));
    }

    /** 兜底：未知异常（记日志，避免把内部细节泄露给前端） */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnknown(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of("INTERNAL_ERROR", "服务器开小差了，请稍后再试"));
    }

    public record ApiError(String code, String message, long timestamp) {
        static ApiError of(String code, String message) {
            return new ApiError(code, message, System.currentTimeMillis());
        }
    }
}
