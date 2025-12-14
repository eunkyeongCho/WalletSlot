package com.ssafy.b108.walletslot.backend.domain.ocr.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.b108.walletslot.backend.config.naverocr.NaverOcrProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "naver.ocr", name = "enabled", havingValue = "true")
public class NaverOcrClient {

    @Qualifier("ocrRestTemplate")
    private final RestTemplate restTemplate;
    private final NaverOcrProperties props;
    private final ObjectMapper om;

    public ResponseEntity<String> receiptOcr(MultipartFile file) throws IOException {
        final String endpoint = UriComponentsBuilder
                .fromHttpUrl(props.getInvokeUrl())
                .path(props.getReceiptPath())
                .buildAndExpand(Map.of(
                        "projectId", props.getProjectId(),
                        "privateKey", props.getPrivateKey()
                ))
                .toUriString();

        // message JSON
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("version", defaultStr(props.getVersion(), "V2"));
        message.put("requestId", UUID.randomUUID().toString());
        message.put("timestamp", Instant.now().toEpochMilli());

        Map<String, Object> imageMeta = new LinkedHashMap<>();
        imageMeta.put("format", guessExt(file.getOriginalFilename()));
        imageMeta.put("name", Optional.ofNullable(file.getOriginalFilename()).orElse("upload"));
        message.put("images", List.of(imageMeta));

        HttpHeaders msgHeaders = new HttpHeaders();
        msgHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> msgPart;
        try {
            msgPart = new HttpEntity<>(om.writeValueAsString(message), msgHeaders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"message\":\"Failed to serialize message JSON\"}");
        }

        // file 바이너리 파트
        Resource fileResource = new InputStreamResource(file.getInputStream()) {
            @Override public String getFilename() { return file.getOriginalFilename(); }
            @Override public long contentLength() { return file.getSize(); }
        };
        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(parseOrDefault(file.getContentType()));
        HttpEntity<Resource> filePart = new HttpEntity<>(fileResource, fileHeaders);

        // multipart 조립
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("message", msgPart);
        body.add("file", filePart);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("X-OCR-SECRET", props.getSecret());

        HttpEntity<MultiValueMap<String, Object>> req = new HttpEntity<>(body, headers);

        try {
            return restTemplate.postForEntity(endpoint, req, String.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    private String guessExt(String filename) {
        if (filename == null) return "jpg";
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) return "png";
        if (lower.endsWith(".webp")) return "webp";
        if (lower.endsWith(".bmp")) return "bmp";
        if (lower.endsWith(".tif") || lower.endsWith(".tiff")) return "tiff";
        if (lower.endsWith(".heic")) return "heic";
        return "jpg";
    }

    private MediaType parseOrDefault(String ct) {
        try { return (ct != null) ? MediaType.parseMediaType(ct) : MediaType.APPLICATION_OCTET_STREAM; }
        catch (Exception e) { return MediaType.APPLICATION_OCTET_STREAM; }
    }

    private String defaultStr(String v, String def) {
        return (v == null || v.isBlank()) ? def : v;
    }
}
