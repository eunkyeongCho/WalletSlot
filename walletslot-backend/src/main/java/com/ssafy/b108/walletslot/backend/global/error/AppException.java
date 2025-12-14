package com.ssafy.b108.walletslot.backend.global.error;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String location;

    public AppException(ErrorCode errorCode, String location) {
        super(buildMessage(errorCode, location));
        this.errorCode = errorCode;
        this.location = location;
    }

    private static String buildMessage(ErrorCode code, String location) {
        return (location == null || location.isBlank())
                ? code.getMessage()
                : "[" + location + "] " + code.getMessage();
    }
}

