package com.ssafy.b108.walletslot.backend.global.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
    private final boolean success = false;
    private final String message;
}