package com.ssafy.b108.walletslot.backend.global.exception;

import com.ssafy.b108.walletslot.backend.global.dto.ErrorResponse;
import com.ssafy.b108.walletslot.backend.global.error.AppException;
import com.ssafy.b108.walletslot.backend.global.error.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex) {
        ErrorCode code = ex.getErrorCode();
        log.warn("AppException at {} -> {} {}", ex.getLocation(), code, ex.getMessage());
        return ResponseEntity.status(code.getStatus())
                .body(ErrorResponse.builder().message(code.getMessage()).build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .orElse(ErrorCode.VALIDATION_FAILED.getMessage());
        return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.getStatus())
                .body(ErrorResponse.builder().message(msg).build());
    }

    // 🔹 쿼리/경로 파라미터 타입 불일치 → 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Type mismatch: param={}, value={}, requiredType={}",
                ex.getName(), ex.getValue(), ex.getRequiredType());
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder().message(ErrorCode.BAD_REQUEST.getMessage()).build());
    }

    // 🔹 필수 파라미터 누락 → 400
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        log.warn("Missing param: {}", ex.getParameterName());
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder().message(ErrorCode.BAD_REQUEST.getMessage()).build());
    }

    // 🔹 본문 파싱 실패/바인딩 실패/제약 위반 → 400
    @ExceptionHandler({HttpMessageNotReadableException.class, BindException.class, ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> handleBindAndReadable(Exception ex) {
        log.warn("Bad request (bind/readable): {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder().message(ErrorCode.BAD_REQUEST.getMessage()).build());
    }

    @ExceptionHandler(OcrUpstreamException.class)
    public ResponseEntity<String> handleOcrUpstream(OcrUpstreamException ex) {
        log.warn("CLOVA upstream error: status={}, body={}", ex.getStatus(), ex.getPayload());
        String body = (ex.getPayload() == null || ex.getPayload().isBlank())
                ? "{\"message\":\"Upstream error\"}" : ex.getPayload();
        return ResponseEntity.status(ex.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    // 비즈니스 오류를 400으로
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder().message(ex.getMessage()).build());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        log.warn("Bad state: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder().message(ex.getMessage()).build());
    }

    // 기타 예상 못한 오류는 500으로 숨김
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleEtc(Exception ex) {
        ex.printStackTrace();
        ErrorResponse body = ErrorResponse.builder()
                .message(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()) // 내부 메시지 노출 X
                .build();
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus()).body(body);
    }
}
