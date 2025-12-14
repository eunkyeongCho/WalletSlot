package com.ssafy.b108.walletslot.backend.domain.ocr.controller;

import com.ssafy.b108.walletslot.backend.config.security.UserPrincipal;
import com.ssafy.b108.walletslot.backend.domain.ocr.dto.ReceiptOcrResponseDto;
import com.ssafy.b108.walletslot.backend.domain.ocr.service.OcrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "naver.ocr", name = "enabled", havingValue = "true")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "OCR", description = "CLOVA OCR 프록시 (영수증 모델)")
public class OcrController {

    private final OcrService ocrService;

    @PostMapping(path = "/receipt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "10-1-1 영수증 OCR (CLOVA Receipt)",
            description = """
                영수증 이미지 1장을 OCR하고 (상호명/날짜/시간/항목)만 축약해 반환합니다.
                """,
            extensions = @Extension(name = "x-order",
                    properties = @ExtensionProperty(name = "order", value = "1")),
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = "X-Device-Id", required = false,
                            description = "디바이스 식별자", example = "A1B2C3D4")
            },
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            examples = @ExampleObject(
                                    name = "폼데이터 예시",
                                    value = "(multipart/form-data)\nfile: <binary image>"
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReceiptOcrResponseDto.class)))
    })
    public ReceiptOcrResponseDto receipt(
            @Parameter(description = "영수증 이미지 파일", required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary")))
            @RequestPart("file") MultipartFile file,
            @RequestHeader(name = "X-Device-Id", required = false) String deviceId,
            @AuthenticationPrincipal UserPrincipal principal
    ) throws Exception {
        return ocrService.parseReceipt(file);
    }
}
