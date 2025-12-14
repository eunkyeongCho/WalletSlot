package com.ssafy.b108.walletslot.backend.domain.notification.dto.notification;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class SimpleOkResponseDto {

    boolean success;

    String message;
}
