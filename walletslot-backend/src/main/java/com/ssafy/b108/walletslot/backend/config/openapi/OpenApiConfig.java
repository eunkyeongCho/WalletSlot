package com.ssafy.b108.walletslot.backend.config.openapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "WalletSlot API Docs",
                description = "SSAFY 13기 특화 프로젝트 WalletSlot API Docs 입니다.",
                version = "v1.0.0"
        ),
        security = {
                @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth"),
                @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "deviceId")
        }
)
@Configuration
// JWT Bearer (우상단 Authorize에 뜸)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
// X-Device-Id 헤더 (우상단 Authorize에 뜸)
@SecurityScheme(
        name = "deviceId",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "X-Device-Id"
)
public class OpenApiConfig {
}
