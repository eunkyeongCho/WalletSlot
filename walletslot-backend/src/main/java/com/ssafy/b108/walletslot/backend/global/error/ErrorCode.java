package com.ssafy.b108.walletslot.backend.global.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 일반적인 에러코드
    BAD_REQUEST("잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    CONFLICT("중복된 요청입니다.", HttpStatus.CONFLICT),
    VALIDATION_FAILED("요청값 검증 실패", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    NOT_FOUND("리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INTERNAL_SERVER_ERROR("알 수 없는 오류가 발생했습니다. 서버관리자에게 문의하세요.", HttpStatus.INTERNAL_SERVER_ERROR), // 서버에서 로그확인 필요

    // Bank
    BANK_NOT_FOUND("존재하지 않는 은행입니다.", HttpStatus.NOT_FOUND),

    // Account
    ACCOUNT_HOLDER_NAME_MISMATCH("현재 사용자명과 예금주명이 일치하지 않습니다. 자신의 계좌의 잔액만 조회할 수 있습니다.", HttpStatus.FORBIDDEN),
    ACCOUNT_NOT_FOUND("존재하지 않는 계좌입니다.", HttpStatus.NOT_FOUND),

    // Slot
    ALLOCATABLE_BUDGET_EXCEEDED("할당 가능한 예산을 초과했습니다.", HttpStatus.UNPROCESSABLE_ENTITY),
    THRIFT_BUDGET_EXCEEDED("지난 달에 절약한 금액 한도 내에서만 다음달 슬롯 예산을 늘릴 수 있습니다.", HttpStatus.UNPROCESSABLE_ENTITY),
    MISSING_UNCATEGORIZED_SLOT("미분류 슬롯이 존재하지 않습니다. 미분류 슬롯을 개설 후 다시 시도해주세요.", HttpStatus.UNPROCESSABLE_ENTITY),
    SLOT_NOT_FOUND("존재하지 않는 슬롯입니다.", HttpStatus.NOT_FOUND),
    ACCOUNT_SLOT_NOT_FOUND("계좌에 존재하지 않는 슬롯입니다.", HttpStatus.NOT_FOUND),

    // User
    LOGIN_FAILED("로그인에 실패했습니다.", HttpStatus.BAD_REQUEST),
    PIN_LOCKED("PIN 시도 초과로 잠금되었습니다.", HttpStatus.TOO_MANY_REQUESTS),
    PIN_MISMATCH("현재 PIN이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    OTP_INVALID("인증코드가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED("인증코드가 만료되었습니다.", HttpStatus.GONE),
    REFRESH_DISABLED("Refresh 기능이 비활성화되었습니다.", HttpStatus.BAD_REQUEST),
    REFRESH_INVALID("유효하지 않은 Refresh 토큰입니다.", HttpStatus.UNAUTHORIZED),
    DEVICE_MISMATCH("요청한 deviceId가 토큰의 deviceId와 일치하지 않습니다.", HttpStatus.CONFLICT),
    TOKEN_INVALID("유효하지 않은 액세스 토큰입니다.", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND("존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),
    MISSING_BASE_DAY("기준일이 없는 사용자입니다.", HttpStatus.NOT_FOUND),

    // Transaction
    INVALID_SPLIT_AMOUNT("나눈 금액들의 합이 원래 금액과 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    MISSING_FCM_TOKEN("FCM 토큰이 등록돼있지 않은 사용자가 존재합니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    TRANSACTION_NOT_FOUND("존재하지 않는 거래입니다. transactionId를 다시 확인해주세요.", HttpStatus.NOT_FOUND),

    // PushEndpoint
    MISSING_PUSH_ENDPOINT("Push Endpoint 기기가 등록돼있지 않은 사용자가 존재합니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PUSH_ENDPOINT_NOTFOUND("푸시알림을 받을 기기가 등록돼있지 않은 사용자입니다. 기기를 등록하고 다시 시도해주세요.", HttpStatus.NOT_FOUND);

    private final String message;
    private final HttpStatus status;
}
