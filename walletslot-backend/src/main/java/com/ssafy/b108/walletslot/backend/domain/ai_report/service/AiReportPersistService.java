package com.ssafy.b108.walletslot.backend.domain.ai_report.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.b108.walletslot.backend.domain.account.entity.Account;
import com.ssafy.b108.walletslot.backend.domain.account.repository.AccountRepository;
import com.ssafy.b108.walletslot.backend.domain.ai_report.dto.GetAiReportResponseDto;
import com.ssafy.b108.walletslot.backend.domain.ai_report.entity.AiReport;
import com.ssafy.b108.walletslot.backend.domain.ai_report.repository.AiReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiReportPersistService {

    private final AiReportRepository aiReportRepo;
    private final AccountRepository accountRepo;
    private final ObjectMapper objectMapper;

    // AiReportPersistService#saveInNewTx(Long accountId, Map<String,Object> content)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public GetAiReportResponseDto.PersistInfo saveInNewTx(Long accountId, Map<String, Object> content) {
        try {
            var json = objectMapper.valueToTree(content); // JsonNode
            var accountRef = accountRepo.getReferenceById(accountId);
            AiReport saved = aiReportRepo.save(
                    AiReport.builder().account(accountRef).content(json).build()
            );
            // flush로 DB에 바로 밀어넣고 created_at 채워지게 함
            aiReportRepo.flush();

            return GetAiReportResponseDto.PersistInfo.builder()
                    .id(saved.getUuid())
                    .createdAt(saved.getCreatedAt())
                    .build();

        } catch (DataIntegrityViolationException ex) {
            log.error("[AI-REPORT][PERSIST] DataIntegrityViolation: {}", ex.getMessage(), ex);
            throw ex; // 여기서는 삼키지 말고 던져서 상위 로그/트레이싱에 확실히 남기자
        } catch (Exception ex) {
            log.error("[AI-REPORT][PERSIST] Unexpected: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

}
