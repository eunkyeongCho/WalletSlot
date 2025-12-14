package com.ssafy.b108.walletslot.backend.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class OcrUpstreamException extends RuntimeException {
    private final HttpStatusCode status;
    private final String payload;

    public OcrUpstreamException(HttpStatusCode status, String payload) {
        super("CLOVA OCR upstream error: status=" + status);
        this.status = status;
        this.payload = payload;
    }
}
