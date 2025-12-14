package com.ssafy.b108.walletslot.backend.domain.ocr.service;

import com.ssafy.b108.walletslot.backend.domain.ocr.client.NaverOcrClient;
import com.ssafy.b108.walletslot.backend.domain.ocr.dto.ReceiptOcrResponseDto;
import com.ssafy.b108.walletslot.backend.domain.ocr.mapper.ReceiptOcrMapper;
import com.ssafy.b108.walletslot.backend.global.exception.OcrUpstreamException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class OcrService {

    private final NaverOcrClient ocrClient;
    private final ReceiptOcrMapper mapper;

    /** 컨트롤러는 이 메서드만 호출 */
    public ReceiptOcrResponseDto parseReceipt(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file 파트가 필요합니다.");
        }

        ResponseEntity<String> upstream = ocrClient.receiptOcr(file);

        // 업스트림 에러는 서비스에서 예외로 승격 → 전역 핸들러에서 처리
        if (!upstream.getStatusCode().is2xxSuccessful()) {
            throw new OcrUpstreamException(upstream.getStatusCode(), upstream.getBody());
        }

        return mapper.map(upstream.getBody());
    }
}
