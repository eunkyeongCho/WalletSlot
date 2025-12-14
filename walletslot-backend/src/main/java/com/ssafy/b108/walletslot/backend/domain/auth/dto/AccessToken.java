// src/main/java/com/ssafy/b108/walletslot/backend/domain/auth/dto/AccessToken.java
package com.ssafy.b108.walletslot.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccessToken {
    @Schema(example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;
}
