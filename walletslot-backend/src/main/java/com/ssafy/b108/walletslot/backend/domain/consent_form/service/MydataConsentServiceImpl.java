package com.ssafy.b108.walletslot.backend.domain.consent_form.service;

import com.ssafy.b108.walletslot.backend.domain.consent_form.dto.*;
import com.ssafy.b108.walletslot.backend.domain.consent_form.entity.ConsentForm;
import com.ssafy.b108.walletslot.backend.domain.consent_form.entity.UserConsent;
import com.ssafy.b108.walletslot.backend.domain.consent_form.repository.ConsentFormRepository;
import com.ssafy.b108.walletslot.backend.domain.consent_form.repository.UserConsentRepository;
import com.ssafy.b108.walletslot.backend.domain.user.entity.User;
import com.ssafy.b108.walletslot.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MydataConsentServiceImpl implements MydataConsentService {

    private final UserRepository userRepository;
    private final ConsentFormRepository consentFormRepository;
    private final UserConsentRepository userConsentRepository;

    @PersistenceContext
    private EntityManager em; // DB default 컬럼(agreed_at) 다시 읽기용

    private static LocalDateTime defaultExpiry(LocalDateTime x) {
        return (x != null) ? x : LocalDateTime.now().plusYears(1);
    }

    @Override
    public ConsentCreateResponseDto create(Long userId, ConsentCreateRequestDto req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        ConsentForm form = consentFormRepository.findByUuid(req.getConsentFormUuid())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consent form not found"));

        if (userConsentRepository.existsByUserIdAndConsentFormIdAndStatus(userId, form.getId(), UserConsent.Status.ACTIVE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Active consent already exists");
        }

        UserConsent saved = userConsentRepository.save(UserConsent.builder()
                .user(user)
                .consentForm(form)
                .status(UserConsent.Status.ACTIVE)
                .expiredAt(defaultExpiry(req.getExpiredAt()))
                .build());

        // agreed_at은 DB DEFAULT라 저장 직후 엔티티에 값이 없을 수 있음 → 새로 고침
        em.flush();
        em.refresh(saved);

        return ConsentCreateResponseDto.builder()
                .userConsentUuid(saved.getUuid())
                .consentFormUuid(form.getUuid())
                .consentFormTitle(form.getTitle())
                .status(saved.getStatus())
                .agreedAt(saved.getAgreedAt())
                .expiredAt(saved.getExpiredAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ConsentListResponseDto list(Long userId, UserConsent.Status status) {
        List<UserConsent> list = (status == null)
                ? userConsentRepository.findByUserIdOrderByAgreedAtDesc(userId)
                : userConsentRepository.findByUserIdAndStatusOrderByAgreedAtDesc(userId, status);

        List<ConsentListItemDto> items = list.stream().map(uc -> ConsentListItemDto.builder()
                .userConsentUuid(uc.getUuid())
                .consentFormUuid(uc.getConsentForm().getUuid())
                .consentFormTitle(uc.getConsentForm().getTitle())
                .status(uc.getStatus())
                .agreedAt(uc.getAgreedAt())
                .expiredAt(uc.getExpiredAt())
                .revokedAt(uc.getRevokedAt())
                .build()
        ).toList();

        return ConsentListResponseDto.builder().consents(items).build();
    }

    @Override
    public ConsentRevokeResponseDto revoke(Long userId, ConsentRevokeRequestDto req) {
        UserConsent uc = userConsentRepository.findByUuid(req.getUserConsentUuid())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consent not found"));

        if (!uc.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your consent");
        }
        if (uc.getStatus() != UserConsent.Status.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Consent is not ACTIVE");
        }

        uc.setStatus(UserConsent.Status.REVOKED);
        uc.setRevokedAt(LocalDateTime.now()); // ← revokedAt 컬럼 updatable=true 확인

        return ConsentRevokeResponseDto.builder()
                .userConsentUuid(uc.getUuid())
                .status(uc.getStatus())
                .revokedAt(uc.getRevokedAt())
                .build();
    }

    @Override
    public ConsentRenewResponseDto renew(Long userId, ConsentRenewRequestDto req) {
        ConsentForm form;

        if (req.getPreviousUserConsentUuid() != null && !req.getPreviousUserConsentUuid().isBlank()) {
            UserConsent prev = userConsentRepository.findByUuid(req.getPreviousUserConsentUuid())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Previous consent not found"));
            if (!prev.getUser().getId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your consent");
            }
            form = prev.getConsentForm();

            if (prev.getStatus() == UserConsent.Status.ACTIVE) { // 이전 ACTIVE → EXPIRED 전환
                prev.setStatus(UserConsent.Status.EXPIRED);
                prev.setRevokedAt(null);
            }
        } else {
            if (req.getConsentFormUuid() == null || req.getConsentFormUuid().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "consentFormUuid is required");
            }
            form = consentFormRepository.findByUuid(req.getConsentFormUuid())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consent form not found"));
        }

        if (userConsentRepository.existsByUserIdAndConsentFormIdAndStatus(userId, form.getId(), UserConsent.Status.ACTIVE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Active consent already exists");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UserConsent created = userConsentRepository.save(UserConsent.builder()
                .user(user)
                .consentForm(form)
                .status(UserConsent.Status.ACTIVE)
                .expiredAt(defaultExpiry(req.getExpiredAt()))
                .build());

        em.flush();
        em.refresh(created); // agreed_at 채워져 있도록

        return ConsentRenewResponseDto.builder()
                .newUserConsentUuid(created.getUuid())
                .consentFormUuid(form.getUuid())
                .status(created.getStatus())
                .agreedAt(created.getAgreedAt())
                .expiredAt(created.getExpiredAt())
                .build();
    }
}
