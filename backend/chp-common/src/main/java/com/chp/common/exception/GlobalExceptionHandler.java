package com.chp.common.exception;

import com.chp.common.constant.StatusCode;
import com.chp.common.result.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        int code = e.getCode();
        HttpStatus httpStatus = resolveHttpStatus(code);
        return ResponseEntity.status(httpStatus).body(Result.error(code, e.getMessage()));
    }

    /**
     * 参数校验异常（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Map<String, String>>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(Result.error(StatusCode.BAD_REQUEST, "请求参数错误", errors));
    }

    /**
     * 约束校验异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<String>> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(Result.error(StatusCode.BAD_REQUEST, message));
    }

    /**
     * 资源不存在
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNotFound(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.error(StatusCode.NOT_FOUND, "资源不存在"));
    }

    /**
     * 兜底异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("服务器异常: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(StatusCode.SERVER_ERROR, "服务器内部错误"));
    }

    /**
     * 根据业务错误码映射 HTTP 状态码
     */
    private HttpStatus resolveHttpStatus(int code) {
        return switch (code) {
            case StatusCode.BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            case StatusCode.UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case StatusCode.FORBIDDEN -> HttpStatus.FORBIDDEN;
            case StatusCode.NOT_FOUND -> HttpStatus.NOT_FOUND;
            case StatusCode.SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.OK; // 业务错误码 10001-10006 使用 HTTP 200
        };
    }
}
