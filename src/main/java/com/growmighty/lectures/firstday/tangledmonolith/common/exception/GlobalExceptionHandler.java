package com.growmighty.lectures.firstday.tangledmonolith.common.exception;

import com.growmighty.lectures.firstday.tangledmonolith.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/*
    Controller에서 발생하는 모든 예외를 한 곳에서 처리하기 위해 존재한다.
    궁극적인 목표는 500 Internal Server Error를 내놓지 않고 정확한 오류를 내보내주기 위해.
*/

@RestControllerAdvice // 모든 Controller에서 발생하는 예외를 감시한다.
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        return build(errorCode.getStatus(), errorCode.getCode(), e.getMessage());
    }

    // IllegalArgumentException이 발생하면 아래 메서드를 실행하라.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        return build(ErrorCode.INVALID_INPUT.getStatus(), ErrorCode.INVALID_INPUT.getCode(), e.getMessage());
    }

    // IllegalStateException이 발생하면 아래 메서드를 실행하라.
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException e) {
        return build(ErrorCode.INVALID_STATE.getStatus(), ErrorCode.INVALID_STATE.getCode(), e.getMessage());
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception e) {
        return build(ErrorCode.INVALID_INPUT.getStatus(), ErrorCode.INVALID_INPUT.getCode(), "요청 형식이 올바르지 않습니다.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        return build(ErrorCode.INTERNAL_ERROR.getStatus(), ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage());
    }

    // 예외를 HTTP 응답으로 만들어주는 공통 메서드
    private ResponseEntity<ApiResponse<Void>> build(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(ApiResponse.fail(new ApiResponse.ApiError(code, message)));
    }
}