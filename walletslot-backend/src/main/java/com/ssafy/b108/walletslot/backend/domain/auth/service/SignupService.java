package com.ssafy.b108.walletslot.backend.domain.auth.service;

import com.ssafy.b108.walletslot.backend.config.security.JwtProvider;
import com.ssafy.b108.walletslot.backend.domain.account.dto.external.SSAFYGetUserKeyResponseDto;
import com.ssafy.b108.walletslot.backend.domain.auth.dto.*;
import com.ssafy.b108.walletslot.backend.domain.auth.entity.*;
import com.ssafy.b108.walletslot.backend.domain.auth.repository.*;
import com.ssafy.b108.walletslot.backend.domain.user.entity.Email;
import com.ssafy.b108.walletslot.backend.domain.user.entity.User;
import com.ssafy.b108.walletslot.backend.domain.user.repository.EmailRepository;
import com.ssafy.b108.walletslot.backend.domain.user.repository.UserRepository;
import com.ssafy.b108.walletslot.backend.global.crypto.PepperSecretProvider;
import com.ssafy.b108.walletslot.backend.global.error.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final UserRepository userRepo;
    private final UserPinRepository userPinRepo;
    private final PepperKeyRepository pepperRepo;

    private final PhoneVerifyTicketService ticketService;
    private final PepperSecretProvider pepperSecrets;
    private final JwtProvider jwt;
    private final RestTemplate restTemplate;

    // 인터페이스로 고정 주입 (분기/널가드 불필요)
    private final RefreshTokenService refreshService;

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
    private final EmailRepository emailRepository;
    @Value("${app.security.bcrypt.cost:12}") private int targetCost;

    @Value("${api.ssafy.finance.apiKey}")
    private String ssafyFinanceApiKey;

    @Transactional
    public SignupResponse signup(SignupRequest req) {
        // 1) 휴대폰 인증 티켓 검증/소모
        ticketService.consume(req.getSignupTicket(), "SIGNUP", req.getPhone());

        // 2) 중복 가입 차단
        userRepo.findByPhoneNumber(req.getPhone())
                .ifPresent(u -> { throw new AppException(ErrorCode.CONFLICT, "이미 가입된 번호입니다."); });

        // 3) 사용자 생성
        User.Gender gender = User.Gender.valueOf(req.getGender());
        User.Job job = (req.getJob()==null || req.getJob().isBlank()) ? null : User.Job.valueOf(req.getJob());

        Email email = emailRepository.findByName(req.getName()).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "AccountService - 000"));

        // SSAFY 금융 API >>>>> 사용자 계정 조회
        // 요청보낼 url
        String url1 = "https://finopenapi.ssafy.io/ssafy/api/v1/member/search";

        Map<String, Object> body1 = new HashMap<>();
        body1.put("userId", email.getEmail());
        body1.put("apiKey", ssafyFinanceApiKey);

        // 요청보낼 http entity 만들기
        HttpEntity<Map<String, Object>> httpEntity1 = new HttpEntity<>(body1);

        // 요청 보내기
        ResponseEntity<SSAFYGetUserKeyResponseDto> httpResponse1 = restTemplate.exchange(
                url1,
                HttpMethod.POST,
                httpEntity1,
                SSAFYGetUserKeyResponseDto.class);

        String userKey = httpResponse1.getBody().getUserKey();

        User user = User.builder()
                .userKey(userKey)
                .name(req.getName())
                .phoneNumber(req.getPhone())
                .gender(gender)
                .birthDate(LocalDate.parse(req.getBirthDate()).atStartOfDay())
                .baseDay((short) ((req.getBaseDay()==null)? 1 : req.getBaseDay()))
                .job(job)
                .build();
        user = userRepo.save(user);

        // 4) PIN 설정
        if (req.getPin()!=null && !req.getPin().isBlank()) {
            PepperKey active = pepperRepo.findFirstByStatusOrderByCreatedAtDesc(PepperKey.Status.ACTIVATE)
                    .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "활성 pepper 없음"));
            String secret = pepperSecrets.getSecret(active.getKeyAlias());
            UserPin up = UserPin.builder()
                    .user(user).pepperKey(active)
                    .bcryptedPin(bcrypt.encode(secret + req.getPin()))
                    .cost(targetCost).failedCount(0).lockedUntil(null)
                    .build();
            userPinRepo.save(up);
        }

        // 5) 토큰 발급 (OFF일 땐 Disabled 구현이 null 반환)
        String access  = jwt.createAccessToken(user.getId(), req.getDeviceId());
        String refresh = refreshService.issueAfterLogin(user.getId(), req.getDeviceId());

        return SignupResponse.builder()
                .userId(user.getId())
                .accessToken(access)
                .refreshToken(refresh) // OFF면 null로 응답됨
                .build();
    }
}
