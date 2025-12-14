package com.ssafy.b108.walletslot.backend.domain.notification.dto.notification;

import com.ssafy.b108.walletslot.backend.domain.notification.entity.Notification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class CreateNotificationRequestDto {

    @NotNull
    Long targetUserId;

    @NotBlank
    String title;

    String content;

    @NotNull
    Notification.Type type;
}
